package com.yas.recommendation.vector.product.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.yas.commonlibrary.kafka.cdc.message.Product;
import com.yas.recommendation.vector.product.store.ProductVectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductVectorSyncServiceTest {

    @Mock
    private ProductVectorRepository productVectorRepository;

    @InjectMocks
    private ProductVectorSyncService productVectorSyncService;

    private Product publishedProduct;
    private Product unpublishedProduct;

    @BeforeEach
    void setUp() {
        publishedProduct = new Product();
        publishedProduct.setId(1L);
        publishedProduct.setPublished(true);

        unpublishedProduct = new Product();
        unpublishedProduct.setId(2L);
        unpublishedProduct.setPublished(false);
    }

    @Test
    void createProductVector_whenProductIsPublished_shouldAddVector() {
        productVectorSyncService.createProductVector(publishedProduct);
        verify(productVectorRepository).add(1L);
    }

    @Test
    void createProductVector_whenProductIsNotPublished_shouldDoNothing() {
        productVectorSyncService.createProductVector(unpublishedProduct);
        verifyNoInteractions(productVectorRepository);
    }

    @Test
    void updateProductVector_whenProductIsPublished_shouldUpdateVector() {
        productVectorSyncService.updateProductVector(publishedProduct);
        verify(productVectorRepository).update(1L);
    }

    @Test
    void updateProductVector_whenProductIsNotPublished_shouldDeleteVector() {
        productVectorSyncService.updateProductVector(unpublishedProduct);
        verify(productVectorRepository).delete(2L);
    }

    @Test
    void deleteProductVector_shouldDeleteVector() {
        productVectorSyncService.deleteProductVector(3L);
        verify(productVectorRepository).delete(3L);
    }
}
