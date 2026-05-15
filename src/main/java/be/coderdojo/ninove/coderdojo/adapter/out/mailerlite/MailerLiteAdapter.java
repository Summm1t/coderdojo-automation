package be.coderdojo.ninove.coderdojo.adapter.out.mailerlite;

import be.coderdojo.ninove.coderdojo.application.port.out.MailerLitePort;
import be.coderdojo.ninove.coderdojo.domain.model.Attendee;
import be.coderdojo.ninove.coderdojo.domain.model.Campaign;
import be.coderdojo.ninove.coderdojo.domain.model.TicketType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailerLiteAdapter implements MailerLitePort {

  private final RestClient mailerLiteRestClient;

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Campaign> findLatestCampaign() {
    log.debug("Fetching latest campaigns from MailerLite");
    Map<String, Object> response = mailerLiteRestClient.get()
        .uri("/campaigns?limit=1&status=sent")
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });

    if (response == null || !response.containsKey("data")) {
      return Optional.empty();
    }

    List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
    if (data == null || data.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(mapToCampaign(data.get(0)));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Campaign> findCampaignByTitle(String title) {
    log.debug("Searching for campaign with title: {}", title);
    // Note: MailerLite API filter for name is not allowed, so we need to search by title
    throw new UnsupportedOperationException("Searching for campaign by title is not yet implemented.");
//    Map<String, Object> response = mailerLiteRestClient.get()
//        .uri("/campaigns?limit=1", title)
//        .retrieve()
//        .body(new ParameterizedTypeReference<>() {
//        });
//
//    if (response == null || !response.containsKey("data")) {
//      return Optional.empty();
//    }
//
//    List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
//    if (data == null || data.isEmpty()) {
//      return Optional.empty();
//    }
//
//    return Optional.of(mapToCampaign(data.get(0)));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Campaign> getCampaignDetails(String campaignId) {
    log.debug("Fetching details for campaign: {}", campaignId);
    Map<String, Object> response = mailerLiteRestClient.get()
        .uri("/campaigns/{campaignId}", campaignId)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {
        });

    if (response == null || !response.containsKey("data")) {
      return Optional.empty();
    }

    return Optional.of(mapToCampaign((Map<String, Object>) response.get("data")));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Campaign createCampaign(String title, String content, String fromName, String fromEmail, String subject,
                                 List<String> groups, List<String> segments, String languageId, Map<String, Object> settings, boolean debug) {
    log.debug("Creating new campaign with title: {}, debug: {}", title, debug);

    Map<String, Object> emailRequest = Map.of(
        "subject", subject != null ? subject : title,
//        We cannot set the content, because we get the error "Content submission is only available on advanced plan"
//        "content", content,
        "from_name", fromName,
        "from", fromEmail
    );

    java.util.HashMap<String, Object> request = new java.util.HashMap<>();
    request.put("name", title);
    request.put("type", "regular");
    request.put("emails", List.of(emailRequest));
    if (groups != null && !groups.isEmpty()) {
      request.put("groups", groups);
    }
    if (segments != null && !segments.isEmpty()) {
      request.put("segments", segments);
    }
    if (languageId != null) {
      request.put("language_id", languageId);
    }
    if (settings != null && !settings.isEmpty()) {
      request.put("settings", settings);
    }

    if (debug) {
      log.info("DEBUG MODE: POST to /campaigns skipped. Title: {}, Content preview: {}", title,
          content.substring(0, Math.min(content.length(), 100)));
      return Campaign.builder()
          .id("DEBUG-ID")
          .title(title)
          .content(content)
          .status("draft")
          .fromName(fromName)
          .fromEmail(fromEmail)
          .subject(subject != null ? subject : title)
          .groups(groups)
          .segments(segments)
          .languageId(languageId)
          .settings(settings)
          .build();
    } else {
      Map<String, Object> response = mailerLiteRestClient.post()
          .uri("/campaigns")
          .body(request)
          .retrieve()
          .body(new ParameterizedTypeReference<>() {
          });
      return mapToCampaign((Map<String, Object>) response.get("data"));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Attendee> findSubscriberByEmail(String email) {
    log.debug("Searching for subscriber with email: {}", email);
    try {
      Map<String, Object> response = mailerLiteRestClient.get()
          .uri("/subscribers/{email}", email)
          .retrieve()
          .body(new ParameterizedTypeReference<>() {
          });

      if (response == null || !response.containsKey("data")) {
        return Optional.empty();
      }

      return Optional.of(mapToAttendee((Map<String, Object>) response.get("data")));
    } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Attendee createSubscriber(Attendee attendee, boolean debug) {
    log.debug("Creating new subscriber with email: {}, debug: {}", attendee.getEmail(), debug);
    Map<String, Object> fieldsMap = new java.util.HashMap<>(Map.of(
        "name", attendee.getFirstName(),
        "last_name", attendee.getLastName()
    ));
    if (attendee.getTicketType() != null) {
      fieldsMap.put("ticket_class", attendee.getTicketType().getDescription());
    }

    Map<String, Object> request = Map.of(
        "email", attendee.getEmail(),
        "fields", fieldsMap
    );

    if (debug) {
      log.info("DEBUG MODE: POST to /subscribers skipped for email: {}", attendee.getEmail());
      return attendee;
    } else {
      Map<String, Object> response = mailerLiteRestClient.post()
          .uri("/subscribers")
          .body(request)
          .retrieve()
          .body(new ParameterizedTypeReference<>() {
          });
      return mapToAttendee((Map<String, Object>) response.get("data"));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Attendee updateSubscriber(Attendee attendee, boolean debug) {
    log.debug("Updating subscriber with email: {}, debug: {}", attendee.getEmail(), debug);
    Map<String, Object> fieldsMap = new java.util.HashMap<>(Map.of(
        "name", attendee.getFirstName(),
        "last_name", attendee.getLastName()
    ));
    if (attendee.getTicketType() != null) {
      fieldsMap.put("ticket_type", attendee.getTicketType().getDescription());
    }

    Map<String, Object> request = Map.of(
        "fields", fieldsMap
    );

    if (debug) {
      log.info("DEBUG MODE: PUT to /subscribers skipped for email: {}", attendee.getEmail());
      return attendee;
    } else {
      Map<String, Object> response = mailerLiteRestClient.put()
          .uri("/subscribers/{email}", attendee.getEmail())
          .body(request)
          .retrieve()
          .body(new ParameterizedTypeReference<>() {
          });
      return mapToAttendee((Map<String, Object>) response.get("data"));
    }
  }

  @Override
  public void optOutSubscriber(String email, boolean debug) {
    log.debug("Opting out subscriber with email: {}, debug: {}", email, debug);
    Map<String, Object> request = Map.of(
        "status", "unsubscribed"
    );

    if (debug) {
      log.info("DEBUG MODE: PUT to /subscribers (unsubscribe) skipped for email: {}", email);
    } else {
      mailerLiteRestClient.put()
          .uri("/subscribers/{email}", email)
          .body(request)
          .retrieve()
          .toBodilessEntity();
    }
  }

  @SuppressWarnings("unchecked")
  private Attendee mapToAttendee(Map<String, Object> data) {
    Map<String, Object> fields = (Map<String, Object>) data.get("fields");
    String status = (String) data.get("status");
    return Attendee.builder()
        .id((String) data.get("id"))
        .email((String) data.get("email"))
        .firstName((String) fields.get("name"))
        .lastName((String) fields.get("last_name"))
        .ticketType(TicketType.fromDescription((String) fields.get("ticket_class")))
        .optIn(!"unsubscribed".equalsIgnoreCase(status))
        .build();
  }

  @SuppressWarnings("unchecked")
  private Campaign mapToCampaign(Map<String, Object> data) {
    String id = (String) data.get("id");
    String name = (String) data.get("name");
    String status = (String) data.get("status");
    String languageId = null;
    if (data.containsKey("language_id")) {
      Object langIdObj = data.get("language_id");
      languageId = langIdObj != null ? langIdObj.toString() : null;
    }

    List<String> groups = (List<String>) data.get("groups");
    List<String> segments = (List<String>) data.get("segments");
    Map<String, Object> settings = (Map<String, Object>) data.get("settings");

    // Content might be inside emails list for regular campaigns
    String content = "";
    String fromName = null;
    String fromEmail = null;
    String subject = null;

    if (data.containsKey("emails")) {
      List<Map<String, Object>> emails = (List<Map<String, Object>>) data.get("emails");
      if (emails != null && !emails.isEmpty()) {
        Map<String, Object> firstEmail = emails.get(0);
        content = (String) firstEmail.get("content");
        fromName = (String) firstEmail.get("from_name");
        fromEmail = (String) firstEmail.get("from");
        subject = (String) firstEmail.get("subject");
      }
    }

    return Campaign.builder()
        .id(id)
        .title(name)
        .content(content)
        .status(status)
        .fromName(fromName)
        .fromEmail(fromEmail)
        .subject(subject)
        .groups(groups)
        .segments(segments)
        .languageId(languageId)
        .settings(settings)
        .build();
  }
}
