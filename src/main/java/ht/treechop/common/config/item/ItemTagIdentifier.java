package ht.treechop.common.config.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.stream.Stream;

public class ItemTagIdentifier extends ItemIdentifier {

    public ItemTagIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public Stream<Item> resolve(TagCollection<Item> tags, IForgeRegistry<Item> registry) {
        ResourceLocation tagId = ResourceLocation.tryParse(getNamespace() + ":" + getLocalSpace());
        if (tagId != null) {
            Tag<Item> tag = tags.getTag(tagId);
            if (tag != null) {
                return tag.getValues().stream();
            }
        } else {
            parsingError(String.format("\"%s\" is not a valid resource location", getItemID()));
        }

        return Stream.empty();
    }

}
