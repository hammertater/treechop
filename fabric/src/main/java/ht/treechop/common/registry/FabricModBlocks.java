package ht.treechop.common.registry;

import ht.treechop.TreeChop;
import ht.treechop.common.block.FabricChoppedLogBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class FabricModBlocks {
    public static final TagKey<Block> CHOPPABLES = TagKey.create(Registries.BLOCK, TreeChop.resource("choppables"));

    public static final Block CHOPPED_LOG = new FabricChoppedLogBlock(
            FabricBlockSettings.of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
    );

    public static final BlockEntityType<FabricChoppedLogBlock.MyEntity> CHOPPED_LOG_ENTITY = FabricBlockEntityTypeBuilder.create(FabricChoppedLogBlock.MyEntity::new, CHOPPED_LOG).build();
}
