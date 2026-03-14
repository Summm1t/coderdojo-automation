package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.in.UnsubscribeUseCase;
import be.coderdojo.ninove.coderdojo.application.port.out.MailchimpPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnsubscribeService implements UnsubscribeUseCase {

    private final MailchimpPort mailchimpPort;

    @Override
    public void unsubscribe(List<String> emails, boolean debug) {
        log.info("Starting manual unsubscription process for {} emails (debug: {})", emails.size(), debug);
        for (String email : emails) {
            try {
                log.info("Processing unsubscription for: {} (debug: {})", email, debug);
                boolean tagged = mailchimpPort.tagUser(email, List.of("Removed_manually"), debug);
                if (tagged) {
                    boolean unsubscribed = mailchimpPort.unsubscribeUser(email, debug);
                    if (unsubscribed) {
                        log.info("Successfully processed: {}", email);
                    } else {
                        log.warn("Failed to unsubscribe {}: User not found during unsubscribe call", email);
                    }
                } else {
                    log.warn("Skipping unsubscription for {}: User not found in Mailchimp", email);
                }
            } catch (Exception e) {
                log.error("Failed to unsubscribe {}: {}", email, e.getMessage());
            }
        }
    }
}
