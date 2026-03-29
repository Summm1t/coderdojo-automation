package be.coderdojo.ninove.coderdojo.adapter.in.shell;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyEventUseCase;
import be.coderdojo.ninove.coderdojo.application.port.in.UpdateTicketClassesUseCase;
import be.coderdojo.ninove.coderdojo.domain.model.TicketClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class EventShellAdapter {

    private final CopyEventUseCase copyEventUseCase;
    private final UpdateTicketClassesUseCase updateTicketClassesUseCase;

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

    @ShellMethod(key = "set-participants", value = "Set capacity and quantity_total for each ticket class.")
    public String setParticipants(
            @ShellOption(value = "event", defaultValue = "latest", help = "Date of the event (dd/MM/yyyy) or 'latest'") String eventDate,
            @ShellOption(value = "deelnemers", defaultValue = "20", help = "Capacity for 'Deelnemers'") int deelnemers,
            @ShellOption(value = "vrijwilligers", defaultValue = "15", help = "Capacity for 'Vrijwilligers'") int vrijwilligers,
            @ShellOption(value = "kind-van-vrijwilliger", defaultValue = "10", help = "Capacity for 'Kind van Vrijwilliger'") int kindVanVrijwilliger,
            @ShellOption(value = "met-uitnodiging", defaultValue = "5", help = "Capacity for 'Met Uitnodiging'") int metUitnodiging,
            @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying Eventbrite") boolean debug
    ) {
        log.debug("Received request to set participants: event={}, deelnemers={}, vrijwilligers={}, kind={}, uitnodiging={}, debug={}",
                eventDate, deelnemers, vrijwilligers, kindVanVrijwilliger, metUitnodiging, debug);

        Map<String, Integer> capacities = Map.of(
                "deelnemers", deelnemers,
                "vrijwilligers", vrijwilligers,
                "kind-van-vrijwilliger", kindVanVrijwilliger,
                "met-uitnodiging", metUitnodiging
        );

        String dateToUse = eventDate;
        if (!"latest".equalsIgnoreCase(eventDate)) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
            try {
                dateToUse = LocalDate.parse(eventDate, inputFormatter).format(outputFormatter);
            } catch (java.time.format.DateTimeParseException e) {
                return "Invalid date format. Please use 'dd/MM/yyyy' (e.g. 21/03/2026).";
            }
        }

        List<TicketClass> updated = updateTicketClassesUseCase.setParticipants(dateToUse, capacities, debug);

        if (updated.isEmpty()) {
            return "No ticket classes were updated.";
        }

        return "Successfully updated ticket classes:\n" +
                updated.stream()
                        .map(tc -> String.format("- %s: %d", tc.getName(), tc.getCapacity()))
                        .collect(Collectors.joining("\n"));
    }
}
