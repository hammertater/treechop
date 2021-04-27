package ht.treechop.common.config;

public class OverrideInfo {
    private final int numChops;
    private final boolean always;

    public OverrideInfo(int numChops, boolean always) {
        this.numChops = numChops;
        this.always = always;
    }

    public int getNumChops() {
        return numChops;
    }

    public boolean always() {
        return always;
    }
}
