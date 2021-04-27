package ht.treechop.common.config.item;

public class IdentifierQualifier {
    private final String key;
    private final String value;

    public IdentifierQualifier(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
