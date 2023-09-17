package ht.treechop.common.chop;

import ht.treechop.api.FellData;
import ht.treechop.api.TreeData;

public class FellDataImpl implements FellData {
    private TreeData tree;
    private boolean breakLeaves;

    public FellDataImpl(TreeData tree, boolean breakLeaves) {
        this.tree = tree;
        this.breakLeaves = breakLeaves;
    }

    @Override
    public void setTree(TreeData tree) {
        this.tree = tree;
    }

    @Override
    public void setBreakLeaves(boolean breakLeaves) {
        this.breakLeaves = breakLeaves;
    }

    @Override
    public TreeData getTree() {
        return tree;
    }

    @Override
    public boolean getBreakLeaves() {
        return breakLeaves;
    }
}
