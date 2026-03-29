package be.coderdojo.ninove.coderdojo.adapter.in.shell;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.INPUT_DATE_FORMAT;
import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyMailingUseCase;
import be.coderdojo.ninove.coderdojo.application.port.in.TransferAttendeesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class MailingShellAdapter {

    private final CopyMailingUseCase copyMailingUseCase;
    private final TransferAttendeesUseCase transferAttendeesUseCase;

    @ShellMethod(key = "copy-mailing", value = "Copy an existing campaign and update its content.")
    public String copyMailing(
            @ShellOption(value = {"original-campaign-title"}, defaultValue = LATEST, help = "Title of the existing campaign to copy, or 'latest'") String originalCampaignTitle,
            @ShellOption(value = "date", help = "Date of the new campaign (" + INPUT_DATE_FORMAT + ")") String date,
            @ShellOption(value = "link", help = "Eventbrite registration link for the new campaign") String link,
            @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying MailerLite") boolean debug
    ) {
        log.debug("Received request to copy mailing: originalCampaignTitle={}, date={}, link={}, debug={}", originalCampaignTitle, date, link, debug);
        return copyMailingUseCase.copyMailing(originalCampaignTitle, date, link, debug);
    }

    @ShellMethod(key = "transfer-attendees", value = "Transfer attendees from Eventbrite to MailerLite.")
    public String transferAttendees(
            @ShellOption(value = "event", defaultValue = LATEST, help = "Date of the event (" + INPUT_DATE_FORMAT + ") or 'latest'") String event,
            @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying MailerLite") boolean debug
    ) {
        log.debug("Received request to transfer attendees: event={}, debug={}", event, debug);
        return transferAttendeesUseCase.transferAttendees(event, debug);
    }
}
