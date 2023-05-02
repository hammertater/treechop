package ht.tuber.graph;

import ht.tuber.math.Vector2;
import ht.tuber.math.Vector3;
import ht.tuber.test.TestBlock;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestUtil {

    public final static List<Vector2> fourNeighbors = List.of(
            pos(1, 0),
            pos(0, 1),
            pos(-1, 0),
            pos(0, -1)
    );

    public final static List<Vector3> sixNeighbors = List.of(
            pos(1, 0, 0),
            pos(0, 1, 0),
            pos(0, 0, 1),
            pos(-1, 0, 0),
            pos(0, -1, 0),
            pos(0, 0, -1)
    );

    public final static List<Vector3> allNeighbors = Stream.iterate(0, i -> i < 27, i -> ++i)
            .map(i -> pos(i % 3 - 1, (i / 3) % 3 - 1, (i / 9) % 3 - 1))
            .filter(pos -> !pos.equals(pos(0, 0, 0))).toList();

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

    public static Vector3 pos(int x, int y, int z) {
        return new Vector3(x, y, z);
    }

}
