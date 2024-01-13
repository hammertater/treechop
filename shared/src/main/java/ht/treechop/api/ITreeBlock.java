package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ITreeBlock extends ITreeChopBlockBehavior {
    TreeData getTree(Level level, BlockPos origin);
}
