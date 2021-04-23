package ht.treechop.common.config.item;

import net.minecraft.item.Item;
import net.minecraft.tags.ITagCollection;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class ItemNamespaceIdentifier extends ItemIdentifier {

    public ItemNamespaceIdentifier(String namespace, String parameters, String string) {
        super(namespace, "", parameters, string);
    }

    @Override
    public List<Item> resolve(ITagCollection<Item> tags, IForgeRegistry<Item> registry) {
        List<Item> items = registry.getValues().stream()
                .filter(item -> item.getRegistryName() != null && item.getRegistryName().getNamespace().equals(getNamespace()))
                .collect(Collectors.toList());
        if (items.isEmpty()) {
            parsingError(String.format("no items found in namespace \"%s\"", getNamespace()));
        }
        return items;
    }

}
