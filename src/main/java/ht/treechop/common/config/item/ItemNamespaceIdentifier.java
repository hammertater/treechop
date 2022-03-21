package ht.treechop.common.config.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.stream.Stream;

public class ItemNamespaceIdentifier extends ItemIdentifier {

    public ItemNamespaceIdentifier(String namespace, List<IdentifierQualifier> qualifiers, String string) {
        super(namespace, "", qualifiers, string);
    }

    @Override
    public Stream<Item> resolve(IForgeRegistry<Item> registry) {
        return registry.getValues().stream()
                .filter(item -> item.getRegistryName() != null && item.getRegistryName().getNamespace().equals(getNamespace()));
    }

}
