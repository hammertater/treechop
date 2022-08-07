package ht.treechop.common.config;

import ht.treechop.common.settings.ChopSettings;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public abstract class ConfigHandler {

    public static TagKey<Block> blockTagForDetectingLogs;
    public static TagKey<Block> blockTagForDetectingLeaves;
    public final static ChopSettings fakePlayerChopSettings = new ChopSettings();
    public static Map<Item, OverrideInfo> itemOverrides = null;
    public static int maxBreakLeavesDistance = 7;
    public static boolean ignorePersistentLeaves = true;
    public static boolean removeBarkOnInteriorLogs = false;

    protected static ConfigHandler handler;
    public static ConfigHandler get() {
        return handler;
    };

    public abstract double getLinearM();

    public abstract double getLinearB();

    public abstract double getLogarithmicA();

    public abstract Rounder getChopCountRounding();

    public abstract boolean canRequireMoreChopsThanBlocks();
}
