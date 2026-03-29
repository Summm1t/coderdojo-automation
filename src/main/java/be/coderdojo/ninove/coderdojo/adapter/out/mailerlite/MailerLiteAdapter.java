package be.coderdojo.ninove.coderdojo.adapter.out.mailerlite;

import be.coderdojo.ninove.coderdojo.application.port.out.MailerLitePort;
import be.coderdojo.ninove.coderdojo.domain.model.Campaign;
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
        .uri("/campaigns?limit=100")
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
  public Campaign createCampaign(String title, String content, boolean debug) {
    log.debug("Creating new campaign with title: {}, debug: {}", title, debug);

    Map<String, Object> request = Map.of(
        "name", title,
        "type", "regular",
        "emails", List.of(Map.of(
            "subject", title,
            "content", content
        ))
    );

    if (debug) {
      log.info("DEBUG MODE: POST to /campaigns skipped. Title: {}, Content preview: {}", title,
          content.substring(0, Math.min(content.length(), 100)));
      return Campaign.builder()
          .id("DEBUG-ID")
          .title(title)
          .content(content)
          .status("draft")
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

  private Campaign mapToCampaign(Map<String, Object> data) {
    String id = (String) data.get("id");
    String name = (String) data.get("name");
    String status = (String) data.get("status");

    // Content might be inside emails list for regular campaigns
    String content = "";
    if (data.containsKey("emails")) {
      List<Map<String, Object>> emails = (List<Map<String, Object>>) data.get("emails");
      if (emails != null && !emails.isEmpty()) {
        content = (String) emails.get(0).get("content");
      }
    }

    return Campaign.builder()
        .id(id)
        .title(name)
        .content(content)
        .status(status)
        .build();
  }
}
