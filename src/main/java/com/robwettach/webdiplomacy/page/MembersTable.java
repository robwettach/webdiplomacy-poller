package com.robwettach.webdiplomacy.page;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@AutoValue
public abstract class MembersTable {
    public abstract ImmutableSet<MemberRow> getRows();

    public static MembersTable fromElement(Element element) {
        Elements members = element.select(".member");
        ImmutableSet<MemberRow> rows = members.stream()
                .map(MemberRow::fromElement)
                .collect(toImmutableSet());
        return new AutoValue_MembersTable(rows);
    }

    public static MembersTable fromParent(Element parent) {
        return fromElement(parent.select(".membersList.membersFullTable").first());
    }
}
