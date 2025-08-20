package com.flightbooking.searchservice.enums;

public enum SearchPreference {
    CHEAPEST("cheapest"),
    FASTEST("fastest"),
    NONE("none");

    private final String value;

    SearchPreference(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SearchPreference fromString(String text) {
        for (SearchPreference preference : SearchPreference.values()) {
            if (preference.value.equalsIgnoreCase(text)) {
                return preference;
            }
        }
        return NONE; // Default to NONE if no match found
    }

    @Override
    public String toString() {
        return value;
    }
} 