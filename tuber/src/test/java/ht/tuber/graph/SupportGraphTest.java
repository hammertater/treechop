package ht.tuber.graph;

import ht.tuber.math.Vector2;
import ht.tuber.test.TestBlock;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static ht.tuber.graph.TestUtil.pos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportGraphTest {
    @Test
    void simple() {
        Map<Vector2, TestBlock> world = Map.of(
                pos(0, 0), TestBlock.LOG,
                pos(0, 1), TestBlock.LOG,
                pos(0, 2), TestBlock.LEAVES
        );

        DirectedGraph<Vector2> sourceGraph = GraphUtil.filter(TestUtil.getTestGraph(), pos -> world.get(pos) == TestBlock.LOG);
        SupportGraph<Vector2> result = SupportGraph.create(sourceGraph, pos(0, 0));

        List<Vector2> baseSupport = result.getSupport(pos(0, 0)).toList();
        assertEquals(2, baseSupport.size());
        assertTrue(baseSupport.contains(pos(0,0)));
        assertTrue(baseSupport.contains(pos(0,1)));

        assertEquals(1, result.getSupport(pos(0, 1)).count());
    }

    @Test
    void oneBranch() {
        Map<Vector2, TestBlock> world = Map.of(
                pos(0, 0), TestBlock.LOG,
                pos(0, 1), TestBlock.LOG,
                pos(0, 2), TestBlock.LOG,
                pos(1, 1), TestBlock.LOG,
                pos(2, 1), TestBlock.LOG
        );

        DirectedGraph<Vector2> sourceGraph = GraphUtil.filter(TestUtil.getTestGraph(), pos -> world.get(pos) == TestBlock.LOG);
        SupportGraph<Vector2> result = SupportGraph.create(sourceGraph, pos(0, 0));

        assertEquals(5, result.getSupport(pos(0, 0)).count());
        assertEquals(4, result.getSupport(pos(0, 1)).count());
        assertEquals(1, result.getSupport(pos(0, 2)).count());
        assertEquals(2, result.getSupport(pos(1, 1)).count());
        assertEquals(1, result.getSupport(pos(2, 1)).count());
    }

    @Test
    void fatBranch() {
        Map<Vector2, TestBlock> world = Map.of(
                pos(0, 0), TestBlock.LOG,
                pos(0, 1), TestBlock.LOG,
                pos(0, 2), TestBlock.LOG,
                pos(1, 1), TestBlock.LOG,
                pos(1, 2), TestBlock.LOG,
                pos(2, 1), TestBlock.LOG,
                pos(2, 2), TestBlock.LOG
        );

        DirectedGraph<Vector2> sourceGraph = GraphUtil.filter(TestUtil.getTestGraph(), pos -> world.get(pos) == TestBlock.LOG);
        SupportGraph<Vector2> result = SupportGraph.create(sourceGraph, pos(0, 0));

        assertEquals(7, result.getSupport(pos(0, 0)).distinct().count());
        assertEquals(6, result.getSupport(pos(0, 1)).distinct().count());
        assertEquals(4, result.getSupport(pos(1, 1)).distinct().count());
    }

    @Test
    void supporter() {
    }

}