package ht.treechop.common.config;

public enum ListType {
    BLACKLIST(true),
    WHITELIST(false);

    private final boolean xor;

    ListType(boolean xor) {
        this.xor = xor;
    }

    boolean accepts(boolean truth) {
        return truth ^ xor;
    }
}
