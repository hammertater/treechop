package ht.treechop.common.registry;

import ht.treechop.TreeChop;
import ht.treechop.TreeChopFabric;
import ht.treechop.common.block.FabricChoppedLogBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class FabricModBlocks {
    public static final TagKey<Block> CHOPPABLES = TagKey.create(Registry.BLOCK_REGISTRY, TreeChop.resource("choppables"));

    public static final Block CHOPPED_LOG = new FabricChoppedLogBlock(
            FabricBlockSettings.of(
                    Material.WOOD,
                    MaterialColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
    );

    public static final BlockEntityType<FabricChoppedLogBlock.MyEntity> CHOPPED_LOG_ENTITY = FabricBlockEntityTypeBuilder.create(FabricChoppedLogBlock.MyEntity::new, CHOPPED_LOG).build();

}
