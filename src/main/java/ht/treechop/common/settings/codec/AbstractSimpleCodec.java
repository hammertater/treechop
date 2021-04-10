package ht.treechop.common.settings.codec;

import java.util.Optional;

public abstract class AbstractSimpleCodec<T> implements SimpleCodec<T> {

    @Override
    public final String getLocalizationString(Object object) {
        Optional<T> value = getValueOf(object);
        return (value.isPresent()) ? localizeSafe(value.get()) : "treechop.codec.bad_value";
    }

    protected String localizeSafe(T value) {
        return value.toString();
    }

    protected abstract Class<T> getTypeClass();

    public Optional<T> getValueOf(Object object) {
        if (getTypeClass().isInstance(object)) {
            return Optional.of(getTypeClass().cast(object));
        } else {
            return Optional.empty();
        }
    }

}
