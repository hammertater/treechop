package ht.tuber.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractGraph<T> implements PartialDirectedGraph<T> {

    protected final Map<T, List<T>> nodesAndEdges;

    public AbstractGraph() {
        this(new HashMap<>());
    }

    public AbstractGraph(Map<T, List<T>> nodesAndEdges) {
        this.nodesAndEdges = nodesAndEdges;
    }

    protected void add(T lower, T upper) {
        List<T> neighbors = nodesAndEdges.computeIfAbsent(lower, k -> new ArrayList<>());
        if (!neighbors.contains(upper)) {
            neighbors.add(upper);
        }
    }

    @Override
    public boolean contains(T node) {
        return nodesAndEdges.containsKey(node);
    }

    @Override
    public Stream<T> getNeighbors(T node) {
        List<T> neighbors = nodesAndEdges.get(node);
        return  (neighbors != null) ? neighbors.stream() : Stream.empty();
    }
}
