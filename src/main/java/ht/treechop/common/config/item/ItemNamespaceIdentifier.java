package ht.treechop.common.config.item;

import net.minecraft.world.item.Item;
import net.minecraft.tags.TagCollection;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class ItemNamespaceIdentifier extends ItemIdentifier {

    public ItemNamespaceIdentifier(String namespace, List<IdentifierQualifier> qualifiers, String string) {
        super(namespace, "", qualifiers, string);
    }

    @Override
    public List<Item> resolve(TagCollection<Item> tags, IForgeRegistry<Item> registry) {
        return registry.getValues().stream()
                .filter(item -> item.getRegistryName() != null && item.getRegistryName().getNamespace().equals(getNamespace()))
                .collect(Collectors.toList());
    }

}
