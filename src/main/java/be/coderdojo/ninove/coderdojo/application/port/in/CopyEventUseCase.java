package be.coderdojo.ninove.coderdojo.application.port.in;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public interface CopyEventUseCase {
    String copyEvent(String sourceEventDate, LocalDate newDate, String newTitle, boolean debug);
}
