package ht.treechop.common.config;

import com.google.common.collect.Lists;
import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.capabilities.ChopSettings;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static net.minecraftforge.common.config.Configuration.CATEGORY_SPLITTER;

public class ConfigHandler {

    private static final String LIST_SEPARATOR = ",";

    public static boolean enabled;
    public static boolean canChooseNotToChop;

    public static int maxNumTreeBlocks;
    public static int maxNumLeavesBlocks;
    public static boolean breakLeaves;
    public static boolean breakPersistentLeaves;
    public static int maxBreakLeavesDistance;
    private static List<String> logBlockSynonyms;
    private static List<String> leavesBlockSynonyms;

    public static ChopCountingAlgorithm chopCountingAlgorithm;
    public static Rounder chopCountRounding;
    public static boolean canRequireMoreChopsThanBlocks;
    public static float logarithmicA;
    public static float linearM;
    public static float linearB;

    private static List<String> choppingToolBlacklistNames;

    public static boolean choppingEnabled = true;
    public static boolean fellingEnabled = true;
    public static SneakBehavior sneakBehavior = SneakBehavior.INVERT_CHOPPING;
    public static boolean onlyChopTreesWithLeaves = false;

    private static Set<Block> logBlocks = null;
    private static Set<Block> leavesBlocks = null;
    private static Set<Item> choppingToolBlacklistItems = null;

    private static Configuration config;
    private static Stack<String> categoryStack = new Stack<>();

    // Good reference: https://github.com/Vazkii/Botania/blob/1.12-final/src/main/java/vazkii/botania/common/core/handler/ConfigHandler.java
    public static void onReload() {
        reloadCommon();

        logBlocks = null;
        leavesBlocks = null;
        choppingToolBlacklistItems = null;
    }

