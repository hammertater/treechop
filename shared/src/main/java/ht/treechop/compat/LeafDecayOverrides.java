package ht.treechop.compat;

import ht.treechop.api.ILeaveslikeBlock;
import ht.treechop.api.ITreeChopBlockBehavior;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.Lazy;
import ht.treechop.common.util.LevelUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LeafDecayOverrides {

    private static final Lazy<Set<Block>> nondecayableLeaves = new Lazy<>(
            ConfigHandler.RELOAD,
            () -> ConfigHandler.getIdentifiedBlocks(MyConfigHandler.instance.nondecayableLeavesIds.get()).collect(Collectors.toSet())
    );

    public static void register(TreeChopAPI api) {
        ITreeChopBlockBehavior handler = (ILeaveslikeBlock) (player, level, pos, blockState) ->
                LevelUtil.harvestBlock(player, level, pos,  ItemStack.EMPTY, false);

        nondecayableLeaves.get().forEach(block -> {
            api.overrideLeavesBlock(block, true);
            api.registerBlockBehavior(block, handler);
        });
    }

    public static class MyConfigHandler {
        private static MyConfigHandler instance;
        protected final ForgeConfigSpec.ConfigValue<List<? extends String>> nondecayableLeavesIds;

        public MyConfigHandler(ForgeConfigSpec.Builder builder) {
            nondecayableLeavesIds = builder
                    .comment(String.join("\n",
                            "Leaves in this list will break instead of decaying. This gives players credit " +
                                    "for breaking leaves, which is potentially useful for advanced loot tables and " +
                                    "functionalities added by other mods.",
                            "Specify using registry names (mod:block), tags (#mod:tag), and namespaces (@mod)"))
                    .defineListAllowEmpty("leafDecayExceptions",
                            List.of("#spectrum:colored_leaves"),
                            always -> true);
        }

        public static void init(ForgeConfigSpec.Builder builder) {
            instance = new MyConfigHandler(builder);
        }
    }
}
