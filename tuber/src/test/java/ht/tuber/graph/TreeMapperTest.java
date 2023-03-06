package ht.tuber.graph;

import ht.tuber.math.Vector2;
import ht.tuber.test.TestBlock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static ht.tuber.graph.TestUtil.pos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeMapperTest {
    @Test
    void mapTree() {
        Map<Vector2, TestBlock> world = Map.of(
                pos(0, -1), TestBlock.DIRT,
                pos(0, 0), TestBlock.LOG,
                pos(0, 1), TestBlock.LOG,
                pos(0, 2), TestBlock.LOG,
                pos(1, 1), TestBlock.LOG,
                pos(2, 1), TestBlock.LOG
        );

        List<Vector2> groundedBlocks = new LinkedList<>();

        DirectedGraph<Vector2> graph = pos -> TestUtil.fourNeighbors.stream().map(pos::add).peek(neighbor -> {
            if (world.get(neighbor) == TestBlock.DIRT) {
                groundedBlocks.add(pos);
            }
        });

        FloodFill<Vector2> flood = new FloodFillImpl<>(graph, pos -> world.get(pos) == TestBlock.LOG);
        Set<Vector2> fill = flood.fill(pos(0, 1)).takeWhile(pos -> {
            if (world.get(pos) == TestBlock.DIRT) {
                groundedBlocks.add(pos);
            }

            return true;
        }).collect(Collectors.toSet());

        assertEquals(1, groundedBlocks.size());
        assertTrue(groundedBlocks.contains(pos(0, 0)));

        DirectedGraph<Vector2> treeBlocks = GraphUtil.filter(graph, fill::contains);
        SupportGraph<Vector2> gradient = SupportGraph.create(treeBlocks, groundedBlocks);

        assertEquals(5, gradient.getSupport(pos(0,0)).count());
        assertEquals(4, gradient.getSupport(pos(0,1)).count());
        assertEquals(1, gradient.getSupport(pos(0,2)).count());
        assertEquals(2, gradient.getSupport(pos(1,1)).count());
        assertEquals(1, gradient.getSupport(pos(2,1)).count());
    }
}