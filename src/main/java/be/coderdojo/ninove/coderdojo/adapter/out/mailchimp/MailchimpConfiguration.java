package be.coderdojo.ninove.coderdojo.adapter.out.mailchimp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Configuration
public class MailchimpConfiguration {

    @Value("${mailchimp.api.key}")
    private String apiKey;

    @Bean
    public RestClient mailchimpRestClient() {
        String dataCenter = apiKey.substring(apiKey.lastIndexOf("-") + 1);
        String baseUrl = String.format("https://%s.api.mailchimp.com/3.0", dataCenter);
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(("user:" + apiKey).getBytes());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", authHeader)
                .build();
    }
}
