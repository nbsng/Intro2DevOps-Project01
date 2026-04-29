package com.yas.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.recommendation.configuration.RecommendationConfig;
import com.yas.recommendation.viewmodel.ProductDetailVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.net.URI;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RecommendationConfig config;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        // Mock the RestClient builder chain
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getProductDetail_shouldReturnProductDetailVm() {
        when(config.getApiUrl()).thenReturn("http://localhost:8080");

        ProductDetailVm mockProductDetail = mock(ProductDetailVm.class);
        when(mockProductDetail.id()).thenReturn(1L);
        when(mockProductDetail.name()).thenReturn("Test Product");
        ResponseEntity<ProductDetailVm> responseEntity = ResponseEntity.ok(mockProductDetail);

        when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        ProductDetailVm result = productService.getProductDetail(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Product");
    }

    @Test
    void getProductDetail_shouldCallApiWithExpectedUri() {
        when(config.getApiUrl()).thenReturn("http://localhost:8080");
        ProductDetailVm mockProductDetail = mock(ProductDetailVm.class);
        ResponseEntity<ProductDetailVm> responseEntity = ResponseEntity.ok(mockProductDetail);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        productService.getProductDetail(99L);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(requestHeadersUriSpec).uri(uriCaptor.capture());
        assertThat(uriCaptor.getValue()).isEqualTo(URI.create("http://localhost:8080/storefront/products/detail/99"));
    }

    @Test
    void getProductDetail_whenBodyIsNull_shouldReturnNull() {
        when(config.getApiUrl()).thenReturn("http://localhost:8080");
        ResponseEntity<ProductDetailVm> responseEntity = ResponseEntity.ok(null);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        ProductDetailVm result = productService.getProductDetail(5L);

        assertThat(result).isNull();
    }
}
