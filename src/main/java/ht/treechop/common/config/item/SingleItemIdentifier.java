package ht.treechop.common.config.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.stream.Stream;

public class SingleItemIdentifier extends ItemIdentifier {

    public SingleItemIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public Stream<Item> resolve(TagCollection<Item> tags, IForgeRegistry<Item> registry) {
        String resourceString = getNamespace() + ":" + getLocalSpace();
        ResourceLocation itemId = ResourceLocation.tryParse(resourceString);
        if (itemId != null) {
            if (registry.containsKey(itemId)) {
                Item item = registry.getValue(itemId); // Returns minecraft:air if itemId is not registered
                if (item != null) {
                    return Stream.of(item);
                }
            }
        } else {
            parsingError(String.format("\"%s\" is not a valid resource location", getItemID()));
        }

        return Stream.empty();
    }

}
