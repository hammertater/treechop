package ht.treechop.common.network;

@FunctionalInterface
public interface MessageFromServerHandler<T> {
    void accept(T message);
}
