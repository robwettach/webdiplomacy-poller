package com.robwettach.webdiplomacy.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

/**
 * Properties related to a specific user.
 */
@AutoValue
@JsonDeserialize
public abstract class UserInfo {
    public abstract String getName();
    public abstract int getId();

    @JsonCreator
    public static UserInfo create(@JsonProperty String name, @JsonProperty int id) {
        return new AutoValue_UserInfo(name, id);
    }
}
