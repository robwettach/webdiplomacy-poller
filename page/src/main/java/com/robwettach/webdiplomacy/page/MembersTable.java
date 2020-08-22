package com.robwettach.webdiplomacy.page;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Representation of the countries table in a <em>webDiplomacy</em> game.
 */
@AutoValue
public abstract class MembersTable {
    public abstract ImmutableSet<MemberRow> getRows();

    /**
     * Extract a {@link MembersTable} from a parent HTML {@link Element}.
     *
     * <p>The {@code parent} element is expected to contain a
     * {@code div} with class {@code membersList membersFullTable}.
     *
     * @param parent The parent HTML {@link Element} containing the members table
     * @return A {@link MembersTable} instance
     */
    public static MembersTable fromParent(Element parent) {
        return fromElement(parent.select(".membersList.membersFullTable").first());
    }

    /**
     * Extract a {@link MembersTable} from an HTML {@link Element}.
     *
     * @param element The HTML {@link Element} containing the members table
     * @return A {@link MembersTable} instance
     */
    public static MembersTable fromElement(Element element) {
        Elements members = element.select(".member");
        ImmutableSet<MemberRow> rows = members.stream()
                .map(MemberRow::fromElement)
                .collect(toImmutableSet());
        return new AutoValue_MembersTable(rows);
    }
}
