package com.robwettach.webdiplomacy.notify;

import com.google.auto.value.AutoValue;

/**
 * Simple two-element tuple.
 *
 * @param <LeftT> The type of the {@link #getLeft() left} element
 * @param <RightT> The type of the {@link #getRight() right} element
 */
@AutoValue
public abstract class Pair<LeftT, RightT> {
    public abstract LeftT getLeft();
    public abstract RightT getRight();

    public static <LeftT, RightT> Pair<LeftT, RightT> of(LeftT left, RightT right) {
        return new AutoValue_Pair<>(left, right);
    }
}
