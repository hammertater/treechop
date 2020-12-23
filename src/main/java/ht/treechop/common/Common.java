package ht.treechop.common;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.capabilities.ChopSettingsProvider;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.util.ChopResult;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static ht.treechop.common.util.ChopUtil.isBlockALog;

public class Common {

    public void preInit() {
        ChopSettingsCapability.register();
        PacketHandler.init();
    }

    private static BlockPos currentPos = null; // Assume event-handling is single-threaded; this is used to prevent recursion caused by mod conflicts

    @SubscribeEvent
    public void onBreakEvent(BlockEvent.BreakEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        EntityPlayer agent = event.getPlayer();
        ItemStack tool = agent.getHeldItemMainhand();
        IBlockState blockState = event.getState();

        // Reuse some permission logic from PlayerInteractionManager.tryHarvestBlock
        if (
                pos.equals(currentPos)
                || !isBlockALog(world, pos, blockState)
                || !ConfigHandler.enabled
                || !ChopUtil.canChopWithTool(tool)
                || !ChopUtil.playerWantsToChop(agent)
                || event.isCanceled()
        ) {
            return;
        }

        ChopResult chopResult = ChopUtil.getChopResult(
                world,
                pos,
                agent,
                ChopUtil.getNumChopsByTool(tool),
                tool,
                ChopUtil.playerWantsToFell(agent),
                logPos -> isBlockALog(world, logPos)
        );

        if (chopResult == ChopResult.IGNORED) {
            currentPos = null;
            return;
        }

        event.setCanceled(true);

        if (!agent.isCreative()) {
            ChopUtil.doItemDamage(tool, world, blockState, pos, agent);
        }

        chopResult.apply(pos, agent, tool, ConfigHandler.breakLeaves);

        currentPos = null;
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        final ResourceLocation loc = new ResourceLocation(TreeChopMod.MOD_ID + "chop_settings_capability");

        Entity entity = event.getObject();
        if (entity instanceof EntityPlayer) {
            event.addCapability(loc, new ChopSettingsProvider());
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            EntityPlayer oldPlayer = event.getOriginal();
            EntityPlayer newPlayer = event.getEntityPlayer();
            ChopSettings oldSettings = ChopSettingsCapability.forPlayer(oldPlayer);
            ChopSettings newSettings = ChopSettingsCapability.forPlayer(newPlayer);
            newSettings.copyFrom(oldSettings);
        }
    }

    public boolean isClient() {
        return false;
    }
}
