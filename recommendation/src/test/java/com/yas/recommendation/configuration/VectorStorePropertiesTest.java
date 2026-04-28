package com.yas.recommendation.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.PgVectorStore;

class VectorStorePropertiesTest {

    @Test
    void gettersAndSetters_shouldWorkCorrectly() {
        VectorStoreProperties properties = new VectorStoreProperties();
        
        properties.setDimensions(1536);
        properties.setDistanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE);
        properties.setIndexType(PgVectorStore.PgIndexType.HNSW);
        properties.setInitializeSchema(true);

        assertThat(properties.getDimensions()).isEqualTo(1536);
        assertThat(properties.getDistanceType()).isEqualTo(PgVectorStore.PgDistanceType.COSINE_DISTANCE);
        assertThat(properties.getIndexType()).isEqualTo(PgVectorStore.PgIndexType.HNSW);
        assertThat(properties.isInitializeSchema()).isTrue();
    }
}
