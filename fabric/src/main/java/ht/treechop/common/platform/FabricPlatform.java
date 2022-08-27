package ht.treechop.common.platform;

import ht.treechop.api.ChopData;
import ht.treechop.api.TreeChopEvents;
import ht.treechop.api.TreeData;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.registry.FabricModBlocks;
import ht.treechop.common.settings.EntityChopSettings;
import ht.treechop.common.settings.SettingsField;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class FabricPlatform implements Platform {

    @Override
    public boolean onStartBlockBreak(Player player, ItemStack tool, BlockPos blockPos) {
        Level level = player.level;
        return PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, blockPos, level.getBlockState(blockPos), level.getBlockEntity(blockPos));
    }

    @Override
    public Optional<EntityChopSettings> getPlayerChopSettings(Player player) {
        return Optional.empty();
    }

    @Override
    public TreeData detectTreeEvent(Level level, ServerPlayer player, BlockPos blockPos, BlockState blockState, boolean overrideLeaves) {
        TreeData treeData = new TreeData(overrideLeaves);
        boolean canceled = !TreeChopEvents.DETECT_TREE.invoker().onDetectTree(level, player, blockPos, blockState, overrideLeaves);
        if (canceled) {
            return TreeData.empty();
        }
        return treeData;
    }

    // Returns true if chopping should continue
    @Override
    public boolean startChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData) {
        return !TreeChopEvents.BEFORE_CHOP.invoker().beforeChop(
                level,
                agent,
                pos,
                blockState,
                chopData
        );
    }

    @Override
    public void finishChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData) {
        TreeChopEvents.BEFORE_CHOP.invoker().beforeChop(
                level,
                agent,
                pos,
                blockState,
                chopData
        );
    }

    @Override
    public Block getChoppedLogBlock() {
        return FabricModBlocks.CHOPPED_LOG;
    }

    @Override
    public BlockEntityType<?> getChoppedLogBlockEntity() {
        return FabricModBlocks.CHOPPED_LOG_ENTITY;
    }

    @Override
    public boolean doItemDamage(ItemStack tool, Level level, BlockState blockState, BlockPos pos, Player player) {
        AtomicBoolean broke = new AtomicBoolean(false);
        tool.hurtAndBreak(1, player, (Player thePlayer) -> broke.set(true));
        return broke.get();
    }
}
