package be.coderdojo.ninove.coderdojo.adapter.out.eventbrite;

import be.coderdojo.ninove.coderdojo.domain.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@TestPropertySource(properties = {
    "eventbrite.api.token=test-token",
    "eventbrite.api.organization-id=test-org-id"
})
class EventbriteAdapterTest {

    private EventbriteAdapter adapter;

    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder eventbriteRestClientBuilder = RestClient.builder()
                .baseUrl("https://www.eventbriteapi.com/v3")
                .defaultHeader("Authorization", "Bearer test-token");
        server = MockRestServiceServer.bindTo(eventbriteRestClientBuilder).build();
        adapter = new EventbriteAdapter(eventbriteRestClientBuilder.build());
        org.springframework.test.util.ReflectionTestUtils.setField(adapter, "organizationId", "test-org-id");
    }

    @Test
    void findLatestPastEvent_shouldReturnEvent() {
        String responseJson = """
            {
                "events": [
                    {
                        "id": "1",
                        "name": {"text": "Latest Event"},
                        "start": {"utc": "2023-10-01T10:00:00Z"},
                        "end": {"utc": "2023-10-01T13:00:00Z"},
                        "url": "http://event.url"
                    }
                ]
            }
            """;

        server.expect(requestTo("https://www.eventbriteapi.com/v3/organizations/test-org-id/events/?status=past&order_by=start_desc"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Optional<Event> event = adapter.findLatestPastEvent();

        assertThat(event).isPresent();
        assertThat(event.get().getId()).isEqualTo("1");
        assertThat(event.get().getName()).isEqualTo("Latest Event");
    }

    @Test
    void findEventByDate_shouldReturnEvent() {
        String responseJson = """
            {
                "events": [
                    {
                        "id": "3",
                        "name": {"text": "Target Event"},
                        "start": {"utc": "2023-11-15T10:00:00Z"},
                        "end": {"utc": "2023-11-15T13:00:00Z"},
                        "url": "http://event.url"
                    }
                ]
            }
            """;

        server.expect(requestTo("https://www.eventbriteapi.com/v3/organizations/test-org-id/events/"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Optional<Event> event = adapter.findEventByDate("2023-11-15");

        assertThat(event).isPresent();
        assertThat(event.get().getId()).isEqualTo("3");
        assertThat(event.get().getName()).isEqualTo("Target Event");
    }

    @Test
    void copyEvent_shouldReturnNewEvent() {
        String responseJson = """
            {
                "id": "2",
                "name": {"text": "New Event"},
                "start": {"utc": "2024-10-01T10:00:00Z"},
                "end": {"utc": "2024-10-01T13:00:00Z"},
                "url": "http://new-event.url"
            }
            """;

        server.expect(requestTo("https://www.eventbriteapi.com/v3/events/1/copy/"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Event newEvent = adapter.copyEvent("1", ZonedDateTime.parse("2024-10-01T10:00:00Z"), "New Event");

        assertThat(newEvent.getId()).isEqualTo("2");
        assertThat(newEvent.getUrl()).isEqualTo("http://new-event.url");
    }
}
