package ht.treechop.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Signal<T> implements Runnable {
    private final List<T> targets = new ArrayList<>();
    private final Consumer<T> action;

    Signal(Consumer<T> logic) {
        this.action = logic;
    }

    @Override
    public final void run() {
        targets.forEach(action);
    }

    public void add(T target) {
        targets.add(target);
    }
}
