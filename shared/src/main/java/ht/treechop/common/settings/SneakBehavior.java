package ht.treechop.common.settings;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;
import java.util.function.Predicate;

public enum SneakBehavior implements StringRepresentable {
    NONE("treechop.sneak_behavior.none", agent -> false, agent -> false),
    INVERT_CHOPPING("treechop.sneak_behavior.invert_chopping", Entity::isShiftKeyDown, agent -> false)
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
    public String getSerializedName() {
        return name();
    }

    public boolean shouldChangeChopBehavior(Entity agent) {
        return chopBehavior.test(agent);
    }

    public boolean shouldChangeFellBehavior(Entity agent) {
        return fellBehavior.test(agent);
    }

    public String getFancyText() {
        return I18n.get(langKey);
    }

}
