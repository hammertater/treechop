package ht.treechop.common.capabilities;

import ht.treechop.TreeChopMod;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.SneakBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

public class ChopSettingsCapability extends ChopSettings implements INBTSerializable<CompoundTag> {

    public static final Capability<ChopSettingsCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    private boolean isSynced = false;

    public ChopSettingsCapability() {
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced() {
        this.isSynced = true;
    }

    public static LazyOptional<ChopSettingsCapability> forPlayer(Player player) {
        LazyOptional<ChopSettingsCapability> lazyCapability = player.getCapability(CAPABILITY);
        if (!lazyCapability.isPresent() && !(player instanceof FakePlayer)) {
            TreeChopMod.LOGGER.warn("Player " + player + " is missing chop settings");
        }

        return player.getCapability(CAPABILITY);
    }

    private static final String CHOPPING_ENABLED_KEY = "choppingEnabled";
    private static final String FELLING_ENABLED_KEY = "fellingEnabled";
    private static final String SNEAK_BEHAVIOR_KEY = "sneakBehavior";
    private static final String TREES_MUST_HAVE_LEAVES_KEY = "treesMustHaveLeaves";
    private static final String CHOP_IN_CREATIVE_MODE_KEY = "chopInCreativeMode";
    private static final String IS_SYNCED_KEY = "isSynced";

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean(CHOPPING_ENABLED_KEY, getChoppingEnabled());
        nbt.putBoolean(FELLING_ENABLED_KEY, getFellingEnabled());
        nbt.putString(SNEAK_BEHAVIOR_KEY, getSneakBehavior().name());
        nbt.putBoolean(TREES_MUST_HAVE_LEAVES_KEY, getTreesMustHaveLeaves());
        nbt.putBoolean(CHOP_IN_CREATIVE_MODE_KEY, getChopInCreativeMode());
        nbt.putBoolean(IS_SYNCED_KEY, isSynced());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag != null) {
            Optional<Boolean> choppingEnabled = getBoolean(tag, CHOPPING_ENABLED_KEY);
            Optional<Boolean> fellingEnabled = getBoolean(tag, FELLING_ENABLED_KEY);
            SneakBehavior sneakBehavior;
            try {
                sneakBehavior = SneakBehavior.valueOf(tag.getString(SNEAK_BEHAVIOR_KEY));
            } catch (IllegalArgumentException e) {
                TreeChopMod.LOGGER.warn(String.format("NBT contains bad sneak behavior value \"%s\"; using default value instead", tag.getString(SNEAK_BEHAVIOR_KEY)));
                sneakBehavior = SneakBehavior.INVERT_CHOPPING;
            }
            Optional<Boolean> onlyChopTreesWithLeaves = getBoolean(tag, TREES_MUST_HAVE_LEAVES_KEY);
            Optional<Boolean> chopInCreativeMode = getBoolean(tag, CHOP_IN_CREATIVE_MODE_KEY);
            Optional<Boolean> isSynced = getBoolean(tag, IS_SYNCED_KEY);

            setChoppingEnabled(choppingEnabled.orElse(getChoppingEnabled()));
            setFellingEnabled(fellingEnabled.orElse(getFellingEnabled()));
            setSneakBehavior(sneakBehavior);
            setTreesMustHaveLeaves(onlyChopTreesWithLeaves.orElse(getTreesMustHaveLeaves()));
            setChopInCreativeMode(chopInCreativeMode.orElse(getChopInCreativeMode()));

            if (isSynced.orElse(false)) {
                setSynced();
            }
        } else {
            TreeChopMod.LOGGER.warn("Failed to read ChopSettingsCapability NBT");
        }
    }

    private Optional<Boolean> getBoolean(CompoundTag CompoundTag, String key) {
        return (CompoundTag.contains(key))
                ? Optional.of(CompoundTag.getBoolean(key))
                : Optional.empty();
    }

}
