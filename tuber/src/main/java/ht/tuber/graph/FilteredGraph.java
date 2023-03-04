package ht.tuber.graph;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilteredGraph<T> implements DirectedGraph<T> {
    private final DirectedGraph<T> source;
    private final Predicate<T> condition;

    public FilteredGraph(DirectedGraph<T> graph, Predicate<T> condition) {
        this.source = graph;
        this.condition = condition;
    }

    @Override
    public Stream<T> getNeighbors(T node) {
        if (condition.test(node)) {
            return source.getNeighbors(node).filter(condition);
        } else {
            return Stream.empty();
        }
    }
}
