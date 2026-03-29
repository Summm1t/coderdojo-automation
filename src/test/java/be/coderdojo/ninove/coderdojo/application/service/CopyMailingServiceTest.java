package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.out.MailerLitePort;
import be.coderdojo.ninove.coderdojo.domain.model.Campaign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CopyMailingServiceTest {

    @Mock
    private MailerLitePort mailerLitePort;

    private CopyMailingService copyMailingService;

    @BeforeEach
    void setUp() {
        copyMailingService = new CopyMailingService(mailerLitePort);
    }

    @Test
    void copyMailing_ShouldSuccessfullyCopyAndUpdate() {
        // Given
        String oldContent = "Hallo,\nDe volgende Coderdojo gaat door op zondag 15 februari in de vernieuwe bibliotheek van Ninove, inschrijven kan hier: https://www.eventbrite.com/e/registratie-coderdojo-ninove-1234567890\nGroeten!";
        Campaign latest = Campaign.builder().id("1").title("Old Title").build();
        Campaign details = Campaign.builder().id("1").title("Old Title").content(oldContent).build();
        Campaign created = Campaign.builder().id("2").title("Coderdojo Ninove Nieuwsbrief April").build();

        when(mailerLitePort.findLatestCampaign()).thenReturn(Optional.of(latest));
        when(mailerLitePort.getCampaignDetails("1")).thenReturn(Optional.of(details));
        when(mailerLitePort.createCampaign(anyString(), anyString(), anyBoolean())).thenReturn(created);

        // When
        String result = copyMailingService.copyMailing("April", "12 april", "https://www.eventbrite.com/e/registratie-coderdojo-ninove-newlink", false);

        // Then
        assertThat(result).contains("New mailing campaign created");
        assertThat(result).contains("Coderdojo Ninove Nieuwsbrief April");

        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> debugCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(mailerLitePort).createCampaign(titleCaptor.capture(), contentCaptor.capture(), debugCaptor.capture());

        assertThat(titleCaptor.getValue()).isEqualTo("Coderdojo Ninove Nieuwsbrief April");
        assertThat(contentCaptor.getValue()).contains("zondag 12 april");
        assertThat(contentCaptor.getValue()).contains("https://www.eventbrite.com/e/registratie-coderdojo-ninove-newlink");
        assertThat(contentCaptor.getValue()).doesNotContain("15 februari");
        assertThat(contentCaptor.getValue()).doesNotContain("1234567890");
        assertThat(debugCaptor.getValue()).isFalse();
    }

    @Test
    void copyMailing_WithDebug_ShouldCallCreateCampaignWithDebugTrue() {
        // Given
        String oldContent = "De volgende Coderdojo gaat door op zondag 15 februari in de vernieuwe bibliotheek van Ninove, inschrijven kan hier: https://www.eventbrite.com/e/registratie-coderdojo-ninove-123";
        Campaign latest = Campaign.builder().id("1").build();
        Campaign details = Campaign.builder().id("1").content(oldContent).build();

        when(mailerLitePort.findLatestCampaign()).thenReturn(Optional.of(latest));
        when(mailerLitePort.getCampaignDetails("1")).thenReturn(Optional.of(details));

        // When
        String result = copyMailingService.copyMailing("April", "12 april", "https://www.eventbrite.com/e/registratie-coderdojo-ninove-new", true);

        // Then
        assertThat(result).startsWith("DEBUG MODE:");
        assertThat(result).contains("Coderdojo Ninove Nieuwsbrief April");
        assertThat(result).contains("zondag 12 april");
        verify(mailerLitePort).createCampaign(anyString(), anyString(), eq(true));
    }
}
