package ht.treechop.server;

import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class Server {

    private static final ChopSettings defaultPlayerSettings = new ChopSettings();
    protected static Server instance;

    public static ChopSettings getDefaultPlayerSettings() {
        return defaultPlayerSettings;
    }

    public static void updateDefaultPlayerSettings() {
        Arrays.stream(SettingsField.values())
                .map(Server::getDefaultPlayerSetting)
                .forEach(defaultPlayerSettings::set);
    }

    private static Setting getDefaultPlayerSetting(SettingsField field) {
        Setting defaultSettingIgnoringPermissions = new Setting(field, field.getDefaultValue());
        Permissions permissions = getPermissions();
        if (permissions.isPermitted(defaultSettingIgnoringPermissions)) {
            return defaultSettingIgnoringPermissions;
        } else {
            Object bestValue = field.getValues().stream()
                    .filter(value -> permissions.isPermitted(new Setting(field, value)))
                    .findFirst()
                    .orElse(defaultSettingIgnoringPermissions);
            return new Setting(field, bestValue);
        }
    }

    public static Permissions getPermissions() {
        return new Permissions(ConfigHandler.getServerPermissions());
    }

    public abstract void broadcast(ServerLevel level, BlockPos pos, CustomPacket packet);

    public abstract void sendTo(ServerPlayer player, CustomPacket packet);

    public static Server instance() {
        return instance;
    }
}
