package ht.treechop.common.config;

abstract class Handle {

    private String category;
    private String key;

    public Handle(String category, String key) {
        this.category = category;
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public String getKey() {
        return key;
    }

}
