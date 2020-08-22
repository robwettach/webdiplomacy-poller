package com.robwettach.webdiplomacy.poller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.robwettach.webdiplomacy.model.GameState;
import java.time.ZonedDateTime;

@AutoValue
public abstract class Snapshot {
    public abstract ZonedDateTime getTime();
    public abstract GameState getState();

    @JsonCreator
    public static Snapshot create(@JsonProperty ZonedDateTime time, @JsonProperty GameState state) {
        return new AutoValue_Snapshot(time, state);
    }
}
