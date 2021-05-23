package ht.treechop.common.config;

import com.google.common.collect.Lists;
import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import ht.treechop.server.Server;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static net.minecraftforge.common.config.Configuration.CATEGORY_SPLITTER;

public class ConfigHandler {

    private static final String LIST_SEPARATOR = ",";

    public static boolean enabled;

    public static int maxNumTreeBlocks;
    public static int maxNumLeavesBlocks;
    public static boolean breakLeaves;
    public static boolean ignorePersistentLeaves;
    public static int maxBreakLeavesDistance;
    private static List<String> logBlockSynonyms;
    private static List<String> leavesBlockSynonyms;

    public static ChopCountingAlgorithm chopCountingAlgorithm;
    public static Rounder chopCountRounding;
    public static boolean canRequireMoreChopsThanBlocks;
    public static float logarithmicA;
    public static float linearM;
    public static float linearB;

    public static ListType blacklistOrWhitelist;
    private static List<String> choppingToolBlacklistNames;
    // TODO: item overrides list

    public static boolean choppingEnabled;
    public static boolean fellingEnabled;
    public static SneakBehavior sneakBehavior;
    public static boolean treesMustHaveLeaves;
    public static boolean chopInCreativeMode;

    private static Set<Block> logBlocks = null;
    private static Set<Block> leavesBlocks = null;
    private static Set<Item> choppingToolBlacklistItems = null;

    public static boolean showChoppingIndicators;
    public static int indicatorXOffset;
    public static int indicatorYOffset;
    private static boolean showFellingOptions;
    private static boolean showFeedbackMessages;

    private static Configuration config;
    private static Stack<String> categoryStack = new Stack<>();
    private static final List<Pair<Setting, Boolean>> rawPermissions = new LinkedList<>();

    // Good reference: https://github.com/Vazkii/Botania/blob/1.12-final/src/main/java/vazkii/botania/common/core/handler/ConfigHandler.java
    public static void onReload() {
        reloadCommon();

        logBlocks = null;
        leavesBlocks = null;
        choppingToolBlacklistItems = null;

        updatePermissions();
    }

    private static void updatePermissions() {
        Set<Setting> permittedSettings = rawPermissions.stream()
                .filter(Pair::getRight)
                .map(Pair::getLeft)
                .collect(Collectors.toSet());

        Permissions permissions = new Permissions(permittedSettings);

        Server.updatePermissions(permissions);
    }

    private static void reloadCommon() {
        pushCategory("permissions");
        enabled = getBoolean(
                "Whether this mod is enabled or not", "enabled",
                true);

        rawPermissions.clear();
        for (SettingsField field : SettingsField.values()) {
            String fieldName = field.getConfigKey();
            pushCategory(fieldName);
            for (Object value : field.getValues()) {
                String valueName = getPrettyValueName(value);
                boolean permitted = getBoolean( "canBe" + valueName, true);
                rawPermissions.add(Pair.of(new Setting(field, value), permitted));
            }
            popCategory();
        }
        popCategory();

        if (TreeChopMod.proxy instanceof Client) {
            pushCategory("default-player-settings");
            pushCategory("chopping");
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
            treesMustHaveLeaves = getBoolean(
                    "Whether to ignore trees without connected leaves",
                    "treesMustHaveLeaves",
                    true);
            chopInCreativeMode = getBoolean(
                    "Whether to enable chopping when in creative mode (even when false, sneaking can still enable chopping)",
                    "chopInCreativeMode",
                    true);
            popCategory();

            pushCategory("visuals");
            // TODO: useProceduralChoppedModels
            // TODO: removeBarkOnInteriorLogs
            pushCategory("chopping-indicator");
            showChoppingIndicators = getBoolean(
                    "Whether to show an on-screen icon indicating whether targeted blocks can be chopped",
                    "enabled",
                    true);
            indicatorXOffset = getInt(
                    "Horizontal location of the indicator relative to the player's crosshairs; positive values move the indicator to the right",
                    "xOffset",
                    16,
                    -256,
                    256);
            indicatorYOffset = getInt(
                    "Vertical location of the indicator relative to the player's crosshairs; positive values move the indicator down",
                    "yOffset",
                    0,
                    -256,
                    256);
            popCategory();
            popCategory();

            if (Minecraft.getMinecraft().world != null) {
                Client.updateChopSettings();
            }
            popCategory();

            pushCategory("settings-screen");
            showFellingOptions = getBoolean(
                    "Whether to show in-game options for enabling and disable felling",
                    "showFellingOptions",
                    false);
            showFeedbackMessages = getBoolean(
                    "Whether to show chat confirmations when using hotkeys to change chop settings",
                    "showFeedbackMessages",
                    true);
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
        ignorePersistentLeaves = getBoolean(
                "Whether non-decayable leaves are ignored when detecting leaves",
                "ignorePersistentLeaves", true);
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

        pushCategory("blacklist");
        blacklistOrWhitelist = getEnum(
                "Whether the listed items should be blacklisted or whitelisted",
                "blacklistOrWhitelist",
                ListType.BLACKLIST, ListType.class);
        choppingToolBlacklistNames = getStringList(
                "Comma-separated list of items that should not chop when used to break a log\nOre dictionary names are also acceptable",
                "choppingToolsBlacklist",
                Lists.newArrayList("mekanism:atomic_disassembler"));
        popCategory();
        popCategory();

//        pushCategory("specific");
//        popCategory();
        popCategory();

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static String getPrettyValueName(Object value) {
        return Arrays.stream(value.toString().toLowerCase().split("_"))
                .map(WordUtils::capitalize)
                .collect(Collectors.joining());
    }

    private static String getPrettyCategoryName(Object value) {
        return value.toString().replaceAll("([A-Z])", "-$1").toLowerCase();
    }

    private static void pushCategory(String name, String comment) {
        pushCategory(name);
        config.setCategoryComment(getCategory(), comment);
    }

    private static void pushCategory(String name) {
        categoryStack.push(getPrettyCategoryName(name));
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
        chopSettings.setTreesMustHaveLeaves(treesMustHaveLeaves);
        chopSettings.setChopInCreativeMode(chopInCreativeMode);
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

    private static boolean getBoolean(String key, boolean defaultValue) {
        Property prop = config.get(getCategory(), key, defaultValue);
        prop.setLanguageKey(key);
        return prop.getBoolean(defaultValue);
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

    public static boolean canChopWithItem(Item item) {
        boolean isListed = ConfigHandler.getChoppingToolBlacklistItems().contains(item);
        if (blacklistOrWhitelist == ListType.WHITELIST) {
            return isListed;
        } else {
            return !isListed;
        }
    }

}
