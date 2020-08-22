package com.robwettach.webdiplomacy.page;

import static com.google.common.base.Verify.verify;
import static java.lang.String.format;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Representation of a row in a {@link MembersTable}.
 */
@AutoValue
public abstract class MemberRow {
    private static final Splitter COMMA_SPLITTER = Splitter.on(",").trimResults();
    private static final String SUPPLY_CENTERS_KEY = "supplyCenters";
    private static final String UNITS_KEY = "units";

    private static final Pattern SUPPLY_CENTERS_UNITS_PATTERN = Pattern.compile(
            format("(?<%s>\\d+) supply-centers, (?<%s>\\d+) units", SUPPLY_CENTERS_KEY, UNITS_KEY));
    public static final String DEFEATED = "Defeated";
    public static final String NO_ORDERS = "-";

    public abstract String getCountryName();
    public abstract String getStatus();
    public abstract CountryUserLink getUser();
    public abstract int getSupplyCenterCount();
    public abstract int getUnitCount();
    public abstract ImmutableSet<String> getVotes();

    /**
     * Extract a {@link MemberRow} from an HTML {@link Element}.
     *
     * @param element The HTML {@link Element} containing the row
     * @return A {@link MemberRow} instance
     */
    public static MemberRow fromElement(Element element) {
        String countryName = element.select(".memberCountryName > span[class~=country\\d+]").first().text();
        Element statusImg = element.select("span[class*=\"StatusIcon\"] > img").first();
        String status = statusImg != null ? statusImg.attr("alt") : NO_ORDERS;

        CountryUserLink user = CountryUserLink.fromElement(element.select(".memberName").first());
        int supplyCenterCount = 0;
        int unitCount = 0;

        Elements memberStatus = element.select(".memberStatus > em");
        if (!memberStatus.isEmpty() && DEFEATED.equals(memberStatus.first().text())) {
            status = DEFEATED;
        } else {
            String scUnits = element.select(".memberSCCount").first().text();
            Matcher scUnitMatcher = SUPPLY_CENTERS_UNITS_PATTERN.matcher(scUnits);
            verify(scUnitMatcher.find(), "Failed to parse supply centers and units: %s", scUnits);
            supplyCenterCount = Integer.parseInt(scUnitMatcher.group(SUPPLY_CENTERS_KEY));
            unitCount = Integer.parseInt(scUnitMatcher.group(UNITS_KEY));
        }

        ImmutableSet<String> votes = ImmutableSet.of();
        Element memberVotes = element.select(".memberVotes").first();
        if (memberVotes != null) {
            votes = ImmutableSet.copyOf(COMMA_SPLITTER.splitToList(memberVotes.text()));
        }

        return new AutoValue_MemberRow(countryName, status, user, supplyCenterCount, unitCount, votes);
    }
}
