package ht.treechop.api;

import java.util.Optional;

public interface ChopDataImmutable {
    int getNumChops();

    boolean getFelling();

    default Optional<TreeData> getTree() {
        return Optional.empty();
    }
}