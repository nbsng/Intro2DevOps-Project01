package com.yas.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.commonlibrary.config.ServiceUrlConfig;
import com.yas.cart.viewmodel.ProductThumbnailVm;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ServiceUrlConfig serviceUrlConfig;

    private ProductService productService;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String BASE_URL = "http://api.yas.local/media";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        productService = new ProductService(restClient, serviceUrlConfig);
    }

    @Nested
    class GetProductsTest {
        @Test
        void getProducts_whenGivenValidIds_shouldReturnProductThumbnailVms() throws Exception {
            List<Long> ids = List.of(1L, 2L, 3L);
            String expectedUrl = BASE_URL + "/storefront/products/list-featured?productId=1&productId=2&productId=3";
            
            when(serviceUrlConfig.product()).thenReturn(BASE_URL);

            String jsonResponse = objectMapper.writeValueAsString(getProductThumbnailVms());

            mockServer.expect(requestTo(expectedUrl))
                      .andExpect(method(HttpMethod.GET))
                      .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            List<ProductThumbnailVm> result = productService.getProducts(ids);

            mockServer.verify();
            assertThat(result).hasSize(3);
            assertThat(result).extracting(ProductThumbnailVm::id).containsExactly(1L, 2L, 3L);
        }
    }

    @Nested
    class GetProductByIdTest {
        @Test
        void getProductById_whenProductExists_shouldReturnProductThumbnailVm() throws Exception {
            Long productId = 1L;
            String expectedUrl = BASE_URL + "/storefront/products/list-featured?productId=" + productId; 
            
            when(serviceUrlConfig.product()).thenReturn(BASE_URL);

            ProductThumbnailVm mockProduct = new ProductThumbnailVm(1L, "Product 1", "product-1", "http://example.com/product1.jpg");
            String jsonResponse = objectMapper.writeValueAsString(List.of(mockProduct));

            mockServer.expect(requestTo(expectedUrl))
                      .andExpect(method(HttpMethod.GET))
                      .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            ProductThumbnailVm result = productService.getProductById(productId);

            mockServer.verify();
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Product 1");
        }

        @Test
        void getProductById_whenProductNotFound_shouldReturnNull() throws Exception {
            Long productId = 99L;
            String expectedUrl = BASE_URL + "/storefront/products/list-featured?productId=" + productId; 
            
            when(serviceUrlConfig.product()).thenReturn(BASE_URL);

            String jsonResponse = objectMapper.writeValueAsString(Collections.emptyList());

            mockServer.expect(requestTo(expectedUrl))
                      .andExpect(method(HttpMethod.GET))
                      .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            ProductThumbnailVm result = productService.getProductById(productId);

            mockServer.verify();
            assertThat(result).isNull();
        }
    }

    @Nested
    class ExistsByIdTest {
        @Test
        void existsById_whenProductExists_shouldReturnTrue() throws Exception {
            Long productId = 1L;
            String expectedUrl = BASE_URL + "/storefront/products/list-featured?productId=" + productId; 
            
            when(serviceUrlConfig.product()).thenReturn(BASE_URL);

            ProductThumbnailVm mockProduct = new ProductThumbnailVm(1L, "Product 1", "product-1", "http://example.com/product1.jpg");
            String jsonResponse = objectMapper.writeValueAsString(List.of(mockProduct));

            mockServer.expect(requestTo(expectedUrl))
                      .andExpect(method(HttpMethod.GET))
                      .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            boolean result = productService.existsById(productId);

            mockServer.verify();
            assertThat(result).isTrue();
        }

        @Test
        void existsById_whenProductDoesNotExist_shouldReturnFalse() throws Exception {
            Long productId = 99L;
            String expectedUrl = BASE_URL + "/storefront/products/list-featured?productId=" + productId; 
            
            when(serviceUrlConfig.product()).thenReturn(BASE_URL);

            String jsonResponse = objectMapper.writeValueAsString(Collections.emptyList());

            mockServer.expect(requestTo(expectedUrl))
                      .andExpect(method(HttpMethod.GET))
                      .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            boolean result = productService.existsById(productId);

            mockServer.verify();
            assertThat(result).isFalse();
        }
    }

    @Nested
    class FallbackMethodsTest {
        @Test
        void handleProductThumbnailFallback_whenCalled_shouldThrowException() throws Throwable {
            Throwable dummyException = new RuntimeException("Service is down");

            assertThrows(Throwable.class, () -> productService.handleProductThumbnailFallback(dummyException));
        }
    }

    private List<ProductThumbnailVm> getProductThumbnailVms() {
        return List.of(
            new ProductThumbnailVm(1L, "Product 1", "product-1", "http://example.com/product1.jpg"),
            new ProductThumbnailVm(2L, "Product 2", "product-2", "http://example.com/product2.jpg"),
            new ProductThumbnailVm(3L, "Product 3", "product-3", "http://example.com/product3.jpg")
        );
    }
}