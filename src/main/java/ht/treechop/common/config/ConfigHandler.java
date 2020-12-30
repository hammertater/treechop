package ht.treechop.common.config;

import com.google.common.collect.Lists;
import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.capabilities.ChopSettings;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
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
import java.util.stream.Collectors;

public class ConfigHandler {

    public static boolean enabled = true;
    public static boolean canChooseNotToChop = true;

    public static int maxNumTreeBlocks = 8096;
    public static int maxNumLeavesBlocks = 8096;
    public static boolean breakLeaves = true;
    public static int maxBreakLeavesDistance = 4;

    public static ChopCountingAlgorithm chopCountingAlgorithm = ChopCountingAlgorithm.LOGARITHMIC;
    public static float chopCountScale = 1F;

    public static boolean choppingEnabled = true;
    public static boolean fellingEnabled = true;
    public static SneakBehavior sneakBehavior = SneakBehavior.INVERT_CHOPPING;
    public static boolean onlyChopTreesWithLeaves = false;

    private static List<String> logBlockSynonyms = Lists.newArrayList("logWood");
    private static List<String> leavesBlockSynonyms = Lists.newArrayList("treeLeaves");
    private static List<String> choppingToolBlacklistNames = Lists.newArrayList("mekanism:atomic_disassembler");
    private static Set<Item> logItems = null;
    private static Set<Block> logBlocks = null;
    private static Set<Item> leavesItems = null;
    private static Set<Block> leavesBlocks = null;
    private static Set<Item> choppingToolBlacklistItems = null;

    static Configuration config;

    private static final String GENERAL = "1-GENERAL";
    private static final String PLAYER_SETTINGS = "2-DEFAULT_PLAYER_SETTINGS";
    private static final String TREE_DETECTION = "3-TREE-DETECTION";
    private static final String CHOPPING = "4-CHOP-COUNTING";
    private static String category;

