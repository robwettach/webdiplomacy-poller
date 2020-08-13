package com.robwettach.webdiplomacy.notify;

import java.util.List;

public interface Notifier {
    void notify(List<Diff> diffs);
}
