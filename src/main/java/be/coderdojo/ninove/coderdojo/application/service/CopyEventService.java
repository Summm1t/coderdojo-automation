package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyEventUseCase;
import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CopyEventService implements CopyEventUseCase {

    private final EventbritePort eventbritePort;

    @Override
    public String copyEvent(String sourceEventDate, ZonedDateTime newDate, String newTitle, boolean debug) {
        Optional<Event> sourceEventOpt;
        if ("latest".equalsIgnoreCase(sourceEventDate)) {
            sourceEventOpt = eventbritePort.findLatestPastEvent();
        } else {
            sourceEventOpt = eventbritePort.findEventByDate(sourceEventDate);
        }

        if (sourceEventOpt.isEmpty()) {
            throw new IllegalArgumentException("Source event not found for: " + sourceEventDate);
        }

        Event sourceEvent = sourceEventOpt.get();

        if (debug) {
            return String.format("DEBUG MODE: Would copy event '%s' (ID: %s) to new event '%s' on %s",
                    sourceEvent.getName(), sourceEvent.getId(), newTitle, newDate);
        } else {
            Event newEvent = eventbritePort.copyEvent(sourceEvent.getId(), newDate, newTitle);
            return newEvent.getUrl();
        }
    }
}
