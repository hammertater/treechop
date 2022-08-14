package ht.treechop.server;

import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;

import java.util.Arrays;

public class Server {

    private static final Permissions permissions = new Permissions();
    private static final ChopSettings defaultPlayerSettings = new ChopSettings();

    public static Permissions getPermissions() {
        return permissions;
    }

    public static ChopSettings getDefaultPlayerSettings() {
        return defaultPlayerSettings;
    }

    public static void updatePermissions(Permissions permissions) {
        Server.permissions.copy(permissions);
        updateDefaultPlayerSettings();
    }

    private static void updateDefaultPlayerSettings() {
        Arrays.stream(SettingsField.values())
                .map(Server::getDefaultPlayerSetting)
                .forEach(defaultPlayerSettings::set);
    }

    private static Setting getDefaultPlayerSetting(SettingsField field) {
        Setting defaultSettingIgnoringPermissions = new Setting(field, field.getDefaultValue());
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

}
