//package ht.treechop.common.compat;
//
//import ht.treechop.TreeChopMod;
//import ht.treechop.client.Client;
//import ht.treechop.common.block.ChoppedLogBlock;
//import ht.treechop.common.util.ChopUtil;
//import mcp.mobius.waila.api.*;
//import mcp.mobius.waila.api.config.IPluginConfig;
//import mcp.mobius.waila.api.event.WailaRayTraceEvent;
//import net.minecraft.core.BlockPos;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraftforge.common.MinecraftForge;
//
//import java.util.Set;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
//@WailaPlugin(TreeChopMod.MOD_ID + ":waila_plugin")
//public class Jade implements IWailaPlugin, IComponentProvider {
//    private static final Jade INSTANCE = new Jade();
//    private static IWailaClientRegistration client;
//
//    public static final ResourceLocation SHOW_TREE_BLOCKS = new ResourceLocation(TreeChopMod.MOD_ID, "show_tree_block_counts");
//    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = new ResourceLocation(TreeChopMod.MOD_ID, "show_num_chops_remaining");
//
//    @Override
//    public void registerClient(IWailaClientRegistration registrar) {
//        Jade.client = registrar;
//        registrar.registerComponentProvider(INSTANCE, TooltipPosition.BODY, Block.class);
//        MinecraftForge.EVENT_BUS.addListener(this::overrideChoppedLog);
//    }
//
//    public void overrideChoppedLog(WailaRayTraceEvent event) {
//        Accessor<?> genericAccessor = event.getAccessor();
//        if (genericAccessor instanceof BlockAccessor accessor) {
//            BlockState state = getLogState(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState());
//            if (state != accessor.getBlockState()) {
//                event.setAccessor(client.createBlockAccessor(
//                        state,
//                        null,
//                        accessor.getLevel(),
//                        accessor.getPlayer(),
//                        null,
//                        accessor.getHitResult(),
//                        accessor.isServerConnected()
//                ));
//            }
//        }
//    }
//
//    @Override
//    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
//        if (ChopUtil.playerWantsToChop(accessor.getPlayer())
//                && ChopUtil.isBlockChoppable(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState())
//                && (config.get(SHOW_TREE_BLOCKS) || config.get(SHOW_NUM_CHOPS_REMAINING))) {
//            Level level = accessor.getLevel();
//            AtomicInteger numChops = new AtomicInteger(0);
//
//            Set<BlockPos> treeBlocks = ChopUtil.getTreeBlocks(level, accessor.getPosition(), Client.getChopSettings().getTreesMustHaveLeaves());
//
//            if (config.get(SHOW_TREE_BLOCKS)) {
//                treeBlocks.stream()
//                        .collect(Collectors.groupingBy((BlockPos pos) -> {
//                            BlockState state = level.getBlockState(pos);
//                            numChops.getAndAdd(ChopUtil.getNumChops(level, pos, state));
//                            return getLogState(level, pos, state).getBlock();
//                        }, Collectors.counting()))
//                        .forEach((block, count) -> {
////                            ITooltip tooltipIcon = new ItemComponent(accessor.getBlock().asItem());
////                            ITooltip tooltipCount = new WrappedComponent(new TextComponent(count.toString()));
////                            tooltip.addLine(new PairComponent(tooltipIcon, tooltipCount));
//                        });
//            } else {
//                treeBlocks.forEach((BlockPos pos) -> numChops.getAndAdd(ChopUtil.getNumChops(level, pos)));
//            }
//
//            if (config.get(SHOW_NUM_CHOPS_REMAINING)) {
//                tooltip.add(new TranslatableComponent("treechop.waila.x_out_of_y_chops", numChops.get(), ChopUtil.numChopsToFell(treeBlocks.size())));
//            }
//        }
//    }
//
//    private BlockState getLogState(Level level, BlockPos pos, BlockState state) {
//        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.Entity entity) {
//            return entity.getOriginalState();
//        } else {
//            return state;
//        }
//    }
//}
