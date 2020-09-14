package com.robwettach.webdiplomacy.diff;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * {@link DiffChecker} that reports when a single country has not yet
 * submitted orders or marked themselves as {@link CountryStatus#Ready}.
 */
public class OrderChecker implements DiffChecker {
    @Override
    public List<Diff> check(Snapshot previous, Snapshot current) {
        List<Diff> diffs = new ArrayList<>();

        Optional<String> notYetSubmitted = getSingleCountryMatching(
                previous.getState(),
                current.getState(),
                this::getNotSubmittedCountries);
        notYetSubmitted.map(c -> Diff.global("Only %s has not yet submitted orders", c))
                .ifPresent(diffs::add);
        Optional<String> notYetReady = getSingleCountryMatching(
                previous.getState(),
                current.getState(),
                this::getNotReadyCountries);
        // Don't report both:
        // - Only Russia has not yet submitted orders
        // - Only Russia is not yet ready
        notYetReady.filter(rc -> notYetSubmitted.map(sc -> !sc.equals(rc)).orElse(true))
                .map(c -> Diff.global("Only %s is not yet ready", c))
                .ifPresent(diffs::add);

        return diffs;
    }

    private Optional<String> getSingleCountryMatching(
            GameState previous,
            GameState current,
            Function<GameState, Set<String>> getMatching) {
        Optional<String> previousMatchingCountry = getOnlyCountry(previous, getMatching);
        Optional<String> currentMatchingCountry = getOnlyCountry(current, getMatching);
        return currentMatchingCountry.isPresent() && currentMatchingCountry != previousMatchingCountry
                ? currentMatchingCountry : Optional.empty();
    }

    private Optional<String> getOnlyCountry(GameState state, Function<GameState, Set<String>> getMatching) {
        Set<String> countries = getMatching.apply(state);
        if (countries.size() == 1) {
            return Optional.of(Iterables.getOnlyElement(countries));
        } else {
            return Optional.empty();
        }
    }

    private Set<String> getNotSubmittedCountries(GameState state) {
        return state.getActiveCountries()
                .stream()
                .filter(c -> c.getStatus().equals(CountryStatus.NotReceived))
                .map(CountryState::getCountryName)
                .collect(toSet());
    }

    private static final Set<CountryStatus> EFFECTIVELY_READY_STATUSES = ImmutableSet.of(
            CountryStatus.Ready,
            CountryStatus.NoOrders);

    private Set<String> getNotReadyCountries(GameState state) {
        return state.getActiveCountries()
                .stream()
                .filter(c -> !EFFECTIVELY_READY_STATUSES.contains(c.getStatus()))
                .map(CountryState::getCountryName)
                .collect(toSet());
    }
}
