package com.robwettach.webdiplomacy.diff;

import static java.util.stream.Collectors.joining;

import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameState;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * {@link DiffChecker} that reports when the game is finished, and who won or drew.
 */
public class FinishedChecker implements DiffChecker {
    @Override
    public List<Diff> check(Snapshot previous, Snapshot current) {
        GameState state = current.getState();
        if (!previous.getState().isFinished() && state.isFinished()) {
            Optional<String> winner = state.getCountries()
                    .stream()
                    .filter(c -> c.getStatus().equals(CountryStatus.Won))
                    .map(CountryState::getCountryName)
                    .findFirst();
            String draws = state.getCountries()
                    .stream()
                    .filter(c -> c.getStatus().equals(CountryStatus.Drawn))
                    .map(CountryState::getCountryName)
                    // Mostly just for deterministic tests
                    .sorted()
                    .collect(joining(", "));
            return Collections.singletonList(
                    winner.map(w -> Diff.global("Game Finished - Won by %s", w))
                            .orElse(Diff.global("Game Finished - Drawn by %s", draws)));
        }
        return Collections.emptyList();
    }
}
