package ht.treechop.api;

@FunctionalInterface
public interface ITreeChopAPIProvider {
    TreeChopAPI get(String modId);
}
