package com.robwettach.webdiplomacy.notify;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameState;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * {@link DiffChecker} that reports when a country has been defeated.
 */
public class DefeatedChecker implements DiffChecker {
    @Override
    public List<Diff> check(Snapshot previous, Snapshot current) {
        Set<String> previousDefeatedCountries = getDefeatedCountries(previous.getState());
        Set<String> currentDefeatedCountries = getDefeatedCountries(current.getState());
        Set<String> newDefeatedCountries = Sets.difference(currentDefeatedCountries, previousDefeatedCountries);
        if (!newDefeatedCountries.isEmpty()) {
            return newDefeatedCountries.stream()
                    .map(c -> Diff.global("%s has been defeated", c))
                    .collect(toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Set<String> getDefeatedCountries(GameState state) {
        return state.getCountries()
                .stream()
                .filter(c -> c.getStatus().equals(CountryStatus.Defeated))
                .map(CountryState::getCountryName)
                .collect(toSet());
    }
}
