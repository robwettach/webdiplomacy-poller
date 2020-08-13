package com.robwettach.webdiplomacy.notify;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Pair<Left, Right> {
    public abstract Left getLeft();
    public abstract Right getRight();

    public static <Left, Right> Pair<Left, Right> of(Left left, Right right) {
        return new AutoValue_Pair<>(left, right);
    }
}
