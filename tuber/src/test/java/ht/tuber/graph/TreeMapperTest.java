package ht.tuber.graph;

import ht.tuber.math.Vector2;
import ht.tuber.test.TestBlock;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static ht.tuber.graph.TestUtil.pos;

class TreeMapperTest {
    @Test
    void mapTree() {
        Map<Vector2, TestBlock> world = TestUtil.getTestWorld();
        DirectedGraph<Vector2> graph = TestUtil.getTestGraph();

        List<Vector2> groundedBlocks = new LinkedList<>();

        Set<Vector2> fill = GraphUtil.fill(graph, pos(0, 1), pos -> world.get(pos) == TestBlock.LOG).takeWhile(pos -> {
            if (world.get(pos) == TestBlock.DIRT) {
                groundedBlocks.add(pos);
            }

            return true;
        }).collect(Collectors.toSet());

        DirectedGraph<Vector2> tree = GraphUtil.filter(graph, fill::contains);
        SupportGraph<Vector2> gradient = SupportGraph.create(tree, groundedBlocks);
    }

    private SupportGraph<Vector2> makeWatershedGradients(DirectedGraph<Vector2> graph, Collection<Vector2> sinks) {
        return null;
    }

}