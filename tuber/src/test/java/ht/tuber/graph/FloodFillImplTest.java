package ht.tuber.graph;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FloodFillImplTest {

    @Test
    void fill() {
        DirectedGraph<Integer> world = new DirectedGraph<>() {
            @Override
            public Stream<Integer> getNeighbors(Integer node) {
                return switch (node) {
                    case 1 -> Stream.of(2, 3);
                    case 2 -> Stream.of(3);
                    default -> Stream.empty();
                };
            }
        };

        FloodFill<Integer> flood = new FloodFillImpl<>(world, a -> (a > 0));
        Set<Integer> result = flood.fill(1).collect(Collectors.toSet());

        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }
}