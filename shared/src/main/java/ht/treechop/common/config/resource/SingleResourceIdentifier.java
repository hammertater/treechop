package ht.treechop.common.config.resource;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Stream;

public class SingleResourceIdentifier extends ResourceIdentifier {

    public SingleResourceIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public <R extends DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
        String resourceString = getNamespace() + ":" + getLocalSpace();
        ResourceLocation key = ResourceLocation.tryParse(resourceString);
        if (key != null) {
            if (registry.containsKey(key)) {
                T resource = registry.get(key);
                ResourceLocation defaultKey = registry.getDefaultKey();
                if (!registry.getKey(resource).equals(defaultKey) || key.equals(defaultKey)) {
                    return Stream.of(resource);
                }
            }
        } else {
            parsingError(String.format("\"%s\" is not a valid resource location", getResourceLocation()));
        }

        return Stream.empty();
    }

}
