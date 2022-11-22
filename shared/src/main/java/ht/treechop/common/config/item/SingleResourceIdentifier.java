package ht.treechop.common.config.item;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.stream.Stream;

public class SingleResourceIdentifier extends ResourceIdentifier {

    public SingleResourceIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public <R extends DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
        String resourceString = getNamespace() + ":" + getLocalSpace();
        ResourceLocation itemId = ResourceLocation.tryParse(resourceString);
        if (itemId != null) {
            if (registry.containsKey(itemId)) {
                T item = registry.get(itemId); // Returns minecraft:air if itemId is not registered
                if (item != Items.AIR) {
                    return Stream.of(item);
                }
            }
        } else {
            parsingError(String.format("\"%s\" is not a valid resource location", getItemID()));
        }

        return Stream.empty();
    }

}
