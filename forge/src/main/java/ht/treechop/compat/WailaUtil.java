package ht.treechop.compat;

import ht.treechop.common.block.ChoppedLogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class WailaUtil {
    @NotNull
    public static MutableComponent getPrefixedBlockName(ChoppedLogBlock.MyEntity choppedEntity) {
        String originalBlockName = Language.getInstance().getOrDefault(
                choppedEntity.getOriginalState().getBlock().getDescriptionId()
        );
        return Component.translatable("treechop.waila.chopped_x", originalBlockName);
    }

    public static BlockState getLogState(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            return entity.getOriginalState();
        } else {
            return state;
        }
    }
}
