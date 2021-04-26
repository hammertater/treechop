package ht.treechop.common.config.item;

import ht.treechop.TreeChopMod;
import net.minecraft.item.Item;
import net.minecraft.tags.ITagCollection;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ItemIdentifier {

    private static final Pattern PATTERN = Pattern.compile("^\\s*([#@])?([a-z0-9_\\-.]*(?=:))?:?([a-z0-9_\\-./]*)?(.*)?");
    private static final String DEFAULT_NAMESPACE = "minecraft";

    private final String nameSpace;
    private final String localSpace;
    private final String parameters;
    private final String string;

    public ItemIdentifier(String nameSpace, String localSpace, String parameters, String string) {
        this.nameSpace = nameSpace;
        this.localSpace = localSpace;
        this.parameters = parameters;
        this.string = string;
    }

    /**
     * @param string by mod ("@mod"), tag ("#mod:tag"), or single identifier ("mod:item") with optional parameters ("mod:item=2")
     */
    public static ItemIdentifier from(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (matcher.find()) {
            String searchSpace = Optional.ofNullable(matcher.group(1)).orElse("");
            String namespace = Optional.ofNullable(matcher.group(2)).orElse("");
            String localSpace = Optional.ofNullable(matcher.group(3)).orElse("");
            String parameters = Optional.ofNullable(matcher.group(4)).orElse("");

            if (searchSpace.equals("#")) {
                return new ItemTagIdentifier(either(namespace, DEFAULT_NAMESPACE), localSpace, parameters, string);
            } else if (searchSpace.equals("@")) {
                if (namespace.equals("")) {
                    return new ItemNamespaceIdentifier(localSpace, parameters, string);
                } else {
                    return new MalformedItemIdentifier(string, "unqualified identifier does not match \"@mod\"");
                }
            } else {
                return new SingleItemIdentifier(either(namespace, DEFAULT_NAMESPACE), localSpace, parameters, string);
            }
        } else {
            return new MalformedItemIdentifier(string, "unqualified identifier does not match \"@mod\", \"#mod:tag\", or \"mod:item\"");
        }
    }

    private static String either(String string, String fallbackIfEmpty) {
        return string.equals("") ? fallbackIfEmpty : string;
    }

    public String getNamespace() {
        return nameSpace;
    }

    public String getLocalSpace() {
        return localSpace;
    }

    public String getQualifier() {
        return parameters;
    }

    public String getString() {
        return string;
    }

    public String getItemID() {
        return String.format("%s:%s", getNamespace(), getLocalSpace());
    }

    public abstract List<Item> resolve(ITagCollection<Item> tagSupplier, IForgeRegistry<Item> registry);

    public void parsingError(String message) {
        TreeChopMod.LOGGER.warn("Configuration error when parsing \"{}\": {}", getString(), message);
    }

}
