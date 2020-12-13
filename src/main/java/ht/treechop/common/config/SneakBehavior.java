package ht.treechop.common.config;

import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;

import java.util.Arrays;
import java.util.function.Predicate;

public enum SneakBehavior implements IStringSerializable {
    NONE("NONE", agent -> false, agent -> false),
    INVERT_CHOPPING("INVERT CHOPPING", Entity::isSneaking, agent -> false),
    INVERT_FELLING("INVERT FELLING", agent -> false, Entity::isSneaking)
    ;

    public final static int maxNameLength = Arrays.stream(SneakBehavior.values()).map(SneakBehavior::name).map(String::length).max(Integer::compareTo).orElse(0);

    private final String name;
    private final Predicate<Entity> chopBehavior;
    private final Predicate<Entity> fellBehavior;

    SneakBehavior(String name, Predicate<Entity> chopBehavior, Predicate<Entity> fellBehavior) {
        this.name = name;
        this.chopBehavior = chopBehavior;
        this.fellBehavior = fellBehavior;
    }

    public String toString() {
        return name;
    }

    @SuppressWarnings("NullableProblems")
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
