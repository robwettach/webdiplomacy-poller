package com.robwettach.webdiplomacy.notify;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.Vote;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import one.util.streamex.StreamEx;

/**
 * {@link DiffChecker} that reports when a country starts a vote or is the last remaining country to cast a vote.
 */
public class VoteChecker implements DiffChecker {
    @Override
    public List<Diff> check(Snapshot previous, Snapshot current) {
        GameState previousState = previous.getState();
        GameState currentState = current.getState();
        List<Diff> diffs = new ArrayList<>();
        Map<Vote, Set<String>> previousVotes = getVotingCountries(previousState);
        Map<Vote, Set<String>> currentVotes = getVotingCountries(currentState);
        Set<Vote> distinctVotes = Sets.union(previousVotes.keySet(), currentVotes.keySet());
        distinctVotes.forEach((vote) -> {
            Set<String> previousVotingCountries = previousVotes.getOrDefault(vote, Collections.emptySet());
            Set<String> currentVotingCountries = currentVotes.getOrDefault(vote, Collections.emptySet());
            if (!previousVotingCountries.equals(currentVotingCountries)) {
                if (currentVotingCountries.size() == 1 && previousVotingCountries.size() == 0) {
                    String c = Iterables.getOnlyElement(currentVotingCountries);
                    diffs.add(Diff.global("%s is starting a \"%s\" vote", c, vote));
                } else if (currentVotingCountries.size() == currentState.getActiveCountries().size() - 1) {
                    String c = Iterables.getOnlyElement(
                            Sets.difference(
                                    currentState.getActiveCountries()
                                            .stream()
                                            .map(CountryState::getCountryName)
                                            .collect(toSet()),
                                    new HashSet<>(currentVotingCountries)));
                    diffs.add(Diff.global("Only %s has not voted \"%s\" yet", c, vote));
                }
            }
        });
        return diffs;
    }

    private Map<Vote, Set<String>> getVotingCountries(GameState state) {
        return StreamEx.of(state.getActiveCountries())
                .flatMapToEntry(c -> StreamEx.of(c.getVotes()).mapToEntry(v -> c.getCountryName()).toMap())
                .sorted(Map.Entry.comparingByKey())
                .collapseKeys(toSet())
                .toMap();
    }
}