    private static void reloadCommon() {
        pushCategory("permissions");
        enabled = getBoolean(
                "Whether this mod is enabled or not", "enabled",
                true);
        canChooseNotToChop = getBoolean(
                "Whether players can deactivate chopping e.g. by sneaking", "canChooseNotToChop",
                true);
        popCategory();

        if (TreeChopMod.proxy.isClient()) {
            pushCategory("default-player-settings");
            choppingEnabled = getBoolean(
                    "Default setting for whether or not the user wishes to chop (can be toggled in-game)",
                    "choppingEnabled",
                    true);
            fellingEnabled = getBoolean(
                    "Default setting for whether or not the user wishes to fell tree when chopping (can be toggled in-game)",
                    "fellingEnabled",
                    true);
            sneakBehavior = getEnum(
                    "Default setting for the effect that sneaking has on chopping (can be cycled in-game)",
                    "sneakBehavior",
                    SneakBehavior.INVERT_CHOPPING, SneakBehavior.class);
            onlyChopTreesWithLeaves = getBoolean(
                    "Whether to ignore trees without connected leaves",
                    "onlyChopTreesWithLeaves",
                    true);

            if (Minecraft.getMinecraft().world != null) {
                Client.updateChopSettings(getChopSettings());
            }
            popCategory();
        }

        pushCategory("tree-detection");
        maxNumTreeBlocks = getInt(
                "Maximum number of log block that can be detected to belong to one tree",
                "maxNumTreeBlocks", 512, 1, 8096);
        maxNumLeavesBlocks = getInt(
                "Maximum number of leaves block that can destroyed when a tree is felled",
                "maxNumLeavesBlocks", 1024, 1, 8096);
        breakLeaves = getBoolean(
                "Whether to destroy leaves when a tree is felled",
                "breakLeaves", true);
        maxBreakLeavesDistance = getInt(
                "Maximum distance from tree blocks to destroy leaves blocks when felling (Note: smart leaves destruction is not supported in 1.12.2)",
                "maxBreakLeavesDistance", 4, 0, 16);
        logBlockSynonyms = getStringList(
                "Comma-separated list of blocks that can be chopped\nOre dictionary names are also acceptable",
                "logBlocks", Lists.newArrayList("logWood"));
        leavesBlockSynonyms = getStringList(
                "Comma-separated list of blocks that are automatically broken when attached to a felled tree and breakLeaves=true\nOre dictionary names are also acceptable",
                "leavesBlocks", Lists.newArrayList("treeLeaves"));
        popCategory();

        pushCategory("chop-counting");
        chopCountingAlgorithm = getEnum(
                "Method to use for computing the number of chops needed to fell a tree",
                "algorithm", ChopCountingAlgorithm.LOGARITHMIC, ChopCountingAlgorithm.class);
        chopCountRounding = getEnum(
                "How to round the number of chops needed to fell a tree; this is more meaningful for smaller trees",
                "rounding", Rounder.NEAREST, Rounder.class);
        canRequireMoreChopsThanBlocks = getBoolean(
                "Whether felling a tree can require more chops than the number of blocks in the tree",
                "canRequireMoreChopsThanBlocks", false);

        pushCategory("logarithmic", "See https://github.com/hammertater/treechop/#logarithmic");
        logarithmicA = getFloat(
                "Determines the number of chops required to fell a tree; higher values require more chops for bigger trees",
                "a", 10f, 0f, 10000f);
        popCategory();

        pushCategory("linear", "See https://github.com/hammertater/treechop/#linear");
        linearM = getFloat(
                "The number of chops per block required to fell a tree; if chopsPerBlock = 0.5, it will take 50 chops to fell a 100 block tree",
                "chopsPerBlock", 1f, 0f, 1f);
        linearB = getFloat(
                "The base number of chops required to fell a tree regardless of its size",
                "baseNumChops", 0f, -10000f, 10000f);
        popCategory();
        popCategory();

        pushCategory("compatibility");
        pushCategory("general");
        choppingToolBlacklistNames = getStringList(
                "Comma-separated list of items that should not chop when used to break a log\nOre dictionary names are also acceptable",
                "choppingToolsBlacklist",
                Lists.newArrayList("mekanism:atomic_disassembler"));
        popCategory();

        pushCategory("specific");
        popCategory();
        popCategory();

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static void pushCategory(String name, String comment) {
        pushCategory(name);
        config.setCategoryComment(getCategory(), comment);
    }

    private static void pushCategory(String name) {
        categoryStack.push(name);
    }

    private static void popCategory() {
        categoryStack.pop();
    }

    private static String getCategory() {
        return String.join(CATEGORY_SPLITTER, categoryStack);
    }

    public static ChopSettings getChopSettings() {
        ChopSettings chopSettings = new ChopSettings();
        chopSettings.setChoppingEnabled(choppingEnabled);
        chopSettings.setFellingEnabled(fellingEnabled);
        chopSettings.setSneakBehavior(sneakBehavior);
        chopSettings.setOnlyChopTreesWithLeaves(onlyChopTreesWithLeaves);
        return chopSettings;
    }

    private static List<String> getStringList(String comment, String key, List<String> defaultValues) {
        return Arrays.stream(config.getString(key, getCategory(), String.join(",", defaultValues).concat(","), comment)
                .split(LIST_SEPARATOR))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private static boolean getBoolean(String comment, String key, boolean defaultValue) {
        return config.getBoolean(key, getCategory(), defaultValue, comment);
    }

    private static int getInt(String comment, String key, int defaultValue, int lowerBound, int upperBound) {
        return config.getInt(key, getCategory(), defaultValue, lowerBound, upperBound, comment);
    }

    private static float getFloat(String comment, String key, float defaultValue, float lowerBound, float upperBound) {
        return config.getFloat(key, getCategory(), defaultValue, lowerBound, upperBound, comment);
    }

    private static <T extends Enum<T>> T getEnum(String comment, String key, T defaultValue, Class<T> enumClass) {
        String[] possibleValues = getEnumValuesAsStrings(enumClass);
        return Enum.valueOf(enumClass, config.getString(
                key, getCategory(), defaultValue.name(),
                String.format("%s\nOptions: %s", comment, String.join(", ", possibleValues)),
                possibleValues, possibleValues));
    }

    private static <T extends Enum<T>> String[] getEnumValuesAsStrings(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    public static void load(File configFile)
    {
        config = new Configuration(configFile, "0.2", false);
        config.load();

        onReload();
    }

    private static Set<Item> getLogItems() {
         return logBlockSynonyms.stream()
                .flatMap(str -> OreDictionary.getOres(str).stream())
                .map(ItemStack::getItem)
                .collect(Collectors.toSet());
    }

    public static Set<Block> getLogBlocks() {
        if (logBlocks == null) {
            logBlocks = logBlockSynonyms.stream()
                    .map(a -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(a)))
                    .filter(Objects::nonNull)
                    .filter(b -> b != Blocks.AIR)
                    .collect(Collectors.toSet());

            Set<Block> logItemBlocks = getLogItems().stream()
                    .filter(item -> item instanceof ItemBlock)
                    .map(item -> (ItemBlock) item)
                    .map(ItemBlock::getBlock)
                    .collect(Collectors.toSet());
            logBlocks.addAll(logItemBlocks);
        }
        return logBlocks;
    }

    private static Set<Item> getLeavesItems() {
        return leavesBlockSynonyms.stream()
                .flatMap(str -> OreDictionary.getOres(str).stream())
                .map(ItemStack::getItem)
                .collect(Collectors.toSet());
    }

    public static Set<Block> getLeavesBlocks() {
        if (leavesBlocks == null) {
            leavesBlocks = leavesBlockSynonyms.stream()
                    .map(a -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(a)))
                    .filter(Objects::nonNull)
                    .filter(b -> b != Blocks.AIR)
                    .collect(Collectors.toSet());

            Set<Block> leavesItemBlocks = getLeavesItems().stream()
                    .filter(item -> item instanceof ItemBlock)
                    .map(item -> (ItemBlock) item)
                    .map(ItemBlock::getBlock)
                    .collect(Collectors.toSet());
            leavesBlocks.addAll(leavesItemBlocks);
        }
        return leavesBlocks;
    }

    public static Set<Item> getChoppingToolBlacklistItems() {
        if (choppingToolBlacklistItems == null) {
            choppingToolBlacklistItems = choppingToolBlacklistNames.stream()
                    .flatMap(str -> OreDictionary.getOres(str).stream())
                    .map(ItemStack::getItem)
                    .collect(Collectors.toSet());
            choppingToolBlacklistNames.stream()
                    .map(a -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(a)))
                    .filter(Objects::nonNull)
                    .forEach(choppingToolBlacklistItems::add);
        }
        return choppingToolBlacklistItems;
    }

}
