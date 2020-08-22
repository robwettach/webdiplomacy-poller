package com.robwettach.webdiplomacy.poller;

import java.util.List;
import java.util.Optional;

public interface HistoryStore {
    void load();

    List<Integer> getGameIds();

    List<Snapshot> getSnapshotsForGame(int gameId);

    Optional<Snapshot> getLatestSnapshotForGame(int id);

    void addSnapshot(int gameId, Snapshot snapshot);

    void save();
}
