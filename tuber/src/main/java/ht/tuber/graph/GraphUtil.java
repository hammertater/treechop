package ht.tuber.graph;

import java.util.function.Predicate;

public class GraphUtil {
    public static <T> DirectedGraph<T> filter(DirectedGraph<T> graph, Predicate<T> condition) {
        return new FilteredGraph<>(graph, condition);
    }
}
