package com.robwettach.webdiplomacy.notify;

import com.robwettach.webdiplomacy.model.GameState;

import java.util.List;

public interface DiffChecker {
    List<Diff> check(GameState state);
}
