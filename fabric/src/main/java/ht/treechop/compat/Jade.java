package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.registry.FabricModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ui.TextElement;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@WailaPlugin
public class Jade implements IWailaPlugin, IBlockComponentProvider {

    public static final ResourceLocation SHOW_TREE_BLOCKS = new ResourceLocation(TreeChop.MOD_ID, "show_tree_block_counts");
    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = new ResourceLocation(TreeChop.MOD_ID, "show_num_chops_remaining");
    private static final ResourceLocation UID = TreeChop.resource("plugin");

    @Override
    public void registerClient(IWailaClientRegistration registrar) {
        registrar.registerBlockComponent(this, Block.class);
        registrar.registerBlockIcon(this, ChoppedLogBlock.class);
        registrar.addConfig(SHOW_TREE_BLOCKS, true);
        registrar.addConfig(SHOW_NUM_CHOPS_REMAINING, true);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        changeBlockName(tooltip, accessor);

        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        LocalPlayer player = Minecraft.getInstance().player;
        ClientChopSettings chopSettings = Client.getChopSettings();
        boolean showNumBlocks = config.get(SHOW_TREE_BLOCKS);
        boolean showChopsRemaining = config.get(SHOW_NUM_CHOPS_REMAINING);

        if (WailaUtil.playerWantsTreeInfo(level, pos, player, chopSettings, showNumBlocks, showChopsRemaining)) {
            Optional<LinkedList<IElement>> tiles = Optional.empty();
            WailaUtil.addTreeInfo(
                    level,
                    pos,
                    showNumBlocks,
                    showChopsRemaining,
                    tooltip::add,
                    stack -> {
                        IElement icon = tooltip.getElementHelper().item(stack, 1f, Integer.toString(stack.getCount()));
                        tiles.orElseGet(LinkedList::new).add(icon.translate(new Vec2(0, -1.5f)));
                    }
            );
        }
    }

    private static void changeBlockName(ITooltip tooltip, BlockAccessor accessor) {
        final ResourceLocation OBJECT_NAME_COMPONENT_KEY = new ResourceLocation("jade", "object_name");
        try {
            if (accessor.getBlockEntity() instanceof ChoppedLogBlock.MyEntity choppedEntity) {
                if (tooltip.get(OBJECT_NAME_COMPONENT_KEY).get(0) instanceof TextElement textElement) {
                    if (textElement.text instanceof MutableComponent textComponent) {
                        String choppedLogName = FabricModBlocks.CHOPPED_LOG.getName().getString();
                        List<Component> siblings = textComponent.getSiblings();
                        for (int i = 0, n = siblings.size(); i < n; ++i) {
                            if (siblings.get(i).getString().matches(choppedLogName)) {
                                siblings.set(i, WailaUtil.getPrefixedBlockName(choppedEntity));
                                break;
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
