package com.robwettach.webdiplomacy.notify;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@AutoValue
public abstract class CompositeNotifier implements Notifier {
    public abstract ImmutableList<Notifier> getNotifiers();

    public static CompositeNotifier create(Notifier... notifiers) {
        return create(Arrays.asList(notifiers));
    }

    public static CompositeNotifier create(Collection<Notifier> notifiers) {
        return new AutoValue_CompositeNotifier(ImmutableList.copyOf(notifiers));
    }

    @Override
    public void notify(List<Diff> diffs) {
        getNotifiers().forEach(n -> n.notify(diffs));
    }
}
