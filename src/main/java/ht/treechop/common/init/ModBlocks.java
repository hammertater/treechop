package ht.treechop.common.init;

import ht.treechop.TreeChopMod;
import ht.treechop.common.block.ChoppedLogBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TreeChopMod.MOD_ID);

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

}
