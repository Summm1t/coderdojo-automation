package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.out.MailchimpPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnsubscribeServiceTest {

    @Mock
    private MailchimpPort mailchimpPort;

    private UnsubscribeService unsubscribeService;

    @BeforeEach
    void setUp() {
        unsubscribeService = new UnsubscribeService(mailchimpPort);
    }

    @Test
    void unsubscribe_shouldTagAndUnsubscribeEachEmail() {
        // Given
        List<String> emails = List.of("user1@example.com", "user2@example.com");
        when(mailchimpPort.tagUser(anyString(), any(), anyBoolean())).thenReturn(true);
        when(mailchimpPort.unsubscribeUser(anyString(), anyBoolean())).thenReturn(true);

        // When
        unsubscribeService.unsubscribe(emails, false);

        // Then
        verify(mailchimpPort).tagUser("user1@example.com", List.of("Removed_manually"), false);
        verify(mailchimpPort).unsubscribeUser("user1@example.com", false);
        verify(mailchimpPort).tagUser("user2@example.com", List.of("Removed_manually"), false);
        verify(mailchimpPort).unsubscribeUser("user2@example.com", false);
    }

    @Test
    void unsubscribe_shouldStopProcessIfTagUserFails() {
        // Given
        List<String> emails = List.of("notfound@example.com", "found@example.com");
        when(mailchimpPort.tagUser("notfound@example.com", List.of("Removed_manually"), false)).thenReturn(false);
        when(mailchimpPort.tagUser("found@example.com", List.of("Removed_manually"), false)).thenReturn(true);
        when(mailchimpPort.unsubscribeUser("found@example.com", false)).thenReturn(true);

        // When
        unsubscribeService.unsubscribe(emails, false);

        // Then
        verify(mailchimpPort).tagUser("notfound@example.com", List.of("Removed_manually"), false);
        verify(mailchimpPort, never()).unsubscribeUser("notfound@example.com", false);
        
        verify(mailchimpPort).tagUser("found@example.com", List.of("Removed_manually"), false);
        verify(mailchimpPort).unsubscribeUser("found@example.com", false);
    }

    @Test
    void unsubscribe_shouldContinueOnFailure() {
        // Given
        List<String> emails = List.of("fail@example.com", "success@example.com");
        when(mailchimpPort.tagUser(eq("fail@example.com"), any(), anyBoolean())).thenThrow(new RuntimeException("API Error"));
        when(mailchimpPort.tagUser(eq("success@example.com"), any(), anyBoolean())).thenReturn(true);
        when(mailchimpPort.unsubscribeUser(eq("success@example.com"), anyBoolean())).thenReturn(true);

        // When
        unsubscribeService.unsubscribe(emails, false);

        // Then
        verify(mailchimpPort).tagUser("fail@example.com", List.of("Removed_manually"), false);
        verify(mailchimpPort, never()).unsubscribeUser("fail@example.com", false);
        verify(mailchimpPort).tagUser("success@example.com", List.of("Removed_manually"), false);
        verify(mailchimpPort).unsubscribeUser("success@example.com", false);
    }

    @Test
    void unsubscribe_debugMode_shouldCallMailchimpPortWithDebugTrue() {
        // Given
        List<String> emails = List.of("user@example.com");
        when(mailchimpPort.tagUser(anyString(), any(), anyBoolean())).thenReturn(true);
        when(mailchimpPort.unsubscribeUser(anyString(), anyBoolean())).thenReturn(true);

        // When
        unsubscribeService.unsubscribe(emails, true);

        // Then
        verify(mailchimpPort).tagUser("user@example.com", List.of("Removed_manually"), true);
        verify(mailchimpPort).unsubscribeUser("user@example.com", true);
    }
}
