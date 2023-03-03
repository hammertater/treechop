package ht.tuber.graph;

import java.util.List;

public interface DirectedGraph<T> {
    T get(T node);

    List<T> getNeighbors(T node);
}
