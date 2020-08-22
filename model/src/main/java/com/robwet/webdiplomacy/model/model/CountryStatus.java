package com.robwettach.webdiplomacy.model;

public enum CountryStatus {
    NoOrders("-"),
    Completed("Completed"),
    Ready("Ready"),
    NotReceived("Not received"),
    Defeated("Defeated");

    private final String value;

    CountryStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static CountryStatus fromString(String value) {
        for (CountryStatus phase : values()) {
            if (phase.getValue().equals(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
