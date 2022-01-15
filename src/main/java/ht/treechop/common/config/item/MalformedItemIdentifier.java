package ht.treechop.common.config.item;

import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class MalformedItemIdentifier extends ItemIdentifier {

    public MalformedItemIdentifier(String string, String explanation) {
        super("", "", Collections.emptyList(), string);
        parsingError(explanation);
    }

    @Override
    public Stream<Item> resolve(TagCollection<Item> tags, IForgeRegistry<Item> registry) {
        return Stream.empty();
    }

}
