package com.robwettach.webdiplomacy.model;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.Set;

@AutoValue
@JsonDeserialize(builder = CountryState.Builder.class)
public abstract class CountryState {
    public abstract String getCountryName();
    public abstract UserInfo getUser();
    public abstract boolean isCurrentUser();
    public abstract CountryStatus getStatus();
    public abstract boolean isMessageUnread();
    public abstract int getSupplyCenterCount();
    public abstract int getUnitCount();
    public abstract Set<Vote> getVotes();

    public static Builder builder() {
        return Builder.builder()
                .currentUser(false)
                .messageUnread(false);
    }

    @Override
    public String toString() {
        return format(
                "%s%s (%s): %s%s (%d sc, %d u) %s",
                isCurrentUser() ? "* " : "",
                getCountryName(),
                getUser().getName(),
                getStatus(),
                isMessageUnread() ? " - Unread Message" : "",
                getSupplyCenterCount(),
                getUnitCount(),
                getVotes().isEmpty() ? "" : "Votes: " + getVotes().stream().map(Vote::toString).collect(joining(", ")));
    }

    @AutoValue.Builder
    public interface Builder {
        @JsonCreator
        static CountryState.Builder builder() {
            return new AutoValue_CountryState.Builder()
                    .supplyCenterCount(0)
                    .unitCount(0)
                    .votes(Collections.emptySet());
        }

        @JsonProperty
        Builder countryName(String countryName);
        @JsonProperty
        Builder user(UserInfo user);
        @JsonProperty
        Builder currentUser(boolean isCurrentUser);
        @JsonProperty
        Builder status(CountryStatus status);
        @JsonProperty
        Builder messageUnread(boolean isMessageUnread);
        @JsonProperty
        Builder supplyCenterCount(int supplyCenterCount);
        @JsonProperty
        Builder unitCount(int unitCount);
        @JsonProperty
        Builder votes(Set<Vote> votes);
        CountryState build();
    }
}
