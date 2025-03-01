package ht.treechop.mixin;

import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.ChoppingEntity;
import ht.treechop.common.settings.SyncedChopData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityChopSettingsMixin implements ChoppingEntity {
    private SyncedChopData chopSettings;
    private final String KEY = "treechop:chopSettings";

    @Override
    public SyncedChopData getChopData() {
        return chopSettings;
    }

    @Override
    public SyncedChopData setChopData(SyncedChopData chopSettings) {
        this.chopSettings = chopSettings;
        return chopSettings;
    }

    @Inject(method = "saveWithoutId", at = @At("HEAD"), remap = false)
    public void injectDataSaving(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info) {
        if (chopSettings != null) {
            tag.put(KEY, chopSettings.makeSaveData());
        }
    }

    @Inject(method = "load", at = @At("HEAD"), remap = false)
    public void injectDataLoading(CompoundTag tag, CallbackInfo info) {
        CompoundTag data = tag.getCompound(KEY);
        chopSettings = (new SyncedChopData(new ChopSettings())).readSaveData(data);
    }
}
