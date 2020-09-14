package com.robwettach.webdiplomacy.diff;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * {@link DiffChecker} that reports when there is an hour or less remaining before the next turn.
 */
public class HourRemainingChecker implements DiffChecker {
    @Override
    public List<Diff> check(Snapshot previous, Snapshot current) {
        Optional<ZonedDateTime> oneHourRemaining = current.getState().getNextTurnAt().map(d -> d.minusHours(1));
        boolean crossedHourBoundary = oneHourRemaining.map(d ->
                d.isAfter(previous.getTime()) && d.isBefore(current.getTime()))
                .orElse(false);
        if (crossedHourBoundary) {
            return Collections.singletonList(Diff.global("One more hour to submit moves!"));
        } else {
            return Collections.emptyList();
        }
    }
}
