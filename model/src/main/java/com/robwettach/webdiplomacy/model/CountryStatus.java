package com.robwettach.webdiplomacy.model;

/**
 * Enum representing possible statuses for a Country.
 */
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

    /**
     * Get a {@link CountryStatus} instance from its string representation.
     * @param value The string representation of the status to get
     * @return The {@link CountryStatus} corresponding to the given {@code value}
     * @throws IllegalArgumentException if {@code value} is not recognized as a valid {@link CountryStatus}
     */
    public static CountryStatus fromString(String value) {
        for (CountryStatus phase : values()) {
            if (phase.getValue().equals(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
