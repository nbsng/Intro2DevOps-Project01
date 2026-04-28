package com.yas.recommendation.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmbeddingSearchConfigurationTest {

    @Test
    void constructor_and_getters_shouldWorkCorrectly() {
        EmbeddingSearchConfiguration config = new EmbeddingSearchConfiguration(0.85, 10);
        
        assertThat(config.similarityThreshold()).isEqualTo(0.85);
        assertThat(config.topK()).isEqualTo(10);
    }
}
