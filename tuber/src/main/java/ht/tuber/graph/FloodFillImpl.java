package ht.tuber.graph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FloodFillImpl<T> implements FloodFill<T> {
    private final DirectedGraph<T> graph;
    private final Predicate<T> filter;
    private final Set<T> memory = new HashSet<>();
    private final Queue<T> nextNodes = new ArrayDeque<>();

    public FloodFillImpl(DirectedGraph<T> graph, Predicate<T> filter) {
        this.graph = graph;
        this.filter = filter;
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
        if (filter.test(node)) {
            nextNodes.add(node);
        }
    }
}
