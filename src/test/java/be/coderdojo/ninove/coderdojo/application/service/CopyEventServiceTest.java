package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        ZonedDateTime newDate = ZonedDateTime.now();
        String newTitle = "New Title";
        Event sourceEvent = Event.builder()
                .id("123")
                .name("Old Title")
                .build();
        Event newEvent = Event.builder()
                .id("456")
                .name(newTitle)
                .url("http://new-event.url")
                .build();

        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(sourceEvent));
        when(eventbritePort.copyEvent(anyString(), any(), anyString())).thenReturn(newEvent);

        // When
        String result = copyEventService.copyEvent(sourceDate, newDate, newTitle, false);

        // Then
        assertThat(result).isEqualTo("http://new-event.url");
        verify(eventbritePort).findLatestPastEvent();
        verify(eventbritePort).copyEvent("123", newDate, newTitle);
    }

    @Test
    void copyEvent_withDate_shouldCallFindEventByDate() {
        // Given
        String sourceDate = "2023-01-01";
        ZonedDateTime newDate = ZonedDateTime.now();
        String newTitle = "New Title";
        Event sourceEvent = Event.builder()
                .id("123")
                .name("Old Title")
                .build();
        Event newEvent = Event.builder()
                .id("456")
                .name(newTitle)
                .url("http://new-event.url")
                .build();

        when(eventbritePort.findEventByDate(sourceDate)).thenReturn(Optional.of(sourceEvent));
        when(eventbritePort.copyEvent(anyString(), any(), anyString())).thenReturn(newEvent);

        // When
        String result = copyEventService.copyEvent(sourceDate, newDate, newTitle, false);

        // Then
        assertThat(result).isEqualTo("http://new-event.url");
        verify(eventbritePort).findEventByDate(sourceDate);
    }

    @Test
    void copyEvent_debugMode_shouldNotCallCopyEvent() {
        // Given
        String sourceDate = "latest";
        ZonedDateTime newDate = ZonedDateTime.now();
        String newTitle = "New Title";
        Event sourceEvent = Event.builder()
                .id("123")
                .name("Old Title")
                .build();

        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.of(sourceEvent));

        // When
        String result = copyEventService.copyEvent(sourceDate, newDate, newTitle, true);

        // Then
        assertThat(result).contains("DEBUG MODE");
        verify(eventbritePort, never()).copyEvent(anyString(), any(), anyString());
    }

    @Test
    void copyEvent_sourceNotFound_shouldThrowException() {
        // Given
        when(eventbritePort.findLatestPastEvent()).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            copyEventService.copyEvent("latest", ZonedDateTime.now(), "Title", false)
        );
    }
}
