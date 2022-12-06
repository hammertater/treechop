package ht.treechop.common.config.item;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ResourceTagIdentifier extends ResourceIdentifier {

    public ResourceTagIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        super(nameSpace, localSpace, qualifiers, string);
    }

    @Override
    public <R extends DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
        ResourceLocation tagId = ResourceLocation.tryParse(getNamespace() + ":" + getLocalSpace());
        if (tagId != null) {
            TagKey<T> tag = TagKey.create(registry.key(), tagId);
            return StreamSupport.stream(registry.getTagOrEmpty(tag).spliterator(), false).map(Holder::value);
        } else {
            parsingError(String.format("\"%s\" is not a valid resource location", getItemID()));
        }

        return Stream.empty();
    }

}
