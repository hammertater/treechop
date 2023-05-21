package ht.tuber.graph;

import java.util.function.Predicate;

public class FloodFillImpl<T> extends AbstractFloodFill<T> {

    private final Predicate<T> filter;

    public FloodFillImpl(DirectedGraph<T> graph, Predicate<T> filter) {
        super(graph);
        this.filter = filter;
    }

    @Override
    protected void visitNode(T origin, T target) {
        memory.add(target);
        if (filter.test(target)) {
            nextNodes.add(target);
        }
    }
}
