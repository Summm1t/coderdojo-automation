package be.coderdojo.ninove.coderdojo.adapter.out.mailchimp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MailchimpAdapterTest {

    private MailchimpAdapter adapter;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("https://us20.api.mailchimp.com/3.0");
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        adapter = new MailchimpAdapter(restClientBuilder.build());
        org.springframework.test.util.ReflectionTestUtils.setField(adapter, "listId", "test-list-id");
    }

    @Test
    void tagUser_shouldSendCorrectRequest() {
        String email = "test@example.com";
        // md5("test@example.com") = 55502f40dc8b7c769880b10874abc9d0
        String hash = "55502f40dc8b7c769880b10874abc9d0";

        server.expect(requestTo("https://us20.api.mailchimp.com/3.0/lists/test-list-id/members/" + hash + "/tags"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"tags\":[{\"name\":\"Removed_manually\",\"status\":\"active\"}]}"))
                .andRespond(withSuccess());

        adapter.tagUser(email, List.of("Removed_manually"), false);

        server.verify();
    }

    @Test
    void tagUser_withDebug_shouldNotSendRequest() {
        String email = "test@example.com";
        // No server expectation set, if it tries to call it will fail.
        adapter.tagUser(email, List.of("Removed_manually"), true);
    }

    @Test
    void unsubscribeUser_shouldSendCorrectRequest() {
        String email = "test@example.com";
        String hash = "55502f40dc8b7c769880b10874abc9d0";

        server.expect(requestTo("https://us20.api.mailchimp.com/3.0/lists/test-list-id/members/" + hash))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().json("{\"status\":\"unsubscribed\"}"))
                .andRespond(withSuccess());

        adapter.unsubscribeUser(email, false);

        server.verify();
    }

    @Test
    void unsubscribeUser_withDebug_shouldNotSendRequest() {
        String email = "test@example.com";
        // No server expectation set
        adapter.unsubscribeUser(email, true);
    }

    @Test
    void tagUser_shouldLogAndNotThrowExceptionOn404() {
        String email = "notfound@example.com";
        
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/tags")))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        // Should not throw exception anymore as it is caught inside the adapter
        boolean result = adapter.tagUser(email, List.of("tag"), false);
        
        assertThat(result).isFalse();
        server.verify();
    }

    @Test
    void unsubscribeUser_shouldLogAndNotThrowExceptionOn404() {
        String email = "notfound@example.com";
        
        server.expect(requestTo(org.hamcrest.Matchers.endsWith("/notfound@example.com".toLowerCase()))) // this is wrong hash but for matcher it is fine if I use contains
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        // Use a better matcher
        server.reset();
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/members/")))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        boolean result = adapter.unsubscribeUser(email, false);
        
        assertThat(result).isFalse();
        server.verify();
    }
    
    @Test
    void tagUser_shouldThrowExceptionOn401() {
        String email = "unauthorized@example.com";
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/tags")))
                .andRespond(withStatus(org.springframework.http.HttpStatus.UNAUTHORIZED));
        
        assertThrows(org.springframework.web.client.HttpClientErrorException.class, () -> {
            adapter.tagUser(email, List.of("tag"), false);
        });
        server.verify();
    }

    @Test
    void tagUser_shouldThrowExceptionOn400() {
        String email = "badrequest@example.com";
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/tags")))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST));
        
        assertThrows(org.springframework.web.client.HttpClientErrorException.class, () -> {
            adapter.tagUser(email, List.of("tag"), false);
        });
        server.verify();
    }
}
