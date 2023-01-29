package ht.treechop.common.chop;

import ht.treechop.api.ChopData;

public class ChopDataImpl implements ChopData {
    private boolean felling;
    private int numChops;

    public ChopDataImpl(int numChops, boolean felling) {
        this.numChops = numChops;
        this.felling = felling;
    }

    @Override
    public int getNumChops() {
        return numChops;
    }

    @Override
    public void setNumChops(int numChops) {
        this.numChops = numChops;
    }

    @Override
    public boolean getFelling() {
        return felling;
    }

    @Override
    public void setFelling(boolean felling) {
        this.felling = felling;
    }
}
