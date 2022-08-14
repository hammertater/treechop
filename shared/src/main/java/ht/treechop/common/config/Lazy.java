package ht.treechop.common.config;

import java.util.function.Supplier;

public class Lazy<T extends Object> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T cache;
    private boolean available = false;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (!available) {
            cache = supplier.get();
            available = true;
        }
        return cache;
    }

    public void reset() {
        available = false;
    }
}
