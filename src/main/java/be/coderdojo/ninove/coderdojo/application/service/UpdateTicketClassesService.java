package be.coderdojo.ninove.coderdojo.application.service;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;

import be.coderdojo.ninove.coderdojo.application.port.in.UpdateTicketClassesUseCase;
import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import be.coderdojo.ninove.coderdojo.domain.model.TicketClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateTicketClassesService implements UpdateTicketClassesUseCase {

    public static final String DEELNEMER = "Deelnemer";
    public static final String VRIJWILLIGER = "Vrijwilliger";
    public static final String KIND_VAN_VRIJWILLIGER = "Kind van vrijwilliger";
    public static final String MET_UITNODIGING = "Met uitnodiging";
    private final EventbritePort eventbritePort;

    @Override
    public List<TicketClass> setParticipants(String eventDate, Map<String, Integer> capacities, boolean debug) {
        log.info("Setting participants for event on date {} with capacities {}. Debug: {}", eventDate, capacities, debug);

        Optional<Event> eventOptional;
        if (LATEST.equalsIgnoreCase(eventDate)) {
            eventOptional = eventbritePort.findLatestPastEvent();
        } else {
            eventOptional = eventbritePort.findEventByDate(eventDate);
        }

        if (eventOptional.isEmpty()) {
            throw new IllegalArgumentException("No event found for date: " + eventDate);
        }

        Event event = eventOptional.get();
        log.info("Found event: {} (ID: {})", event.getName(), event.getId());

        List<TicketClass> ticketClasses = eventbritePort.getTicketClasses(event.getId());
        List<TicketClass> updatedTicketClasses = new ArrayList<>();

        for (TicketClass ticketClass : ticketClasses) {
            Integer newCapacity = null;

            if (DEELNEMER.equalsIgnoreCase(ticketClass.getName())) {
                newCapacity = capacities.get("deelnemers");
            } else if (VRIJWILLIGER.equalsIgnoreCase(ticketClass.getName())) {
                newCapacity = capacities.get("vrijwilligers");
            } else if (KIND_VAN_VRIJWILLIGER.equalsIgnoreCase(ticketClass.getName())) {
                newCapacity = capacities.get("kind-van-vrijwilliger");
            } else if (MET_UITNODIGING.equalsIgnoreCase(ticketClass.getName())) {
                newCapacity = capacities.get("met-uitnodiging");
            }

            if (newCapacity != null) {
                log.info("Updating ticket class '{}' (ID: {}) to capacity {}", ticketClass.getName(), ticketClass.getId(), newCapacity);
                if (!debug) {
                    updatedTicketClasses.add(eventbritePort.updateTicketClass(event.getId(), ticketClass.getId(), newCapacity, newCapacity));
                } else {
                    log.info("[DEBUG] Would update ticket class '{}' to capacity {}", ticketClass.getName(), newCapacity);
                    updatedTicketClasses.add(TicketClass.builder()
                            .id(ticketClass.getId())
                            .name(ticketClass.getName())
                            .capacity(newCapacity)
                            .quantityTotal(newCapacity)
                            .build());
                }
            } else {
                log.warn("Ticket class '{}' (ID: {}) did not match any criteria, skipping.", ticketClass.getName(), ticketClass.getId());
            }
        }

        return updatedTicketClasses;
    }
}
