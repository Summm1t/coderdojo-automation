package be.coderdojo.ninove.coderdojo.application.port.in;

import java.time.LocalDate;

public interface CopyEventUseCase {
    String copyEvent(String sourceEventDate, String newDate, String place, boolean debug);
}
