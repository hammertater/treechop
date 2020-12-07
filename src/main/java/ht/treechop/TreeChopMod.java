package ht.treechop;

import ht.treechop.block.ChoppedLogBlock;
import ht.treechop.capabilities.ChopSettings;
import ht.treechop.capabilities.ChopSettingsProvider;
import ht.treechop.config.ConfigHandler;
import ht.treechop.init.ModBlocks;
import ht.treechop.client.KeyBindings;
import ht.treechop.network.PacketHandler;
import ht.treechop.util.ChopUtil;
import net.minecraft.block.BlockState;
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
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ht.treechop.util.ChopUtil.isBlockChoppable;

@Mod("treechop")
public class TreeChopMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "treechop";

    public TreeChopMod() {
//        SilentGear.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
//        modBus.register(SilentGear.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener((ModConfig.Loading e) -> ConfigHandler.onConfigLoad());
        modBus.addListener((ModConfig.Reloading e) -> ConfigHandler.onConfigLoad());
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onCommonSetup);

        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        ModBlocks.BLOCKS.register(modBus);
        eventBus.register(this);
        eventBus.addListener(KeyBindings::buttonPressed);
    }

    @SubscribeEvent
    public void onBreakEvent(BlockEvent.BreakEvent event) {
        World world = (World) event.getWorld();
        BlockPos blockPos = event.getPos();
        PlayerEntity agent = event.getPlayer();
        ItemStack tool = agent.getHeldItemMainhand();
        BlockState oldBlockState = event.getState();

        // Reuse some permission logic from PlayerInteractionManager.tryHarvestBlock
        if (
                !ConfigHandler.COMMON.enabled.get() ||
                !playerWantsToChop(agent) ||
                event.isCanceled() ||
                !(event.getWorld() instanceof World)
        ) {
            return;
        }

        if (isBlockChoppable(world, blockPos, oldBlockState)) {
            boolean firstChop = !(oldBlockState.getBlock() instanceof ChoppedLogBlock);
            BlockState newBlockState = firstChop ? ChopUtil.chipBlock(world, blockPos, 1, event.getPlayer(), tool) : oldBlockState;

            event.setCanceled(true);

            if (newBlockState != null && newBlockState.getBlock() instanceof ChoppedLogBlock) {
                ChoppedLogBlock block = (ChoppedLogBlock) newBlockState.getBlock();
                ChoppedLogBlock.ChopResult chopResult = block.chop(world, blockPos, newBlockState, event.getPlayer(), (firstChop) ? 0 : 1, tool);
                BlockPos choppedBlockPos = chopResult.getChoppedBlockPos();
                BlockState choppedBlockState = chopResult.getChoppedBlockState();
                if (choppedBlockPos == blockPos) {
                    choppedBlockState = oldBlockState;
                }

                // The event was canceled to prevent the block from being broken, but still want all the other consequences of breaking blocks
                // TODO: do we need to handle fortune, silk touch, etc.?
                doItemDamage(tool, world, choppedBlockState, choppedBlockPos, agent);
                dropExperience(world, choppedBlockPos, choppedBlockState, event.getExpToDrop());
                doExhaustion(agent);
                agent.addStat(Stats.BLOCK_MINED.get(choppedBlockState.getBlock()));
            } else {
                TreeChopMod.LOGGER.warn(String.format("Player \"%s\" failed to chip block \"%s\"", agent.getName(), oldBlockState.getBlock().getRegistryName()));
            }
        }
    }

    private boolean playerWantsToChop(PlayerEntity player) {
        ChopSettings chopSettings = player.getCapability(ChopSettings.CAPABILITY).orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty"));
        if (ConfigHandler.COMMON.canChooseNotToChop.get()) {
            return chopSettings.getChoppingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeChopBehavior(player);
        } else {
            return true;
        }
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

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        KeyBindings.clientSetup(event);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        ChopSettings.register();
        PacketHandler.init();
    }

    // Helpful reference: https://gist.github.com/FireController1847/c7a50144f45806a996d13efcff468d1b
    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        final ResourceLocation loc = new ResourceLocation(TreeChopMod.MOD_ID + "chop_settings_capability");

        Entity entity = event.getObject();
        if (entity instanceof PlayerEntity) {
//            TreeChopMod.LOGGER.info("Initializing chop settings for player " + entity.getName());
            event.addCapability(loc, new ChopSettingsProvider());
        }
    }

}
