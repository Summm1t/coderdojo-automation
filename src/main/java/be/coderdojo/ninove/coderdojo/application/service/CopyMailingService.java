package be.coderdojo.ninove.coderdojo.application.service;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyMailingUseCase;
import be.coderdojo.ninove.coderdojo.application.port.out.MailerLitePort;
import be.coderdojo.ninove.coderdojo.domain.model.Campaign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CopyMailingService implements CopyMailingUseCase {


    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("nl"));

    private final MailerLitePort mailerLitePort;

    @Override
    public String copyMailing(String originalCampaignTitle, String newDate, String eventbriteLink, boolean debug) {
        log.debug("Copying mailing campaign: originalCampaignTitle={}, newDate={}, eventbriteLink={}, debug={}",
                originalCampaignTitle, newDate, eventbriteLink, debug);

        Optional<Campaign> originalCampaignOpt;
        if (LATEST.equalsIgnoreCase(originalCampaignTitle)) {
            originalCampaignOpt = mailerLitePort.findLatestCampaign();
        } else {
            originalCampaignOpt = mailerLitePort.findCampaignByTitle(originalCampaignTitle);
        }

        if (originalCampaignOpt.isEmpty()) {
            return "No original campaign found with title: " + originalCampaignTitle;
        }

        Campaign original = originalCampaignOpt.get();
        // Get details to ensure we have the content
        Optional<Campaign> detailsOpt = mailerLitePort.getCampaignDetails(original.getId());
        if (detailsOpt.isEmpty()) {
            return "Could not fetch details for the campaign: " + original.getId();
        }

        Campaign details = detailsOpt.get();
        String oldContent = details.getContent();

        LocalDate date = LocalDate.parse(newDate, INPUT_FORMATTER);
        String formattedDate = OUTPUT_FORMATTER.format(date);

        String originalTitle = details.getTitle();
        // Title transformation: replace the old month year with new month year
        // Example: "Coderdojo Ninove Nieuwsbrief april 2026" becomes "Coderdojo Ninove Nieuwsbrief mei 2026"
        // We look for any month (in Dutch) followed by 4 digits.
        String newTitle = updateTitle(originalTitle, formattedDate);

        // Update the Eventbrite registration link in the content
        // An Eventbrite registration link is in the format https://www.eventbrite.com/e/.*
        String newContent = updateContentLink(oldContent, eventbriteLink);

        // Update the subject line
        String newSubject = updateTitle(details.getSubject(), formattedDate);

        Campaign created = mailerLitePort.createCampaign(newTitle, newContent, details.getFromName(), details.getFromEmail(), newSubject,
                details.getGroups(), details.getSegments(), details.getLanguageId(), details.getSettings(), debug);
        if (debug) {
            return "DEBUG MODE: New mailing campaign would be:\n" +
                    "Title: " + newTitle + "\n" +
                    "Content: " + newContent.replaceAll("\n", "");
        }
        return "New mailing campaign created: " + created.getTitle() + " (ID: " + created.getId() + ")";
    }

    private String updateTitle(String originalTitle, String newMonthYear) {
        if (originalTitle == null) {
            return newMonthYear;
        }
        // This regex tries to find "Month YYYY" at the end of the title
        // In Dutch: januari, februari, maart, april, mei, juni, juli, augustus, september, oktober, november, december
        String monthRegex = "(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december)\\s+\\d{4}";
        Pattern pattern = Pattern.compile(monthRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(originalTitle);

        if (matcher.find()) {
            return matcher.replaceFirst(newMonthYear);
        } else {
            log.warn("Could not find date pattern in title: {}. Appending new date.", originalTitle);
            return originalTitle + " " + newMonthYear;
        }
    }

    private String updateContentLink(String content, String newLink) {
        String linkPatternStr = "https://www\\.eventbrite\\.[a-z.]+/e/[\\w-]+";
        Pattern linkPattern = Pattern.compile(linkPatternStr);
        Matcher linkMatcher = linkPattern.matcher(content);

        if (linkMatcher.find()) {
            return linkMatcher.replaceAll(newLink);
        } else {
            log.warn("Eventbrite link pattern not found in campaign content.");
            return content;
        }
    }
}
