package ht.treechop.api;

public interface ChopData extends ChopDataImmutable {
    void setNumChops(int numChops);

    void setFelling(boolean felling);
}
