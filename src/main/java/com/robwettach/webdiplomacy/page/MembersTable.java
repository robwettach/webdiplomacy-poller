package com.robwettach.webdiplomacy.page;

import com.google.common.base.Splitter;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.UserInfo;
import com.robwettach.webdiplomacy.model.Vote;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Verify.verify;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class MembersTable {
    private static final Splitter COMMA_SPLITTER = Splitter.on(",").trimResults();
    private static final String SUPPLY_CENTERS_KEY = "supplyCenters";
    private static final String UNITS_KEY = "units";

    private static final Pattern SUPPLY_CENTERS_UNITS_PATTERN = Pattern.compile(
            format("(?<%s>\\d+) supply-centers, (?<%s>\\d+) units", SUPPLY_CENTERS_KEY, UNITS_KEY));

    private final WebDiplomacyPage page;
    private final Element membersTable;

    public MembersTable(WebDiplomacyPage page, Element parentNode) {
        this.page = page;
        this.membersTable = parentNode.select(".membersList.membersFullTable").first();
    }

    public Set<CountryState> getCountries() {
        if (membersTable == null) {
            return Collections.emptySet();
        }
        Elements members = membersTable.select(".member");
        return members.stream().map(this::parseCountry).collect(toSet());
    }

    private CountryState parseCountry(Element countryElement) {
        CountryState.Builder countryBuilder = CountryState.builder();
        countryBuilder.countryName(countryElement.select(".memberCountryName > span[class~=country\\d+]").first().text());
        Element statusImg = countryElement.select("span[class*=\"StatusIcon\"] > img").first();
        countryBuilder.status(statusImg != null ? CountryStatus.fromString(statusImg.attr("alt")) : CountryStatus.NoOrders);

        UserInfo countryPlayer = parseCountryUser(countryElement);
        countryBuilder.user(countryPlayer);

        Elements memberStatus = countryElement.select(".memberStatus > em");
        if (!memberStatus.isEmpty() && "Defeated".equals(memberStatus.first().text())) {
            countryBuilder.status(CountryStatus.Defeated);
        } else {
            String scUnits = countryElement.select(".memberSCCount").first().text();
            Matcher scUnitMatcher = SUPPLY_CENTERS_UNITS_PATTERN.matcher(scUnits);
            verify(scUnitMatcher.find(), "Failed to parse supply centers and units: %s", scUnits);
            countryBuilder.supplyCenterCount(Integer.parseInt(scUnitMatcher.group(SUPPLY_CENTERS_KEY)));
            countryBuilder.unitCount(Integer.parseInt(scUnitMatcher.group(UNITS_KEY)));
        }

        return countryBuilder.build();
    }

    private UserInfo parseCountryUser(Element countryElement) {
        Element userA = countryElement.select(".memberName > a").first();
        return UserInfo.create(userA.text(), page.getUserIdFromATag(userA));
    }
}
