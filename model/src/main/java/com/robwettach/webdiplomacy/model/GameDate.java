package com.robwettach.webdiplomacy.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of a game date, e.g. "Spring, 1901".
 */
@AutoValue
public abstract class GameDate {
    private static final Pattern DATE_PATTERN = Pattern.compile("(?<season>\\w+), (?<year>\\d+)");

    public abstract Season getSeason();
    public abstract int getYear();

    @JsonCreator
    public static GameDate create(@JsonProperty Season season, @JsonProperty int year) {
        return new AutoValue_GameDate(season, year);
    }

    public String toString() {
        return format("%s, %4d", getSeason(), getYear());
    }

    /**
     * Parse a date string into a {@link GameDate} instance.
     *
     * @param date The string representation of the date to parse
     * @return The parsed {@link GameDate} instance
     * @throws NullPointerException if {@code date} is null
     * @throws IllegalArgumentException if {@code date} does not match the pattern /\w+, \d+/
     */
    public static GameDate parse(String date) {
        checkNotNull(date);
        Matcher matcher = DATE_PATTERN.matcher(date);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not parse date: " + date);
        }

        Season season = Season.valueOf(matcher.group("season"));
        int year = Integer.parseInt(matcher.group("year"));

        return GameDate.create(season, year);
    }

}
