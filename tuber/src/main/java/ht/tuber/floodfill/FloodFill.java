package ht.tuber.floodfill;

import java.util.stream.Stream;

public interface FloodFill<T> {
    Stream<T> fill(T i);
}
