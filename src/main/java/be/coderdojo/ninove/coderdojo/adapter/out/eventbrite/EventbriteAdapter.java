package be.coderdojo.ninove.coderdojo.adapter.out.eventbrite;

import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventbriteAdapter implements EventbritePort {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd'T'HH:mm:ss'Z'");
  private final RestClient eventbriteRestClient;

  @Value("${eventbrite.api.organization-id}")
  private String organizationId;

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Event> findLatestPastEvent() {
    log.debug("Fetching latest past event for organization: {}", organizationId);
    Map<String, Object> response = eventbriteRestClient.get()
        .uri("/organizations/{orgId}/events/?order_by=start_desc", organizationId)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });

    if (response == null || !response.containsKey("events")) {
      log.warn("No events found in response for organization: {}", organizationId);
      return Optional.empty();
    }

    List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
    if (events.isEmpty()) {
      log.warn("Event list is empty for organization: {}", organizationId);
      return Optional.empty();
    }

    return Optional.of(mapToEvent(events.getFirst()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Event> findEventByDate(String date) {
    log.debug("Searching for event on date: {}", date);
    // The Eventbrite API doesn't support direct searching for events by date.
    // As a workaround, we retrieve all events for the organization and then filter them by their start date.
    Map<String, Object> response = eventbriteRestClient.get()
        .uri("/organizations/{orgId}/events/?order_by=start_desc", organizationId)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });

    if (response == null || !response.containsKey("events")) {
      log.warn("No events found in response for organization: {}", organizationId);
      return Optional.empty();
    }

    List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
    return events.stream()
        .map(this::mapToEvent)
        .filter(e -> e.getStartTime().toLocalDate().toString().equals(date))
        .findFirst();
  }

  @Override
  public Event copyEvent(String eventId, ZonedDateTime newStartTime, ZonedDateTime newEndTime) {
    log.debug("Copying event ID {} to new time range: {} - {}", eventId, newStartTime, newEndTime);
    String startTimeInUTC = newStartTime.withZoneSameInstant(java.time.ZoneOffset.UTC)
        .format(DATE_TIME_FORMATTER);
    String endTimeInUTC = newEndTime.withZoneSameInstant(java.time.ZoneOffset.UTC)
        .format(DATE_TIME_FORMATTER);

    Map<String, Object> request = Map.of(
        "start_date", startTimeInUTC,
        "end_date", endTimeInUTC
    );

    log.debug("POST to copy endpoint with request {}", request);
    Map<String, Object> response = eventbriteRestClient.post()
        .uri("/events/{eventId}/copy/", eventId)
        .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
        .body(toFormData(request))
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });

    return mapToEvent(Objects.requireNonNull(response));
  }

  @Override
  public Event updateEvent(String eventId, String newTitle) {
    log.debug("Updating event ID {} with new title: {}", eventId, newTitle);
    Map<String, Object> request = Map.of(
        "event", Map.of(
            "name", Map.of("html", newTitle)
        )
    );

    Map<String, Object> response = eventbriteRestClient.post()
        .uri("/events/{eventId}/", eventId)
        .body(request)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });

    return mapToEvent(Objects.requireNonNull(response));
  }

  private String toFormData(Map<String, Object> params) {
    return params.entrySet().stream()
        .map(e -> java.net.URLEncoder.encode(e.getKey(), java.nio.charset.StandardCharsets.UTF_8)
            + "=" +
            java.net.URLEncoder.encode(e.getValue().toString(),
                java.nio.charset.StandardCharsets.UTF_8))
        .collect(java.util.stream.Collectors.joining("&"));
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
