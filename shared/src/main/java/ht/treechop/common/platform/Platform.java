package ht.treechop.common.platform;

import ht.treechop.api.ChopData;
import ht.treechop.api.TreeData;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.SettingsField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface Platform {

    void onDestroyItem(Player agent, ItemStack mockItemStack, InteractionHand mainHand);

    boolean onStartBlockBreak(Player player, ItemStack tool, BlockPos blockPos);

    Optional<ChopSettings> getPlayerChopSettings(Player player);

    TreeData detectTreeEvent(Level level, ServerPlayer agent, BlockPos blockPos, BlockState blockState, boolean overrideLeaves);

    boolean startChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData);

    boolean finishChopEvent(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ChopData chopData);

    Block getChoppedLogBlock();

    BlockEntityType<?> getChoppedLogBlockEntity();

    void sendClientSettingsRequest(SettingsField field, Object value);
}