    // Good reference: https://github.com/Vazkii/Botania/blob/1.12-final/src/main/java/vazkii/botania/common/core/handler/ConfigHandler.java
    public static void onReload() {
        category(GENERAL);
        enabled = getBoolean("enabled", "Whether this mod is enabled or not",
                enabled);
        canChooseNotToChop = getBoolean("canChooseNotToChop", "Whether players can deactivate chopping e.g. by sneaking",
                canChooseNotToChop);

        if (TreeChopMod.proxy.isClient()) {
        category(PLAYER_SETTINGS);
            choppingEnabled = getBoolean("choppingEnabled", "Default setting for whether or not the user wishes to chop (can be toggled in-game)",
                    choppingEnabled);
            fellingEnabled = getBoolean("fellingEnabled", "Default setting for whether or not the user wishes to fell tree when chopping (can be toggled in-game)",
                    fellingEnabled);
            sneakBehavior = getEnum("sneakBehavior", "Default setting for the effect that sneaking has on chopping (can be cycled in-game)",
                    sneakBehavior, SneakBehavior.class);
            onlyChopTreesWithLeaves = getBoolean("onlyChopTreesWithLeaves", "Whether to ignore trees without connected leaves",
                    onlyChopTreesWithLeaves);

            if (Minecraft.getMinecraft().world != null) {
                Client.updateChopSettings(getChopSettings());
            }
        }

        category(TREE_DETECTION);
        maxNumTreeBlocks = getInt("maxNumTreeBlocks", "Maximum number of log block that can be detected to belong to one tree",
                maxNumTreeBlocks, 0, 8096);
        maxNumLeavesBlocks = getInt("maxNumLeavesBlocks", "Maximum number of leaves block that can destroyed when a tree is felled",
                maxNumLeavesBlocks, 0, 8096);
        breakLeaves = getBoolean("breakLeaves", "Whether to destroy leaves when a tree is felled",
                breakLeaves);
        maxBreakLeavesDistance = getInt("maxBreakLeavesDistance", "Maximum distance from tree blocks to destroy leaves blocks when felling (Note: smart leaves destruction is not supported in 1.12.2)",
                maxBreakLeavesDistance, 0, 16);

        logBlockSynonyms = getStringList("logBlocks", "Blocks that can be chopped\nOre dictionary names are also acceptable",
                logBlockSynonyms);
        logItems = null;
        logBlocks = null;

        leavesBlockSynonyms = getStringList("leavesBlocks", "Blocks that are automatically broken when attached to a felled tree and breakLeaves=true\nOre dictionary names are also acceptable",
                leavesBlockSynonyms);
        leavesBlocks = null;
        leavesItems = null;

        choppingToolBlacklistNames = getStringList("choppingToolsBlacklist", "List of items that should not chop when used to break a log\nOre dictionary names are also acceptable",
                choppingToolBlacklistNames);
        choppingToolBlacklistItems = null;

        category(CHOPPING);
        chopCountingAlgorithm = getEnum("chopCountingMethod", "Method to use for computing the number of chops needed to fell a tree",
                chopCountingAlgorithm, ChopCountingAlgorithm.class);
        chopCountScale = getFloat("chopCountScale", "Scales the number of chops (rounding down) required to fell a tree; with chopCountingMethod=LINEAR, this is exactly the number of chops per block",
                chopCountScale, 0, 1024);
        
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static ChopSettings getChopSettings() {
        ChopSettings chopSettings = new ChopSettings();
        chopSettings.setChoppingEnabled(choppingEnabled);
        chopSettings.setFellingEnabled(fellingEnabled);
        chopSettings.setSneakBehavior(sneakBehavior);
        chopSettings.setOnlyChopTreesWithLeaves(onlyChopTreesWithLeaves);
        return chopSettings;
    }

    private static List<String> getStringList(String key, String comment, List<String> defaultValues) {
        return Arrays.stream(config.getString(key, category, String.join(",", defaultValues).concat(","), comment)
                .split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private static boolean getBoolean(String key, String comment, boolean defaultValue) {
        return config.getBoolean(key, category, defaultValue, comment);
    }

    private static int getInt(String key, String comment, int defaultValue, int lowerBound, int upperBound) {
        return config.getInt(key, category, defaultValue, lowerBound, upperBound, comment);
    }

    private static float getFloat(String key, String comment, float defaultValue, float lowerBound, float upperBound) {
        return config.getFloat(key, category, defaultValue, lowerBound, upperBound, comment);
    }

    private static <T extends Enum<T>> T getEnum(String key, String comment, T defaultValue, Class<T> enumClass) {
        String[] possibleValues = getEnumValuesAsStrings(enumClass);
        return Enum.valueOf(enumClass, config.getString(
                key, category, defaultValue.name(),
                String.format("%s\nOptions: %s", comment, String.join(", ", possibleValues)),
                possibleValues, possibleValues));
    }

    private static void category(String name) {
        category = name;
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

    public static Set<Item> getLogItems() {
        if (logItems == null) {
            logItems = logBlockSynonyms.stream()
                    .flatMap(str -> OreDictionary.getOres(str).stream())
                    .map(ItemStack::getItem)
                    .collect(Collectors.toSet());
        }
        return logItems;
    }

    public static Set<Block> getLogBlocks() {
        if (logBlocks == null) {
            logBlocks = logBlockSynonyms.stream()
                    .map(a -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(a)))
                    .filter(Objects::nonNull)
                    .filter(b -> b != Blocks.AIR)
                    .collect(Collectors.toSet());
        }
        return logBlocks;
    }

    public static Set<Item> getLeavesItems() {
        if (leavesItems == null) {
            leavesItems = leavesBlockSynonyms.stream()
                    .flatMap(str -> OreDictionary.getOres(str).stream())
                    .map(ItemStack::getItem)
                    .collect(Collectors.toSet());
        }
        return leavesItems;
    }

    public static Set<Block> getLeavesBlocks() {
        if (leavesBlocks == null) {
            leavesBlocks = leavesBlockSynonyms.stream()
                    .map(a -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(a)))
                    .filter(Objects::nonNull)
                    .filter(b -> b != Blocks.AIR)
                    .collect(Collectors.toSet());
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
