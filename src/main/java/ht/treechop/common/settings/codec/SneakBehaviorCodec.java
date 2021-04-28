package ht.treechop.common.settings.codec;

import ht.treechop.common.settings.SneakBehavior;

public class SneakBehaviorCodec extends EnumCodec<SneakBehavior> {

    public SneakBehaviorCodec() {
        super(SneakBehavior.class);
    }

    @Override
    protected String localizeSafe(SneakBehavior sneakBehavior) {
        return sneakBehavior.getFancyText();
    }

}
