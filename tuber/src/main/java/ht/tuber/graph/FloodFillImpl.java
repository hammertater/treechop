package ht.tuber.graph;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FloodFillImpl<T> implements FloodFill<T> {
    private final DirectedGraph<T> graph;
    private final Set<T> memory = new HashSet<>();
    private final Queue<T> nextNodes;

    public FloodFillImpl(DirectedGraph<T> graph) {
        this.graph = graph;
        this.nextNodes = new LinkedList<>();
    }

    public FloodFillImpl(DirectedGraph<T> graph, Function<T, Integer> heuristic) {
        this.graph = graph;
        this.nextNodes = new PriorityQueue<>(Comparator.comparing(heuristic));
    }

    @Override
    public Stream<T> fill(T start) {
        return fill(List.of(start));
    }

    @Override
    public Stream<T> fill(Collection<T> starts) {
        starts.forEach(this::visitNode);

        return Stream.iterate(nextNodes.poll(), Objects::nonNull, ignored -> nextNodes.poll())
                .peek(node -> graph.getNeighbors(node).forEach(neighbor -> {
                    if (!memory.contains(neighbor)) {
                        visitNode(neighbor);
                    }
                }));
    }

    private void visitNode(T node) {
        memory.add(node);
        nextNodes.add(node);
    }
}
