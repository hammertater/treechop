package ht.treechop.common;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.capabilities.ChopSettingsProvider;
import ht.treechop.common.config.ConfigHandler;
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
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TreeChopMod.MOD_ID)
public class Common {

    public void preInit() {
        ChopSettingsCapability.register();
//        PacketHandler.init(); // TODO
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        World world = event.getWorld();
        BlockPos blockPos = event.getPos();
        EntityPlayer agent = event.getPlayer();
        ItemStack tool = agent.getHeldItemMainhand();

        // Reuse some permission logic from PlayerInteractionManager.tryHarvestBlock
        if (
                !ConfigHandler.enabled ||
                !ChopUtil.canChopWithTool(tool) ||
                !ChopUtil.playerWantsToChop(agent) ||
                event.isCanceled()
        ) {
            return;
        }

        ChopResult chopResult = ChopUtil.chop(world, blockPos, agent, ChopUtil.getNumChopsByTool(tool), tool, ChopUtil.playerWantsToFell(agent));
        if (chopResult == ChopResult.IGNORED) {
            return;
        }

        event.setCanceled(true);
        BlockPos choppedBlockPos = chopResult.getChoppedBlockPos();
        IBlockState choppedBlockState = chopResult.getChoppedBlockState();

        // The event was canceled to prevent the block from being broken, but still want all the other consequences of breaking block
        // TODO: do we need to handle fortune, silk touch, etc.?
        ChopUtil.doItemDamage(tool, world, choppedBlockState, choppedBlockPos, agent);
        ChopUtil.dropExperience(world, choppedBlockPos, choppedBlockState, event.getExpToDrop());
        ChopUtil.doExhaustion(agent);
        //agent.addStat(Stats.BLOCK_MINED.get(choppedBlockState.getBlock())); // TODO
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        final ResourceLocation loc = new ResourceLocation(TreeChopMod.MOD_ID + "chop_settings_capability");

        Entity entity = event.getObject();
        if (entity instanceof EntityPlayer) {
            event.addCapability(loc, new ChopSettingsProvider());
        }
    }
}
