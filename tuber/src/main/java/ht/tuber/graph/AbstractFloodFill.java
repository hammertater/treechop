package ht.tuber.graph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractFloodFill<T> implements FloodFill<T> {
    protected final DirectedGraph<T> graph;
    protected final Set<T> memory = new HashSet<>();
    protected final Queue<T> nextNodes = new ArrayDeque<>();

    public AbstractFloodFill(DirectedGraph<T> graph) {
        this.graph = graph;
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
                        visitNode(node, neighbor);
                    }
                }));
    }

    private void visitNode(T node) {
        visitNode(node, node);
    }

    protected abstract void visitNode(T origin, T target);
}
