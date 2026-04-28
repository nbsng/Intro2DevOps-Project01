package com.yas.recommendation.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class RestClientConfigTest {

    @Test
    void getRestClient_shouldReturnConfiguredRestClient() {
        RestClientConfig config = new RestClientConfig();
        RestClient restClient = config.getRestClient(RestClient.builder());
        
        assertThat(restClient).isNotNull();
    }
}
