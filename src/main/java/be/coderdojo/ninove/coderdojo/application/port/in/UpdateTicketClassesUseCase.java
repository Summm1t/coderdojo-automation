package be.coderdojo.ninove.coderdojo.application.port.in;

import be.coderdojo.ninove.coderdojo.domain.model.TicketClass;
import java.util.List;

public interface UpdateTicketClassesUseCase {

  List<TicketClass> setParticipants(String eventDate, int deelnemers, int vrijwilligers,
      int kindVanVrijwilliger, int metUitnodiging, boolean debug);
}
