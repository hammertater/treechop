package ht.tuber.graph;

import ht.tuber.math.Vector2;
import ht.tuber.test.TestBlock;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestUtil {

    public static List<Vector2> fourNeighbors = List.of(
            pos(1, 0),
            pos(0, 1),
            pos(-1, 0),
            pos(0, -1)
    );;

    public static Map<Vector2, TestBlock> getTestWorld() {
        return Map.of(
                pos(0, -1), TestBlock.DIRT,
                pos(0, 0), TestBlock.LOG,
                pos(0, 1), TestBlock.LOG,
                pos(0, 2), TestBlock.LEAVES
        );
    }

    public static DirectedGraph<Vector2> getTestGraph() {
        return node -> fourNeighbors.stream().map(node::add);
    }

    public static Vector2 pos(double x, double y) {
        return new Vector2(x, y);
    }

}
