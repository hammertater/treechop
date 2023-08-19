package ht.tuber.graph;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FloodFillImplTest {

    @Test
    void fill() {
        DirectedGraph<Integer> world = node -> Stream.of(1, 2, 3);

        FloodFill<Integer> flood = new FloodFillImpl<>(List.of(1), world, a -> 0);
        Set<Integer> result = flood.fill().collect(Collectors.toSet());

        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }

    @Test
    void fillAscending() {
        DirectedGraph<Integer> world = node -> Stream.of(1, 2, 3);

        FloodFill<Integer> flood = new FloodFillImpl<>(List.of(1), world, a -> a);
        Object[] result = flood.fill().toArray();

        assertArrayEquals(new Integer[]{1, 2, 3}, result);
    }

    @Test
    void fillDescending() {
        DirectedGraph<Integer> world = node -> Stream.of(1, 2, 3);

        FloodFill<Integer> flood = new FloodFillImpl<>(List.of(1), world, a -> -a);
        Object[] result = flood.fill().toArray();

        assertArrayEquals(new Integer[]{1, 3, 2}, result);
    }

    @Test
    void fillOnce() {
        DirectedGraph<Integer> world = node -> Stream.of(1, 2, 3);

        FloodFillImpl<Integer> flood = new FloodFillImpl<>(List.of(1), world, a -> 0);
        AtomicInteger counter = new AtomicInteger(0);
        flood.fillOnce(a -> counter.incrementAndGet());

        assertEquals(1, counter.get());
    }
}