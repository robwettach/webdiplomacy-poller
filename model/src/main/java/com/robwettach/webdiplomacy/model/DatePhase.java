package com.robwettach.webdiplomacy.model;

import com.google.auto.value.AutoValue;

import static java.lang.String.format;

@AutoValue
public abstract class DatePhase {
    public abstract GameDate getDate();
    public abstract GamePhase getPhase();

    public static DatePhase create(GameDate date, GamePhase phase) {
        return new AutoValue_DatePhase(date, phase);
    }

    @Override
    public String toString() {
        return format("%s, %s", getDate(), getPhase());
    }
}
