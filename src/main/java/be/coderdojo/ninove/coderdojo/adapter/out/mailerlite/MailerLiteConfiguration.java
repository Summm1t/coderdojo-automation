package be.coderdojo.ninove.coderdojo.adapter.out.mailerlite;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MailerLiteConfiguration {

    @Value("${mailerlite.api.token}")
    private String apiToken;

    @Value("${mailerlite.api.base-url:https://connect.mailerlite.com/api}")
    private String baseUrl;

    @Bean
    public RestClient.Builder mailerLiteRestClientBuilder() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken);
    }

    @Bean
    public RestClient mailerLiteRestClient(RestClient.Builder mailerLiteRestClientBuilder) {
        return mailerLiteRestClientBuilder.build();
    }
}
