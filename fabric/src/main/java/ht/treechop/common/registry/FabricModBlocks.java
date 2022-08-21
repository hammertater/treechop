package ht.treechop.common.registry;

import ht.treechop.TreeChop;
import ht.treechop.TreeChopFabric;
import ht.treechop.common.block.FabricChoppedLogBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class FabricModBlocks {

    public static final Block CHOPPED_LOG = Registry.register(
            Registry.BLOCK,
            TreeChop.resource("chopped_log"),
            new FabricChoppedLogBlock(
            FabricBlockSettings.of(
                    Material.WOOD,
                    MaterialColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
            )
    );

    public static final BlockEntityType<FabricChoppedLogBlock.MyEntity> CHOPPED_LOG_ENTITY = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            new ResourceLocation(TreeChop.MOD_ID, "chopped_log_entity"),
            FabricBlockEntityTypeBuilder.create(FabricChoppedLogBlock.MyEntity::new, CHOPPED_LOG).build()
    );

}
