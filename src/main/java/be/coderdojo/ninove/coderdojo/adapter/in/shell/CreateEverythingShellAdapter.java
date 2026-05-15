package be.coderdojo.ninove.coderdojo.adapter.in.shell;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.INPUT_DATE_FORMAT;
import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;

import be.coderdojo.ninove.coderdojo.application.port.in.CopyEventUseCase;
import be.coderdojo.ninove.coderdojo.application.port.in.CopyMailingUseCase;
import be.coderdojo.ninove.coderdojo.application.port.in.TransferAttendeesUseCase;
import be.coderdojo.ninove.coderdojo.application.port.in.UpdateTicketClassesUseCase;
import be.coderdojo.ninove.coderdojo.domain.model.TicketClass;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class CreateEverythingShellAdapter {

  private final CopyEventUseCase copyEventUseCase;
  private final UpdateTicketClassesUseCase updateTicketClassesUseCase;
  private final CopyMailingUseCase copyMailingUseCase;
  private final TransferAttendeesUseCase transferAttendeesUseCase;

  @ShellMethod(key = "create-everything", value = "Create all things for a new event: copy-event, set-participants, copy-mailing and transfer-attendees.")
  public String createEverything(
      @ShellOption(value = "source-event", help = "Date of the event to copy (" + INPUT_DATE_FORMAT
          + ") or '" + LATEST + "'") String sourceEvent,
      @ShellOption(value = "newDate", help = "New event newDate (" + INPUT_DATE_FORMAT
          + ")") String newDate,
      @ShellOption(value = "place", defaultValue = ShellOption.NULL, help = "Place for the new event") String place,
      @ShellOption(value = "debug", defaultValue = "false", help = "Show details without modifying Eventbrite") boolean debug,
      @ShellOption(value = "deelnemers", defaultValue = "20", help = "Capacity for 'Deelnemers'") int deelnemers,
      @ShellOption(value = "vrijwilligers", defaultValue = "15", help = "Capacity for 'Vrijwilligers'") int vrijwilligers,
      @ShellOption(value = "kind-van-vrijwilliger", defaultValue = "10", help = "Capacity for 'Kind van Vrijwilliger'") int kindVanVrijwilliger,
      @ShellOption(value = "met-uitnodiging", defaultValue = "5", help = "Capacity for 'Met Uitnodiging'") int metUitnodiging,
      @ShellOption(value = {
          "original-campaign-title"}, defaultValue = LATEST, help = "Title of the existing campaign to copy, or 'latest'") String originalCampaignTitle
  ) {
    StringBuilder result = new StringBuilder();

    // 1. Copy event
    log.info("Step 1/4: Copying event...");
    String copyEventResult = copyEventUseCase.copyEvent(sourceEvent, newDate, place, debug);
    result.append("=== Copy Event ===\n").append(copyEventResult).append("\n\n");

    // 2. Set participants
    log.info("Step 2/4: Setting participants...");
    List<TicketClass> updated = updateTicketClassesUseCase.setParticipants(newDate, deelnemers,
        vrijwilligers, kindVanVrijwilliger, metUitnodiging, debug);
    result.append("=== Set Participants ===\n");
    if (updated.isEmpty()) {
      result.append("No ticket classes were updated.\n\n");
    } else {
      result.append("Successfully updated ticket classes:\n")
          .append(updated.stream()
              .map(tc -> String.format("- %s: %d", tc.getName(), tc.getCapacity()))
              .collect(Collectors.joining("\n")))
          .append("\n\n");
    }

    // 3. Copy mailing
    log.info("Step 3/4: Copying mailing...");
    String link = copyEventResult; // The URL returned by copyEvent
    String copyMailingResult = copyMailingUseCase.copyMailing(originalCampaignTitle, newDate, link,
        debug);
    result.append("=== Copy Mailing ===\n").append(copyMailingResult).append("\n\n");

    // 4. Transfer attendees
    log.info("Step 4/4: Transferring attendees...");
    String transferResult = transferAttendeesUseCase.transferAttendees(newDate, debug);
    result.append("=== Transfer Attendees ===\n").append(transferResult);

    return result.toString();
  }
}
