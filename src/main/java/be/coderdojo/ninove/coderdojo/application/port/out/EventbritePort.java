package be.coderdojo.ninove.coderdojo.application.port.out;

import be.coderdojo.ninove.coderdojo.domain.model.Event;
import be.coderdojo.ninove.coderdojo.domain.model.TicketClass;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface EventbritePort {
    Optional<Event> findLatestPastEvent();
    Optional<Event> findEventByDate(String date);
    Event copyEvent(String eventId, ZonedDateTime newStartTime, ZonedDateTime newEndTime);
    Event updateEvent(String eventId, String newTitle, ZonedDateTime newStartTime, ZonedDateTime newEndTime);
    List<TicketClass> getTicketClasses(String eventId);
    TicketClass updateTicketClass(String eventId, String ticketClassId, int capacity, int quantityTotal);
}
