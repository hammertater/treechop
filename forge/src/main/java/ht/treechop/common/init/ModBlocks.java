package ht.treechop.common.init;

import ht.treechop.TreeChopMod;
import ht.treechop.common.block.ChoppedLogBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TreeChopMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TreeChopMod.MOD_ID);

    // Blocks
    public static final RegistryObject<Block> CHOPPED_LOG = BLOCKS.register("chopped_log",
            () -> new ChoppedLogBlock(
                    Block.Properties.of(
                            Material.WOOD,
                            MaterialColor.WOOD)
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
            )
    );

    // Block entities
    public static final RegistryObject<BlockEntityType<ChoppedLogBlock.Entity>> CHOPPED_LOG_ENTITY = ENTITIES.register("chopped_log",
            () -> BlockEntityType.Builder.of(ChoppedLogBlock.Entity::new, ModBlocks.CHOPPED_LOG.get()).build(null)
    );
}
