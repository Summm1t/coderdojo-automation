package be.coderdojo.ninove.coderdojo.adapter.out.eventbrite;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class EventbriteConfiguration {

    @Value("${eventbrite.api.token}")
    private String apiToken;

    @Value("${eventbrite.api.base-url:https://www.eventbriteapi.com/v3}")
    private String baseUrl;

    @Bean
    public RestClient.Builder eventbriteRestClientBuilder() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken);
    }

    @Bean
    public RestClient eventbriteRestClient(RestClient.Builder eventbriteRestClientBuilder) {
        return eventbriteRestClientBuilder.build();
    }
}
