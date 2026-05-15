package be.coderdojo.ninove.coderdojo.adapter.in.shell;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.INPUT_DATE_FORMAT;
import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyEventUseCase;
import be.coderdojo.ninove.coderdojo.application.port.in.UpdateTicketClassesUseCase;
import be.coderdojo.ninove.coderdojo.domain.model.TicketClass;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class EventShellAdapter {

  public static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern(
      INPUT_DATE_FORMAT);
  private final CopyEventUseCase copyEventUseCase;
  private final UpdateTicketClassesUseCase updateTicketClassesUseCase;

  @ShellMethod(key = "copy-event", value = "Copy an existing event to a new newDate.")
  public String copyEvent(
      @ShellOption(value = "source-event", help = "Date of the event to copy (" + INPUT_DATE_FORMAT
          + ") or '" + LATEST + "'") String sourceEvent,
      @ShellOption(value = "newDate", help = "New event newDate (" + INPUT_DATE_FORMAT
          + ")") String newDate,
      @ShellOption(value = "place", defaultValue = ShellOption.NULL, help = "Place for the new event") String place,
      @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying Eventbrite") boolean debug
  ) {
    log.debug("Received request to copy event: sourceEvent={}, newDate={}, place={}, debug={}",
        sourceEvent, newDate, place, debug);

    return copyEventUseCase.copyEvent(sourceEvent, newDate, place, debug);
  }

  @ShellMethod(key = "set-participants", value = "Set capacity and quantity_total for each ticket class.")
  public String setParticipants(
      @ShellOption(value = "event", defaultValue = LATEST, help = "Date of the event ("
          + INPUT_DATE_FORMAT + ") or 'latest'") String eventDate,
      @ShellOption(value = "deelnemers", defaultValue = "20", help = "Capacity for 'Deelnemers'") int deelnemers,
      @ShellOption(value = "vrijwilligers", defaultValue = "15", help = "Capacity for 'Vrijwilligers'") int vrijwilligers,
      @ShellOption(value = "kind-van-vrijwilliger", defaultValue = "10", help = "Capacity for 'Kind van Vrijwilliger'") int kindVanVrijwilliger,
      @ShellOption(value = "met-uitnodiging", defaultValue = "5", help = "Capacity for 'Met Uitnodiging'") int metUitnodiging,
      @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying Eventbrite") boolean debug
  ) {
    log.debug(
        "Received request to set participants: event={}, deelnemers={}, vrijwilligers={}, kind={}, uitnodiging={}, debug={}",
        eventDate, deelnemers, vrijwilligers, kindVanVrijwilliger, metUitnodiging, debug);

    List<TicketClass> updated = updateTicketClassesUseCase.setParticipants(eventDate, deelnemers,
        vrijwilligers, kindVanVrijwilliger, metUitnodiging, debug);

    if (updated.isEmpty()) {
      return "No ticket classes were updated.";
    }

    return "Successfully updated ticket classes:\n" +
        updated.stream()
            .map(tc -> String.format("- %s: %d", tc.getName(), tc.getCapacity()))
            .collect(Collectors.joining("\n"));
  }
}
