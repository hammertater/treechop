package ht.treechop.common.config.item;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.stream.Stream;

public class ItemTagIdentifier extends ItemIdentifier {

    public ItemTagIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public Stream<Item> resolve() {
        ResourceLocation tagId = ResourceLocation.tryParse(getNamespace() + ":" + getLocalSpace());
        if (tagId != null) {
            TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, tagId);
            return Registry.ITEM.stream().filter(item -> item.builtInRegistryHolder().is(tag));
        } else {
            parsingError(String.format("\"%s\" is not a valid resource location", getItemID()));
        }

        return Stream.empty();
    }

}
