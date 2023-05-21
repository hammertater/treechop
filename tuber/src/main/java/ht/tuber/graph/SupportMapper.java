package ht.tuber.graph;

import ht.tuber.graph.SupportGraph;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface SupportMapper<K, V> {
    SupportGraph<K> filter(Function<K, V> world);
}
