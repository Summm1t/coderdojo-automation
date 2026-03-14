package be.coderdojo.ninove.coderdojo.adapter.out.mailchimp;

import be.coderdojo.ninove.coderdojo.application.port.out.MailchimpPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailchimpAdapter implements MailchimpPort {

    private final RestClient mailchimpRestClient;

    @Value("${mailchimp.api.list-id}")
    private String listId;

    @Override
    public boolean tagUser(String email, List<String> tags, boolean debug) {
        String subscriberHash = computeSubscriberHash(email);
        log.debug("Tagging user {} (hash: {}) with tags: {}", email, subscriberHash, tags);

        List<Map<String, String>> tagObjects = tags.stream()
                .map(tag -> Map.of("name", tag, "status", "active"))
                .collect(Collectors.toList());

        String uri = "/lists/{listId}/members/{subscriberHash}/tags";
        Map<String, Object> body = Map.of("tags", tagObjects);

        if (debug) {
            log.info("[DEBUG] URI: {}", uri.replace("{listId}", listId).replace("{subscriberHash}", subscriberHash));
            log.info("[DEBUG] Request Body: {}", body);
            return true;
        } else {
            log.info("Sending tag request to Mailchimp for user {}", email);
            try {
                mailchimpRestClient.post()
                        .uri(uri, listId, subscriberHash)
                        .body(body)
                        .retrieve()
                        .onStatus(status -> status == HttpStatus.NOT_FOUND, (request, response) -> {
                            log.warn("User {} not found in Mailchimp list {}", email, listId);
                            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found in Mailchimp");
                        })
                        .onStatus(status -> status.is4xxClientError() && status != HttpStatus.NOT_FOUND, (request, response) -> {
                            log.error("Mailchimp API error: {} - {}", response.getStatusCode(), response.getStatusText());
                            throw new HttpClientErrorException(response.getStatusCode(), "Mailchimp API error: " + response.getStatusText());
                        })
                        .toBodilessEntity();
                return true;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    log.error("Could not tag user {}: Not found in Mailchimp", email);
                    return false;
                }
                throw e;
            }
        }
    }

    @Override
    public boolean unsubscribeUser(String email, boolean debug) {
        String subscriberHash = computeSubscriberHash(email);
        log.debug("Unsubscribing user {} (hash: {})", email, subscriberHash);

        String uri = "/lists/{listId}/members/{subscriberHash}";
        Map<String, Object> body = Map.of("status", "unsubscribed");

        if (debug) {
            log.info("[DEBUG] URI: {}", uri.replace("{listId}", listId).replace("{subscriberHash}", subscriberHash));
            log.info("[DEBUG] Request Body: {}", body);
            return true;
        } else {
            log.info("Sending unsubscribe request to Mailchimp for user {}", email);
            try {
                mailchimpRestClient.patch()
                        .uri(uri, listId, subscriberHash)
                        .body(body)
                        .retrieve()
                        .onStatus(status -> status == HttpStatus.NOT_FOUND, (request, response) -> {
                            log.warn("User {} not found in Mailchimp list {}", email, listId);
                            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found in Mailchimp");
                        })
                        .onStatus(status -> status.is4xxClientError() && status != HttpStatus.NOT_FOUND, (request, response) -> {
                            log.error("Mailchimp API error: {} - {}", response.getStatusCode(), response.getStatusText());
                            throw new HttpClientErrorException(response.getStatusCode(), "Mailchimp API error: " + response.getStatusText());
                        })
                        .toBodilessEntity();
                return true;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    log.warn("Could not unsubscribe user {}: Not found in Mailchimp", email);
                    return false;
                }
                throw e;
            }
        }
    }

    private String computeSubscriberHash(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(email.trim().toLowerCase().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
