package ht.treechop.common.config.item;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.stream.Stream;

public class SingleItemIdentifier extends ItemIdentifier {

    public SingleItemIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public Stream<Item> resolve() {
        String resourceString = getNamespace() + ":" + getLocalSpace();
        ResourceLocation itemId = ResourceLocation.tryParse(resourceString);
        if (itemId != null) {
            if (Registry.ITEM.containsKey(itemId)) {
                Item item = Registry.ITEM.get(itemId); // Returns minecraft:air if itemId is not registered
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
