package ht.treechop.common.config.item;

import ht.treechop.TreeChopMod;
import net.minecraft.item.Item;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collections;
import java.util.List;

public class SingleItemIdentifier extends ItemIdentifier {

    public SingleItemIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public List<Item> resolve(ITagCollection<Item> tags, IForgeRegistry<Item> registry) {
        String resourceString = getNamespace() + ":" + getLocalSpace();
        ResourceLocation resource = ResourceLocation.tryParse(resourceString);
        if (resource != null) {
            Item item = registry.getValue(resource);
            if (item != null) {
                return Collections.singletonList(item);
            } else {
                TreeChopMod.LOGGER.warn("Configuration error when parsing {}: item {} does not exist", getString(), resourceString);
            }
        } else {
            TreeChopMod.LOGGER.warn("Configuration error when parsing {}: {} is not a valid item ID", getString(), resourceString);
        }

        return Collections.emptyList();
    }

}
