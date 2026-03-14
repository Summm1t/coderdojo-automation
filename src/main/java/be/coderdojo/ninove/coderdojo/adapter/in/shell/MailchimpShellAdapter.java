package be.coderdojo.ninove.coderdojo.adapter.in.shell;

import be.coderdojo.ninove.coderdojo.application.port.in.UnsubscribeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class MailchimpShellAdapter {

    private final UnsubscribeUseCase unsubscribeUseCase;

    @ShellMethod(key = "unsubscribe-mailchimp", value = "Tag and unsubscribe a list of comma-separated email addresses from Mailchimp.")
    public String unsubscribeMailchimp(
            @ShellOption(help = "Comma-separated list of email addresses") String emails,
            @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying Mailchimp") boolean debug
    ) {
        log.debug("Received request to unsubscribe emails: {}, debug: {}", emails, debug);
        List<String> emailList = Arrays.stream(emails.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toList());

        if (emailList.isEmpty()) {
            return "No valid email addresses provided.";
        }

        try {
            unsubscribeUseCase.unsubscribe(emailList, debug);
            return (debug ? "[DEBUG MODE] " : "") + "Unsubscription process completed for " + emailList.size() + " email(s). Check logs for details.";
        } catch (Exception e) {
            log.error("Error during unsubscription process", e);
            return "An error occurred: " + e.getMessage();
        }
    }
}
