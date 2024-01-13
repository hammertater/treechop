package ht.tuber.graph;

import ht.tuber.math.Vector2;
import ht.tuber.test.TestBlock;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ht.tuber.graph.TestUtil.pos;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeMapperTest {
    @Test
    void mapTree() {
        Map<Vector2, TestBlock> world = TestUtil.getTestWorld();
        DirectedGraph<Vector2> graph = TestUtil.getTestGraph();

        List<Vector2> groundedBlocks = new LinkedList<>();

        Set<Vector2> fill = GraphUtil.flood(
                GraphUtil.filter(graph, pos -> world.get(pos) == TestBlock.LOG),
                pos(0, 1)
        ).fill().takeWhile(pos -> {
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

    @Test
    void haha() {
        final AtomicInteger[] nextNode = {new AtomicInteger(0)};
        Stream.iterate(nextNode[0], Objects::nonNull, ignored -> nextNode[0])
                .peek(node -> {
                    assertTrue(node != null);
                    if (nextNode[0].get() < 100) {
                        nextNode[0].getAndIncrement();
                    } else {
                        nextNode[0] = null;
                    }
                }).forEach(a -> {});
    }

}