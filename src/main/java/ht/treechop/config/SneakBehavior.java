package ht.treechop.config;

import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;

import java.util.function.Predicate;

public enum SneakBehavior implements IStringSerializable {
    NONE("none", agent -> false),
    INVERTS("inverts", Entity::isSneaking)
    ;

    private final String name;
    private Predicate<Entity> chopBehavior;

    SneakBehavior(String name, Predicate<Entity> chopBehavior) {
        this.name = name;
        this.chopBehavior = chopBehavior;
    }

    public String toString() {
        return name;
    }

    @Override
    public String getString() {
        return name;
    }

    public boolean shouldChangeChopBehavior(Entity agent) {
        return chopBehavior.test(agent);
    }
}
