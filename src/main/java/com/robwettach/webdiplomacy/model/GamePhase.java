package com.robwettach.webdiplomacy.model;

public enum GamePhase {
    PreGame("Pre-game"),
    Diplomacy("Diplomacy"),
    Retreats("Retreats"),
    Builds("Builds");

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

    public static GamePhase fromString(String value) {
        for (GamePhase phase : values()) {
            if (phase.getValue().equals(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unknown phase: " + value);
    }
}
