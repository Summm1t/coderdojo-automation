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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
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

        server.expect(requestTo("https://www.eventbriteapi.com/v3/organizations/test-org-id/events/?order_by=start_desc"))
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

        server.expect(requestTo("https://www.eventbriteapi.com/v3/organizations/test-org-id/events/?order_by=start_desc"))
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
                "name": {"text": "Original Event"},
                "start": {"utc": "2024-10-01T10:00:00Z"},
                "end": {"utc": "2024-10-01T13:00:00Z"},
                "url": "http://new-event.url"
            }
            """;

        server.expect(requestTo("https://www.eventbriteapi.com/v3/events/1/copy/"))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("start_date=2024-10-01T10%3A00%3A00Z")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("end_date=2024-10-01T13%3A00%3A00Z")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("event.name.html"))))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Event newEvent = adapter.copyEvent("1", ZonedDateTime.parse("2024-10-01T10:00:00Z"), ZonedDateTime.parse("2024-10-01T13:00:00Z"));

        assertThat(newEvent.getId()).isEqualTo("2");
    }

    @Test
    void updateEvent_shouldReturnUpdatedEvent() {
        String responseJson = """
            {
                "id": "2",
                "name": {"text": "Updated Event"},
                "start": {"utc": "2024-10-01T10:00:00Z"},
                "end": {"utc": "2024-10-01T13:00:00Z"},
                "url": "http://updated-event.url"
            }
            """;

        server.expect(requestTo("https://www.eventbriteapi.com/v3/events/2/"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"name\":{\"html\":\"Updated Event\"}")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        Event updatedEvent = adapter.updateEvent("2", "Updated Event");

        assertThat(updatedEvent.getName()).isEqualTo("Updated Event");
        assertThat(updatedEvent.getUrl()).isEqualTo("http://updated-event.url");
    }
}
