package ht.treechop.common.config.item;

import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.stream.Stream;

public class MalformedItemIdentifier extends ItemIdentifier {

    public MalformedItemIdentifier(String string, String explanation) {
        super("", "", Collections.emptyList(), string);
        parsingError(explanation);
    }

    @Override
    public Stream<Item> resolve() {
        return Stream.empty();
    }

}