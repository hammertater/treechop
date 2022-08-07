package ht.treechop.common.compat;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ui.ItemStackElement;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@WailaPlugin //(TreeChopMod.MOD_ID + ":waila_plugin")
public class Jade implements IWailaPlugin, IBlockComponentProvider {

    private static final ResourceLocation UID = ResourceLocation.tryBuild(TreeChop.MOD_ID, "plugin");

    public static final ResourceLocation SHOW_TREE_BLOCKS = new ResourceLocation(TreeChop.MOD_ID, "show_tree_block_counts");
    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = new ResourceLocation(TreeChop.MOD_ID, "show_num_chops_remaining");

    @Override
    public void registerClient(IWailaClientRegistration registrar) {
        registrar.registerBlockComponent(this, ChoppedLogBlock.class);
        registrar.registerBlockIcon(this, ChoppedLogBlock.class);
        registrar.addConfig(SHOW_TREE_BLOCKS, true);
        registrar.addConfig(SHOW_NUM_CHOPS_REMAINING, true);
    }

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        BlockState state = getLogState(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState());
        return ItemStackElement.of(
                (state != accessor.getBlockState())
                        ? state.getBlock().asItem().getDefaultInstance()
                        : Items.OAK_LOG.getDefaultInstance()
        );
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (ChopUtil.playerWantsToChop(accessor.getPlayer(), Client.getChopSettings())
                && ChopUtil.isBlockChoppable(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState())
                && (config.get(SHOW_TREE_BLOCKS) || config.get(SHOW_NUM_CHOPS_REMAINING))) {
            Level level = accessor.getLevel();
            AtomicInteger numChops = new AtomicInteger(0);

            Set<BlockPos> treeBlocks = ChopUtil.getTreeBlocks(level, accessor.getPosition(), Client.getChopSettings().getTreesMustHaveLeaves());

            if (config.get(SHOW_NUM_CHOPS_REMAINING)) {
                treeBlocks.forEach((BlockPos pos) -> numChops.getAndAdd(ChopUtil.getNumChops(level, pos)));
                tooltip.add(Component.translatable("treechop.waila.x_out_of_y_chops", numChops.get(), ChopUtil.numChopsToFell(treeBlocks.size())));
            }

            if (config.get(SHOW_TREE_BLOCKS)) {
                LinkedList<IElement> tiles = new LinkedList<>();
                treeBlocks.stream()
                        .collect(Collectors.groupingBy((BlockPos pos) -> {
                            BlockState state = level.getBlockState(pos);
                            return getLogState(level, pos, state).getBlock();
                        }, Collectors.counting()))
                        .forEach((block, count) -> {
                            IElement icon = tooltip.getElementHelper().item(block.asItem().getDefaultInstance(), 1f, count.toString());
                            tiles.add(icon.translate(new Vec2(0, -1.5f)));
                        });
                tooltip.add(tiles);
            }
        }
    }

    private BlockState getLogState(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.Entity entity) {
            return entity.getOriginalState();
        } else {
            return state;
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
