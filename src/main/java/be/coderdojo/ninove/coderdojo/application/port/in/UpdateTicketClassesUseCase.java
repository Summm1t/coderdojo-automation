package be.coderdojo.ninove.coderdojo.application.port.in;

import be.coderdojo.ninove.coderdojo.domain.model.TicketClass;
import java.util.List;
import java.util.Map;

public interface UpdateTicketClassesUseCase {
    List<TicketClass> setParticipants(String eventDate, Map<String, Integer> capacities, boolean debug);
}
