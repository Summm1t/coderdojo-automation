package be.coderdojo.ninove.coderdojo.adapter.in.shell;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class EventShellAdapter {

    private final CopyEventUseCase copyEventUseCase;

    @ShellMethod(key = "copy-event", value = "Copy an existing event to a new date.")
    public String copyEvent(
            @ShellOption(value = "source-event", help = "Date of the event to copy (dd/MM/yyyy) or 'latest'") String sourceEvent,
            @ShellOption(value = "date", help = "New event date (dd/MM/yyyy)") String date,
            @ShellOption(value = "place", defaultValue = ShellOption.NULL, help = "Place for the new event") String place,
            @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying Eventbrite") boolean debug
    ) {
        log.debug("Received request to copy event: sourceEvent={}, date={}, place={}, debug={}", sourceEvent, date, place, debug);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date, formatter);
        } catch (java.time.format.DateTimeParseException e) {
            return "Invalid date format. Please use 'dd/MM/yyyy' (e.m. 21/03/2026).";
        }

        return copyEventUseCase.copyEvent(sourceEvent, localDate, place, debug);
    }
}
