package ht.treechop.common.chop;

import ht.treechop.api.TreeData;
import ht.treechop.common.util.BlockNeighbors;
import ht.tuber.graph.*;
import ht.tuber.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LazyTreeData implements TreeData {

    Boolean hasLeaves = null;
    DirectedGraph<BlockPos> treeBlocks = new AbstractGraph<>() {
        @Override
        public Stream<BlockPos> getNeighbors(BlockPos pos) {
            return BlockNeighbors.ADJACENTS_AND_DIAGONALS.stream().map(pos::offset);
        }
    };
    Stream<BlockPos> treeFiller;

    public LazyTreeData(Level level, BlockPos startPos) {
        treeFiller = new PeekingFloodFill<>(
                treeBlocks,
                pos -> checkForLog(level, pos),
                (origin, target) -> checkForGround(level, origin, target))
                .fill(startPos);

        SupportMapper<Vector3, BlockState> supportGraph = world -> {
            Set<Vector3> groundedBlocks = new HashSet<>();

            DirectedGraph<Vector3> graph = pos -> BlockNeighbors.ADJACENTS_AND_DIAGONALS.stream().map(pos::add).peek(neighbor -> {
                if (world.get(neighbor) == ht.tuber.test.TestBlock.DIRT) {
                    groundedBlocks.add(pos);
                }
            });

            FloodFill<Vector3> flood = new FloodFillImpl<>(graph, pos -> world.get(pos) == ht.tuber.test.TestBlock.LOG);
            Set<Vector3> fill = flood.fill(startingPoint).collect(Collectors.toSet());

            DirectedGraph<Vector3> treeBlocks = GraphUtil.filter(graph, fill::contains);
            return SupportGraph.create(treeBlocks, groundedBlocks);
        };
    }

    private void checkForGround(Level level, BlockPos pos, BlockPos neighbor) {
        if (ChopUtil.isBlockGround(level, neighbor, level.getBlockState(neighbor))) {
            groundedBlocks.add(pos);
        }
    }

    private boolean checkForLog(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }

        if (hasLeaves == null && ChopUtil.isBlockLeaves(state)) {
            hasLeaves = Boolean.TRUE;
            return false;
        }

        return ChopUtil.isBlockALog(level, pos, state);
    }

    @Override
    public boolean hasLeaves() {
        if (hasLeaves == null) {
            hasLeaves = true;
        }

        return hasLeaves;
    }

    @Override
    public void setLogBlocks(Set<net.minecraft.core.BlockPos> logBlocks) {

    }

    @Override
    public void setLeaves(boolean hasLeaves) {

    }

    @Override
    public Optional<Set<net.minecraft.core.BlockPos>> getLogBlocks() {
        return Optional.empty();
    }

    @Override
    public Set<net.minecraft.core.BlockPos> getLogBlocksOrEmpty() {
        return null;
    }

    @Override
    public Stream<net.minecraft.core.BlockPos> streamLogBlocks() {
        return null;
    }

    @Override
    public boolean isAProperTree(boolean mustHaveLeaves) {
        return false;
    }

    private class TreeBlock {
    }
}
