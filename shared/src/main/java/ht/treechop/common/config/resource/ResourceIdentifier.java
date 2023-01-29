package ht.treechop.common.config.resource;

import ht.treechop.TreeChop;
import net.minecraft.core.DefaultedRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ResourceIdentifier {

    private static final Pattern PATTERN = Pattern.compile("^\\s*([#@])?([a-z0-9_\\-.]*(?=:))?:?([a-z0-9_\\-./]*)?(.*)?");
    private static final Pattern QUALIFIERS_PATTERN = Pattern.compile("^\\?(.*)$");
    private static final String DEFAULT_NAMESPACE = "minecraft";

    private final String nameSpace;
    private final String localSpace;
    private final List<IdentifierQualifier> qualifiers;
    private final String string;

    public ResourceIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
        this.nameSpace = nameSpace;
        this.localSpace = localSpace;
        this.qualifiers = qualifiers;
        this.string = string;
    }

    /**
     * @param string by mod ("@mod"), tag ("#mod:tag"), or single identifier ("mod:id") with optional qualifiers
     *               ("mod:id=2")
     */
    public static ResourceIdentifier from(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (matcher.find()) {
            String searchSpace = Optional.ofNullable(matcher.group(1)).orElse("");
            String namespace = Optional.ofNullable(matcher.group(2)).orElse("");
            String localSpace = Optional.ofNullable(matcher.group(3)).orElse("");
            List<IdentifierQualifier> qualifiers = parseQualifiers(Optional.ofNullable(matcher.group(4)).orElse(""));

            if (searchSpace.equals("#")) {
                return new ResourceTagIdentifier(either(namespace, DEFAULT_NAMESPACE), localSpace, qualifiers, string);
            } else if (searchSpace.equals("@")) {
                if (namespace.equals("")) {
                    return new ResourceNamespaceIdentifier(localSpace, qualifiers, string);
                } else {
                    return new MalformedResourceIdentifier(string, "unqualified identifier does not match \"@mod\"");
                }
            } else {
                return new SingleResourceIdentifier(either(namespace, DEFAULT_NAMESPACE), localSpace, qualifiers, string);
            }
        } else {
            return new MalformedResourceIdentifier(string, "unqualified identifier does not match \"@mod\", \"#mod:tag\", or \"mod:id\"");
        }
    }

    private static List<IdentifierQualifier> parseQualifiers(String qualifiersString) {
        Matcher matcher = QUALIFIERS_PATTERN.matcher(qualifiersString);
        if (matcher.find()) {
            return Arrays.stream(matcher.group(1).split(","))
                    .map(ResourceIdentifier::parseQualifier)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private static IdentifierQualifier parseQualifier(String string) {
        String[] parts = string.split("=", 2);
        if (parts.length <= 1) {
            return new IdentifierQualifier(string, null);
        } else {
            return new IdentifierQualifier(parts[0], parts[1]);
        }
    }

    private static String either(String string, String fallbackIfEmpty) {
        return string.equals("") ? fallbackIfEmpty : string;
    }

    private static void parsingError(String idString, String message) {
        TreeChop.LOGGER.warn("Configuration issue: failed to parse \"{}\": {} (to silence this warning, find and delete \"{}\" in treechop-common.toml)", idString, message, idString);
    }

    public String getNamespace() {
        return nameSpace;
    }

    public String getLocalSpace() {
        return localSpace;
    }

    public List<IdentifierQualifier> getQualifiers() {
        return qualifiers;
    }

    public String getString() {
        return string;
    }

    public String getResourceLocation() {
        return String.format("%s:%s", getNamespace(), getLocalSpace());
    }

    public abstract <R extends DefaultedRegistry<T>, T> Stream<T> resolve(R registry);

    public void parsingError(String message) {
        parsingError(getString(), message);
    }

    public Optional<String> getQualifier(String key) {
        return qualifiers.stream()
                .filter(qualifier -> qualifier.getKey().equals(key))
                .map(IdentifierQualifier::getValue)
                .findFirst();
    }

    public boolean hasQualifier(String key) {
        return qualifiers.stream()
                .anyMatch(qualifier -> qualifier.getKey().equals(key));
    }
}
