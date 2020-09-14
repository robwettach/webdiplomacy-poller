package com.robwettach.webdiplomacy.notify;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.robwettach.webdiplomacy.diff.Diff;
import java.util.Arrays;
import java.util.List;

/**
 * {@link Notifier} that sends notifications to a collection of other {@link Notifier}s.
 */
@AutoValue
public abstract class CompositeNotifier implements Notifier {
    @JsonIgnore
    public abstract ImmutableList<Notifier> getNotifiers();

    @JsonProperty("notifiers")
    ImmutableList<UnionNotifier> getUnionNotifiers() {
        return getNotifiers().stream().map(UnionNotifier::create).collect(toImmutableList());
    }

    public static CompositeNotifier create(Notifier... notifiers) {
        return create(Arrays.asList(notifiers));
    }

    public static CompositeNotifier create(List<Notifier> notifiers) {
        return new AutoValue_CompositeNotifier(ImmutableList.copyOf(notifiers));
    }

    @JsonCreator
    static CompositeNotifier createJson(@JsonProperty("notifiers") List<UnionNotifier> notifiers) {
        return create(notifiers.stream().map(UnionNotifier::getNotifier).collect(toList()));
    }

    @Override
    public void notify(List<Diff> diffs) {
        getNotifiers().forEach(n -> n.notify(diffs));
    }
}
