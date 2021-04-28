package ht.treechop.common.settings.codec;

import ht.treechop.common.settings.SneakBehavior;

public class SneakBehaviorCodec extends EnumCodec<SneakBehavior> {

    public SneakBehaviorCodec(Class<SneakBehavior> enumType) {
        super(enumType);
    }

    @Override
    public String localizeSafe(SneakBehavior sneakBehavior) {
        return sneakBehavior.getFancyText();
    }

}
