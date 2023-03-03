package ht.tuber.floodfill;

import ht.tuber.graph.DirectedGraph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FloodFillImpl<T> implements FloodFill<T> {
    private final DirectedGraph<T> graph;
    private final Predicate<T> filter;

    public FloodFillImpl(DirectedGraph<T> graph, Predicate<T> filter) {
        this.graph = graph;
        this.filter = filter;
    }

    @Override
    public Stream<T> fill(T startingNode) {
        Set<T> memory = new HashSet<>();
        Queue<T> nextNodes = new ArrayDeque<>();
        Queue<T> markedNodes = new ArrayDeque<>();

        visitNode(memory, nextNodes, markedNodes, startingNode);

        while (!nextNodes.isEmpty()) {
            T node = nextNodes.poll();

            for (T neighbor : graph.getNeighbors(node)) {
                if (!memory.contains(neighbor)) {
                    visitNode(memory, nextNodes, markedNodes, neighbor);
                }
            }
        }

        return markedNodes.stream();
    }

    private void visitNode(Set<T> memory, Queue<T> nextNodes, Queue<T> markedNodes, T node) {
        memory.add(node);
        if (filter.test(node)) {
            nextNodes.add(node);
            markedNodes.add(node);
        }
    }
}
