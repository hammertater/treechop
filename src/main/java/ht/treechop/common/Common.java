package ht.treechop.common;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.capabilities.ChopSettingsProvider;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.util.ChopResult;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static ht.treechop.common.util.ChopUtil.isBlockALog;

public class Common {

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(Common::onBreakEvent);
        eventBus.addListener(Common::onTagsUpdated);
        eventBus.addGenericListener(Entity.class, Common::onAttachCapabilities);

        ConfigHandler.onReload();
        ChopSettingsCapability.register();
        PacketHandler.init();
    }

    private static void onTagsUpdated(TagsUpdatedEvent event) {
        ITagCollection<Block> blockTags = event.getTagManager().getBlockTags();
        ConfigHandler.updateTags(blockTags);
    }

    private static void onBreakEvent(BlockEvent.BreakEvent event) {
        PlayerEntity agent = event.getPlayer();
        ItemStack tool = agent.getHeldItemMainhand();
        BlockState blockState = event.getState();
        BlockPos pos = event.getPos();

        // Reuse some permission logic from PlayerInteractionManager.tryHarvestBlock
        if (
                !isBlockALog(blockState)
                || !ConfigHandler.COMMON.enabled.get()
                || !ChopUtil.canChopWithTool(tool)
                || !ChopUtil.playerWantsToChop(agent)
                || event.isCanceled()
                || !(event.getWorld() instanceof World)
        ) {
            return;
        }

        World world = (World) event.getWorld();;
        boolean canceled = MinecraftForge.EVENT_BUS.post(new ChopEvent.StartChopEvent(world, agent));
        if (canceled) {
            return;
        }

        ChopResult chopResult = ChopUtil.getChopResult(
                world,
                pos,
                agent,
                ChopUtil.getNumChopsByTool(tool),
                ChopUtil.playerWantsToFell(agent),
                logPos -> isBlockALog(world, logPos)
        );

        if (chopResult != ChopResult.IGNORED) {
            if (chopResult.apply(pos, agent, tool, ConfigHandler.COMMON.breakLeaves.get())) {
                event.setCanceled(true);

                if (!agent.isCreative()) {
                    ChopUtil.doItemDamage(tool, world, blockState, pos, agent);
                }
            }

            MinecraftForge.EVENT_BUS.post(new ChopEvent.FinishChopEvent(world, agent));
        }
    }

    // Helpful reference: https://gist.github.com/FireController1847/c7a50144f45806a996d13efcff468d1b
    private static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        final ResourceLocation loc = new ResourceLocation(TreeChopMod.MOD_ID + "chop_settings_capability");

        Entity entity = event.getObject();
        if (entity instanceof PlayerEntity) {
            event.addCapability(loc, new ChopSettingsProvider());
        }
    }

}
