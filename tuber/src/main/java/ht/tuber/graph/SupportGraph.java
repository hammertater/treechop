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
            int lowerHops = nodeValues.get(lowerNode);
            int nextHops = lowerHops + 1;

            sourceGraph.getNeighbors(lowerNode).forEach(upperNode -> {
                Integer upperValue = nodeValues.putIfAbsent(upperNode, nextHops);
                int upperHops = upperValue == null ? nextHops : upperValue;

                if (upperHops >= lowerHops) {
                    supportGraph.add(lowerNode, upperNode);

                    if (upperHops > lowerHops) {
                        nextNodes.add(upperNode);

                        if (upperHops > nextHops) {
                            nodeValues.put(upperNode, nextHops);
                        }
                    }
                }
            });
        }

        return supportGraph;
    }

    public Stream<T> getSupport(T base) {
        Queue<T> nextNodes = new LinkedList<>(List.of(base));
        Set<T> visited = new HashSet<>();

        return Stream.concat(
                Stream.of(base),
                Stream.iterate(nextNodes.poll(), Objects::nonNull, ignored -> nextNodes.poll())
                        .filter(node -> !visited.contains(node))
                        .flatMap(node -> {
                            visited.add(node);
                            return getNeighbors(node).peek(nextNodes::add);
                        })
        ).distinct();
    }
}
