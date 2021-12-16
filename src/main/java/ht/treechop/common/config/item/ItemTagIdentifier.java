package ht.treechop.common.config.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collections;
import java.util.List;

public class ItemTagIdentifier extends ItemIdentifier {

    public ItemTagIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public List<Item> resolve(TagCollection<Item> tags, IForgeRegistry<Item> registry) {
        ResourceLocation resource = ResourceLocation.tryParse(getNamespace() + ":" + getLocalSpace());
        if (resource != null) {
            Tag<Item> tag = tags.getTag(resource);
            if (tag != null) {
                return tag.getValues();
            } else {
                parsingError(String.format("item tag \"%s\" does not exist", getNamespace()));
            }
        } else {
            parsingError(String.format("\"%s\" is not a valid item tag", getItemID()));
        }

        return Collections.emptyList();
    }

}
