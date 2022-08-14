package ht.treechop.common.config;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class FabricConfigHandler extends ModRules {
    @Override
    public double getLinearM() {
        return 0;
    }

    @Override
    public double getLinearB() {
        return 0;
    }

    @Override
    public double getLogarithmicA() {
        return 0;
    }

    @Override
    public Rounder getChopCountRounding() {
        return null;
    }

    @Override
    public boolean canRequireMoreChopsThanBlocks() {
        return false;
    }

    @Override
    public int getMaxNumLeavesBlocks() {
        return 0;
    }

    @Override
    public boolean shouldOverrideItemBehavior(Item item, boolean b) {
        return false;
    }

    @Override
    public TagKey<Block> getBlockTagForDetectingLogs() {
        return null;
    }

    @Override
    public TagKey<Block> getBlockTagForDetectingLeaves() {
        return null;
    }

    @Override
    public boolean getIgnorePersistentLeaves() {
        return false;
    }
}
