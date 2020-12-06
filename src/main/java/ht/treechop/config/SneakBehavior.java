package ht.treechop.config;

import net.minecraft.util.IStringSerializable;

public enum SneakBehavior implements IStringSerializable {
    NONE("none"),
    ENABLES("enables"),
    DISABLES("disables")
    ;

    private final String name;

    SneakBehavior(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    @Override
    public String getString() {
        return name;
    }
}
