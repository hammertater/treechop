package ht.treechop.common.config;

import com.google.common.collect.Lists;
import ht.treechop.client.Client;
import net.minecraft.block.Block;
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
    public static ChopCountingAlgorithm chopCountingAlgorithm = ChopCountingAlgorithm.LOGARITHMIC;
    public static float chopCountScale = 1F;
    private static List<String> logBlockSynonyms = Lists.newArrayList("logWood");
    private static List<String> leavesBlockSynonyms = Lists.newArrayList("treeLeaves");
    private static List<String> choppingToolBlacklistNames = Lists.newArrayList("");
    private static Set<Item> logItems = null;
    private static Set<Block> logBlocks = null;
    private static Set<Item> leavesItems = null;
    private static Set<Block> leavesBlocks = null;
    public static Set<Item> choppingToolBlacklistItems = null;

    static Configuration config;

    private static final String GENERAL = "General";
    private static final String TREE_DETECTION = "Block Detection";
    private static final String PLAYER_SETTINGS = "Default Settings";
    private static String category;

    // Good reference: https://github.com/Vazkii/Botania/blob/1.12-final/src/main/java/vazkii/botania/common/core/handler/ConfigHandler.java
    public static void onReload() {
        category(GENERAL);
        enabled = getBoolean("enabled", "Whether this mod is enabled or not",
                enabled);
        canChooseNotToChop = getBoolean("canChooseNotToChop", "Whether players can deactivate chopping e.g. by sneaking",
                canChooseNotToChop);

        category(TREE_DETECTION);
        maxNumTreeBlocks = getInt("maxNumTreeBlocks", "Maximum number of log block that can be detected to belong to one tree",
                maxNumTreeBlocks, 0, 8096);
        maxNumLeavesBlocks = getInt("maxNumLeavesBlocks", "Maximum number of leaves block that can destroyed when a tree is felled",
                maxNumLeavesBlocks, 0, 8096);
        breakLeaves = getBoolean("breakLeaves", "Whether to destroy leaves when a tree is felled",
                breakLeaves);
        chopCountingAlgorithm = getEnum("chopCountingMethod", "Method to use for computing the number of chops needed to fell a tree",
                ChopCountingAlgorithm.class, chopCountingAlgorithm);
        chopCountScale = getFloat("chopCountScale", "Scales the number of chops (rounding down) required to fell a tree; with chopCountingMethod=LINEAR, this is exactly the number of chops per block",
                chopCountScale, 0, 1024);

        category(PLAYER_SETTINGS);
        Client.getChopSettings().setChoppingEnabled(
                getBoolean("choppingEnabled", "Default setting for whether or not the user wishes to chop (can be toggled in-game)",
                        Client.getChopSettings().getChoppingEnabled())
        );
        Client.getChopSettings().setFellingEnabled(
                getBoolean("fellingEnabled", "Default setting for whether or not the user wishes to fell tree when chopping (can be toggled in-game)",
                        Client.getChopSettings().getFellingEnabled())
        );
        Client.getChopSettings().setSneakBehavior(
                getEnum("sneakBehavior", "Default setting for the effect that sneaking has on chopping (can be cycled in-game)",
                        SneakBehavior.class, Client.getChopSettings().getSneakBehavior())
        );

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
        
        if (config.hasChanged()) {
            config.save();
        }
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

    private static <T extends Enum<T>> T getEnum(String key, String comment, Class<T> enumClass, T defaultValue) {
        String[] possibleValues = getEnumValuesAsStrings(enumClass);
        return Enum.valueOf(enumClass, config.getString(
                key, category, defaultValue.name(),
                String.format("%s [options: %s]", comment, String.join(", ", possibleValues)),
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
        }
        return choppingToolBlacklistItems;
    }

//    public static class Common {
//
//        public Common(ForgeConfigSpec.Builder builder) {
//            blockTagForDetectingLogs = builder
//                    .comment("The tag that block must have to be considered choppable (default: treechop:choppables)")
//                    .define("blockTagForDetectingLogs", "treechop:choppables");
//            blockTagForDetectingLeaves = builder
//                    .comment("The tag that block must have to be considered leaves (default: treechop:leaves_like)")
//                    .define("blockTagForDetectingLeaves", "treechop:leaves_like");
//            // See https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/common/core/handler/ConfigHandler.java
//            choppingToolsBlacklist = builder
//                    .comment("List of item registry names (mod:item) and tags (#mod:tag) for items that should not chop when used to break a log")
//                    .defineList(
//                            "choppingToolsBlacklist",
//                            Collections.singletonList("#forge:saws"),
//                            ConfigHandler::isRegistryNameOrTag
//                    );
//        }
//    }
//
//    private static boolean isRegistryNameOrTag(Object object) {
//        if (object instanceof String) {
//            String string = (String) object;
//            return (string.startsWith("#") && ResourceLocation.tryCreate(string.substring(1) + ":test") != null ||
//                    ResourceLocation.tryCreate(string + ":test") != null);
//        } else {
//            return false;
//        }
//    }
//
}
