package ht.tuber.graph;

public interface PartialDirectedGraph<T> extends DirectedGraph<T> {
    boolean contains(T node);
}
