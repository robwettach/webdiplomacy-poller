package com.robwettach.webdiplomacy.notify;

import com.google.common.collect.Sets;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameState;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class DefeatedChecker implements DiffChecker {
    private Set<String> defeatedCountries = new HashSet<>();

    @Override
    public List<Diff> check(GameState state) {
        Set<String> currentDefeatedCountries = state.getCountries()
                .stream()
                .filter(c -> c.getStatus().equals(CountryStatus.Defeated))
                .map(CountryState::getCountryName)
                .collect(toSet());
        Set<String> newDefeatedCountries = Sets.difference(currentDefeatedCountries, defeatedCountries);
        defeatedCountries = currentDefeatedCountries;
        if (!newDefeatedCountries.isEmpty()) {
            return newDefeatedCountries.stream()
                    .map(c -> Diff.global("%s has been defeated", c))
                    .collect(toList());
        } else {
            return Collections.emptyList();
        }
    }
}
