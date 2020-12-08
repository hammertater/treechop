package ht.treechop.config;

import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;

import java.util.function.Predicate;

public enum SneakBehavior implements IStringSerializable {
    NONE("none", agent -> false, agent -> false),
    INVERT_CHOPPING("invert_chopping", Entity::isSneaking, agent -> false),
    INVERT_FELLING("invert_felling", agent -> false, Entity::isSneaking)
    ;

    private final String name;
    private Predicate<Entity> chopBehavior;
    private Predicate<Entity> fellBehavior;

    SneakBehavior(String name, Predicate<Entity> chopBehavior, Predicate<Entity> fellBehavior) {
        this.name = name;
        this.chopBehavior = chopBehavior;
        this.fellBehavior = fellBehavior;
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

    public boolean shouldChangeFellBehavior(Entity agent) {
        return fellBehavior.test(agent);
    }
}
