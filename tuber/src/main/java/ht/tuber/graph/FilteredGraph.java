package ht.tuber.graph;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilteredGraph<T> implements DirectedGraph<T> {
    private final DirectedGraph<T> source;
    private final Predicate<T> originFilter;
    private final Predicate<T> neighborFilter;

    public FilteredGraph(DirectedGraph<T> graph, Predicate<T> originFilter, Predicate<T> neighborFilter) {
        this.source = graph;
        this.originFilter = originFilter;
        this.neighborFilter = neighborFilter;
    }

    @Override
    public Stream<T> getNeighbors(T node) {
        if (originFilter.test(node)) {
            return source.getNeighbors(node).filter(neighborFilter);
        } else {
            return Stream.empty();
        }
    }
}
