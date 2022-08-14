package ht.treechop.common.registry;

import ht.treechop.TreeChop;
import ht.treechop.common.block.ForgeChoppedLogBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgeModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TreeChop.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TreeChop.MOD_ID);

    // Blocks
    public static final RegistryObject<Block> CHOPPED_LOG = BLOCKS.register("chopped_log",
            () -> new ForgeChoppedLogBlock(
                    Block.Properties.of(
                            Material.WOOD,
                            MaterialColor.WOOD)
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
            )
    );

    // Block entities
    public static final RegistryObject<BlockEntityType<ForgeChoppedLogBlock.MyEntity>> CHOPPED_LOG_ENTITY = ENTITIES.register("chopped_log",
            () -> BlockEntityType.Builder.of(ForgeChoppedLogBlock.MyEntity::new, CHOPPED_LOG.get()).build(null)
    );
}
