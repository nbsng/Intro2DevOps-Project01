package com.yas.recommendation.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AppConfigTest {

    @Test
    void objectMapper_shouldReturnNonNullObjectMapper() {
        AppConfig appConfig = new AppConfig();
        ObjectMapper objectMapper = appConfig.objectMapper();
        assertThat(objectMapper).isNotNull();
    }
}
