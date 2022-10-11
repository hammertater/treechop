package ht.treechop.common.events;

public abstract class EventHandler {
    protected static EventHandler INSTANCE;

    public static EventHandler get() {
        return INSTANCE;
    }

}
