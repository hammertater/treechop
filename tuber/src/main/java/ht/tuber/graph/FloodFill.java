package ht.tuber.graph;

import java.util.Collection;
import java.util.stream.Stream;

public interface FloodFill<T> {
    Stream<T> fill();

    Stream<T> fill(T start);

    Stream<T> fill(Collection<T> start);
}
