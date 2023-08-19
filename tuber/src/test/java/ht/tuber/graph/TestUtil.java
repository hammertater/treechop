package ht.tuber.graph;

import ht.tuber.math.Vector2;
import ht.tuber.test.TestBlock;

import java.util.List;
import java.util.Map;

public class TestUtil {
    public static Map<Vector2, TestBlock> getTestWorld() {
        return Map.of(
                pos(0, 0), TestBlock.LOG,
                pos(0, 1), TestBlock.LOG,
                pos(0, 2), TestBlock.LEAVES
        );
    }

    public static DirectedGraph<Vector2> getTestGraph() {
        final List<Vector2> neighborOffsets = List.of(
                pos(1, 0),
                pos(0, 1),
                pos(-1, 0),
                pos(0, -1)
        );

        return node -> neighborOffsets.stream().map(node::add);
    }

    public static Vector2 pos(double x, double y) {
        return new Vector2(x, y);
    }

}
