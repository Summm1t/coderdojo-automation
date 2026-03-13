package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CopyEventServiceTest {

    @Mock
    private EventbritePort eventbritePort;

    private CopyEventService copyEventService;

    @BeforeEach
    void setUp() {
        copyEventService = new CopyEventService(eventbritePort);
    }

    @Test
    void copyEvent_withLatest_shouldCallFindLatestPastEvent() {
        // Given
        String sourceDate = "latest";
        LocalDate newDate = LocalDate.now();
        String newTitle = "New Title";
        ZonedDateTime sourceStartTime = ZonedDateTime.parse("2023-01-01T10:00:00Z");
        ZonedDateTime sourceEndTime = ZonedDateTime.parse("2023-01-01T13:00:00Z");
        Event sourceEvent = Event.builder()
                .id("123")
                .name("Old Title")
                .startTime(sourceStartTime)
                .endTime(sourceEndTime)
                .build();
        Event newEvent = Event.builder()
                .id("456")
                .name(newTitle)
                .url("http://new-event.url")
                .build();

        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(sourceEvent));
        when(eventbritePort.copyEvent(anyString(), any(), any(), anyString())).thenReturn(newEvent);

        // When
        String result = copyEventService.copyEvent(sourceDate, newDate, newTitle, false);

        // Then
        assertThat(result).isEqualTo("http://new-event.url");
        verify(eventbritePort).findLatestPastEvent();
        verify(eventbritePort).copyEvent(eq("123"), any(), any(), eq(newTitle));
    }

    @Test
    void copyEvent_withDate_shouldCallFindEventByDate() {
        // Given
        String sourceDate = "01/01/2023";
        LocalDate newDate = LocalDate.now();
        String newTitle = "New Title";
        ZonedDateTime sourceStartTime = ZonedDateTime.parse("2023-01-01T10:00:00Z");
        ZonedDateTime sourceEndTime = ZonedDateTime.parse("2023-01-01T13:00:00Z");
        Event sourceEvent = Event.builder()
                .id("123")
                .name("Old Title")
                .startTime(sourceStartTime)
                .endTime(sourceEndTime)
                .build();
        Event newEvent = Event.builder()
                .id("456")
                .name(newTitle)
                .url("http://new-event.url")
                .build();

        when(eventbritePort.findEventByDate("2023-01-01")).thenReturn(Optional.of(sourceEvent));
        when(eventbritePort.copyEvent(anyString(), any(), any(), anyString())).thenReturn(newEvent);

        // When
        String result = copyEventService.copyEvent(sourceDate, newDate, newTitle, false);

        // Then
        assertThat(result).isEqualTo("http://new-event.url");
        verify(eventbritePort).findEventByDate("2023-01-01");
    }

    @Test
    void copyEvent_debugMode_shouldNotCallCopyEvent() {
        // Given
        String sourceDate = "latest";
        LocalDate newDate = LocalDate.now();
        String newTitle = "New Title";
        ZonedDateTime sourceStartTime = ZonedDateTime.parse("2023-01-01T10:00:00Z");
        ZonedDateTime sourceEndTime = ZonedDateTime.parse("2023-01-01T13:00:00Z");
        Event sourceEvent = Event.builder()
                .id("123")
                .name("Old Title")
                .startTime(sourceStartTime)
                .endTime(sourceEndTime)
                .build();

        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(sourceEvent));

        // When
        String result = copyEventService.copyEvent(sourceDate, newDate, newTitle, true);

        // Then
        assertThat(result).contains("DEBUG MODE");
        verify(eventbritePort, never()).copyEvent(anyString(), any(), any(), anyString());
    }

    @Test
    void copyEvent_sourceNotFound_shouldThrowException() {
        // Given
        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            copyEventService.copyEvent("latest", LocalDate.now(), "Title", false)
        );
    }
}
