package com.grocery.grocery_price_minimizer.model;

public enum Store {
    WALMART("Walmart", "walmart.ca"),
    MAXI("Maxi", "maxi.ca"),
    METRO("Metro", "metro.ca"),
    IGA("IGA", "iga.net");

    private final String displayName;
    private final String domain;

    Store(String displayName, String domain) {
        this.displayName = displayName;
        this.domain = domain;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDomain() {
        return domain;
    }

    public static Store fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Store name cannot be null");
        }
        for (Store s : values()) {
            if (s.displayName.equalsIgnoreCase(name)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown store: " + name);
    }
}
