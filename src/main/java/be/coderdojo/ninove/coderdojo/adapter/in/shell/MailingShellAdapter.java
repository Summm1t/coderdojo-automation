package be.coderdojo.ninove.coderdojo.adapter.in.shell;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyMailingUseCase;
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

    @ShellMethod(key = "copy-mailing", value = "Copy the latest mailing campaign and update its content.")
    public String copyMailing(
            @ShellOption(value = "title", help = "Title of the newsletter") String title,
            @ShellOption(value = "date", help = "New event date (e.g. 21 maart)") String date,
            @ShellOption(value = "link", help = "Eventbrite link") String link,
            @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying MailerLite") boolean debug
    ) {
        log.debug("Received request to copy mailing: title={}, date={}, link={}, debug={}", title, date, link, debug);
        return copyMailingUseCase.copyMailing(title, date, link, debug);
    }
}
