package be.coderdojo.ninove.coderdojo.adapter.out.eventbrite;

import be.coderdojo.ninove.coderdojo.application.port.out.EventbritePort;
import be.coderdojo.ninove.coderdojo.domain.model.Attendee;
import be.coderdojo.ninove.coderdojo.domain.model.Event;
import be.coderdojo.ninove.coderdojo.domain.model.TicketClass;
import be.coderdojo.ninove.coderdojo.domain.model.TicketType;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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
        .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
          log.warn("Organization {} not found in Eventbrite", organizationId);
        })
        .onStatus(status -> status.is4xxClientError() && status != HttpStatus.NOT_FOUND,
            (req, res) -> {
              log.error("Eventbrite API error: {} - {}", res.getStatusCode(), res.getStatusText());
              throw new HttpClientErrorException(res.getStatusCode(),
                  "Eventbrite API error: " + res.getStatusText());
            })
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
        .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
          log.warn("Organization {} not found in Eventbrite", organizationId);
        })
        .onStatus(status -> status.is4xxClientError() && status != HttpStatus.NOT_FOUND,
            (req, res) -> {
              log.error("Eventbrite API error: {} - {}", res.getStatusCode(), res.getStatusText());
              throw new HttpClientErrorException(res.getStatusCode(),
                  "Eventbrite API error: " + res.getStatusText());
            })
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
        .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
          log.error("Event {} not found for copying", eventId);
          throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Event not found");
        })
        .onStatus(status -> status.is4xxClientError() && status != HttpStatus.NOT_FOUND,
            (req, res) -> {
              log.error("Eventbrite API error: {} - {}", res.getStatusCode(), res.getStatusText());
              throw new HttpClientErrorException(res.getStatusCode(),
                  "Eventbrite API error: " + res.getStatusText());
            })
        .body(new ParameterizedTypeReference<>() {
        });

    return mapToEvent(Objects.requireNonNull(response));
  }

  @Override
  public Event updateEvent(String eventId, String newTitle, ZonedDateTime newStartTime,
      ZonedDateTime newEndTime) {
    log.debug("Updating event ID {} with new title: {} and time range: {} - {}", eventId, newTitle,
        newStartTime, newEndTime);

    String startTimeInUTC = newStartTime.withZoneSameInstant(java.time.ZoneOffset.UTC)
        .format(DATE_TIME_FORMATTER);
    String endTimeInUTC = newEndTime.withZoneSameInstant(java.time.ZoneOffset.UTC)
        .format(DATE_TIME_FORMATTER);

    Map<String, Object> request = Map.of(
        "event", Map.of(
            "name", Map.of("html", newTitle),
            "start", Map.of(
                "timezone", newStartTime.getZone().getId(),
                "utc", startTimeInUTC
            ),
            "end", Map.of(
                "timezone", newEndTime.getZone().getId(),
                "utc", endTimeInUTC
            )
        )
    );

    Map<String, Object> response = eventbriteRestClient.post()
        .uri("/events/{eventId}/", eventId)
        .body(request)
        .retrieve()
        .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
          log.error("Event {} not found for updating", eventId);
          throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Event not found");
        })
        .onStatus(status -> status.is4xxClientError() && status != HttpStatus.NOT_FOUND,
            (req, res) -> {
              log.error("Eventbrite API error: {} - {}", res.getStatusCode(), res.getStatusText());
              throw new HttpClientErrorException(res.getStatusCode(),
                  "Eventbrite API error: " + res.getStatusText());
            })
        .body(new ParameterizedTypeReference<>() {
        });

    return mapToEvent(Objects.requireNonNull(response));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<TicketClass> getTicketClasses(String eventId) {
    log.debug("Fetching ticket classes for event ID: {}", eventId);
    Map<String, Object> response = eventbriteRestClient.get()
        .uri("/events/{eventId}/ticket_classes/", eventId)
        .retrieve()
        .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
          log.error("Event {} not found when fetching ticket classes", eventId);
          throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Event not found");
        })
        .onStatus(status -> status.is4xxClientError() && status != HttpStatus.NOT_FOUND,
            (req, res) -> {
              log.error("Eventbrite API error: {} - {}", res.getStatusCode(), res.getStatusText());
              throw new HttpClientErrorException(res.getStatusCode(),
                  "Eventbrite API error: " + res.getStatusText());
            })
        .body(new ParameterizedTypeReference<>() {
        });

    if (response == null || !response.containsKey("ticket_classes")) {
      log.warn("No ticket classes found in response for event: {}", eventId);
      return List.of();
    }

    List<Map<String, Object>> ticketClassesData = (List<Map<String, Object>>) response.get(
        "ticket_classes");
    return ticketClassesData.stream()
        .map(this::mapToTicketClass)
        .toList();
  }

  @Override
  public TicketClass updateTicketClass(String eventId, String ticketClassId, int capacity,
      int quantityTotal) {
    log.debug("Updating ticket class ID {} for event ID {} with capacity {} and quantity_total {}",
        ticketClassId, eventId, capacity, quantityTotal);

    Map<String, Object> request = Map.of(
        "ticket_class", Map.of(
            "capacity", capacity,
            "quantity_total", quantityTotal
        )
    );

    Map<String, Object> response = eventbriteRestClient.post()
        .uri("/events/{eventId}/ticket_classes/{ticketClassId}/", eventId, ticketClassId)
        .body(request)
        .retrieve()
        .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
          log.error("Ticket class {} or event {} not found for updating", ticketClassId, eventId);
          throw new HttpClientErrorException(HttpStatus.NOT_FOUND,
              "Ticket class or event not found");
        })
        .onStatus(status -> status.is4xxClientError() && status != HttpStatus.NOT_FOUND,
            (req, res) -> {
              log.error("Eventbrite API error: {} - {}", res.getStatusCode(), res.getStatusText());
              throw new HttpClientErrorException(res.getStatusCode(),
                  "Eventbrite API error: " + res.getStatusText());
            })
        .body(new ParameterizedTypeReference<>() {
        });

    return mapToTicketClass(Objects.requireNonNull(response));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Attendee> getAttendees(String eventId) {
    log.debug("Fetching attendees for event: {}", eventId);
    Set<Attendee> allAttendees = new LinkedHashSet<>();
    boolean hasMoreItems = true;
    String continuation = null;

    while (hasMoreItems) {
      String uri = "/events/" + eventId + "/attendees/";
      if (continuation != null) {
        uri = uri + "?continuation=" + continuation;
      }

      Map<String, Object> response = eventbriteRestClient.get()
          .uri(uri)
          .retrieve()
          .body(new ParameterizedTypeReference<>() {
          });

      if (response != null && response.containsKey("attendees")) {
        List<Map<String, Object>> attendeesData = (List<Map<String, Object>>) response.get(
            "attendees");
        for (Map<String, Object> attendeeData : attendeesData) {
          Attendee attendee = mapToAttendee(attendeeData);
          if (attendee.getEmail() != null) {
            allAttendees.add(attendee);
          }
        }

        Map<String, Object> pagination = (Map<String, Object>) response.get("pagination");
        hasMoreItems = (boolean) pagination.get("has_more_items");
        continuation = (String) pagination.get("continuation");
      } else {
        hasMoreItems = false;
      }
    }

    return new ArrayList<>(allAttendees);
  }

  @SuppressWarnings("unchecked")
  private Attendee mapToAttendee(Map<String, Object> attendeeData) {
    Map<String, Object> profile = (Map<String, Object>) attendeeData.get("profile");
    String email = (String) profile.get("email");
    String firstName = (String) profile.get("first_name");
    String lastName = (String) profile.get("last_name");
    boolean optInFound = false;
    boolean optIn = true;

    String ticketClassName = (String) attendeeData.get("ticket_class_name");
    TicketType ticketType = TicketType.fromDescription(ticketClassName);

    if (attendeeData.containsKey("answers")) {
      List<Map<String, Object>> answers = (List<Map<String, Object>>) attendeeData.get("answers");
      for (Map<String, Object> answerMap : answers) {
        String question = (String) answerMap.get("question");
        String answer = (String) answerMap.get("answer");

        if ("Voornaam (ouder/voogd)".equals(question) && answer != null && !answer.isEmpty()) {
          firstName = answer;
        } else if ("Achternaam (ouder/voogd)".equals(question) && answer != null
            && !answer.isEmpty()) {
          lastName = answer;
        } else if (question != null && question.matches(
            ".*Mogen we jou via mail op de hoogte brengen over volgende CoderDojo-sessies\\?")) {
          optIn = answer != null && answer.startsWith("Je mag mij contacteren");
          optInFound = true;
        }
      }
    }

    if (!optInFound) {
      optIn = false;
    }

    return Attendee.builder()
        .id((String) attendeeData.get("id"))
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .optIn(optIn)
        .ticketType(ticketType)
        .build();
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

  @SuppressWarnings("unchecked")
  private TicketClass mapToTicketClass(Map<String, Object> ticketClassData) {
    if (ticketClassData.containsKey("ticket_class")) {
      ticketClassData = (Map<String, Object>) ticketClassData.get("ticket_class");
    }
    String id = (String) ticketClassData.get("id");
    String name = (String) ticketClassData.get("name");
    Integer capacity = (Integer) ticketClassData.get("capacity");
    Integer quantityTotal = (Integer) ticketClassData.get("quantity_total");

    return TicketClass.builder()
        .id(id)
        .name(name)
        .capacity(capacity)
        .quantityTotal(quantityTotal)
        .build();
  }
}
