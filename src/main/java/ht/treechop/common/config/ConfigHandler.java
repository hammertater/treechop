package ht.treechop.common.config;

import ht.treechop.api.IChoppingItem;
import ht.treechop.common.config.item.ItemIdentifier;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import ht.treechop.server.Server;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConfigHandler {

    public static ITag<Block> blockTagForDetectingLogs;
    public static ITag<Block> blockTagForDetectingLeaves;
    public final static ChopSettings fakePlayerChopSettings = new ChopSettings();
    private static Set<Item> itemsBlacklist = null;
    public static Map<Item, OverrideInfo> itemOverrides = null;
    public static int maxBreakLeavesDistance = 7;
    public static boolean ignorePersistentLeaves = true;

    public static void onReload() {
        maxBreakLeavesDistance = COMMON.maxBreakLeavesDistance.get();
        ignorePersistentLeaves = COMMON.ignorePersistentLeaves.get();
        fakePlayerChopSettings.setChoppingEnabled(COMMON.fakePlayerChoppingEnabled.get());
        fakePlayerChopSettings.setFellingEnabled(COMMON.fakePlayerFellingEnabled.get());
        fakePlayerChopSettings.setTreesMustHaveLeaves(COMMON.fakePlayerTreesMustHaveLeaves.get());

        itemsBlacklist = null;
        itemOverrides = null;

        updatePermissions();
    }

    public static void updateTags(ITagCollectionSupplier tagManager) {
        blockTagForDetectingLogs = tagManager.getBlockTags().get(new ResourceLocation(COMMON.blockTagForDetectingLogs.get()));
        blockTagForDetectingLeaves = tagManager.getBlockTags().get(new ResourceLocation(COMMON.blockTagForDetectingLeaves.get()));
        itemsBlacklist = null;
        itemOverrides = null;
    }

    private static void updatePermissions() {
        Set<Setting> permittedSettings = COMMON.rawPermissions.stream()
                .filter(settingAndConfig -> settingAndConfig.getRight().get())
                .map(Pair::getLeft)
                .collect(Collectors.toSet());

        Permissions permissions = new Permissions(permittedSettings);

        Server.updatePermissions(permissions);
    }

    private static Set<Item> getItemsFromConfigList(ITagCollection<Item> tags, List<? extends String> identifiers, Predicate<Item> filter) {
        Map<Item, Integer> qualifiedItems = getQualifiedItemsFromConfigList(tags, identifiers, filter, $ -> 0);
        return qualifiedItems.keySet();
    }

    private static <T> Map<Item, T> getQualifiedItemsFromConfigList(
            ITagCollection<Item> tags,
            List<? extends String> identifiers,
            Predicate<Item> filter,
            Function<ItemIdentifier, T> qualifierParser) {
        return transformConfigList(identifiers, string -> {
            ItemIdentifier id = ItemIdentifier.from(string);
            List<Item> items = id.resolve(tags, ForgeRegistries.ITEMS);
            T qualifier = qualifierParser.apply(id);
            return items.stream().map(item -> Pair.of(item, qualifier)).collect(Collectors.toList());
        }).stream()
                .flatMap(Collection::stream)
                .filter(itemQualifierPair -> itemQualifierPair.getLeft() != Items.AIR && itemQualifierPair.getRight() != null && filter.test(itemQualifierPair.getLeft()))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private static <T> List<T> transformConfigList(List<? extends String> identifiers, Function<String, T> transformer) {
        return identifiers.stream().map(transformer).collect(Collectors.toList());
    }

    public static boolean shouldOverrideItemBehavior(Item item, boolean chopping) {
        OverrideInfo info = getItemOverrides().get(item);
        return (info != null && info.shouldOverride(chopping));
    }

    private static Map<Item, OverrideInfo> getItemOverrides() {
        if (itemOverrides == null) {
            itemOverrides = getQualifiedItemsFromConfigList(
                    TagCollectionManager.getManager().getItemTags(),
                    COMMON.itemsToOverride.get(),
                    item -> !(item instanceof IChoppingItem),
                    id -> new OverrideInfo(getQualifierChops(id), getQualifierOverride(id))
            );
        }

        return itemOverrides;
    }

    private static OverrideType getQualifierOverride(ItemIdentifier id) {
        Optional<String> override = id.getQualifier("override");
        if (override.isPresent()) {
            switch (override.get().toLowerCase()) {
                case "always":
                    return OverrideType.ALWAYS;
                case "chopping":
                    return OverrideType.WHEN_CHOPPING;
                case "never":
                    return OverrideType.NEVER;
                default:
                    id.parsingError(String.format("override qualifier value \"%s\" is not valid", override.get()));
                    return OverrideType.WHEN_CHOPPING;
            }
        } else {
            return OverrideType.WHEN_CHOPPING;
        }
    }

    private static int getQualifierChops(ItemIdentifier id) {
        Optional<String> chops = id.getQualifier("chops");
        if (chops.isPresent()) {
            try {
                return Integer.parseInt(chops.get());
            } catch (NumberFormatException e) {
                id.parsingError(String.format("chops value \"%s\" is not an integer", chops.get()));
                return 1;
            }
        } else {
            return 1;
        }
    }

    /**
     * @return {@code null} if there is no override info for {@code item}
     */
    public static Integer getNumChopsOverride(Item item) {
        OverrideInfo overrideInfo = getItemOverrides().get(item);
        return overrideInfo == null ? null : overrideInfo.getNumChops();
    }

    public static boolean canChopWithItem(Item item) {
        if (itemsBlacklist == null) {
            itemsBlacklist = getItemsFromConfigList(
                    TagCollectionManager.getManager().getItemTags(),
                    COMMON.itemsToBlacklist.get(),
                    item1 -> !(item1 instanceof IChoppingItem));
        }

        if (COMMON.blacklistOrWhitelist.get() == ListType.BLACKLIST) {
            return !itemsBlacklist.contains(item);
        } else {
            return itemsBlacklist.contains(item);
        }
    }

    public static class Common {

        public final ForgeConfigSpec.BooleanValue enabled;

        protected final List<Pair<Setting, ForgeConfigSpec.BooleanValue>> rawPermissions = new LinkedList<>();

        public final ForgeConfigSpec.IntValue maxNumTreeBlocks;
        public final ForgeConfigSpec.IntValue maxNumLeavesBlocks;
        public final ForgeConfigSpec.BooleanValue breakLeaves;
        public final ForgeConfigSpec.BooleanValue ignorePersistentLeaves;
        protected final ForgeConfigSpec.IntValue maxBreakLeavesDistance;
        protected final ForgeConfigSpec.ConfigValue<String> blockTagForDetectingLogs;
        protected final ForgeConfigSpec.ConfigValue<String> blockTagForDetectingLeaves;

        public final ForgeConfigSpec.EnumValue<ChopCountingAlgorithm> chopCountingAlgorithm;
        public final ForgeConfigSpec.EnumValue<Rounder> chopCountRounding;
        public final ForgeConfigSpec.BooleanValue canRequireMoreChopsThanBlocks;
        public final ForgeConfigSpec.DoubleValue logarithmicA;
        public final ForgeConfigSpec.DoubleValue linearM;
        public final ForgeConfigSpec.DoubleValue linearB;

        public final ForgeConfigSpec.EnumValue<ListType> blacklistOrWhitelist;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> itemsToBlacklist;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> itemsToOverride;

        public final ForgeConfigSpec.BooleanValue preventChoppingOnRightClick;
        public final ForgeConfigSpec.BooleanValue preventChopRecursion;
        public final ForgeConfigSpec.BooleanValue compatForProjectMMO;
        public final ForgeConfigSpec.BooleanValue compatForCarryOn;
        public final ForgeConfigSpec.BooleanValue compatForDynamicTrees;
        public final ForgeConfigSpec.BooleanValue fakePlayerChoppingEnabled;
        public final ForgeConfigSpec.BooleanValue fakePlayerFellingEnabled;
        public final ForgeConfigSpec.BooleanValue fakePlayerTreesMustHaveLeaves;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("permissions");
            enabled = builder
                    .comment("Whether this mod is enabled or not")
                    .define("enabled", true);

            for (SettingsField field : SettingsField.values()) {
                String fieldName = field.getConfigKey();
                for (Object value : field.getValues()) {
                    String valueName = getPrettyValueName(value);
                    ForgeConfigSpec.BooleanValue configHandle = builder.define(fieldName + ".canBe" + valueName, true);
                    rawPermissions.add(Pair.of(new Setting(field, value), configHandle));
                }
            }

            builder.pop();

            builder.push("treeDetection");
            maxNumTreeBlocks = builder
                    .comment("Maximum number of log blocks that can be detected to belong to one tree")
                    .defineInRange("maxTreeBlocks", 320, 1, 8096);
            maxNumLeavesBlocks = builder
                    .comment("Maximum number of leaves blocks that can destroyed when a tree is felled")
                    .defineInRange("maxLeavesBlocks", 1024, 1, 8096);
            breakLeaves = builder
                    .comment("Whether to destroy leaves when a tree is felled")
                    .define("breakLeaves", true);
            ignorePersistentLeaves = builder
                    .comment("Whether non-decayable leaves are ignored when detecting leaves")
                    .define("ignorePersistentLeaves", true);
            maxBreakLeavesDistance = builder
                    .comment("Maximum distance from log blocks to destroy non-standard leaves blocks (e.g. mushroom caps) when felling")
                    .defineInRange("maxBreakLeavesDistance", 7, 0, 16);
            blockTagForDetectingLogs = builder
                    .comment("The tag that blocks must have to be considered choppable (default: treechop:choppables)")
                    .define("blockTagForDetectingLogs", "treechop:choppables");
            blockTagForDetectingLeaves = builder
                    .comment("The tag that blocks must have to be considered leaves (default: treechop:leaves_like)")
                    .define("blockTagForDetectingLeaves", "treechop:leaves_like");
            builder.pop();

            builder.push("chopCounting");
            chopCountingAlgorithm = builder
                    .comment("Method to use for computing the number of chops needed to fell a tree")
                    .defineEnum("algorithm", ChopCountingAlgorithm.LOGARITHMIC);
            chopCountRounding = builder
                    .comment("How to round the number of chops needed to fell a tree; this is more meaningful for smaller trees")
                    .defineEnum("rounding", Rounder.NEAREST);
            canRequireMoreChopsThanBlocks = builder
                    .comment("Whether felling a tree can require more chops than the number of blocks in the tree")
                    .define("canRequireMoreChopsThanBlocks", false);

            builder.comment("See https://github.com/hammertater/treechop/#logarithmic").push("logarithmic");
            logarithmicA = builder
                    .comment("Determines the number of chops required to fell a tree; higher values require more chops for bigger trees")
                    .defineInRange("a", 10.0, 0.0, 10000.0);
            builder.pop();

            builder.comment("See https://github.com/hammertater/treechop/#linear").push("linear");
            linearM = builder
                    .comment("The number of chops per block required to fell a tree; if chopsPerBlock = 0.5, it will take 50 chops to fell a 100 block tree")
                    .defineInRange("chopsPerBlock", 7.0, 0.0, 1.0);
            linearB = builder
                    .comment("The base number of chops required to fell a tree regardless of its size")
                    .defineInRange("baseNumChops", 0.0, -10000.0, 10000.0);
            builder.pop();
            builder.pop();

            builder.push("compatibility");
            builder.push("general");
            preventChoppingOnRightClick = builder
                    .comment("Whether to prevent chopping during right-click actions; automatically enabled if compatibility.carryOn = true with Carry On versions prior to carryon-1.16.5-1.15.2.9")
                    .define("preventChoppingOnRightClick", false);
            preventChopRecursion = builder
                    .comment("Whether to prevent infinite loops when chopping; fixes crashes when using modded items that break multiple blocks")
                    .define("preventChopRecursion", true);

            builder.push("blacklist");
            blacklistOrWhitelist = builder
                    .comment("Whether the listed items should be blacklisted or whitelisted")
                    .defineEnum("blacklistOrWhitelist", ListType.BLACKLIST);
            itemsToBlacklist = builder
                    .comment(String.join("\n",
                            "List of item registry names (mod:item), tags (#mod:tag), and namespaces (@mod) for items that should not chop when used to break a log",
                            "- Items in this list that have special support for TreeChop will not be blacklisted; see https://github.com/hammertater/treechop/blob/main/docs/compatibility.md#blacklist"))
                    .defineList("items",
                            Arrays.asList(
                                    "botania:terra_axe",
                                    "mekanism:atomic_disassembler",
                                    "@lumberjack",
                                    "practicaltools:iron_greataxe",
                                    "practicaltools:golden_greataxe",
                                    "practicaltools:diamond_greataxe",
                                    "practicaltools:netherite_greataxe"),
                            always -> true);
            builder.pop();

            builder.push("overrides");
            itemsToOverride = builder
                    .comment(String.join("\n",
                            "List of item registry names (mod:item), tags (#mod:tag), and namespaces (@mod) for items that should not perform their default behavior when chopping",
                            "- Add \"?chops=N\" to specify the number of chops to be performed when breaking a log with the item (defaults to 1)",
                            "- Add \"?override=always\" to disable default behavior even when chopping is disabled",
                            "- Add \"?override=never\" to never disable default behavior",
                            "- Items in this list that have special support for TreeChop will not be overridden; see https://github.com/hammertater/treechop/blob/main/docs/compatibility.md#overrides",
                            "- This might not work as expected for some items; see https://github.com/hammertater/treechop/blob/main/docs/compatibility.md#caveats"))
                    .defineList("items",
                            Arrays.asList(
                                    "#tconstruct:modifiable/harvest",
                                    "silentgear:saw?chops=3,override=always"),
                            always -> true);
            builder.pop();

            builder.comment("The chop settings used by non-player entities, such as robots");
            builder.push("fakePlayerChopSettings");
            fakePlayerChoppingEnabled = builder
                    .comment("Use with caution! May cause conflicts with some mods, e.g. https://github.com/hammertater/treechop/issues/71")
                    .define("choppingEnabled", false);
            fakePlayerFellingEnabled = builder
                    .comment("Felling only matters if chopping is enabled; probably best to leave this on")
                    .define("fellingEnabled", true);
            fakePlayerTreesMustHaveLeaves = builder
                    .define("treesMustHaveLeaves", true);
            builder.pop();

            builder.pop();

            builder.push("specific");
            compatForProjectMMO = builder
                    .comment(String.join("\n",
                            "Whether to enable compatibility with ProjectMMO; for example, award XP for chopping",
                            "See https://www.curseforge.com/minecraft/mc-mods/project-mmo"))
                    .define("projectMMO", true);
            compatForCarryOn = builder
                    .comment(String.join("\n",
                            "Whether to prevent conflicts with Carry On when it is configured to allow picking up logs",
                            "See https://www.curseforge.com/minecraft/mc-mods/carry-on"))
                    .define("carryOn", true);
            compatForDynamicTrees = builder
                    .comment(String.join("\n",
                            "Whether to prevent conflicts with DynamicTrees",
                            "See https://www.curseforge.com/minecraft/mc-mods/dynamictrees"))
                    .define("dynamicTrees", true);
            builder.pop();
            builder.pop();
        }

        @SuppressWarnings("deprecation")
        private String getPrettyValueName(Object value) {
            return Arrays.stream(value.toString().toLowerCase().split("_"))
                    .map(WordUtils::capitalize)
                    .collect(Collectors.joining());
        }
    }

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Client {

        public final ForgeConfigSpec.BooleanValue choppingEnabled;
        public final ForgeConfigSpec.BooleanValue fellingEnabled;
        public final ForgeConfigSpec.EnumValue<SneakBehavior> sneakBehavior;
        public final ForgeConfigSpec.BooleanValue treesMustHaveLeaves;
        public final ForgeConfigSpec.BooleanValue chopInCreativeMode;
        public final ForgeConfigSpec.BooleanValue useProceduralChoppedModels;
        public final ForgeConfigSpec.BooleanValue showChoppingIndicators;
        public final ForgeConfigSpec.BooleanValue removeBarkOnInteriorLogs;
        public final ForgeConfigSpec.IntValue indicatorXOffset;
        public final ForgeConfigSpec.IntValue indicatorYOffset;
        public final ForgeConfigSpec.BooleanValue showFellingOptions;
        public final ForgeConfigSpec.BooleanValue showFeedbackMessages;

//        public final ForgeConfigSpec.BooleanValue treesMustBeUniform; // TODO: a nice implementation requires chopped logs to be typed

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("chopping");
            choppingEnabled = builder
                    .comment("Default setting for whether or not the user wishes to chop (can be toggled in-game)")
                    .define("choppingEnabled", true);
            fellingEnabled = builder
                    .comment("Default setting for whether or not the user wishes to fell tree when chopping (can be toggled in-game)")
                    .define("fellingEnabled", true);
            sneakBehavior = builder
                    .comment("Default setting for the effect that sneaking has on chopping (can be cycled in-game)")
                    .defineEnum("sneakBehavior", SneakBehavior.INVERT_CHOPPING);
            treesMustHaveLeaves = builder
                    .comment("Whether to ignore trees without connected leaves")
                    .define("treesMustHaveLeaves", true);
            chopInCreativeMode = builder
                    .comment("Whether to enable chopping when in creative mode (even when false, sneaking can still enable chopping)")
                    .define("chopInCreativeMode", false);
            builder.pop();

            builder.push("visuals");
            useProceduralChoppedModels = builder
                    .comment("Whether to use procedural chopped log models; disable to use models added by a resource pack")
                    .define("useProceduralChoppedModels", true);
            removeBarkOnInteriorLogs = builder
                    .comment("Whether to replace the interior sides of logs with a chopped texture instead of bark")
                    .define("removeBarkOnInteriorLogs", true);

            builder.push("choppingIndicator");
            showChoppingIndicators = builder
                    .comment("Whether to show an on-screen icon indicating whether targeted blocks can be chopped")
                    .define("enabled", true);
            indicatorXOffset = builder
                    .comment("Horizontal location of the indicator relative to the player's crosshairs; positive values move the indicator to the right")
                    .defineInRange("xOffset", 16, -256, 256);
            indicatorYOffset = builder
                    .comment("Vertical location of the indicator relative to the player's crosshairs; positive values move the indicator down")
                    .defineInRange("yOffset", 0, -256, 256);
            builder.pop();
            builder.pop();

            builder.push("settingsScreen");
            showFellingOptions = builder
                    .comment("Whether to show in-game options for enabling and disable felling")
                    .define("showFellingOptions", false);
            showFeedbackMessages = builder
                    .comment("Whether to show chat confirmations when using hotkeys to change chop settings")
                    .define("showFeedbackMessages", true);
            builder.pop();

//            treesMustBeUniform = builder
//                    .comment("Whether to disallow different types of log blocks from belonging to the same tree")
//                    .define("treesMustBeUniform", true);
        }

        public ChopSettings getChopSettings() {
            ChopSettings chopSettings = new ChopSettings();
            chopSettings.setChoppingEnabled(ConfigHandler.CLIENT.choppingEnabled.get());
            chopSettings.setFellingEnabled(ConfigHandler.CLIENT.fellingEnabled.get());
            chopSettings.setSneakBehavior(ConfigHandler.CLIENT.sneakBehavior.get());
            chopSettings.setTreesMustHaveLeaves(ConfigHandler.CLIENT.treesMustHaveLeaves.get());
            chopSettings.setChopInCreativeMode(ConfigHandler.CLIENT.chopInCreativeMode.get());
            return chopSettings;
        }
    }

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

}
