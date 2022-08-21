package ht.treechop.common.capabilities;

import ht.treechop.TreeChop;
import ht.treechop.common.settings.EntityChopSettings;
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

public class ChopSettingsCapability extends EntityChopSettings implements INBTSerializable<CompoundTag> {

    public static final Capability<ChopSettingsCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    public ChopSettingsCapability() {
    }

    public static LazyOptional<ChopSettingsCapability> forPlayer(Player player) {
        LazyOptional<ChopSettingsCapability> lazyCapability = player.getCapability(CAPABILITY);
        if (!lazyCapability.isPresent() && !(player instanceof FakePlayer)) {
            TreeChop.LOGGER.warn("Player " + player + " is missing chop settings");
        }

        return player.getCapability(CAPABILITY);
    }

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
                TreeChop.LOGGER.warn(String.format("NBT contains bad sneak behavior value \"%s\"; using default value instead", tag.getString(SNEAK_BEHAVIOR_KEY)));
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
            TreeChop.LOGGER.warn("Failed to read ChopSettingsCapability NBT");
        }
    }

}
