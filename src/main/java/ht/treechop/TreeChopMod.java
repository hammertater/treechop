package ht.treechop;

import ht.treechop.capabilities.ChopSettings;
import ht.treechop.capabilities.ChopSettingsCapability;
import ht.treechop.capabilities.ChopSettingsProvider;
import ht.treechop.client.Client;
import ht.treechop.client.KeyBindings;
import ht.treechop.config.ConfigHandler;
import ht.treechop.init.ModBlocks;
import ht.treechop.network.PacketHandler;
import ht.treechop.util.ChopResult;
import ht.treechop.util.ChopUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("treechop")
public class TreeChopMod {
    public static final String MOD_ID = "treechop";
    public static final String MOD_NAME = "HT's TreeChop";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public TreeChopMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener((ModConfig.Loading e) -> ConfigHandler.onReload());
        modBus.addListener((ModConfig.Reloading e) -> ConfigHandler.onReload());
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onCommonSetup);

        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        ModBlocks.BLOCKS.register(modBus);

        eventBus.addListener(this::onBreakEvent);
        eventBus.addGenericListener(Entity.class, this::onAttachCapabilities);
        eventBus.addListener(Client::onConnect);
        eventBus.addListener(this::onPlayerCloned);

        eventBus.addListener(KeyBindings::buttonPressed);
    }

    public void onBreakEvent(BlockEvent.BreakEvent event) {
        World world = (World) event.getWorld();
        BlockPos blockPos = event.getPos();
        PlayerEntity agent = event.getPlayer();
        ItemStack tool = agent.getHeldItemMainhand();

        // Reuse some permission logic from PlayerInteractionManager.tryHarvestBlock
        if (
                !ConfigHandler.COMMON.enabled.get() ||
                !canChopWithTool(tool) ||
                !playerWantsToChop(agent) ||
                event.isCanceled() ||
                !(event.getWorld() instanceof World)
        ) {
            return;
        }

        ChopResult chopResult = ChopUtil.chop(world, blockPos, agent, getNumChopsByTool(tool), tool, playerWantsToFell(agent));
        if (chopResult == ChopResult.IGNORED) {
            return;
        }

        event.setCanceled(true);
        BlockPos choppedBlockPos = chopResult.getChoppedBlockPos();
        BlockState choppedBlockState = chopResult.getChoppedBlockState();

        // The event was canceled to prevent the block from being broken, but still want all the other consequences of breaking blocks
        // TODO: do we need to handle fortune, silk touch, etc.?
        doItemDamage(tool, world, choppedBlockState, choppedBlockPos, agent);
        dropExperience(world, choppedBlockPos, choppedBlockState, event.getExpToDrop());
        doExhaustion(agent);
        agent.addStat(Stats.BLOCK_MINED.get(choppedBlockState.getBlock()));
    }

    private boolean canChopWithTool(ItemStack tool) {
        return !(ConfigHandler.choppingToolItemsBlacklist.contains(tool.getItem().getRegistryName()) ||
                tool.getItem().getTags().stream().anyMatch(ConfigHandler.choppingToolTagsBlacklist::contains));
    }

    public int getNumChopsByTool(ItemStack tool) {
        return 1;
    }

    private boolean playerWantsToChop(PlayerEntity player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        if (ConfigHandler.COMMON.canChooseNotToChop.get()) {
            return chopSettings.getChoppingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeChopBehavior(player);
        } else {
            return true;
        }
    }

    private boolean playerWantsToFell(PlayerEntity player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        return chopSettings.getFellingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeFellBehavior(player);
    }

    private boolean isLocalPlayer(PlayerEntity player) {
        return !player.isServerWorld() && Minecraft.getInstance().player == player;
    }

    @SuppressWarnings("ConstantConditions")
    private ChopSettings getPlayerChopSettings(PlayerEntity player) {
        return isLocalPlayer(player) ? Client.getChopSettings() : player.getCapability(ChopSettingsCapability.CAPABILITY).orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty"));
    }

    private void doExhaustion(PlayerEntity agent) {
        agent.addExhaustion(0.005F);
    }

    private void doItemDamage(ItemStack itemStack, World world, BlockState blockState, BlockPos blockPos, PlayerEntity agent) {
        ItemStack mockItemStack = itemStack.copy();
        itemStack.onBlockDestroyed(world, blockState, blockPos, agent);
        if (itemStack.isEmpty() && !mockItemStack.isEmpty()) {
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(agent, mockItemStack, Hand.MAIN_HAND);
        }
    }

    private static void dropExperience(World world, BlockPos blockPos, BlockState blockState, int amount) {
        if (world instanceof ServerWorld) {
            blockState.getBlock().dropXpOnBlockBreak((ServerWorld) world, blockPos, amount);
        }
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        KeyBindings.clientSetup(event);
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        ChopSettingsCapability.register();
        PacketHandler.init();
    }

    // Helpful reference: https://gist.github.com/FireController1847/c7a50144f45806a996d13efcff468d1b
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        final ResourceLocation loc = new ResourceLocation(TreeChopMod.MOD_ID + "chop_settings_capability");

        Entity entity = event.getObject();
        if (entity instanceof PlayerEntity) {
            event.addCapability(loc, new ChopSettingsProvider());
        }
    }

    // Server-side
    public void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            PlayerEntity oldPlayer = event.getOriginal();
            PlayerEntity newPlayer = event.getPlayer();
            ChopSettings oldSettings = ChopSettingsCapability.forPlayer(oldPlayer);
            ChopSettings newSettings = ChopSettingsCapability.forPlayer(newPlayer);
            newSettings.copyFrom(oldSettings);
        }
    }
}
