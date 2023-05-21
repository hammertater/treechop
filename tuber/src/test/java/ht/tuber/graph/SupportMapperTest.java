package ht.tuber.graph;

import ht.tuber.math.Vector3;
import ht.tuber.test.TestBlock;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static ht.tuber.graph.TestUtil.pos;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SupportMapperTest {

    private static SupportMapper<Vector3, TestBlock> makeTreeMapper(Vector3 startingPoint) {
        return blockGetter -> {
            Set<Vector3> groundedBlocks = new HashSet<>();

            DirectedGraph<Vector3> graph = pos -> TestUtil.allNeighbors.stream().map(pos::add).peek(neighbor -> {
                if (blockGetter.apply(neighbor) == ht.tuber.test.TestBlock.DIRT) {
                    groundedBlocks.add(pos);
                }
            });

            FloodFill<Vector3> flood = new FloodFillImpl<>(graph, pos -> blockGetter.apply(pos) == ht.tuber.test.TestBlock.LOG);
            Set<Vector3> fill = flood.fill(startingPoint).collect(Collectors.toSet());

            DirectedGraph<Vector3> treeBlocks = GraphUtil.filter(graph, fill::contains);
            return SupportGraph.create(treeBlocks, groundedBlocks);
        };
    }

    @Test
    void mapOneColumnTree() {
        Map<Vector3, TestBlock> world = Map.of(
                pos(0, -1, 0), ht.tuber.test.TestBlock.DIRT,
                pos(0, 0, 0), ht.tuber.test.TestBlock.LOG,
                pos(0, 1, 0), ht.tuber.test.TestBlock.LOG,
                pos(0, 2, 0), ht.tuber.test.TestBlock.LOG
        );

        SupportGraph<Vector3> gradient = makeTreeMapper(pos(0,0,0)).filter(world::get);

        assertEquals(3, gradient.getSupport(pos(0,0, 0)).count());
        assertEquals(2, gradient.getSupport(pos(0,1, 0)).count());
        assertEquals(1, gradient.getSupport(pos(0,2, 0)).count());
    }

    @Test
    void mapFatTree() {
        Map<Vector3, TestBlock> world = Map.of(
                pos(0, -1, 0), ht.tuber.test.TestBlock.DIRT,
                pos(1, -1, 0), ht.tuber.test.TestBlock.DIRT,
                pos(0, 0, 0), ht.tuber.test.TestBlock.LOG,
                pos(1, 0, 0), ht.tuber.test.TestBlock.LOG
        );

        SupportGraph<Vector3> gradient = makeTreeMapper(pos(0,0,0)).filter(world::get);

        assertEquals(2, gradient.getSupport(pos(0,0, 0)).count());
        assertEquals(2, gradient.getSupport(pos(1,0, 0)).count());
    }

    @Test
    void mapBigTree() {
        final int width = 3;
        final int length = 3;
        final int height = 3;

        Map<Vector3, TestBlock> world = new HashMap<>();
        for (int x = 0; x < width; ++x) {
            for (int z = 0; z < length; ++z) {
                world.put(pos(x, -1, z), TestBlock.DIRT);

                for (int y = 0; y < height; ++y) {
                    world.put(pos(x, y, z), TestBlock.LOG);
                }
            }
        }

//        long startTime = System.nanoTime();
//        int n = 100;
//        for (int i = 0; i < n; ++i) {
//            treeMapper.filter(world);
//        }
//        long timePassed = (System.nanoTime() - startTime) / 1000000;
//        long avgTime = timePassed / n;

        SupportGraph<Vector3> gradient = makeTreeMapper(pos(0,0,0)).filter(world::get);

        assertEquals(width * length * height, gradient.getSupport(pos(0, 0, 0)).count());
    }

}