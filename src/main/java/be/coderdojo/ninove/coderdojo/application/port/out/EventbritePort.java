package be.coderdojo.ninove.coderdojo.application.port.out;

import be.coderdojo.ninove.coderdojo.domain.model.Event;
import java.time.ZonedDateTime;
import java.util.Optional;

public interface EventbritePort {
    Optional<Event> findLatestPastEvent();
    Optional<Event> findEventByDate(String date);
    Event copyEvent(String eventId, ZonedDateTime newStartTime, ZonedDateTime newEndTime, String newTitle);
}
