package ht.treechop.common.config.item;

import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collections;
import java.util.List;

public class ItemTagIdentifier extends ItemIdentifier {

    public ItemTagIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public List<Item> resolve(ITagCollection<Item> tags, IForgeRegistry<Item> registry) {
        ResourceLocation resource = ResourceLocation.tryCreate(getNamespace() + ":" + getLocalSpace());
        if (resource != null) {
            ITag<Item> tag = tags.get(resource);
            if (tag != null) {
                return tag.getAllElements();
            } else {
                parsingError(String.format("item tag \"%s\" does not exist", getNamespace()));
            }
        } else {
            parsingError(String.format("\"%s\" is not a valid item tag", getItemID()));
        }

        return Collections.emptyList();
    }

}
