package be.coderdojo.ninove.coderdojo.adapter.in.shell;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyEventUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ShellComponent
@RequiredArgsConstructor
public class EventShellAdapter {

    private final CopyEventUseCase copyEventUseCase;

    @ShellMethod(key = "copy-event", value = "Copy an existing event to a new date.")
    public String copyEvent(
            @ShellOption(value = "source-event", help = "Date of the event to copy or 'latest'") String sourceEvent,
            @ShellOption(value = "date", help = "New event date (yyyy-MM-dd HH:mm)") String date,
            @ShellOption(value = "title", help = "New event title") String title,
            @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying Eventbrite") boolean debug
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ZonedDateTime zonedDateTime;
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);
            zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        } catch (java.time.format.DateTimeParseException e) {
            return "Invalid date format. Please use 'yyyy-MM-dd HH:mm' (e.m. 2026-03-21 13:30).";
        }

        return copyEventUseCase.copyEvent(sourceEvent, zonedDateTime, title, debug);
    }
}
