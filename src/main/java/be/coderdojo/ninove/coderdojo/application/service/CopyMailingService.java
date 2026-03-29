package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyMailingUseCase;
import be.coderdojo.ninove.coderdojo.application.port.out.MailerLitePort;
import be.coderdojo.ninove.coderdojo.domain.model.Campaign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CopyMailingService implements CopyMailingUseCase {

    private final MailerLitePort mailerLitePort;

    @Override
    public String copyMailing(String titleArgument, String newDateArgument, String eventBriteLinkArgument, boolean debug) {
        log.debug("Copying mailing campaign: titleArgument={}, newDateArgument={}, eventBriteLinkArgument={}, debug={}",
                titleArgument, newDateArgument, eventBriteLinkArgument, debug);

        Optional<Campaign> latestCampaignOpt = mailerLitePort.findLatestCampaign();
        if (latestCampaignOpt.isEmpty()) {
            return "No campaigns found in MailerLite.";
        }

        Campaign latest = latestCampaignOpt.get();
        // Get details to ensure we have the content
        Optional<Campaign> detailsOpt = mailerLitePort.getCampaignDetails(latest.getId());
        if (detailsOpt.isEmpty()) {
            return "Could not fetch details for the latest campaign: " + latest.getId();
        }

        Campaign details = detailsOpt.get();
        String oldContent = details.getContent();

        String newTitle = "Coderdojo Ninove Nieuwsbrief " + titleArgument;

        // 1. Change the date in the content
        // Pattern: "De volgende Coderdojo gaat door op zondag {date} in de vernieuwe bibliotheek van Ninove, inschrijven kan hier"
        String datePatternStr = "De volgende Coderdojo gaat door op zondag (.*?) in";
        Pattern datePattern = Pattern.compile(datePatternStr);
        Matcher dateMatcher = datePattern.matcher(oldContent);

        String newContent;
        if (dateMatcher.find()) {
            newContent = dateMatcher.replaceFirst("De volgende Coderdojo gaat door op zondag " + newDateArgument + " in");
        } else {
            log.warn("Date pattern not found in campaign content. Using old content as base.");
            newContent = oldContent;
        }

        // 2. Change the link
        // Pattern: "https://www.eventbrite.com/e/registratie-coderdojo-ninove-..."
        String linkPatternStr = "https://www.eventbrite.com/e/registratie-coderdojo-ninove-[\\w-]+";
        Pattern linkPattern = Pattern.compile(linkPatternStr);
        Matcher linkMatcher = linkPattern.matcher(newContent);

        if (linkMatcher.find()) {
            newContent = linkMatcher.replaceAll(eventBriteLinkArgument);
        } else {
            log.warn("Eventbrite link pattern not found in campaign content.");
        }

        Campaign created = mailerLitePort.createCampaign(newTitle, newContent, debug);
        if (debug) {
            return "DEBUG MODE: New mailing campaign would be:\n" +
                    "Title: " + newTitle + "\n" +
                    "Content: " + newContent;
        }
        return "New mailing campaign created: " + created.getTitle() + " (ID: " + created.getId() + ")";
    }
}
