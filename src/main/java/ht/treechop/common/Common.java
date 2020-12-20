package ht.treechop.common;

import ht.treechop.TreeChopMod;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.capabilities.ChopSettingsProvider;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.util.BlockThatWasBroken;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class Common {

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(Common::onBreakEvent);
        eventBus.addGenericListener(Entity.class, Common::onAttachCapabilities);

        ChopSettingsCapability.register();
        PacketHandler.init();
    }

    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        World world = (World) event.getWorld();
        BlockPos blockPos = event.getPos();
        PlayerEntity agent = event.getPlayer();
        ItemStack tool = agent.getHeldItemMainhand();

        // Reuse some permission logic from PlayerInteractionManager.tryHarvestBlock
        if (
                !ConfigHandler.COMMON.enabled.get() ||
                !ChopUtil.canChopWithTool(tool) ||
                !ChopUtil.playerWantsToChop(agent) ||
                event.isCanceled() ||
                !(event.getWorld() instanceof World)
        ) {
            return;
        }

        // TODO: instead of this weird cached block data structure, create a chop job data structure to hold data and perform chopping at our leisure
        BlockThatWasBroken choppedBlock = ChopUtil.chop(world, blockPos, agent, ChopUtil.getNumChopsByTool(tool), tool, ChopUtil.playerWantsToFell(agent));
        if (choppedBlock == BlockThatWasBroken.IGNORED) {
            return;
        }

        event.setCanceled(true);

        // The event was canceled to prevent the block from being broken, but we still want all the other consequences of breaking blocks
        if (!agent.isCreative()) {
            BlockPos choppedBlockPos = choppedBlock.getPos();
            BlockState choppedBlockState = choppedBlock.getState();

            ChopUtil.doItemDamage(tool, world, choppedBlockState, choppedBlockPos, agent);

            if (choppedBlock.canHarvest()) {
                choppedBlockState.getBlock().harvestBlock(world, agent, choppedBlockPos, choppedBlockState, choppedBlock.getTileEntity(), tool); // handles exhaustion, stat-keeping, enchantment effects, and item spawns
                ChopUtil.dropExperience(world, choppedBlockPos, choppedBlockState, event.getExpToDrop());
            }
            // Vanilla does not do exhaustion if the block couldn't be harvested
        }
    }

    // Helpful reference: https://gist.github.com/FireController1847/c7a50144f45806a996d13efcff468d1b
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        final ResourceLocation loc = new ResourceLocation(TreeChopMod.MOD_ID + "chop_settings_capability");

        Entity entity = event.getObject();
        if (entity instanceof PlayerEntity) {
            event.addCapability(loc, new ChopSettingsProvider());
        }
    }
}
