package ht.treechop.common.config.item;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.stream.Stream;

public class ItemNamespaceIdentifier extends ItemIdentifier {

    public ItemNamespaceIdentifier(String namespace, List<IdentifierQualifier> qualifiers, String string) {
        super(namespace, "", qualifiers, string);
    }

    @Override
    public Stream<Item> resolve() {
        return Registry.ITEM.stream()
                .filter(item -> {
                    ResourceLocation loc = Registry.ITEM.getKey(item);
                    return loc != Registry.ITEM.getDefaultKey() && loc.getNamespace().equals(getNamespace());
                });
    }

}
