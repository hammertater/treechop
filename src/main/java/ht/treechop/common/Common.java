package ht.treechop.common;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.capabilities.ChopSettingsProvider;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.util.BlockThatWasBroken;
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

public class Common {

    public void preInit() {
        ChopSettingsCapability.register();
        PacketHandler.init();
    }

    @SubscribeEvent
    public void onBreakEvent(BlockEvent.BreakEvent event) {
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

        BlockThatWasBroken choppedBlock = ChopUtil.chop(world, blockPos, agent, ChopUtil.getNumChopsByTool(tool), tool, ChopUtil.playerWantsToFell(agent));
        if (choppedBlock == BlockThatWasBroken.IGNORED) {
            return;
        }

        event.setCanceled(true);

        // The event was canceled to prevent the block from being broken, but we still want all the other consequences of breaking blocks
        if (!agent.isCreative()) {
            BlockPos choppedBlockPos = choppedBlock.getPos();
            IBlockState choppedBlockState = choppedBlock.getState();

            ChopUtil.doItemDamage(tool, world, choppedBlockState, choppedBlockPos, agent);

            if (choppedBlock.canHarvest()) {
                choppedBlockState.getBlock().harvestBlock(world, agent, choppedBlockPos, choppedBlockState, choppedBlock.getTileEntity(), tool); // handles exhaustion, stat-keeping, enchantment effects, and item spawns
                ChopUtil.dropExperience(world, choppedBlockPos, choppedBlockState, event.getExpToDrop());
            }
            // Vanilla does not do exhaustion if the block couldn't be harvested
        }
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
