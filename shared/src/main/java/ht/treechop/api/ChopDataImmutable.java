package ht.treechop.api;

import java.util.Optional;

public interface ChopDataImmutable {
    int getNumChops();

    default Optional<TreeData> getTree() {
        return Optional.empty();
    }
}