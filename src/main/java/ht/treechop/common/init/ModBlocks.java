package ht.treechop.common.init;

import ht.treechop.TreeChopMod;
import ht.treechop.common.block.ChoppedLogBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TreeChopMod.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, TreeChopMod.MOD_ID);

    // Blocks
    public static final RegistryObject<Block> CHOPPED_LOG = BLOCKS.register("chopped_log",
            () -> new ChoppedLogBlock(
                    AbstractBlock.Properties.of(
                            Material.WOOD,
                            MaterialColor.WOOD)
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
            )
    );

    // Tile entities
    public static final RegistryObject<TileEntityType<ChoppedLogBlock.Entity>> CHOPPED_LOG_ENTITY = ENTITIES.register("chopped_log",
            () -> TileEntityType.Builder.of(ChoppedLogBlock.Entity::new, ModBlocks.CHOPPED_LOG.get()).build(null)
    );

}
