package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.application.port.out.MailerLitePort;
import be.coderdojo.ninove.coderdojo.domain.model.Attendee;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import be.coderdojo.ninove.coderdojo.domain.model.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferAttendeesServiceTest {

    @Mock
    private EventbritePort eventbritePort;

    @Mock
    private MailerLitePort mailerLitePort;

    private TransferAttendeesService transferAttendeesService;

    @BeforeEach
    void setUp() {
        transferAttendeesService = new TransferAttendeesService(eventbritePort, mailerLitePort);
    }

    @Test
    void transferAttendees_ShouldCreateNewSubscriber_WhenNotExists() {
        // Given
        Event event = Event.builder().id("ev1").name("Event 1").build();
        Attendee attendee = Attendee.builder()
                .email("new@example.com")
                .firstName("John")
                .lastName("Doe")
                .optIn(true)
                .build();

        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(event));
        when(eventbritePort.getAttendees("ev1")).thenReturn(List.of(attendee));
        when(mailerLitePort.findSubscriberByEmail("new@example.com")).thenReturn(Optional.empty());

        // When
        String result = transferAttendeesService.transferAttendees(LATEST, false);

        // Then
        assertThat(result).contains("1 created");
        verify(mailerLitePort).createSubscriber(eq(attendee), eq(false));
    }

    @Test
    void transferAttendees_ShouldUpdateExistingSubscriber_WhenInfoDiffers() {
        // Given
        Event event = Event.builder().id("ev1").name("Event 1").build();
        Attendee ebAttendee = Attendee.builder()
                .email("existing@example.com")
                .firstName("John")
                .lastName("Updated")
                .optIn(true)
                .build();
        Attendee mlAttendee = Attendee.builder()
                .email("existing@example.com")
                .firstName("John")
                .lastName("Doe")
                .optIn(true)
                .build();

        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(event));
        when(eventbritePort.getAttendees("ev1")).thenReturn(List.of(ebAttendee));
        when(mailerLitePort.findSubscriberByEmail("existing@example.com")).thenReturn(Optional.of(mlAttendee));

        // When
        String result = transferAttendeesService.transferAttendees(LATEST, false);

        // Then
        assertThat(result).contains("1 updated");
        verify(mailerLitePort).updateSubscriber(eq(ebAttendee), eq(false));
    }

    @Test
    void transferAttendees_ShouldUpdateExistingSubscriber_WhenTicketClassDiffers() {
        // Given
        Event event = Event.builder().id("ev1").name("Event 1").build();
        Attendee ebAttendee = Attendee.builder()
                .email("existing@example.com")
                .firstName("John")
                .lastName("Doe")
                .ticketType(TicketType.DEELNEMER)
                .optIn(true)
                .build();
        Attendee mlAttendee = Attendee.builder()
                .email("existing@example.com")
                .firstName("John")
                .lastName("Doe")
                .ticketType(TicketType.VRIJWILLIGER)
                .optIn(true)
                .build();

        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(event));
        when(eventbritePort.getAttendees("ev1")).thenReturn(List.of(ebAttendee));
        when(mailerLitePort.findSubscriberByEmail("existing@example.com")).thenReturn(Optional.of(mlAttendee));

        // When
        String result = transferAttendeesService.transferAttendees(LATEST, false);

        // Then
        assertThat(result).contains("1 updated");
        verify(mailerLitePort).updateSubscriber(eq(ebAttendee), eq(false));
    }

    @Test
    void transferAttendees_ShouldOptOutSubscriber_WhenEBAttendeeHasNoOptIn() {
        // Given
        Event event = Event.builder().id("ev1").name("Event 1").build();
        Attendee ebAttendee = Attendee.builder()
                .email("nooptin@example.com")
                .firstName("John")
                .lastName("Doe")
                .optIn(false)
                .build();
        Attendee mlAttendee = Attendee.builder()
                .email("nooptin@example.com")
                .firstName("John")
                .lastName("Doe")
                .optIn(true)
                .build();

        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(event));
        when(eventbritePort.getAttendees("ev1")).thenReturn(List.of(ebAttendee));
        when(mailerLitePort.findSubscriberByEmail("nooptin@example.com")).thenReturn(Optional.of(mlAttendee));

        // When
        String result = transferAttendeesService.transferAttendees(LATEST, false);

        // Then
        assertThat(result).contains("1 opted out");
        verify(mailerLitePort).optOutSubscriber(eq("nooptin@example.com"), eq(false));
    }

    @Test
    void transferAttendees_ShouldDoNothing_WhenExistingSubscriberMatches() {
        // Given
        Event event = Event.builder().id("ev1").name("Event 1").build();
        Attendee ebAttendee = Attendee.builder()
                .email("match@example.com")
                .firstName("John")
                .lastName("Doe")
                .optIn(true)
                .build();
        Attendee mlAttendee = Attendee.builder()
                .email("match@example.com")
                .firstName("John")
                .lastName("Doe")
                .optIn(true)
                .build();

        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(event));
        when(eventbritePort.getAttendees("ev1")).thenReturn(List.of(ebAttendee));
        when(mailerLitePort.findSubscriberByEmail("match@example.com")).thenReturn(Optional.of(mlAttendee));

        // When
        String result = transferAttendeesService.transferAttendees(LATEST, false);

        // Then
        assertThat(result).contains("0 created, 0 updated, 0 opted out");
        verify(mailerLitePort, never()).createSubscriber(any(), anyBoolean());
        verify(mailerLitePort, never()).updateSubscriber(any(), anyBoolean());
        verify(mailerLitePort, never()).optOutSubscriber(anyString(), anyBoolean());
    }
}
