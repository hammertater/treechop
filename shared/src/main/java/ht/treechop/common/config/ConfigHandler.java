package ht.treechop.common.config;

import ht.treechop.TreeChop;
import ht.treechop.common.config.item.ResourceIdentifier;
import ht.treechop.common.platform.ModLoader;
import ht.treechop.common.settings.*;
import ht.treechop.common.util.AxeAccessor;
import ht.treechop.compat.ProjectMMOChopXp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigHandler {

    private static final Signal<Lazy<?>> RELOAD = new Signal<>(Lazy::reset);
    private static final Signal<Lazy<?>> UPDATE_TAGS = new Signal<>(Lazy::reset);

    public final static Lazy<ChopSettings> defaultChopSettings = new Lazy<>(
            RELOAD,
            () -> {
                ChopSettings chopSettings = new ChopSettings();
                Permissions permissions = getServerPermissions();

                chopSettings.forEach((field, value) -> {
                    if (!permissions.isPermitted(field, value)) {
                        chopSettings.set(field, field.getValues().stream()
                                .filter(candidate -> permissions.isPermitted(new Setting(field, candidate)))
                                .findFirst()
                                .orElse(value)
                        );
                    }
                });
                return chopSettings;
            });

    public final static Lazy<EntityChopSettings> fakePlayerChopSettings = new Lazy<>(
            RELOAD,
            () -> {
                EntityChopSettings chopSettings = new EntityChopSettings() {
                    @Override
                    public boolean isSynced() {
                        return true;
                    }
                };

                chopSettings.setChoppingEnabled(ConfigHandler.COMMON.fakePlayerChoppingEnabled.get())
                        .setFellingEnabled(ConfigHandler.COMMON.fakePlayerFellingEnabled.get())
                        .setTreesMustHaveLeaves(ConfigHandler.COMMON.fakePlayerTreesMustHaveLeaves.get());

                return chopSettings;
            });

    public static Lazy<Boolean> removeBarkOnInteriorLogs = new Lazy<>(
            RELOAD,
            () -> {
                try {
                    return ConfigHandler.CLIENT.removeBarkOnInteriorLogs.get();
                } catch (IllegalStateException e) {
                    // this config isn't available on server, and that's just fine
                    return false;
                }
            });

    public static Lazy<Map<Block, BlockState>> inferredStrippedStates = new Lazy<>(
            UPDATE_TAGS,
            ConfigHandler::inferStrippedStates);

    public static void onReload() {
        RELOAD.run();
        updateTags();
    }

    public static void updateTags() {
        UPDATE_TAGS.run();
    }

    @NotNull
    private static Map<Block, BlockState> inferStrippedStates() {
        Set<Block> choppableBlocks = COMMON.choppableBlocks.get();
        HashMap<Block, BlockState> map = new HashMap<>();
        choppableBlocks.forEach(block -> {
            Block unstripped = inferUnstripped(block);
            if (unstripped != Blocks.AIR && AxeAccessor.getStripped(unstripped) == null) {
                map.put(unstripped, block.defaultBlockState());
            }
        });
        return map;
    }

    private static Block inferUnstripped(Block block) {
        ResourceLocation resource = Registry.BLOCK.getKey(block);
        return inferUnstripped(resource);
    }

    private static Block inferUnstripped(ResourceLocation resource) {
        if (resource != null) {
            ResourceLocation unstripped = getFilteredResourceLocation(resource, "stripped");
            if (unstripped != null) {
                return Registry.BLOCK.get(unstripped);
            }
        }
        return Blocks.AIR;
    }

    private static ResourceLocation getFilteredResourceLocation(ResourceLocation resource, String filterTerm) {
        if (resource != null) {
            String strippedPath = resource.getPath();
            String unstrippedPath = Arrays.stream(strippedPath.split("_")).filter(token -> !token.equals(filterTerm)).collect(Collectors.joining("_"));
            if (!strippedPath.equals(unstrippedPath)) {
                return new ResourceLocation(resource.getNamespace(), unstrippedPath);
            }
        }
        return null;
    }


    private static Stream<Item> getIdentifiedItems(String stringId) {
        ResourceIdentifier id = ResourceIdentifier.from(stringId);
        return id.resolve(Registry.ITEM);
    }

    private static Stream<Block> getIdentifiedBlocks(String stringId) {
        ResourceIdentifier id = ResourceIdentifier.from(stringId);
        return id.resolve(Registry.BLOCK);
    }

    public static boolean canChopWithTool(Player player, ItemStack tool, Level level, BlockPos pos, BlockState blockState) {
        return TreeChop.api.getRegisteredChoppingItemBehavior(tool.getItem())
                .map(choppingItem -> choppingItem.canChop(player, tool, level, pos, blockState))
                .orElse(choppingItemIsBlacklisted(tool.getItem()));
    }

    private static boolean choppingItemIsBlacklisted(Item item) {
        return COMMON.itemsBlacklistOrWhitelist.get().accepts(COMMON.choppingItemsList.get().contains(item));
    }

    public static Permissions getServerPermissions() {
        return new Permissions(ConfigHandler.COMMON.rawPermissions.stream()
                .filter(settingAndConfig -> settingAndConfig.getValue().get())
                .map(Pair::getKey)
                .collect(Collectors.toSet()));
    }

    private static <T> InitializedSupplier<T> defaultValue(T defaultValue) {
        return new InitializedSupplier<>(() -> defaultValue);
    }

    public static Stream<Block> getMushroomStems() {
        return ConfigHandler.getIdentifiedBlocks(getMushroomStemsTagId());
    }

    private static String getMushroomStemsTagId() {
        return String.format("#%s:mushroom_stems", TreeChop.platform.uses(ModLoader.FORGE) ? "forge" : "c");
    }

    public static class InitializedSupplier<T> implements Supplier<T> {
        private Supplier<T> supplier;

        public InitializedSupplier(Supplier<T> defaultSupplier) {
            supplier = defaultSupplier;
        }

        @Override
        public T get() {
            return supplier.get();
        }

        private void set(Supplier<T> newSupplier) {
            supplier = newSupplier;
        }
    }

    public static class Common {

        public final ForgeConfigSpec.BooleanValue enabled;

        protected final List<Pair<Setting, ForgeConfigSpec.BooleanValue>> rawPermissions = new LinkedList<>();

        public final Lazy<Set<Item>> choppingItemsList = new Lazy<>(
                UPDATE_TAGS,
                () -> {
                    Set<Item> items = COMMON.choppingItemsToBlacklistOrWhitelist.get().stream()
                            .flatMap(ConfigHandler::getIdentifiedItems)
                            .collect(Collectors.toSet());

                    ListType blackListOrWhiteList = COMMON.itemsBlacklistOrWhitelist.get();
                    TreeChop.api.getChoppingItemOverrides()
                            .forEach(itemCanChop -> {
                                if (blackListOrWhiteList.accepts(itemCanChop.getValue())) {
                                    items.add(itemCanChop.getKey());
                                } else {
                                    items.remove(itemCanChop.getKey());
                                }
                            });

                    return items;
                }
        );

        public final Lazy<Set<Block>> choppableBlocks = new Lazy<>(
                UPDATE_TAGS,
                () -> {
                    Set<Block> exceptions = COMMON.choppableBlocksExceptionsList.get().stream()
                            .flatMap(ConfigHandler::getIdentifiedBlocks)
                            .collect(Collectors.toSet());

                    Set<Block> blocks = COMMON.choppableBlocksList.get().stream()
                            .flatMap(ConfigHandler::getIdentifiedBlocks)
                            .filter(block -> !exceptions.contains(block))
                            .collect(Collectors.toSet());

                    TreeChop.api.getChoppableBlockOverrides().forEach(blockIsChoppable -> {
                        if (blockIsChoppable.getValue()) {
                            blocks.add(blockIsChoppable.getKey());
                        } else {
                            blocks.remove(blockIsChoppable.getKey());
                        }
                    });

                    return blocks;
                }
        );

        public final Lazy<Set<Block>> leavesBlocks = new Lazy<>(
                UPDATE_TAGS,
                () -> {
                    Set<Block> exceptions = COMMON.leavesBlocksExceptionsList.get().stream()
                            .flatMap(ConfigHandler::getIdentifiedBlocks)
                            .collect(Collectors.toSet());

                    Set<Block> blocks = COMMON.leavesBlocksList.get().stream()
                            .flatMap(ConfigHandler::getIdentifiedBlocks)
                            .filter(block -> !exceptions.contains(block))
                            .collect(Collectors.toSet());

                    TreeChop.api.getLeavesBlockOverrides().forEach(blockIsLeaves -> {
                        if (blockIsLeaves.getValue()) {
                            blocks.add(blockIsLeaves.getKey());
                        } else {
                            blocks.remove(blockIsLeaves.getKey());
                        }
                    });

                    return blocks;
                }
        );

        public final ForgeConfigSpec.BooleanValue dropLootForChoppedBlocks;

        public final ForgeConfigSpec.IntValue maxNumTreeBlocks;
        public final ForgeConfigSpec.IntValue maxNumLeavesBlocks;
        public final ForgeConfigSpec.BooleanValue breakLeaves;
        public final ForgeConfigSpec.BooleanValue ignorePersistentLeaves;
        public final ForgeConfigSpec.IntValue maxBreakLeavesDistance;

        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> choppableBlocksList;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> choppableBlocksExceptionsList;

        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> leavesBlocksList;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> leavesBlocksExceptionsList;

        public final ForgeConfigSpec.EnumValue<ChopCountingAlgorithm> chopCountingAlgorithm;
        public final ForgeConfigSpec.EnumValue<Rounder> chopCountRounding;
        public final ForgeConfigSpec.BooleanValue canRequireMoreChopsThanBlocks;
        public final ForgeConfigSpec.DoubleValue logarithmicA;
        public final ForgeConfigSpec.DoubleValue linearM;
        public final ForgeConfigSpec.DoubleValue linearB;

        public final ForgeConfigSpec.EnumValue<ListType> itemsBlacklistOrWhitelist;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> choppingItemsToBlacklistOrWhitelist;

        public final ForgeConfigSpec.BooleanValue preventChoppingOnRightClick;
        public final ForgeConfigSpec.BooleanValue preventChopRecursion;

        public final ForgeConfigSpec.BooleanValue fakePlayerChoppingEnabled;
        public final ForgeConfigSpec.BooleanValue fakePlayerFellingEnabled;
        public final ForgeConfigSpec.BooleanValue fakePlayerTreesMustHaveLeaves;

        public final InitializedSupplier<Boolean> compatForMushroomStems = defaultValue(true);

        public final InitializedSupplier<Boolean> compatForProjectMMO = defaultValue(true);
        public final InitializedSupplier<ProjectMMOChopXp> pmmoXpMethod = defaultValue(ProjectMMOChopXp.USE_BLOCK_XP);
        public final InitializedSupplier<Double> pmmoScaleXp = defaultValue(1.0);
        public final InitializedSupplier<Long> pmmoOverrideXp = defaultValue(80L);

        public final InitializedSupplier<Boolean> compatForDynamicTrees = defaultValue(true);

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("permissions");
            enabled = builder
                    .comment("Set to false to disable TreeChop without having to uninstall the mod")
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

            builder.push("general");
            dropLootForChoppedBlocks = builder
                    .comment("If false, log items will be destroyed when chopping")
                    .define("dropLootForChoppedBlocks", true);
            builder.pop();

            builder.push("treeDetection");
            maxNumTreeBlocks = builder
                    .comment("Maximum number of log blocks that can be detected to belong to one tree")
                    .defineInRange("maxTreeBlocks", 320, 1, 8096);
            maxNumLeavesBlocks = builder
                    .comment("Maximum number of leaves blocks that can destroyed when a tree is felled")
                    .defineInRange("maxLeavesBlocks", 1024, 1, 8096);
            breakLeaves = builder
                    .comment("Destroy leaves when a tree is felled")
                    .define("breakLeaves", true);
            ignorePersistentLeaves = builder
                    .comment("Non-decayable leaves are ignored when detecting leaves")
                    .define("ignorePersistentLeaves", true);
            maxBreakLeavesDistance = builder
                    .comment("Maximum distance from log blocks to destroy non-standard leaves blocks (e.g. mushroom caps) when felling")
                    .defineInRange("maxBreakLeavesDistance", 7, 0, 16);

            builder.push("logs");
            choppableBlocksList = builder
                    .comment(String.join("\n",
                            "Blocks that should be considered choppable",
                            "Specify using registry names (mod:block), tags (#mod:tag), and namespaces (@mod)"))
                    .defineList("blocks",
                            List.of("#treechop:choppables",
                                    "#minecraft:logs",
                                    "#forge:mushroom_stems"),
                            always -> true);
            choppableBlocksExceptionsList = builder
                    .comment(String.join("\n",
                            "Blocks that should never be chopped, even if included in the list above",
                            "Specify using registry names (mod:block), tags (#mod:tag), and namespaces (@mod)"))
                    .defineList("exceptions",
                            List.of("minecraft:bamboo"),
                            always -> true);
            builder.pop();

            builder.push("leaves");
            leavesBlocksList = builder
                    .comment(String.join("\n",
                            "Blocks that should be considered leaves",
                            "Specify using registry names (mod:block), tags (#mod:tag), and namespaces (@mod)"))
                    .defineList("blocks",
                            List.of("#treechop:leaves_like",
                                    "#minecraft:leaves",
                                    "#minecraft:wart_blocks",
                                    "#forge:mushroom_caps",
                                    "minecraft:shroomlight"),
                            always -> true);
            leavesBlocksExceptionsList = builder
                    .comment(String.join("\n",
                            "Blocks that should never be considered leaves, even if included in the list above",
                            "Specify using registry names (mod:block), tags (#mod:tag), and namespaces (@mod)"))
                    .defineList("exceptions",
                            List.of(),
                            always -> true);
            builder.pop();
            builder.pop();

            builder.push("chopCounting");
            chopCountingAlgorithm = builder
                    .comment("Method to use for computing the number of chops needed to fell a tree")
                    .defineEnum("algorithm", ChopCountingAlgorithm.LOGARITHMIC);
            chopCountRounding = builder
                    .comment("How to round the number of chops needed to fell a tree; this is more meaningful for smaller trees")
                    .defineEnum("rounding", Rounder.NEAREST);
            canRequireMoreChopsThanBlocks = builder
                    .comment("Felling a tree can require more chops than the number of blocks in the tree")
                    .define("canRequireMoreChopsThanBlocks", false);

            builder.comment("See https://github.com/hammertater/treechop/#logarithmic").push("logarithmic");
            logarithmicA = builder
                    .comment("Determines the number of chops required to fell a tree; higher values require more chops for bigger trees")
                    .defineInRange("a", 10.0, 0.0, 10000.0);
            builder.pop();

            builder.comment("See https://github.com/hammertater/treechop/#linear").push("linear");
            linearM = builder
                    .comment("The number of chops per block required to fell a tree; if chopsPerBlock = 0.5, it will take 50 chops to fell a 100 block tree")
                    .defineInRange("chopsPerBlock", 1.0, 0.0, 7.0);
            linearB = builder
                    .comment("The base number of chops required to fell a tree regardless of its size")
                    .defineInRange("baseNumChops", 0.0, -10000.0, 10000.0);
            builder.pop();
            builder.pop();

            builder.push("compatibility");
            builder.push("general");
            preventChoppingOnRightClick = builder
                    .comment("Prevent chopping when right-clicking blocks")
                    .define("preventChoppingOnRightClick", false);
            preventChopRecursion = builder
                    .comment("Prevent infinite loops when chopping; fixes crashes when using modded items that break multiple blocks")
                    .define("preventChopRecursion", true);

            builder.push("blacklist");
            itemsBlacklistOrWhitelist = builder
                    .comment("Whether the listed items should be blacklisted or whitelisted")
                    .defineEnum("blacklistOrWhitelist", ListType.BLACKLIST);
            choppingItemsToBlacklistOrWhitelist = builder
                    .comment(String.join("\n",
                            "List of item registry names (mod:item), tags (#mod:tag), and namespaces (@mod) for items that should not chop when used to break a log",
                            "- Items in this list that have special support for TreeChop will not be blacklisted; see https://github.com/hammertater/treechop/blob/main/docs/compatibility.md#blacklist"))
                    .defineList("items",
                            Arrays.asList(
                                    "#tconstruct:modifiable/harvest",
                                    "botania:terra_axe",
                                    "mekanism:atomic_disassembler",
                                    "@lumberjack",
                                    "practicaltools:iron_greataxe",
                                    "practicaltools:golden_greataxe",
                                    "practicaltools:diamond_greataxe",
                                    "practicaltools:netherite_greataxe"),
                            always -> true);
            builder.pop();

            builder.comment("The chop settings used by non-player entities, such as robots and machine blocks");
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

            compatForMushroomStems.set(builder
                    .comment(String.format("Better chopping behavior for block with the %s tag", getMushroomStemsTagId()))
                    .define("mushroomStems", true));

            if (TreeChop.platform.uses(ModLoader.FORGE)) {
                compatForDynamicTrees.set(builder
                        .comment(String.join("\n",
                                "Prevent conflicts with DynamicTrees",
                                "See https://www.curseforge.com/minecraft/mc-mods/dynamictrees"))
                        .define("dynamicTrees", true));

                builder.push("projectMMO");
                compatForProjectMMO.set(builder
                        .comment(String.join("\n",
                                "Fix ProjectMMO XP awards for chopping",
                                "See https://www.curseforge.com/minecraft/mc-mods/project-mmo"))
                        .define("projectMMO", true));
                pmmoXpMethod.set(builder
                        .comment("When chopping, award the default XP for the chopped block or use a custom value")
                        .defineEnum("useBlockXpOrOverride", ProjectMMOChopXp.USE_BLOCK_XP));
                pmmoScaleXp.set(builder
                        .comment(String.format("Multiplier for the amount of XP awarded if useBlockXpOrOverride = %s", ProjectMMOChopXp.USE_BLOCK_XP.name()))
                        .defineInRange("xpMultiplier", 1.0, 0.0, 100000.0));
                pmmoOverrideXp.set(builder
                        .comment(String.format("How much XP to award if useBlockXpOrOverride = %s", ProjectMMOChopXp.OVERRIDE.name()))
                        .defineInRange("xpOverride", 80L, 0L, 100000L));
                builder.pop();
            }

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
        public final ForgeConfigSpec.BooleanValue showChoppingIndicators;
        public final ForgeConfigSpec.BooleanValue removeBarkOnInteriorLogs;
        public final ForgeConfigSpec.IntValue indicatorXOffset;
        public final ForgeConfigSpec.IntValue indicatorYOffset;
        public final ForgeConfigSpec.BooleanValue showFellingOptions;
        public final ForgeConfigSpec.BooleanValue showFeedbackMessages;
        public final ForgeConfigSpec.BooleanValue showTooltips;

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
                    .comment("Ignore trees without connected leaves (can be toggled in-game)")
                    .define("treesMustHaveLeaves", true);
            chopInCreativeMode = builder
                    .comment("Enable chopping in creative mode (even when false, sneaking can still enable chopping) (can be toggled in-game)")
                    .define("chopInCreativeMode", false);
            builder.pop();

            builder.push("visuals");
            removeBarkOnInteriorLogs = builder
                    .comment("Visually replace the interior sides of logs with a chopped texture instead of bark")
                    .define("removeBarkOnInteriorLogs", true);

            builder.push("choppingIndicator");
            showChoppingIndicators = builder
                    .comment("Show an on-screen indicator when a block will be chopped instead of broken (can be toggled in-game)")
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
                    .comment("Show in-game options for enabling and disable felling (can be toggled in-game)")
                    .define("showFellingOptions", false);
            showFeedbackMessages = builder
                    .comment("Show chat confirmations when using hotkeys to change chop settings (can be toggled in-game)")
                    .define("showFeedbackMessages", true);
            showTooltips = builder
                    .comment("Show tooltips in the settings screen (can be toggled in-game)")
                    .define("showTooltips", true);
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