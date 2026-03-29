package be.coderdojo.ninove.coderdojo.application.service;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyEventUseCase;
import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CopyEventService implements CopyEventUseCase {

    private final EventbritePort eventbritePort;
    private static final ZoneId BRUSSELS_ZONE = ZoneId.of("Europe/Brussels");

    @Override
    public String copyEvent(String sourceEventDate, LocalDate newDate, String place, boolean debug) {
        log.debug("Processing copy event for sourceEventDate: {}, newDate: {}, place: {}, debug: {}",
                sourceEventDate, newDate, place, debug);
        Optional<Event> sourceEventOpt;
        if (LATEST.equalsIgnoreCase(sourceEventDate)) {
            sourceEventOpt = eventbritePort.findLatestPastEvent();
        } else {
            // Convert sourceEventDate from dd/MM/yyyy to yyyy-MM-dd if needed, or update port to handle it
            String formattedDate = sourceEventDate;
            try {
                LocalDate date = LocalDate.parse(sourceEventDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                // assume it's already in yyyy-MM-dd or let the port handle it
            }
            sourceEventOpt = eventbritePort.findEventByDate(formattedDate);
        }

        if (sourceEventOpt.isEmpty()) {
            log.error("Source event not found for: {}", sourceEventDate);
            throw new IllegalArgumentException("Source event not found for: " + sourceEventDate);
        }

        Event sourceEvent = sourceEventOpt.get();
        log.debug("Source event found: {} (ID: {})", sourceEvent.getName(), sourceEvent.getId());

        LocalTime startTime = LocalTime.of(9, 15);
        LocalTime endTime = LocalTime.of(12, 30);

        ZonedDateTime newStartTime = LocalDateTime.of(newDate, startTime).atZone(BRUSSELS_ZONE);
        ZonedDateTime newEndTime = LocalDateTime.of(newDate, endTime).atZone(BRUSSELS_ZONE);

        String suffix = (place == null || place.isBlank()) ? "bibliotheek Ninove" : place;
        String formattedNewDate = newDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String newTitle = String.format("CoderDojo Ninove - %s - %s", formattedNewDate, suffix);
        log.debug("New event title: {}", newTitle);

        if (debug) {
            log.info("DEBUG MODE: Skipping actual Eventbrite API calls.");
            return String.format("DEBUG MODE: Would copy event '%s' (ID: %s) to new event '%s' on %s (from %s to %s)",
                    sourceEvent.getName(), sourceEvent.getId(), newTitle, newDate, newStartTime, newEndTime);
        } else {
            log.info("Copying event ID {} to {}", sourceEvent.getId(), newStartTime);
            Event newEvent = eventbritePort.copyEvent(sourceEvent.getId(), newStartTime, newEndTime);
            log.info("Updating title for newly copied event ID {}", newEvent.getId());
            newEvent = eventbritePort.updateEvent(newEvent.getId(), newTitle, newStartTime,
                newEndTime);
            return newEvent.getUrl();
        }
    }
}
