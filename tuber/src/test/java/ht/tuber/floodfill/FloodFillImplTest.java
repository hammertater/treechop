package ht.tuber.floodfill;

import ht.tuber.graph.DirectedGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class FloodFillImplTest {

    @Test
    void doIt() {
        DirectedGraph<Integer> world = new DirectedGraph<>() {
            @Override
            public Integer get(Integer index) {
                return 1;
            }

            @Override
            public List<Integer> getNeighbors(Integer node) {
                return switch (get(node)) {
                    case 1 -> List.of(2, 3);
                    case 2 -> List.of(3);
                    default -> Collections.emptyList();
                };
            }
        };

        FloodFill<Integer> flood = new FloodFillImpl<>(world, a -> (a > 0));
        List<Integer> result = flood.fill(1).toList();

        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.containsAll(List.of(1, 2, 3)));
    }
}