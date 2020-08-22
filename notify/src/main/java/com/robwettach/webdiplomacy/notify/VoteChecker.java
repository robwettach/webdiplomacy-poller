package com.robwettach.webdiplomacy.notify;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.Vote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import one.util.streamex.StreamEx;

public class VoteChecker implements DiffChecker {
    private final Map<Vote, String> startingVotes = new HashMap<>();
    private final Map<Vote, String> lastRemainingVote = new HashMap<>();

    public List<Diff> check(GameState state) {
        List<Diff> diffs = new ArrayList<>();
        Map<Vote, Set<String>> votingCountries = StreamEx.of(state.getActiveCountries())
                .flatMapToEntry(c -> StreamEx.of(c.getVotes()).mapToEntry(v -> c.getCountryName()).toMap())
                .sorted(Map.Entry.comparingByKey())
                .collapseKeys(toSet())
                .toMap();
        votingCountries.forEach((vote, countriesVoting) -> {
            if (countriesVoting.size() == 1) {
                String c = Iterables.getOnlyElement(countriesVoting);
                String e = startingVotes.get(vote);
                if (!c.equals(e)) {
                    startingVotes.put(vote, c);
                    diffs.add(Diff.global("%s is starting a \"%s\" vote", c, vote));
                }
            } else if (countriesVoting.size() == state.getActiveCountries().size() - 1) {
                String c = Iterables.getOnlyElement(
                        Sets.difference(
                                state.getActiveCountries()
                                        .stream()
                                        .map(CountryState::getCountryName)
                                        .collect(toSet()),
                                new HashSet<>(countriesVoting)));
                String e = lastRemainingVote.get(vote);
                if (!c.equals(e)) {
                    lastRemainingVote.put(vote, c);
                    diffs.add(Diff.global("Only %s has not voted \"%s\" yet", c, vote));
                }
            } else {
                startingVotes.remove(vote);
                lastRemainingVote.remove(vote);
            }
        });
        return diffs;
    }
}
