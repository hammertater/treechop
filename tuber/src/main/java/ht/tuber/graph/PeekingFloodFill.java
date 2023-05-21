package ht.tuber.graph;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class PeekingFloodFill<T> extends FloodFillImpl<T> {

    private final BiConsumer<T, T> peeker;

    public PeekingFloodFill(DirectedGraph<T> graph, Predicate<T> filter, BiConsumer<T, T> peeker) {
        super(graph, filter);
        this.peeker = peeker;
    }

    @Override
    protected void visitNode(T origin, T target) {
        super.visitNode(origin, target);
        peeker.accept(origin, target);
    }
}
