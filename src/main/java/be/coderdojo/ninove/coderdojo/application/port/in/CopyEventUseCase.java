package be.coderdojo.ninove.coderdojo.application.port.in;

import java.time.ZonedDateTime;

public interface CopyEventUseCase {
    String copyEvent(String sourceEventDate, ZonedDateTime newDate, String newTitle, boolean debug);
}
