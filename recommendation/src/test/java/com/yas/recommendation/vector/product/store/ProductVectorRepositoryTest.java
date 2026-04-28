package com.yas.recommendation.vector.product.store;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;
import com.yas.recommendation.configuration.EmbeddingSearchConfiguration;
import com.yas.recommendation.service.ProductService;
import com.yas.recommendation.vector.product.document.ProductDocument;
import com.yas.recommendation.viewmodel.ProductDetailVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ProductVectorRepositoryTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ProductService productService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmbeddingSearchConfiguration embeddingSearchConfiguration;

    @InjectMocks
    private ProductVectorRepository productVectorRepository;

    @BeforeEach
    void setUp() {
        // Manually inject dependencies that are autowired via setters in the abstract superclass
        ReflectionTestUtils.setField(productVectorRepository, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(productVectorRepository, "embeddingSearchConfiguration", embeddingSearchConfiguration);
    }

    @Test
    void add_shouldFetchEntityAndAddToVectorStore() {
        ProductDetailVm mockProduct = mock(ProductDetailVm.class);
        when(productService.getProductDetail(1L)).thenReturn(mockProduct);
        when(objectMapper.convertValue(mockProduct, Map.class)).thenReturn(new java.util.HashMap<>());

        productVectorRepository.add(1L);

        verify(vectorStore).add(anyList());
    }

    @Test
    void delete_shouldRemoveFromVectorStore() {
        productVectorRepository.delete(1L);
        verify(vectorStore).delete(anyList());
    }

    @Test
    void update_shouldDeleteAndAdd() {
        ProductDetailVm mockProduct = mock(ProductDetailVm.class);
        when(productService.getProductDetail(1L)).thenReturn(mockProduct);
        when(objectMapper.convertValue(mockProduct, Map.class)).thenReturn(new java.util.HashMap<>());

        productVectorRepository.update(1L);

        // Verify delete is called
        verify(vectorStore).delete(anyList());
        // Verify add is called
        verify(vectorStore).add(anyList());
    }

    @Test
    void search_shouldPerformSimilaritySearch() {
        ProductDetailVm mockProduct = mock(ProductDetailVm.class);
        when(productService.getProductDetail(1L)).thenReturn(mockProduct);
        when(objectMapper.convertValue(mockProduct, Map.class)).thenReturn(new java.util.HashMap<>());
        
        when(embeddingSearchConfiguration.topK()).thenReturn(5);
        when(embeddingSearchConfiguration.similarityThreshold()).thenReturn(0.8);
        
        Document mockDocument = new Document("Test Content");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(mockDocument));

        List<ProductDocument> results = productVectorRepository.search(1L);

        verify(vectorStore).similaritySearch(any(SearchRequest.class));
        assert !results.isEmpty();
    }
}
