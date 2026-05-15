package be.coderdojo.ninove.coderdojo.application.service;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.INPUT_DATE_FORMAT;
import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;

import be.coderdojo.ninove.coderdojo.application.port.in.TransferAttendeesUseCase;
import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.application.port.out.MailerLitePort;
import be.coderdojo.ninove.coderdojo.domain.model.Attendee;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferAttendeesService implements TransferAttendeesUseCase {

    private final EventbritePort eventbritePort;
    private final MailerLitePort mailerLitePort;

    @Override
    public String transferAttendees(String eventDate, boolean debug) {
        log.debug("Processing attendee transfer for eventDate: {}, debug: {}", eventDate, debug);

        Optional<Event> eventOpt;
        if (LATEST.equalsIgnoreCase(eventDate)) {
            eventOpt = eventbritePort.findLatestPastEvent();
        } else {
            eventOpt = eventbritePort.findEventByDate(eventDate);
        }

        if (eventOpt.isEmpty()) {
            log.error("Event not found for: {}", eventDate);
            return "Event not found for: " + eventDate;
        }

        Event event = eventOpt.get();
        log.info("Found event: {} (ID: {})", event.getName(), event.getId());

        List<Attendee> eventbriteAttendees = eventbritePort.getAttendees(event.getId());
        log.info("Found {} attendees in Eventbrite for event {}", eventbriteAttendees.size(), event.getName());

        int created = 0;
        int updated = 0;
        int optedOut = 0;

        for (Attendee ebAttendee : eventbriteAttendees) {
            Optional<Attendee> mlAttendeeOpt = mailerLitePort.findSubscriberByEmail(ebAttendee.getEmail());

            if (mlAttendeeOpt.isPresent()) {
                Attendee mlAttendee = mlAttendeeOpt.get();
                log.debug("Attendee {} already exists in MailerLite", ebAttendee.getEmail());

                // Rule: If attendee does not want to receive emails, check if they opted out in MailerLite.
                if (!ebAttendee.isOptIn()) {
                    if (mlAttendee.isOptIn()) {
                        log.info("Opting out subscriber: {}", ebAttendee.getEmail());
                        mailerLitePort.optOutSubscriber(ebAttendee.getEmail(), debug);
                        optedOut++;
                    }
                } else {
                    // Rule: If more information in Eventbrite, add extra info.
                    // We check if name, last name or ticket class is different.
                    boolean needsUpdate = !ebAttendee.getFirstName().equals(mlAttendee.getFirstName()) ||
                            !ebAttendee.getLastName().equals(mlAttendee.getLastName()) ||
                            (ebAttendee.getTicketType() != mlAttendee.getTicketType());

                    if (needsUpdate) {
                        log.info("Updating subscriber info: {}", ebAttendee.getEmail());
                        mailerLitePort.updateSubscriber(ebAttendee, debug);
                        updated++;
                    }
                }
            } else {
                log.info("Adding new subscriber to MailerLite: {}", ebAttendee.getEmail());
                mailerLitePort.createSubscriber(ebAttendee, debug);
                created++;

                if (!ebAttendee.isOptIn()) {
                    log.info("Opting out new subscriber: {}", ebAttendee.getEmail());
                    mailerLitePort.optOutSubscriber(ebAttendee.getEmail(), debug);
                    optedOut++;
                }
            }
        }

        return String.format("Transfer completed for event '%s'. Processed %d attendees: %d created, %d updated, %d opted out.",
                event.getName(), eventbriteAttendees.size(), created, updated, optedOut);
    }
}
