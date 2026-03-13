package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyEventUseCase;
import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CopyEventService implements CopyEventUseCase {

    private final EventbritePort eventbritePort;

    @Override
    public String copyEvent(String sourceEventDate, LocalDate newDate, String place, boolean debug) {
        Optional<Event> sourceEventOpt;
        if ("latest".equalsIgnoreCase(sourceEventDate)) {
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
            throw new IllegalArgumentException("Source event not found for: " + sourceEventDate);
        }

        Event sourceEvent = sourceEventOpt.get();

        LocalTime startTime = sourceEvent.getStartTime().toLocalTime();
        LocalTime endTime = sourceEvent.getEndTime().toLocalTime();

        ZonedDateTime newStartTime = LocalDateTime.of(newDate, startTime).atZone(sourceEvent.getStartTime().getZone());
        ZonedDateTime newEndTime = LocalDateTime.of(newDate, endTime).atZone(sourceEvent.getEndTime().getZone());

        String suffix = (place == null || place.isBlank()) ? "bibliotheek Ninove" : place;
        String formattedNewDate = newDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String newTitle = String.format("CoderDojo Ninove - %s - %s", formattedNewDate, suffix);

        if (debug) {
            return String.format("DEBUG MODE: Would copy event '%s' (ID: %s) to new event '%s' on %s (from %s to %s)",
                    sourceEvent.getName(), sourceEvent.getId(), newTitle, newDate, newStartTime, newEndTime);
        } else {
            Event newEvent = eventbritePort.copyEvent(sourceEvent.getId(), newStartTime, newEndTime);
            newEvent = eventbritePort.updateEvent(newEvent.getId(), newTitle);
            return newEvent.getUrl();
        }
    }
}
