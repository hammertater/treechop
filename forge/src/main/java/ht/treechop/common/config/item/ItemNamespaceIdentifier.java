package ht.treechop.common.config.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
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
                .filter(item -> {
                    ResourceLocation loc = ForgeRegistries.ITEMS.getKey(item);
                    return loc != null && loc.getNamespace().equals(getNamespace());
                });
    }

}
