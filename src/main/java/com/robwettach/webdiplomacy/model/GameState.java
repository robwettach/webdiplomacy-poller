package com.robwettach.webdiplomacy.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

@AutoValue
@JsonDeserialize(builder = GameState.Builder.class)
public abstract class GameState {
    public abstract String getName();
    public abstract int getId();
    public abstract GameDate getDate();
    public abstract GamePhase getPhase();
    public abstract boolean isPaused();
    public abstract Optional<ZonedDateTime> getNextTurnAt();
    public abstract ImmutableSet<CountryState> getCountries();

    @JsonIgnore
    public ImmutableSet<CountryState> getActiveCountries() {
        return getCountries()
                .stream()
                .filter(not(c -> c.getStatus().equals(CountryStatus.Defeated)))
                .collect(toImmutableSet());
    }

    @JsonIgnore
    public DatePhase getDatePhase() {
        return DatePhase.create(getDate(), getPhase());
    }

    public static Builder builder() {
        return Builder.builder();
    }

    public abstract Builder toBuilder();

    @Override
    public String toString() {
        return format(
                "%s: %s, %s. %s%n%s%n",
                getName(),
                getDate(),
                getPhase(),
                isPaused() ? "Paused" : "Next: " + getNextTurnAt().get(),
                getCountries().stream().map(CountryState::toString).collect(joining("\n")));
    }

    @AutoValue.Builder
    public static abstract class Builder {
        @JsonCreator
        public static Builder builder() {
            return new AutoValue_GameState.Builder()
                    .paused(false);
        }

        @JsonProperty
        public abstract Builder name(String name);
        @JsonProperty
        public abstract Builder id(int id);
        @JsonProperty
        public abstract Builder date(GameDate date);
        @JsonProperty
        public abstract Builder phase(GamePhase phase);
        @JsonProperty
        public abstract Builder paused(boolean paused);
        @JsonProperty
        // @Nullable because Jackson renders `null` explicitly and then passes `null` into this when deserializing.
        // By default, AutoValue uses Optional.of(...) instead of Optional.ofNullable(...)
        public abstract Builder nextTurnAt(@Nullable ZonedDateTime nextTurnAt);
        @JsonProperty
        public abstract Builder countries(Collection<CountryState> countries);

        abstract ImmutableSet.Builder<CountryState> countriesBuilder();

        public Builder country(CountryState country) {
            countriesBuilder().add(country);
            return this;
        }

        public abstract GameState build();
    }
}
