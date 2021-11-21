package ht.treechop.common;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.capabilities.ChopSettingsProvider;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.util.ChopResult;
import ht.treechop.common.util.ChopUtil;
import ht.treechop.common.util.FauxPlayerInteractionManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static ht.treechop.common.util.ChopUtil.isBlockALog;
import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class Common {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        ChopSettingsCapability.register();
        PacketHandler.init();
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        ConfigHandler.updateTags(event.getTagManager());
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        ItemStack tool = event.getPlayer().getHeldItemMainhand();
        BlockState blockState = event.getState();
        BlockPos pos = event.getPos();

        if (!isBlockALog(blockState)
                || !ConfigHandler.COMMON.enabled.get()
                || !ChopUtil.canChopWithTool(tool)
                || event.isCanceled()
                || !(event.getWorld() instanceof ServerWorld)
                || !(event.getPlayer() instanceof ServerPlayerEntity)
        ) {
           return;
        }

        ServerWorld world = (ServerWorld) event.getWorld();
        ServerPlayerEntity agent = (ServerPlayerEntity) event.getPlayer();

        if (!ChopUtil.playerWantsToChop(agent)) {
            if (ConfigHandler.shouldOverrideItemBehavior(tool.getItem(), false)) {
                FauxPlayerInteractionManager.harvestBlockSkippingOnBlockStartBreak(agent, world, blockState, pos, event.getExpToDrop());
                event.setCanceled(true);
            }

            return;
        }

        ChopEvent.StartChopEvent startChopEvent = new ChopEvent.StartChopEvent(
                event,
                world,
                agent,
                pos,
                blockState,
                ChopUtil.getNumChopsByTool(tool, blockState),
                ChopUtil.playerWantsToFell(agent)
        );

        boolean canceled = MinecraftForge.EVENT_BUS.post(startChopEvent);
        if (canceled) {
            return;
        }

        ChopResult chopResult = ChopUtil.getChopResult(
                world,
                pos,
                agent,
                startChopEvent.getNumChops(),
                startChopEvent.getFelling(),
                logPos -> isBlockALog(world, logPos)
        );

        if (chopResult != ChopResult.IGNORED) {
            if (chopResult.apply(pos, agent, tool, ConfigHandler.COMMON.breakLeaves.get())) {
                event.setCanceled(true);

                if (!agent.isCreative()) {
                    ChopUtil.doItemDamage(tool, world, blockState, pos, agent);
                }
            }

            MinecraftForge.EVENT_BUS.post(new ChopEvent.FinishChopEvent(world, agent, pos, blockState));
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        final ResourceLocation loc = new ResourceLocation(TreeChopMod.MOD_ID + "chop_settings_capability");

        Entity entity = event.getObject();
        if (entity instanceof FakePlayer) {
            event.addCapability(loc, new ChopSettingsProvider(ConfigHandler.fakePlayerChopSettings));
        } else {
            event.addCapability(loc, new ChopSettingsProvider());
        }
    }

}
