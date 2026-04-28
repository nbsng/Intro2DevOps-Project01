package com.yas.recommendation.vector.product.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;
import com.yas.recommendation.vector.common.query.JdbcVectorService;
import com.yas.recommendation.vector.product.document.ProductDocument;
import com.yas.recommendation.viewmodel.RelatedProductVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class RelatedProductQueryTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private JdbcVectorService jdbcVectorService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RelatedProductQuery query;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(query, "jdbcVectorService", jdbcVectorService);
        ReflectionTestUtils.setField(query, "objectMapper", objectMapper);
    }

    @Test
    void constructor_shouldInitializeCorrectly() {
        assertThat(query).isNotNull();
    }

    @Test
    void similaritySearch_shouldReturnRelatedProducts() {
        Document mockDocument = new Document("content", Map.of("id", 2L));
        List<Document> documents = List.of(mockDocument);

        when(jdbcVectorService.similarityProduct(eq(1L), eq(ProductDocument.class))).thenReturn(documents);

        RelatedProductVm mockRelatedProduct = new RelatedProductVm();
        mockRelatedProduct.setProductId(2L);
        when(objectMapper.convertValue(mockDocument.getMetadata(), RelatedProductVm.class)).thenReturn(mockRelatedProduct);

        List<RelatedProductVm> results = query.similaritySearch(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getProductId()).isEqualTo(2L);
    }
}
