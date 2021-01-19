package ht.treechop.common.config;

import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.util.FaceShape;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigHandler {

    public static ITag<Block> blockTagForDetectingLogs;
    public static ITag<Block> blockTagForDetectingLeaves;
    public static Set<ResourceLocation> choppingToolItemsBlacklist;
    public static Set<ResourceLocation> choppingToolTagsBlacklist;
    public static int maxBreakLeavesDistance;

    public static void onReload() {
        choppingToolItemsBlacklist = COMMON.choppingToolsBlacklist.get().stream()
                .filter(tag -> !tag.startsWith("#"))
                .map(ResourceLocation::tryCreate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        choppingToolTagsBlacklist = COMMON.choppingToolsBlacklist.get().stream()
                .filter(tag -> tag.startsWith("#"))
                .map(tag -> ResourceLocation.tryCreate(tag.substring(1)))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        maxBreakLeavesDistance = COMMON.maxBreakLeavesDistance.get();

        updateTags(BlockTags.getCollection());
    }

    public static void updateTags(ITagCollection<Block> blockTags) {
        blockTagForDetectingLogs = blockTags.get(new ResourceLocation(COMMON.blockTagForDetectingLogs.get()));
        blockTagForDetectingLeaves = blockTags.get(new ResourceLocation(COMMON.blockTagForDetectingLeaves.get()));
    }

    public static class Common {

        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue canChooseNotToChop;

        public final ForgeConfigSpec.IntValue maxNumTreeBlocks;
        public final ForgeConfigSpec.IntValue maxNumLeavesBlocks;
        public final ForgeConfigSpec.BooleanValue breakLeaves;
        protected final ForgeConfigSpec.IntValue maxBreakLeavesDistance;
        protected final ForgeConfigSpec.ConfigValue<String> blockTagForDetectingLogs;
        protected final ForgeConfigSpec.ConfigValue<String> blockTagForDetectingLeaves;

        public final ForgeConfigSpec.EnumValue<ChopCountingAlgorithm> chopCountingAlgorithm;
        public final ForgeConfigSpec.DoubleValue chopCountScale;

        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> choppingToolsBlacklist;

        public Common(ForgeConfigSpec.Builder builder) {
            enabled = builder
                    .comment("Whether this mod is enabled or not")
                    .define("enabled", true);
            canChooseNotToChop = builder
                    .comment("Whether players can deactivate chopping e.g. by sneaking")
                    .define("canChooseNotToChop", true);

            maxNumTreeBlocks = builder
                    .comment("Maximum number of log blocks that can be detected to belong to one tree")
                    .defineInRange("maxTreeBlocks", 320, 1, 8096);
            maxNumLeavesBlocks = builder
                    .comment("Maximum number of leaves blocks that can destroyed when a tree is felled")
                    .defineInRange("maxLeavesBlocks", 1024, 1, 8096);
            breakLeaves = builder
                    .comment("Whether to destroy leaves when a tree is felled")
                    .define("breakLeaves", true);
            maxBreakLeavesDistance = builder
                    .comment("Maximum distance from log blocks to destroy non-standard leaves blocks (e.g. mushroom caps) when felling")
                    .defineInRange("maxBreakLeavesDistance", 7, 0, 16);
            chopCountingAlgorithm = builder
                    .comment("Method to use for computing the number of chops needed to fell a tree")
                    .defineEnum("chopCountingMethod", ChopCountingAlgorithm.LOGARITHMIC);
            chopCountScale = builder
                    .comment("Scales the number of chops (rounding down) required to fell a tree; with chopCountingMethod=LINEAR, this is exactly the number of chops per block")
                    .defineInRange("chopCountScale", 1.0, 0.0, 1024.0);
            blockTagForDetectingLogs = builder
                    .comment("The tag that blocks must have to be considered choppable (default: treechop:choppables)")
                    .define("blockTagForDetectingLogs", "treechop:choppables");
            blockTagForDetectingLeaves = builder
                    .comment("The tag that blocks must have to be considered leaves (default: treechop:leaves_like)")
                    .define("blockTagForDetectingLeaves", "treechop:leaves_like");
            // See https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/common/core/handler/ConfigHandler.java
            choppingToolsBlacklist = builder
                    .comment("List of item registry names (mod:item) and tags (#mod:tag) for items that should not chop when used to break a log")
                    .defineList(
                            "choppingToolsBlacklist",
                            Arrays.asList("#forge:saws", "mekanism:atomic_disassembler"),
                            always -> true
                    );
        }
    }

    private static boolean isRegistryNameOrTag(Object object) {
        if (object instanceof String) {
            String string = (String) object;
            return ((string.startsWith("#") && ResourceLocation.tryCreate(string.substring(1) + ":test") != null) ||
                    ResourceLocation.tryCreate(string + ":test") != null);
        } else {
            return false;
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
        public final ForgeConfigSpec.BooleanValue useProceduralChoppedModels;

//        public final ForgeConfigSpec.BooleanValue treesMustBeUniform; // TODO: a nice implementation requires chopped logs to be typed

        public Client(ForgeConfigSpec.Builder builder) {
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
                    .define("onlyBreakTreesWithLeaves", false);
            useProceduralChoppedModels = builder
                    .comment("Whether to use procedural chopped log models; disable to use models added by a resource pack")
                    .define("useProceduralChoppedModels", true);
//            treesMustBeUniform = builder
//                    .comment("Whether to disallow different types of log blocks from belonging to the same tree")
//                    .define("treesMustBeUniform", true);
        }

        public ChopSettings getChopSettings() {
            ChopSettings chopSettings = new ChopSettings();
            chopSettings.setChoppingEnabled(ConfigHandler.CLIENT.choppingEnabled.get());
            chopSettings.setFellingEnabled(ConfigHandler.CLIENT.fellingEnabled.get());
            chopSettings.setSneakBehavior(ConfigHandler.CLIENT.sneakBehavior.get());
            chopSettings.setTreesMustBeUniform(ConfigHandler.CLIENT.treesMustHaveLeaves.get());
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
