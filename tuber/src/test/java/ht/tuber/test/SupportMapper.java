package ht.tuber.test;

import ht.tuber.graph.SupportGraph;

import java.util.Map;

@FunctionalInterface
public interface SupportMapper<K, V> {
    SupportGraph<K> filter(Map<K, V> world);
}
