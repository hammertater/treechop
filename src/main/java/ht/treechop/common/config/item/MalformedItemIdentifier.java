package ht.treechop.common.config.item;

import net.minecraft.item.Item;
import net.minecraft.tags.ITagCollection;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collections;
import java.util.List;

public class MalformedItemIdentifier extends ItemIdentifier {

    public MalformedItemIdentifier(String string, String explanation) {
        super("", "", "", string);
        parsingError(explanation);
    }

    @Override
    public List<Item> resolve(ITagCollection<Item> tags, IForgeRegistry<Item> registry) {
        return Collections.emptyList();
    }

}
