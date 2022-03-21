package ht.treechop.common.config.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.List;
import java.util.stream.Stream;

public class ItemTagIdentifier extends ItemIdentifier {

    public ItemTagIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public Stream<Item> resolve(IForgeRegistry<Item> registry) {
        ResourceLocation tagId = ResourceLocation.tryParse(getNamespace() + ":" + getLocalSpace());
        if (tagId != null) {
            ITagManager<Item> itemTags = ForgeRegistries.ITEMS.tags();
            if (itemTags != null) {
                return itemTags.getTag(ItemTags.create(tagId)).stream();
            } else {
                return Stream.empty();
            }
        } else {
            parsingError(String.format("\"%s\" is not a valid resource location", getItemID()));
        }

        return Stream.empty();
    }

}
