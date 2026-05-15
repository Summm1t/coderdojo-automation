package be.coderdojo.ninove.coderdojo.application.service;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import be.coderdojo.ninove.coderdojo.domain.model.TicketClass;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTicketClassesServiceTest {

  @Mock
  private EventbritePort eventbritePort;

  private UpdateTicketClassesService updateTicketClassesService;

  @BeforeEach
  void setUp() {
    updateTicketClassesService = new UpdateTicketClassesService(eventbritePort);
  }

  @Test
  void setParticipants_shouldUpdateMatchingTicketClasses() {
    // Given
    String eventDate = "21/03/2026";
    Map<String, Integer> capacities = Map.of(
        "deelnemers", 20,
        "vrijwilligers", 15,
        "kind-van-vrijwilliger", 10,
        "met-uitnodiging", 5
    );
    Event event = Event.builder()
        .id("event123")
        .name("CoderDojo")
        .startTime(ZonedDateTime.now())
        .build();

    List<TicketClass> ticketClasses = List.of(
        TicketClass.builder().id("tc1").name("Deelnemer").capacity(0).build(),
        TicketClass.builder().id("tc2").name("vrijwilliger").capacity(0).build(),
        TicketClass.builder().id("tc3").name("Kind van vrijwilliger").capacity(0).build(),
        TicketClass.builder().id("tc4").name("Met uitnodiging").capacity(0).build(),
        TicketClass.builder().id("tc5").name("Some other ticket").capacity(0).build()
    );

    when(eventbritePort.findEventByDate(eventDate)).thenReturn(Optional.of(event));
    when(eventbritePort.getTicketClasses(event.getId())).thenReturn(ticketClasses);
    when(eventbritePort.updateTicketClass(eq(event.getId()), anyString(), anyInt(), anyInt()))
        .thenAnswer(invocation -> TicketClass.builder()
            .id(invocation.getArgument(1))
            .capacity(invocation.getArgument(2))
            .quantityTotal(invocation.getArgument(3))
            .name("Updated")
            .build());

    // When
    List<TicketClass> result = updateTicketClassesService.setParticipants(eventDate, 20, 15, 10, 5,
        false);

    // Then
    assertThat(result).hasSize(4);
    verify(eventbritePort).updateTicketClass(event.getId(), "tc1", 20, 20);
    verify(eventbritePort).updateTicketClass(event.getId(), "tc2", 15, 15);
    verify(eventbritePort).updateTicketClass(event.getId(), "tc3", 10, 10);
    verify(eventbritePort).updateTicketClass(event.getId(), "tc4", 5, 5);
    verify(eventbritePort, times(4)).updateTicketClass(eq(event.getId()), anyString(), anyInt(),
        anyInt());
  }

  @Test
  void setParticipants_debugMode_shouldNotCallUpdatePort() {
    // Given
    String eventDate = LATEST;
    Map<String, Integer> capacities = Map.of("deelnemers", 20);
    Event event = Event.builder().id("event123").name("CoderDojo").build();
    List<TicketClass> ticketClasses = List.of(
        TicketClass.builder().id("tc1").name("Deelnemer").build());

    when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(event));
    when(eventbritePort.getTicketClasses(event.getId())).thenReturn(ticketClasses);

    // When
    List<TicketClass> result = updateTicketClassesService.setParticipants(eventDate, 20, 15, 10, 5,
        true);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getCapacity()).isEqualTo(20);
    verify(eventbritePort, never()).updateTicketClass(anyString(), anyString(), anyInt(), anyInt());
  }
}
