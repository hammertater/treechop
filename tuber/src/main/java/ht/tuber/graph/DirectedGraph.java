package ht.tuber.graph;

import java.util.stream.Stream;

public interface DirectedGraph<T> {
    Stream<T> getNeighbors(T node);
}
