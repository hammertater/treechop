package ht.treechop.common;

import ht.treechop.TreeChopMod;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.capabilities.ChopSettingsProvider;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.util.ChopResult;
import ht.treechop.common.util.ChopUtil;
import ht.treechop.common.util.FauxPlayerInteractionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static ht.treechop.common.util.ChopUtil.isBlockALog;
import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class Common {

    // Don't @SubscribeEvent; FMLCommonSetupEvent fires on Bus.MOD
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        ConfigHandler.updateTags();
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        ItemStack tool = event.getPlayer().getMainHandItem();
        BlockState blockState = event.getState();
        BlockPos pos = event.getPos();

        if (!isBlockALog(blockState)
                || !ConfigHandler.COMMON.enabled.get()
                || event.isCanceled()
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getPlayer() instanceof ServerPlayer agent)
                || !blockState.canHarvestBlock(level, pos, agent)
                || !ChopUtil.canChopWithTool(tool)
        ) {
           return;
        }

        if (!ChopUtil.playerWantsToChop(agent)) {
            if (ConfigHandler.shouldOverrideItemBehavior(tool.getItem(), false)) {
                FauxPlayerInteractionManager.harvestBlockSkippingOnBlockStartBreak(agent, level, blockState, pos, event.getExpToDrop());
                event.setCanceled(true);
            }

            return;
        }

        ChopEvent.StartChopEvent startChopEvent = new ChopEvent.StartChopEvent(
                event,
                level,
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
                level,
                pos,
                agent,
                startChopEvent.getNumChops(),
                startChopEvent.getFelling(),
                logPos -> isBlockALog(level, logPos)
        );

        if (chopResult != ChopResult.IGNORED) {
            if (chopResult.apply(pos, agent, tool, ConfigHandler.COMMON.breakLeaves.get())) {
                event.setCanceled(true);

                if (!agent.isCreative()) {
                    ChopUtil.doItemDamage(tool, level, blockState, pos, agent);
                }
            }

            MinecraftForge.EVENT_BUS.post(new ChopEvent.FinishChopEvent(level, agent, pos, blockState));
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

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(ChopSettingsCapability.class);
    }

}
