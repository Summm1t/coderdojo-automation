package be.coderdojo.ninove.coderdojo.application.port.in;

import java.time.LocalDate;

public interface CopyEventUseCase {
    String copyEvent(String sourceEventDate, LocalDate newDate, String place, boolean debug);
}
