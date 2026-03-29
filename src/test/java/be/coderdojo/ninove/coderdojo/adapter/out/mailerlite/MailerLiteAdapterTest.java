package be.coderdojo.ninove.coderdojo.adapter.out.mailerlite;

import be.coderdojo.ninove.coderdojo.domain.model.Campaign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    void getCampaignDetails_shouldReturnCampaignWithContent() {
        String responseJson = """
            {
                "data": {
                    "id": "101",
                    "name": "Campaign Detail",
                    "status": "sent",
                    "emails": [
                        {
                            "content": "Sample Content"
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
    }

    @Test
    void createCampaign_shouldReturnCreatedCampaign() {
        String responseJson = """
            {
                "data": {
                    "id": "102",
                    "name": "New Campaign",
                    "status": "draft"
                }
            }
            """;

        server.expect(requestTo("https://connect.mailerlite.com/api/campaigns"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Campaign result = adapter.createCampaign("New Campaign", "New Content", false);

        assertThat(result.getId()).isEqualTo("102");
        assertThat(result.getTitle()).isEqualTo("New Campaign");
    }

    @Test
    void createCampaign_inDebugMode_shouldNotPost() {
        // No server expectations means it will fail if a request is made

        Campaign result = adapter.createCampaign("Debug Campaign", "Debug Content", true);

        assertThat(result.getId()).isEqualTo("DEBUG-ID");
        assertThat(result.getTitle()).isEqualTo("Debug Campaign");
        assertThat(result.getContent()).isEqualTo("Debug Content");
        // server.verify() is implicitly checked if we use MockRestServiceServer with expect()
        // but here we just ensure it returns the debug object without calling the API.
    }
}
