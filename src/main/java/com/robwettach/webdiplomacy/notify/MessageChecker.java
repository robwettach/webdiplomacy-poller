package com.robwettach.webdiplomacy.notify;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.GameState;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class MessageChecker implements DiffChecker {
    private static final Joiner COMMA_JOINER = Joiner.on(", ");

    private Set<String> unreadMessages = new HashSet<>();

    @Override
    public List<Diff> check(GameState state) {
        Set<String> currentUnreadMessages = state.getCountries()
                .stream()
                .filter(CountryState::isMessageUnread)
                .map(CountryState::getCountryName)
                .collect(toSet());
        Set<String> newUnreadMessages = Sets.difference(currentUnreadMessages, unreadMessages);
        unreadMessages = currentUnreadMessages;
        if (!newUnreadMessages.isEmpty()) {
            return Collections.singletonList(Diff.personal(
                    "New message from: %s",
                    COMMA_JOINER.join(newUnreadMessages)));
        } else {
            return Collections.emptyList();
        }
    }
}
