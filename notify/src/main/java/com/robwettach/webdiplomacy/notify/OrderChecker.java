package com.robwettach.webdiplomacy.notify;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableSet;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.DatePhase;
import com.robwettach.webdiplomacy.model.GameState;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@link DiffChecker} that reports when a single country has not yet
 * submitted orders or marked themselves as {@link CountryStatus#Ready}.
 */
public class OrderChecker implements DiffChecker {
    private Pair<DatePhase, String> singleCountryNotSubmitted = null;
    private Pair<DatePhase, String> singleCountryNotReady = null;

    @Override
    public List<Diff> check(GameState state) {
        List<Diff> diffs = new ArrayList<>();

        checkNotSubmitted(state, diffs);
        checkNotReady(state, diffs);

        return diffs;
    }

    private void checkNotSubmitted(GameState state, List<Diff> diffs) {
        List<String> notSubmittedCountries = state.getActiveCountries()
                .stream()
                .filter(c -> c.getStatus().equals(CountryStatus.NotReceived))
                .map(CountryState::getCountryName)
                .collect(toList());
        if (notSubmittedCountries.size() == 1) {
            String c = notSubmittedCountries.get(0);
            Pair<DatePhase, String> pair = Pair.of(state.getDatePhase(), c);
            if (!pair.equals(singleCountryNotSubmitted)) {
                singleCountryNotSubmitted = pair;
                diffs.add(Diff.global("Only %s has not yet submitted orders", c));
            }
        } else {
            singleCountryNotSubmitted = null;
        }
    }

    private static final Set<CountryStatus> EFFECTIVELY_READY_STATUSES = ImmutableSet.of(
            CountryStatus.Ready,
            CountryStatus.NoOrders);

    private void checkNotReady(GameState state, List<Diff> diffs) {
        List<String> notReadyCountries = state.getActiveCountries()
                .stream()
                .filter(c -> !EFFECTIVELY_READY_STATUSES.contains(c.getStatus()))
                .map(CountryState::getCountryName)
                .collect(toList());
        if (notReadyCountries.size() == 1) {
            String c = notReadyCountries.get(0);
            Pair<DatePhase, String> pair = Pair.of(state.getDatePhase(), c);
            // If this is a different country that hasn't clicked "Ready"
            // AND that country isn't *also* "Not Submitted"
            // otherwise, we'd get duplicate notifications for the same behavior:
            // - Only Russia has not yet submitted orders
            // - Only Russia is not yet ready
            if (!pair.equals(singleCountryNotReady) && !pair.equals(singleCountryNotSubmitted)) {
                singleCountryNotReady = pair;
                diffs.add(Diff.global("Only %s is not yet ready", c));
            }
        } else {
            singleCountryNotReady = null;
        }
    }
}
