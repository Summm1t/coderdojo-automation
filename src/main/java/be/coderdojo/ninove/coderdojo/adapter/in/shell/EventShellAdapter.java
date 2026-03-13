package be.coderdojo.ninove.coderdojo.adapter.in.shell;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyEventUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ShellComponent
@RequiredArgsConstructor
public class EventShellAdapter {

    private final CopyEventUseCase copyEventUseCase;

    @ShellMethod(key = "copy-event", value = "Copy an existing event to a new date.")
    public String copyEvent(
            @ShellOption(value = "source-event", help = "Date of the event to copy (dd/MM/yyyy) or 'latest'") String sourceEvent,
            @ShellOption(value = "date", help = "New event date (dd/MM/yyyy)") String date,
            @ShellOption(value = "title", help = "New event title") String title,
            @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying Eventbrite") boolean debug
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date, formatter);
        } catch (java.time.format.DateTimeParseException e) {
            return "Invalid date format. Please use 'dd/MM/yyyy' (e.m. 21/03/2026).";
        }

        return copyEventUseCase.copyEvent(sourceEvent, localDate, title, debug);
    }
}
