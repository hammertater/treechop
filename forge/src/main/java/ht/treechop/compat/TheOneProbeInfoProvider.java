package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.TreeData;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.util.TreeCache;
import ht.treechop.server.Server;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.elements.ElementHorizontal;
import mcjty.theoneprobe.apiimpl.elements.ElementItemLabel;
import mcjty.theoneprobe.apiimpl.elements.ElementText;
import mcjty.theoneprobe.apiimpl.elements.ElementVertical;
import mcjty.theoneprobe.apiimpl.styles.TextStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TheOneProbeInfoProvider implements IProbeInfoProvider {
    private static final boolean SHOW_TREE_BLOCKS = true;
    private static final boolean SHOW_NUM_CHOPS_REMAINING = true;
    private static final TreeCache treeCache = new TreeCache();

    public static Void register(ITheOneProbe probe) {
        probe.registerProvider(new TheOneProbeInfoProvider());
        return null;
    }

    private static BlockState getLogState(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            return entity.getOriginalState();
        } else {
            return state;
        }
    }

    @Override
    public ResourceLocation getID() {
        return TreeChop.resource("tree_info");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo builder, Player player, Level level, BlockState blockState, IProbeHitData iProbeHitData) {
        changeBlockName(builder, level, iProbeHitData.getPos());

        BlockPos blockPos = iProbeHitData.getPos();
        ChopSettings chopSettings = Server.instance().getPlayerChopData(player).getSettings();

        if (ChopUtil.playerWantsToChop(player, chopSettings)
                && ChopUtil.isBlockChoppable(level, blockPos, blockState)
                && SHOW_TREE_BLOCKS || SHOW_NUM_CHOPS_REMAINING) {
            AtomicInteger numChops = new AtomicInteger(0);

            int maxNumTreeBlocks = ConfigHandler.COMMON.maxNumTreeBlocks.get();
            TreeData tree = treeCache.getTree(level, blockPos, maxNumTreeBlocks);
            if (tree.isAProperTree(chopSettings.getTreesMustHaveLeaves())) {
                tree.getLogBlocks().ifPresent(
                        treeBlocks -> {
                            if (SHOW_NUM_CHOPS_REMAINING) {
                                treeBlocks.forEach((BlockPos pos) -> numChops.getAndAdd(ChopUtil.getNumChops(level, pos)));
                                builder.text(Component.translatable("treechop.waila.x_out_of_y_chops", numChops.get(), ChopUtil.numChopsToFell(level, treeBlocks)));
                            }

                            if (SHOW_TREE_BLOCKS) {
                                IItemStyle itemStyle = builder.defaultItemStyle();
                                IProbeInfo tiles = builder.horizontal();
                                treeBlocks.stream()
                                        .collect(Collectors.groupingBy((BlockPos pos) -> {
                                            BlockState state = level.getBlockState(pos);
                                            return getLogState(level, pos, state).getBlock();
                                        }, Collectors.counting()))
                                        .forEach((block, count) -> {
                                            ItemStack stack = block.asItem().getDefaultInstance();
                                            stack.setCount(count.intValue());
                                            tiles.item(stack, itemStyle);
                                        });
                            }
                        });
            }
        }
    }

    private void changeBlockName(IProbeInfo builder, Level level, BlockPos pos) {
        try {
            if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity choppedEntity) {
                if (builder.getElements().get(0) instanceof ElementHorizontal iconAndBlockInfo) {
                    if (iconAndBlockInfo.getElements().get(1) instanceof ElementVertical blockNameAndMod) {
                        if (blockNameAndMod.getElements().get(0) instanceof ElementItemLabel label) {
                            blockNameAndMod.getElements().set(0, new ElementText(WailaUtil.getPrefixedBlockName(choppedEntity), new TextStyle().height(label.getHeight())));
                        }
                    }
                }
            }
        } catch (NoSuchMethodError | NoClassDefFoundError | NullPointerException ignored) {
        }
    }
}
