package ht.treechop.common.config.item;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.stream.Stream;

public class ResourceNamespaceIdentifier extends ResourceIdentifier {

    public ResourceNamespaceIdentifier(String namespace, List<IdentifierQualifier> qualifiers, String string) {
        super(namespace, "", qualifiers, string);
    }

    @Override
    public <R extends DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
        return registry.stream()
                .filter(item -> {
                    ResourceLocation loc = registry.getKey(item);
                    return loc != registry.getDefaultKey() && loc.getNamespace().equals(getNamespace());
                });
    }

}
