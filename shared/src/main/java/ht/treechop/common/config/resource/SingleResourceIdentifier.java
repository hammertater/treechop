package ht.treechop.common.config.resource;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SingleResourceIdentifier extends ResourceIdentifier {

    public SingleResourceIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public <R extends DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
        String resourceString = getNamespace() + ":" + getLocalSpace();
        ResourceLocation key = ResourceLocation.tryParse(resourceString);
        ResourceLocation defaultKey = registry.getDefaultKey();

        if (key != null && !key.equals(defaultKey)) {
            if (registry.containsKey(key)) {
                Optional<Holder.Reference<T>> resource = registry.get(key);
                return resource.map(Holder.Reference::value).stream();
            }
        } else {
            parsingError(String.format("\"%s\" is not a valid resource location", getResourceLocation()));
        }

        return Stream.empty();
    }

}
