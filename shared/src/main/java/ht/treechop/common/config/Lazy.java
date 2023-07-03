package ht.treechop.common.config;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T cache;
    private boolean available = false;
    private boolean locked = false;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public Lazy(Signal<Lazy<?>> resetSignal, Supplier<T> supplier) {
        this(supplier);
        resetSignal.add(this);
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
        if (!locked) {
            available = false;
        }
    }

    public void override(T override) {
        this.cache = override;
        available = true;
        locked = true;
    }
}
