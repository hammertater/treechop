package ht.tuber.graph;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractGraph<T> implements DirectedGraph<T> {

    private final Map<T, List<T>> nodesAndEdges;

    public AbstractGraph() {
        this(new HashMap<>());
    }

    public AbstractGraph(Map<T, List<T>> nodesAndEdges) {
        this.nodesAndEdges = nodesAndEdges;
    }

    @Override
    public Stream<T> getNeighbors(T node) {
        List<T> neighbors = nodesAndEdges.get(node);
        return  (neighbors != null) ? neighbors.stream() : Stream.empty();
    }

//    public AbstractGraph<T> reduce(HashSet<T> nodes) {
//        HashMap<T, List<T>> newGraphData = new HashMap<>();
//
//        nodes.forEach(node -> {
//            List<T> neighbors = nodesAndEdges.get(node);
//            if (neighbors != null) {
//                newGraphData.put(node, neighbors.stream().filter(nodes::contains).toList());
//            }
//        });
//
//        return new AbstractGraph<>(newGraphData);
//    }

    protected void add(T lower, T upper) {
        List<T> neighbors = nodesAndEdges.computeIfAbsent(lower, k -> new ArrayList<>());
        if (!neighbors.contains(upper)) {
            neighbors.add(upper);
        }
    }
}
