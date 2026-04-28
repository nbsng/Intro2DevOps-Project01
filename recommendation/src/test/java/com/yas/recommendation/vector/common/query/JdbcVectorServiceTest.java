package com.yas.recommendation.vector.common.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;
import com.yas.recommendation.configuration.EmbeddingSearchConfiguration;
import com.yas.recommendation.vector.product.document.ProductDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class JdbcVectorServiceTest {

    @Mock
    private JdbcTemplate jdbcClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmbeddingSearchConfiguration embeddingSearchConfiguration;

    @InjectMocks
    private JdbcVectorService jdbcVectorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jdbcVectorService, "vectorTableName", "vector_store");
    }

    @Test
    void similarityProduct_shouldReturnDocuments() {
        Document mockDocument = new Document("content");
        List<Document> mockDocuments = List.of(mockDocument);

        when(jdbcClient.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(mockDocuments);

        List<Document> results = jdbcVectorService.similarityProduct(1L, ProductDocument.class);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).isEqualTo("content");
    }
}
