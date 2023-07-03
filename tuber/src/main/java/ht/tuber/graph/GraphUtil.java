package ht.tuber.graph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GraphUtil {

    public static <T> Stream<T> fill(DirectedGraph<T> graph, T start, Predicate<T> condition) {
        return fill(filter(graph, condition), start);
    }

    public static <T> Stream<T> fill(DirectedGraph<T> graph, Collection<T> start, Predicate<T> condition) {
        return fill(filter(graph, condition), start);
    }

    public static <T> Stream<T> fill(DirectedGraph<T> graph, T start) {
        return fill(graph, Collections.singleton(start));
    }

    public static <T> Stream<T> fill(DirectedGraph<T> graph, Collection<T> start) {
        return new FloodFillImpl<>(graph).fill(start);
    }

    public static <T> DirectedGraph<T> filter(DirectedGraph<T> graph, Predicate<T> condition) {
        return new FilteredGraph<>(graph, condition);
    }

    public static <T> Stream<Link<T>> supportStream(DirectedGraph<T> sourceGraph, T bases) {
        return supportStream(sourceGraph, Collections.singleton(bases));
    }

    public static <T> Stream<Link<T>> supportStream(DirectedGraph<T> sourceGraph, Collection<T> bases) {
        Map<T, Integer> nodeValues = new HashMap<>();

        bases.forEach(sink -> nodeValues.put(sink, 0));
        Queue<T> nextNodes = new ArrayDeque<>(bases);

        Link<T> recycledLink = new Link<>();
        return Stream.iterate(nextNodes.poll(), Objects::nonNull, t -> nextNodes.poll())
                .flatMap(lowerNode -> {
                    recycledLink.node = lowerNode;
                    int hops = nodeValues.get(lowerNode) + 1;

                    return sourceGraph.getNeighbors(lowerNode).map(upperNode -> {
                        Integer upperHops = nodeValues.getOrDefault(upperNode, hops + 1);

                        if (upperHops >= hops) {
                            if (upperHops > hops) {
                                nodeValues.put(upperNode, hops);
                                nextNodes.add(upperNode);
                            }
                            recycledLink.neighbor = upperNode;
                            return recycledLink;
                        }

                        return null;
                    }).takeWhile(Objects::nonNull);
                });
    }
}
