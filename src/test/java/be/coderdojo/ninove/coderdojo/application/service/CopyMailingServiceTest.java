package be.coderdojo.ninove.coderdojo.application.service;

import be.coderdojo.ninove.coderdojo.application.port.out.MailerLitePort;
import be.coderdojo.ninove.coderdojo.domain.model.Campaign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static be.coderdojo.ninove.coderdojo.domain.model.Constants.LATEST;
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
        String oldContent = "Hallo,\ninschrijven kan hier: https://www.eventbrite.com/e/registratie-coderdojo-ninove-1234567890\nGroeten!";
        Map<String, Object> settings = Map.of("track_opens", "enabled");
        Campaign latest = Campaign.builder().id("1").title("Coderdojo Ninove Nieuwsbrief april 2026").build();
        Campaign details = Campaign.builder()
                .id("1")
                .title("Coderdojo Ninove Nieuwsbrief april 2026")
                .content(oldContent)
                .settings(settings)
                .build();
        Campaign created = Campaign.builder().id("2").title("Coderdojo Ninove Nieuwsbrief mei 2026").build();

        when(mailerLitePort.findLatestCampaign()).thenReturn(Optional.of(latest));
        when(mailerLitePort.getCampaignDetails("1")).thenReturn(Optional.of(details));
        when(mailerLitePort.createCampaign(anyString(), anyString(), any(), any(), anyString(), any(), any(), any(), any(), anyBoolean())).thenReturn(created);

        // When
        String result = copyMailingService.copyMailing(LATEST, "17/05/2026", "https://www.eventbrite.com/e/registratie-coderdojo-ninove-newlink", false);

        // Then
        assertThat(result).contains("New mailing campaign created");
        assertThat(result).contains("Coderdojo Ninove Nieuwsbrief mei 2026");

        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fromEmailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<java.util.List> groupsCaptor = ArgumentCaptor.forClass(java.util.List.class);
        ArgumentCaptor<java.util.List> segmentsCaptor = ArgumentCaptor.forClass(java.util.List.class);
        ArgumentCaptor<String> languageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> settingsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Boolean> debugCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(mailerLitePort).createCampaign(titleCaptor.capture(), contentCaptor.capture(), fromNameCaptor.capture(), fromEmailCaptor.capture(),
                subjectCaptor.capture(), groupsCaptor.capture(), segmentsCaptor.capture(), languageIdCaptor.capture(), settingsCaptor.capture(), debugCaptor.capture());

        assertThat(titleCaptor.getValue()).isEqualTo("Coderdojo Ninove Nieuwsbrief mei 2026");
        assertThat(contentCaptor.getValue()).contains("https://www.eventbrite.com/e/registratie-coderdojo-ninove-newlink");
        assertThat(contentCaptor.getValue()).doesNotContain("1234567890");
        assertThat(settingsCaptor.getValue()).isEqualTo(settings);
        assertThat(debugCaptor.getValue()).isFalse();
    }

    @Test
    void copyMailing_WithSpecificTitle_ShouldFindAndCopy() {
        // Given
        String oldContent = "Inschrijven: https://www.eventbrite.com/e/old-link";
        String specificTitle = "Coderdojo Ninove Nieuwsbrief januari 2026";
        Campaign original = Campaign.builder().id("10").title(specificTitle).build();
        Campaign details = Campaign.builder().id("10").title(specificTitle).content(oldContent).build();
        Campaign created = Campaign.builder().id("11").title("Coderdojo Ninove Nieuwsbrief februari 2026").build();

        when(mailerLitePort.findCampaignByTitle(specificTitle)).thenReturn(Optional.of(original));
        when(mailerLitePort.getCampaignDetails("10")).thenReturn(Optional.of(details));
        when(mailerLitePort.createCampaign(anyString(), anyString(), any(), any(), any(), any(), any(), any(), any(), anyBoolean())).thenReturn(created);

        // When
        String result = copyMailingService.copyMailing(specificTitle, "15/02/2026", "https://www.eventbrite.com/e/new-link", false);

        // Then
        assertThat(result).contains("New mailing campaign created");
        verify(mailerLitePort).findCampaignByTitle(specificTitle);
        verify(mailerLitePort).createCampaign(eq("Coderdojo Ninove Nieuwsbrief februari 2026"), anyString(), any(), any(), any(), any(), any(), any(), any(), eq(false));
    }

    @Test
    void copyMailing_WithDebug_ShouldCallCreateCampaignWithDebugTrue() {
        // Given
        String oldContent = "Inschrijven: https://www.eventbrite.com/e/old-link";
        Campaign latest = Campaign.builder().id("1").title("Coderdojo Ninove Nieuwsbrief april 2026").build();
        Campaign details = Campaign.builder().id("1").title("Coderdojo Ninove Nieuwsbrief april 2026").content(oldContent).build();

        when(mailerLitePort.findLatestCampaign()).thenReturn(Optional.of(latest));
        when(mailerLitePort.getCampaignDetails("1")).thenReturn(Optional.of(details));

        // When
        String result = copyMailingService.copyMailing(LATEST, "17/05/2026", "https://www.eventbrite.com/e/new-link", true);

        // Then
        assertThat(result).startsWith("DEBUG MODE:");
        assertThat(result).contains("Coderdojo Ninove Nieuwsbrief mei 2026");
        verify(mailerLitePort).createCampaign(anyString(), anyString(), any(), any(), any(), any(), any(), any(), any(), eq(true));
    }

    @Test
    void updateContentLink_ShouldHandleVariousEventbriteTLDs() {
        // Given
        String oldContent = "Inschrijven: https://www.eventbrite.co.uk/e/old-link-12345";
        Campaign latest = Campaign.builder().id("1").title("Coderdojo Ninove Nieuwsbrief april 2026").build();
        Campaign details = Campaign.builder().id("1").title("Coderdojo Ninove Nieuwsbrief april 2026").content(oldContent).build();

        when(mailerLitePort.findLatestCampaign()).thenReturn(Optional.of(latest));
        when(mailerLitePort.getCampaignDetails("1")).thenReturn(Optional.of(details));

        // When
        String newLink = "https://www.eventbrite.nl/e/new-link-67890";
        copyMailingService.copyMailing(LATEST, "17/05/2026", newLink, true);

        // Then
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailerLitePort).createCampaign(anyString(), contentCaptor.capture(), any(), any(), any(), any(), any(), any(), any(), eq(true));
        assertThat(contentCaptor.getValue()).contains(newLink);
        assertThat(contentCaptor.getValue()).doesNotContain("old-link-12345");
    }
}
