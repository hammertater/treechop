package ht.tuber.graph;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilteredGraph<T> implements DirectedGraph<T> {
    private final DirectedGraph<T> source;
    private final Predicate<T> condition;
    private final boolean onlyFilterNeighbors;

    public FilteredGraph(DirectedGraph<T> graph, Predicate<T> condition) {
        this(graph, condition, false);
    }

    public FilteredGraph(DirectedGraph<T> graph, Predicate<T> condition, boolean onlyFilterNeighbors) {
        this.source = graph;
        this.condition = condition;
        this.onlyFilterNeighbors = onlyFilterNeighbors;
    }

    @Override
    public Stream<T> getNeighbors(T node) {
        if (onlyFilterNeighbors || condition.test(node)) {
            return source.getNeighbors(node).filter(condition);
        } else {
            return Stream.empty();
        }
    }
}
