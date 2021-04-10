package ht.treechop.common.settings;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Permissions {

    private Set<Setting> permittedSettings = new HashSet<>();

    public Permissions() {

    }

    public void permit(Setting setting) {
        permittedSettings.add(setting);
    }

    public void forbid(Setting setting) {
        permittedSettings.remove(setting);
    }

    public void set(Setting setting, boolean permitted) {
        if (permitted) {
            permit(setting);
        } else {
            forbid(setting);
        }
    }

    public boolean isPermitted(Setting setting) {
        return permittedSettings.contains(setting);
    }

    public Set<Object> getPermittedValues(SettingsField field) {
        return field.getValues().stream()
                .filter(value -> permittedSettings.contains(new Setting(field, value)))
                .collect(Collectors.toSet());
    }

}
