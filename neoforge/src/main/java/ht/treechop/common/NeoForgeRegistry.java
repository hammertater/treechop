package ht.treechop.common;

import ht.treechop.TreeChop;
import ht.treechop.common.block.NeoForgeChoppedLogBlock;
import ht.treechop.common.loot.CountBlockChopsLootItemCondition;
import ht.treechop.common.loot.TreeFelledLootItemCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeRegistry {

    public static class Blocks {
        public static final DeferredRegister<Block> REGISTRY = DeferredRegister.createBlocks(TreeChop.MOD_ID);
        public static final DeferredHolder<Block, NeoForgeChoppedLogBlock> CHOPPED_LOG = REGISTRY.register("chopped_log",
                () -> new NeoForgeChoppedLogBlock(
                        Block.Properties.of()
                                .mapColor(blockState -> MapColor.WOOD)
                                .instrument(NoteBlockInstrument.BASS)
                                .strength(2.0F)
                                .sound(SoundType.WOOD)
                                .ignitedByLava()
                )
        );
    }

    public static class BlockEntities {
        public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TreeChop.MOD_ID);
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NeoForgeChoppedLogBlock.MyEntity>> CHOPPED_LOG_ENTITY = REGISTRY.register("chopped_log",
                () -> BlockEntityType.Builder.of(NeoForgeChoppedLogBlock.MyEntity::new, Blocks.CHOPPED_LOG.get()).build(null)
        );
    }

    public static class LootConditionTypes {
        public static final DeferredRegister<LootItemConditionType> REGISTRY =
                DeferredRegister.create(net.minecraft.core.registries.Registries.LOOT_CONDITION_TYPE, TreeChop.MOD_ID);

        public static final Supplier<LootItemConditionType> COUNT_BLOCK_CHOPS =
                REGISTRY.register(CountBlockChopsLootItemCondition.ID.getPath(), () -> CountBlockChopsLootItemCondition.TYPE);

        public static final Supplier<LootItemConditionType> TREE_FELLED =
                REGISTRY.register(TreeFelledLootItemCondition.ID.getPath(), () -> TreeFelledLootItemCondition.TYPE);
    }
}
