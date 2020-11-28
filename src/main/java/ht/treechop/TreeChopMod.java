package ht.treechop;

import ht.treechop.block.ChoppedLogBlock;
import ht.treechop.config.ConfigHandler;
import ht.treechop.init.ModBlocks;
import ht.treechop.util.ChopUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ht.treechop.util.ChopUtil.isBlockChoppable;

@Mod("treechop")
public class TreeChopMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "treechop";

    public TreeChopMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener((ModConfig.Loading e) -> ConfigHandler.onConfigLoad());
        modBus.addListener((ModConfig.Reloading e) -> ConfigHandler.onConfigLoad());

        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
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
                event.isCanceled() ||
                !(event.getWorld() instanceof World) ||
                event.getPlayer().isSneaking() ||
                !oldBlockState.canHarvestBlock(world, blockPos, agent) ||
                agent.getHeldItemMainhand().onBlockStartBreak(blockPos, agent) ||
                agent.blockActionRestricted(world, blockPos, agent.getServer().getGameType())
        ) {
            return;
        }

        if (isBlockChoppable(world, blockPos, oldBlockState)) {
            BlockState blockState;
            boolean firstChop;

            if (!(oldBlockState.getBlock() instanceof ChoppedLogBlock)) {
                // TODO: do we need to handle fortune, feather touch, etc.?
                blockState = ChopUtil.chipBlock(world, blockPos, 1, event.getPlayer(), tool);
                firstChop = false;
            } else {
                blockState = oldBlockState;
                firstChop = true;
            }

            if (blockState.getBlock() instanceof ChoppedLogBlock) { // This should always be true... but just in case
                ChoppedLogBlock block = (ChoppedLogBlock) blockState.getBlock();
                ChoppedLogBlock.ChopResult chopResult = block.chop(world, blockPos, blockState, event.getPlayer(), (firstChop) ? 0 : 1, tool);
                BlockPos choppedBlockPos = chopResult.getChoppedBlockPos();
                BlockState choppedBlockState = chopResult.getChoppedBlockState();
                if (choppedBlockState == blockState) {
                    choppedBlockState = oldBlockState;
                }

                // We must cancel the event to prevent the block from being broken, but still want all the other consequences of breaking blocks
                event.setCanceled(true);
                doItemDamage(tool, agent);
                dropExperience(world, choppedBlockPos, choppedBlockState, event.getExpToDrop());
                doExhaustion(agent);
                if (!firstChop) {
                    playBreakingSound(world, choppedBlockPos, choppedBlockState);
                }
                agent.addStat(Stats.BLOCK_MINED.get(choppedBlockState.getBlock()));
                tool.onBlockDestroyed(world, choppedBlockState, choppedBlockPos, agent);
            } else {
                TreeChopMod.LOGGER.warn(String.format("Player \"%s\" failed to chip block \"%s\"", agent.getName(), oldBlockState.getBlock().getRegistryName()));
            }
        }
    }

    private void playBreakingSound(World world, BlockPos blockPos, BlockState blockState) {
        // Copied from World.destroyBlock
        world.playEvent(2001, blockPos, Block.getStateId(blockState));
    }

    private void doExhaustion(PlayerEntity agent) {
        agent.addExhaustion(0.005F);
    }

    private void doItemDamage(ItemStack itemStack, PlayerEntity agent) {
        ItemStack mockItemStack = itemStack.copy();
        if (itemStack.isEmpty() && !mockItemStack.isEmpty()) {
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(agent, mockItemStack, Hand.MAIN_HAND);
        }
    }

    private static void dropExperience(World world, BlockPos blockPos, BlockState blockState, int amount) {
        if (world instanceof ServerWorld) {
            blockState.getBlock().dropXpOnBlockBreak((ServerWorld) world, blockPos, amount);
        }
    }
}
