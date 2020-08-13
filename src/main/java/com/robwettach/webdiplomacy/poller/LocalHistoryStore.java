package com.robwettach.webdiplomacy.poller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.robwettach.webdiplomacy.model.Json.OBJECT_MAPPER;

public class LocalHistoryStore implements HistoryStore {
    private static final String SNAPSHOTS_FILE_NAME = "snapshots.json";

    private final Path snapshotsPath;

    private Map<Integer, List<Snapshot>> snapshots = new HashMap<>();

    public LocalHistoryStore(Path configDirPath) {
        checkNotNull(configDirPath, "configDirPath must not be null");
        this.snapshotsPath = configDirPath.resolve(SNAPSHOTS_FILE_NAME);
    }

    @Override
    public void load() {
        if (Files.exists(snapshotsPath)) {
            try {
                // Make a known-mutable copy
                snapshots = new HashMap<>(OBJECT_MAPPER.readValue(
                        snapshotsPath.toFile(),
                        new TypeReference<>() {}));
            } catch (IOException e) {
                System.err.println("Failed to read JSON from : " + snapshotsPath);
                e.printStackTrace();
            }
            int snapshotCount = snapshots.values().stream().mapToInt(List::size).sum();
            System.out.printf(
                    "Loaded %d snapshots from %d games from: %s%n",
                    snapshotCount,
                    snapshots.size(),
                    snapshotsPath);
        }
    }

    @Override
    public ImmutableList<Integer> getGameIds() {
        return snapshots.keySet().stream().sorted().collect(toImmutableList());
    }

    @Override
    public ImmutableList<Snapshot> getSnapshotsForGame(int gameId) {
        return ImmutableList.copyOf(snapshots.getOrDefault(gameId, new ArrayList<>()));
    }

    @Override
    public Optional<Snapshot> getLatestSnapshotForGame(int id) {
        List<Snapshot> gameSnapshots = getSnapshotsForGame(id);
        return Optional.ofNullable(Iterables.getLast(gameSnapshots, null));
    }

    @Override
    public void addSnapshot(int gameId, Snapshot snapshot) {
        List<Snapshot> gameSnapshots = snapshots.computeIfAbsent(gameId, (x) -> new ArrayList<>());
        gameSnapshots.add(snapshot);
    }

    @Override
    public void save() {
        try {
            OBJECT_MAPPER.writeValue(snapshotsPath.toFile(), snapshots);
            int snapshotCount = snapshots.values().stream().mapToInt(List::size).sum();
            System.out.printf(
                    "Saved %d snapshots from %d games to: %s%n",
                    snapshotCount,
                    snapshots.size(),
                    snapshotsPath);
        } catch (IOException e) {
            System.err.println("Failed to write snapshots to: " + snapshotsPath);
            e.printStackTrace();
        }
    }
}
