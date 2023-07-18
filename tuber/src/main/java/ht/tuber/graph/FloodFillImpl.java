package ht.tuber.graph;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FloodFillImpl<T> implements FloodFill<T> {
    private final DirectedGraph<T> graph;
    private final Set<T> memory = new HashSet<>();
    private final Queue<T> nextNodes;
    private T nextNode;

    public FloodFillImpl(Collection<T> starts, DirectedGraph<T> graph, Function<T, Integer> heuristic) {
        this.graph = graph;
        this.nextNodes = new PriorityQueue<>(Comparator.comparing(heuristic));
        addNodes(starts);
    }

    private Stream<T> fill(Consumer<T> visitor) {
        return Stream.iterate(nextNode, Objects::nonNull, ignored -> nextNode)
                .peek(node -> {
                    graph.getNeighbors(node).forEach(neighbor -> {
                        if (!memory.contains(neighbor)) {
                            visitor.accept(neighbor);
                        }
                    });
                    nextNode = nextNodes.poll();
                });
    }

    @Override
    public Stream<T> fill() {
        return fill(this::visitNode);
    }

    @Override
    public Stream<T> fill(Collection<T> starts) {
        addNodes(starts);
        return fill();
    }

    public void fillOnce(Consumer<T> forEach) {
        List<T> nextNextNodes = new LinkedList<>();
        fill(node -> {
            memory.add(node);
            nextNextNodes.add(node);
        }).forEach(forEach);
        addNodes(nextNextNodes);
    }

    private void visitNode(T node) {
        memory.add(node);
        nextNodes.add(node);
    }

    public Stream<T> getNextNodes() {
        return nextNodes.stream();
    }

    private void addNodes(Collection<T> nodes) {
        nodes.forEach(this::visitNode);

        // Reset the next node using the priority queue
        if (nextNode != null) {
            nextNodes.add(nextNode);
        }
        nextNode = nextNodes.poll();
    }
}
