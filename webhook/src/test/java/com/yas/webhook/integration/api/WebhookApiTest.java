package com.yas.webhook.integration.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class WebhookApiTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private WebhookApi webhookApi;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void test_notify_withSecret() throws Exception {
        String url = "http://test.com";
        String secret = "secret";
        JsonNode payload = objectMapper.readTree("{\"key\":\"value\"}");

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(url)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(JsonNode.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        webhookApi.notify(url, secret, payload);

        verify(restClient).post();
        verify(requestBodyUriSpec).uri(url);
        verify(requestBodySpec).header(eq(WebhookApi.X_HUB_SIGNATURE_256), anyString());
        verify(requestBodySpec).body(payload);
        verify(requestBodySpec).retrieve();
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void test_notify_withoutSecret() throws Exception {
        String url = "http://test.com";
        JsonNode payload = objectMapper.readTree("{\"key\":\"value\"}");

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(url)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(JsonNode.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        webhookApi.notify(url, null, payload);

        verify(restClient).post();
        verify(requestBodyUriSpec).uri(url);
        verify(requestBodySpec).body(payload);
        verify(requestBodySpec).retrieve();
        verify(responseSpec).toBodilessEntity();
    }
}
