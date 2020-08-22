package com.robwettach.webdiplomacy.notify;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Pair<LeftT, RightT> {
    public abstract LeftT getLeft();
    public abstract RightT getRight();

    public static <LeftT, RightT> Pair<LeftT, RightT> of(LeftT left, RightT right) {
        return new AutoValue_Pair<>(left, right);
    }
}
