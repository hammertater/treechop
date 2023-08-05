package ht.treechop.common.config.resource;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringUtil;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ResourcePatternIdentifier extends ResourceIdentifier {

    private final Pattern pattern;

    public ResourcePatternIdentifier(Pattern pattern, List<IdentifierQualifier> qualifiers, String string) {
        super("", "", qualifiers, string);
        this.pattern = pattern;
    }

    @Override
    public <R extends DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
        return registry.stream()
                .filter(resource -> {
                    ResourceLocation loc = registry.getKey(resource);
                    return loc != registry.getDefaultKey() && pattern.matcher(loc.toString()).matches();
                });
    }

}
