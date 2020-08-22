package com.robwettach.webdiplomacy.notify;

import static java.lang.String.format;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Diff {
    public abstract String getMessage();
    public abstract boolean isPersonal();
    public boolean isGlobal() {
        return !isPersonal();
    }

    public static Diff global(String message, Object... args) {
        return new AutoValue_Diff(format(message, args), false);
    }

    public static Diff personal(String message, Object... args) {
        return new AutoValue_Diff(format(message, args), true);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
