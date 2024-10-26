package ht.treechop.common.chop;

import ht.treechop.api.ChopData;
import ht.treechop.api.TreeData;

import java.util.Optional;

public class ChopDataImpl implements ChopData {
    private int numChops;
    private TreeData tree;

    public ChopDataImpl(int numChops, TreeData tree) {
        this.numChops = numChops;
        this.tree = tree;
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
    public Optional<TreeData> getTree() {
        return Optional.ofNullable(tree);
    }

    public void setTree(TreeData tree) {
        this.tree = tree;
    }
}
