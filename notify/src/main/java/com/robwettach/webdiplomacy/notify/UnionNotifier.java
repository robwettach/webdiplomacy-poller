package com.robwettach.webdiplomacy.notify;

import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A <a href="https://en.wikipedia.org/wiki/Union_type">union-type</a> for known {@link Notifier} subclasses that
 * enables serialization and deserialization to/from JSON via {@link com.robwettach.webdiplomacy.json.Json}.
 *
 * <p>Supports:
 * <ul>
 *     <li>{@link StdOutNotifier}</li>
 *     <li>{@link SlackNotifier}</li>
 *     <li>{@link CompositeNotifier}</li>
 * </ul>
 */
@AutoValue
@JsonDeserialize(builder = UnionNotifier.Builder.class)
public abstract class UnionNotifier implements Notifier {
    private static final List<Function<UnionNotifier, Optional<? extends Notifier>>> NOTIFIERS = List.of(
            UnionNotifier::getStdOut,
            UnionNotifier::getSlack,
            UnionNotifier::getComposite);

    @JsonProperty
    abstract Optional<StdOutNotifier> getStdOut();
    @JsonProperty
    abstract Optional<SlackNotifier> getSlack();
    @JsonProperty
    abstract Optional<CompositeNotifier> getComposite();

    public static UnionNotifier create(StdOutNotifier stdOut) {
        return builder().stdOut(stdOut).build();
    }

    public static UnionNotifier create(SlackNotifier slack) {
        return builder().slack(slack).build();
    }

    public static UnionNotifier create(CompositeNotifier composite) {
        return builder().composite(composite).build();
    }

    /**
     * Create a {@link UnionNotifier} from an arbitrary supported {@link Notifier} type.
     *
     * @param notifier The {@link Notifier} to wrap in this union type
     * @return The {@link UnionNotifier} containing the specified {@code notifier}
     * @throws IllegalArgumentException if the runtime type of {@link Notifier} is not one of the supported types
     */
    public static UnionNotifier create(Notifier notifier) {
        Builder builder = builder();
        if (notifier instanceof StdOutNotifier) {
            builder.stdOut((StdOutNotifier) notifier);
        } else if (notifier instanceof SlackNotifier) {
            builder.slack((SlackNotifier) notifier);
        } else if (notifier instanceof CompositeNotifier) {
            builder.composite((CompositeNotifier) notifier);
        } else {
            throw new IllegalArgumentException("Unsupported Notifier type: " + notifier.getClass().getName());
        }
        return builder.build();
    }

    /**
     * Get the single {@link Notifier} underlying this union.
     *
     * @return The single contained {@link Notifier}
     */
    @JsonIgnore
    public Notifier getNotifier() {
        return NOTIFIERS.stream()
                .flatMap(f -> f.apply(this).stream())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Expected one notifier to be present"));
    }

    @Override
    public void notify(List<Diff> diffs) {
        getNotifier().notify(diffs);
    }

    public static Builder builder() {
        return Builder.builder();
    }

    /**
     * Builder class for {@link UnionNotifier}.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        @JsonCreator
        static Builder builder() {
            return new AutoValue_UnionNotifier.Builder();
        }

        @JsonProperty
        public abstract Builder stdOut(StdOutNotifier stdOut);
        @JsonProperty
        public abstract Builder slack(SlackNotifier slack);
        @JsonProperty
        public abstract Builder composite(CompositeNotifier composite);

        abstract UnionNotifier autoBuild();

        /**
         * Build the final {@link UnionNotifier}, asserting that exactly one component was set.
         * @return The built {@link UnionNotifier}
         * @throws IllegalStateException if zero or more than one component {@link Notifier}s were set
         */
        public UnionNotifier build() {
            UnionNotifier it = autoBuild();
            long count = NOTIFIERS.stream()
                    .map(f -> f.apply(it))
                    .filter(Optional::isPresent)
                    .count();
            checkState(count == 1, "Exactly one of stdOut, slack or composite must be set.  Found: %s", count);
            return it;
        }
    }
}
