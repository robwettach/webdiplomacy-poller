package com.robwettach.webdiplomacy.model;

/**
 * Enum representing possible game phases.
 */
public enum GamePhase {
    PreGame("Pre-game"),
    Diplomacy("Diplomacy"),
    Retreats("Retreats"),
    Builds("Builds"),
    Finished("Finished");

    private final String value;

    GamePhase(String value) {
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
     * Get a {@link GamePhase} instance from its string representation.
     *
     * @param value The string representation of the phase to get
     * @return The {@link GamePhase} corresponding to the given {@code value}
     * @throws IllegalArgumentException if {@code value} is not recognized as a valid {@link GamePhase}
     */
    public static GamePhase fromString(String value) {
        for (GamePhase phase : values()) {
            if (phase.getValue().equals(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unknown phase: " + value);
    }
}
