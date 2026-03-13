package be.coderdojo.ninove.coderdojo.adapter.out.eventbrite;

import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventbriteAdapter implements EventbritePort {

    private final RestClient eventbriteRestClient;

    @Value("${eventbrite.api.organization-id}")
    private String organizationId;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Event> findLatestPastEvent() {
        Map<String, Object> response = eventbriteRestClient.get()
                .uri("/organizations/{orgId}/events/?order_by=start_desc", organizationId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || !response.containsKey("events")) {
            return Optional.empty();
        }

        List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
        if (events.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToEvent(events.getFirst()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Event> findEventByDate(String date) {
        // The Eventbrite API doesn't support direct searching for events by date.
        // As a workaround, we retrieve all events for the organization and then filter them by their start date.
        Map<String, Object> response = eventbriteRestClient.get()
                .uri("/organizations/{orgId}/events/?order_by=start_desc", organizationId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || !response.containsKey("events")) {
            return Optional.empty();
        }

        List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
        return events.stream()
                .map(this::mapToEvent)
                .filter(e -> e.getStartTime().toLocalDate().toString().equals(date))
                .findFirst();
    }

    @Override
    public Event copyEvent(String eventId, ZonedDateTime newStartTime, ZonedDateTime newEndTime, String newTitle) {
        Map<String, Object> request = Map.of(
                "event", Map.of(
                        "name", Map.of("html", newTitle),
                        "start", Map.of("timezone", newStartTime.getZone().getId(), "utc", newStartTime.toOffsetDateTime().toString()),
                        "end", Map.of("timezone", newEndTime.getZone().getId(), "utc", newEndTime.toOffsetDateTime().toString())
                )
        );

        Map<String, Object> response = eventbriteRestClient.post()
                .uri("/events/{eventId}/copy/", eventId)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return mapToEvent(Objects.requireNonNull(response));
    }

    @SuppressWarnings("unchecked")
    private Event mapToEvent(Map<String, Object> eventData) {
        String id = (String) eventData.get("id");
        Map<String, Object> nameMap = (Map<String, Object>) eventData.get("name");
        String name = (String) nameMap.get("text");
        Map<String, Object> startMap = (Map<String, Object>) eventData.get("start");
        String startTimeStr = (String) startMap.get("utc");
        Map<String, Object> endMap = (Map<String, Object>) eventData.get("end");
        String endTimeStr = (String) endMap.get("utc");
        String url = (String) eventData.get("url");

        return Event.builder()
                .id(id)
                .name(name)
                .startTime(ZonedDateTime.parse(startTimeStr))
                .endTime(ZonedDateTime.parse(endTimeStr))
                .url(url)
                .build();
    }
}
