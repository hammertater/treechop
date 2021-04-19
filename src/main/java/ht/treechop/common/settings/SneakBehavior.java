package ht.treechop.common.settings;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;

import java.util.Arrays;
import java.util.function.Predicate;

public enum SneakBehavior implements IStringSerializable {
    NONE("treechop.sneak_behavior.none", agent -> false, agent -> false),
    INVERT_CHOPPING("treechop.sneak_behavior.invert_chopping", Entity::isSneaking, agent -> false),
    INVERT_FELLING("treechop.sneak_behavior.invert_felling", agent -> false, Entity::isSneaking)
    ;

    public final static int maxNameLength = Arrays.stream(SneakBehavior.values()).map(SneakBehavior::name).map(String::length).max(Integer::compareTo).orElse(0);

    private final Predicate<Entity> chopBehavior;
    private final Predicate<Entity> fellBehavior;
    private final String langKey;

    SneakBehavior(String langKey, Predicate<Entity> chopBehavior, Predicate<Entity> fellBehavior) {
        this.chopBehavior = chopBehavior;
        this.fellBehavior = fellBehavior;
        this.langKey = langKey;
    }

    public SneakBehavior cycle() {
        return SneakBehavior.values()[Math.floorMod(ordinal() + 1, SneakBehavior.values().length)];
    }

    public String toString() {
        return name();
    }

    @Override
    public String getString() {
        return name();
    }

    public boolean shouldChangeChopBehavior(Entity agent) {
        return chopBehavior.test(agent);
    }

    public boolean shouldChangeFellBehavior(Entity agent) {
        return fellBehavior.test(agent);
    }

    public String getFancyText() {
        return I18n.format(langKey);
    }

}
