package be.coderdojo.ninove.coderdojo.adapter.out.mailerlite;

import be.coderdojo.ninove.coderdojo.domain.model.Campaign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MailerLiteAdapterTest {

    private MailerLiteAdapter adapter;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://connect.mailerlite.com/api")
                .defaultHeader("Authorization", "Bearer test-token");
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new MailerLiteAdapter(builder.build());
    }

    @Test
    void findLatestCampaign_shouldReturnCampaign() {
        String responseJson = """
            {
                "data": [
                    {
                        "id": "101",
                        "name": "Latest Campaign",
                        "status": "sent"
                    }
                ]
            }
            """;

        server.expect(requestTo("https://connect.mailerlite.com/api/campaigns?limit=1&status=sent"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Optional<Campaign> result = adapter.findLatestCampaign();

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("101");
        assertThat(result.get().getTitle()).isEqualTo("Latest Campaign");
    }

    @Test
    void findCampaignByTitle_shouldReturnCampaign() {
        String title = "Specific Campaign";
        String responseJson = """
            {
                "data": [
                    {
                        "id": "105",
                        "name": "Specific Campaign",
                        "status": "sent"
                    }
                ]
            }
            """;

        server.expect(requestTo("https://connect.mailerlite.com/api/campaigns?filter%5Bkeyword%5D=Specific%20Campaign&limit=1"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Optional<Campaign> result = adapter.findCampaignByTitle(title);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("105");
        assertThat(result.get().getTitle()).isEqualTo("Specific Campaign");
    }

    @Test
    void getCampaignDetails_shouldReturnCampaignWithAllDetails() {
        String responseJson = """
            {
                "data": {
                    "id": "101",
                    "name": "Campaign Detail",
                    "status": "sent",
                    "language_id": 1,
                    "groups": ["group1", "group2"],
                    "segments": ["segment1"],
                    "settings": {
                        "track_opens": "enabled",
                        "track_clicks": "enabled"
                    },
                    "emails": [
                        {
                            "content": "Sample Content",
                            "from_name": "Sender Name",
                            "from": "sender@example.com",
                            "subject": "Sample Subject"
                        }
                    ]
                }
            }
            """;

        server.expect(requestTo("https://connect.mailerlite.com/api/campaigns/101"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Optional<Campaign> result = adapter.getCampaignDetails("101");

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("Sample Content");
        assertThat(result.get().getFromName()).isEqualTo("Sender Name");
        assertThat(result.get().getFromEmail()).isEqualTo("sender@example.com");
        assertThat(result.get().getSubject()).isEqualTo("Sample Subject");
        assertThat(result.get().getLanguageId()).isEqualTo("1");
        assertThat(result.get().getGroups()).containsExactly("group1", "group2");
        assertThat(result.get().getSegments()).containsExactly("segment1");
        assertThat(result.get().getSettings()).containsEntry("track_opens", "enabled")
                .containsEntry("track_clicks", "enabled");
    }

    @Test
    void createCampaign_shouldReturnCreatedCampaign() {
        String responseJson = """
            {
                "data": {
                    "id": "102",
                    "name": "New Campaign",
                    "status": "draft",
                    "language_id": 1,
                    "groups": ["group1"],
                    "settings": {
                        "track_opens": "enabled"
                    },
                    "emails": [
                        {
                            "content": "New Content",
                            "from_name": "John Doe",
                            "from": "john@example.com",
                            "subject": "New Subject"
                        }
                    ]
                }
            }
            """;

        server.expect(requestTo("https://connect.mailerlite.com/api/campaigns"))
                .andExpect(jsonPath("$.settings.track_opens").value("enabled"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Campaign result = adapter.createCampaign("New Campaign", "New Content", "John Doe", "john@example.com", "New Subject",
                java.util.List.of("group1"), null, "1", Map.of("track_opens", "enabled"), false);

        assertThat(result.getId()).isEqualTo("102");
        assertThat(result.getTitle()).isEqualTo("New Campaign");
        assertThat(result.getFromName()).isEqualTo("John Doe");
        assertThat(result.getFromEmail()).isEqualTo("john@example.com");
        assertThat(result.getSubject()).isEqualTo("New Subject");
        assertThat(result.getGroups()).containsExactly("group1");
        assertThat(result.getLanguageId()).isEqualTo("1");
        assertThat(result.getSettings()).containsEntry("track_opens", "enabled");
    }

    @Test
    void createCampaign_inDebugMode_shouldNotPost() {
        // No server expectations means it will fail if a request is made

        Campaign result = adapter.createCampaign("Debug Campaign", "Debug Content", "John Doe", "john@example.com", "Debug Subject",
                java.util.List.of("g1"), java.util.List.of("s1"), "1", Map.of("track_opens", "enabled"), true);

        assertThat(result.getId()).isEqualTo("DEBUG-ID");
        assertThat(result.getTitle()).isEqualTo("Debug Campaign");
        assertThat(result.getContent()).isEqualTo("Debug Content");
        assertThat(result.getFromName()).isEqualTo("John Doe");
        assertThat(result.getFromEmail()).isEqualTo("john@example.com");
        assertThat(result.getSubject()).isEqualTo("Debug Subject");
        assertThat(result.getGroups()).containsExactly("g1");
        assertThat(result.getSegments()).containsExactly("s1");
        assertThat(result.getLanguageId()).isEqualTo("1");
        assertThat(result.getSettings()).containsEntry("track_opens", "enabled");
        // server.verify() is implicitly checked if we use MockRestServiceServer with expect()
        // but here we just ensure it returns the debug object without calling the API.
    }
}
