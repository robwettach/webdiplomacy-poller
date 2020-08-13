package com.robwettach.webdiplomacy.notify;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.Vote;
import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class VoteChecker implements DiffChecker {
    private final Map<Vote, CountryState> startingVotes = new HashMap<>();
    private final Map<Vote, CountryState> lastRemainingVote = new HashMap<>();

    public List<Diff> check(GameState state) {
        List<Diff> diffs = new ArrayList<>();
        Map<Vote, Set<CountryState>> votingCountries = StreamEx.of(state.getCountries())
                .flatMapToEntry(c -> StreamEx.of(c.getVotes()).mapToEntry(v -> c).toMap())
                .sorted(Map.Entry.comparingByKey())
                .collapseKeys(toSet())
                .toMap();
        votingCountries.forEach((vote, countriesVoting) -> {
            if (countriesVoting.size() == 1) {
                CountryState c = Iterables.getOnlyElement(countriesVoting);
                CountryState e = startingVotes.get(vote);
                if (!c.equals(e)) {
                    startingVotes.put(vote, c);
                    diffs.add(Diff.global("%s is starting a \"%s\" vote", c.getCountryName(), vote));
                }
            } else if (countriesVoting.size() == state.getCountries().size() - 1) {
                CountryState c = Iterables.getOnlyElement(
                        Sets.difference(
                                state.getCountries(),
                                new HashSet<>(countriesVoting)));
                CountryState e = lastRemainingVote.get(vote);
                if (!c.equals(e)) {
                    lastRemainingVote.put(vote, c);
                    diffs.add(Diff.global("Only %s has not voted \"%s\" yet", c.getCountryName(), vote));
                }
            } else {
                startingVotes.remove(vote);
                lastRemainingVote.remove(vote);
            }
        });
        return diffs;
    }
}
