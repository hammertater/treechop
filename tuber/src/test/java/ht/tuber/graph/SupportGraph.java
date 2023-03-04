package ht.tuber.graph;

import java.util.*;
import java.util.stream.Stream;

public class SupportGraph<T> extends AbstractGraph<T> {
    public SupportGraph() {
        super();
    }

    public static <T> SupportGraph<T> create(DirectedGraph<T> sourceGraph, T base) {
        return create(sourceGraph, List.of(base));
    }

    public static <T> SupportGraph<T> create(DirectedGraph<T> sourceGraph, Collection<T> bases) {
        SupportGraph<T> supportGraph = new SupportGraph<>();
        Map<T, Integer> nodeValues = new HashMap<>();

        bases.forEach(sink -> nodeValues.put(sink, 0));
        Queue<T> nextNodes = new ArrayDeque<>(bases);

        while (!nextNodes.isEmpty()) {
            T lowerNode = nextNodes.poll();
            int hops = nodeValues.get(lowerNode) + 1;

            sourceGraph.getNeighbors(lowerNode).forEach(upperNode -> {
                int upperHops = nodeValues.getOrDefault(upperNode, hops + 1);
                if (upperHops >= hops) {
                    supportGraph.add(lowerNode, upperNode);

                    if (upperHops > hops) {
                        nodeValues.put(upperNode, hops);
                        nextNodes.add(upperNode);
                    }
                }
            });
        }

        return supportGraph;
    }

    public Stream<T> getSupport(T node) {
        return getLocalSupport(node).distinct();
    }

    private Stream<T> getLocalSupport(T node) {
        return Stream.concat(
                Stream.of(node),
                getNeighbors(node).flatMap(this::getSupport)
        );
    }
}
