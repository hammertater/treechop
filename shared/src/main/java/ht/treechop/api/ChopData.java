package ht.treechop.api;

public class ChopData {
    int numChops;
    boolean felling;

    public ChopData(int numChops, boolean felling) {
        this.numChops = numChops;
        this.felling = felling;
    }

    public int getNumChops() {
        return numChops;
    }

    public boolean getFelling() {
        return felling;
    }
}
