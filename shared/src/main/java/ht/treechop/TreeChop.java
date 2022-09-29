package ht.treechop;

import com.mojang.logging.LogUtils;
import ht.treechop.api.ChopData;
import ht.treechop.api.TreeData;
import ht.treechop.common.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public abstract class TreeChop {
    public static final String MOD_ID = "treechop";
    public static final String MOD_NAME = "HT's TreeChop";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static Platform platform;

    @SuppressWarnings("ConstantConditions")
    public static void showText(String text) {
        Minecraft.getInstance().player.displayClientMessage(new TextComponent(String.format("%s[%s] %s%s", ChatFormatting.GRAY, TreeChop.MOD_NAME, ChatFormatting.WHITE, text)), false);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(TreeChop.MOD_ID, path);
    }
}
