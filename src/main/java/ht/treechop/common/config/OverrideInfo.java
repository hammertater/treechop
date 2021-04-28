package ht.treechop.common.config;

public class OverrideInfo {
    private final int numChops;
    private final OverrideType type;

    public OverrideInfo(int numChops, OverrideType always) {
        this.numChops = numChops;
        this.type = always;
    }

    public int getNumChops() {
        return numChops;
    }

    public boolean shouldOverride(boolean chopping) {
        if (type == OverrideType.ALWAYS) {
            return true;
        } else if (type == OverrideType.NEVER) {
            return false;
        } else {
            return chopping;
        }
    }

}
