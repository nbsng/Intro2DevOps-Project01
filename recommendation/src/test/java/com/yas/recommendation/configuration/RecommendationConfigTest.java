package com.yas.recommendation.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RecommendationConfigTest {

    @Test
    void getter_shouldReturnApiUrl() {
        RecommendationConfig config = new RecommendationConfig();
        ReflectionTestUtils.setField(config, "apiUrl", "http://test-url");

        assertThat(config.getApiUrl()).isEqualTo("http://test-url");
    }
}
