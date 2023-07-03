package ht.tuber.graph;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FloodFillImplTest {

    @Test
    void fill() {
        DirectedGraph<Integer> world = node -> Stream.of(1, 2, 3);

        FloodFill<Integer> flood = new FloodFillImpl<>(world);
        Set<Integer> result = flood.fill(1).collect(Collectors.toSet());

        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }

    @Test
    void fillAscending() {
        DirectedGraph<Integer> world = node -> Stream.of(1, 2, 3);

        FloodFill<Integer> flood = new FloodFillImpl<>(world, a -> a);
        Object[] result = flood.fill(1).toArray();

        assertArrayEquals(new Integer[]{1, 2, 3}, result);
    }

    @Test
    void fillDescending() {
        DirectedGraph<Integer> world = node -> Stream.of(1, 2, 3);

        FloodFill<Integer> flood = new FloodFillImpl<>(world, a -> -a);
        Object[] result = flood.fill(1).toArray();

        assertArrayEquals(new Integer[]{1, 3, 2}, result);
    }
}