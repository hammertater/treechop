package ht.tuber.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public class SupportGraph<T> extends AbstractGraph<T> {

    public static <T> SupportGraph<T> create(DirectedGraph<T> sourceGraph, T base) {
        return create(sourceGraph, Collections.singleton(base));
    }

    public static <T> SupportGraph<T> create(DirectedGraph<T> sourceGraph, Collection<T> bases) {
        SupportGraph<T> graph = new SupportGraph<>();
        GraphUtil.supportStream(sourceGraph, bases).forEach(link -> graph.add(link.node, link.neighbor));
        return graph;
    }

//    public static <T> SupportGraph<T> create(DirectedGraph<T> sourceGraph, Collection<T> bases) {
//        SupportGraph<T> supportGraph = new SupportGraph<>();
//        Map<T, Integer> nodeValues = new HashMap<>();
//
//        bases.forEach(sink -> nodeValues.put(sink, 0));
//        Queue<T> nextNodes = new ArrayDeque<>(bases);
//
//        while (!nextNodes.isEmpty()) {
//            T lowerNode = nextNodes.poll();
//            int hops = nodeValues.get(lowerNode) + 1;
//
//            sourceGraph.getNeighbors(lowerNode).forEach(upperNode -> {
//                int upperHops = nodeValues.getOrDefault(upperNode, hops + 1);
//                if (upperHops >= hops) {
//                    supportGraph.add(lowerNode, upperNode);
//
//                    if (upperHops > hops) {
//                        nodeValues.put(upperNode, hops);
//                        nextNodes.add(upperNode);
//                    }
//                }
//            });
//        }
//
//        return supportGraph;
//    }

    public Stream<T> getSupport(T node) {
        return getLocalSupport(node).distinct();
    }

    private Stream<T> getLocalSupport(T node) {
        return Stream.concat(
                Stream.of(node),
                getNeighbors(node).flatMap(this::getSupport)
        );
    }

    @Override
    public boolean contains(T node) {
        return false;
    }

    @Override
    public Stream<T> getNeighbors(T node) {
        return null;
    }
}
