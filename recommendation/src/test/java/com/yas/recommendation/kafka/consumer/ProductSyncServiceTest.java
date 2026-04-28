package com.yas.recommendation.kafka.consumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.yas.commonlibrary.kafka.cdc.message.Operation;
import com.yas.commonlibrary.kafka.cdc.message.Product;
import com.yas.commonlibrary.kafka.cdc.message.ProductCdcMessage;
import com.yas.commonlibrary.kafka.cdc.message.ProductMsgKey;
import com.yas.recommendation.vector.product.service.ProductVectorSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductSyncServiceTest {

    @Mock
    private ProductVectorSyncService productVectorSyncService;

    @InjectMocks
    private ProductSyncService productSyncService;

    private ProductMsgKey key;
    private Product product;

    @BeforeEach
    void setUp() {
        key = new ProductMsgKey();
        key.setId(1L);

        product = new Product();
        product.setId(1L);
    }

    @Test
    void sync_whenMessageIsNull_shouldCallDeleteProductVector() {
        productSyncService.sync(key, null);
        verify(productVectorSyncService).deleteProductVector(1L);
    }

    @Test
    void sync_whenOperationIsDelete_shouldCallDeleteProductVector() {
        ProductCdcMessage message = new ProductCdcMessage();
        message.setOp(Operation.DELETE);

        productSyncService.sync(key, message);
        verify(productVectorSyncService).deleteProductVector(1L);
    }

    @Test
    void sync_whenOperationIsCreate_shouldCallCreateProductVector() {
        ProductCdcMessage message = new ProductCdcMessage();
        message.setOp(Operation.CREATE);
        message.setAfter(product);

        productSyncService.sync(key, message);
        verify(productVectorSyncService).createProductVector(product);
    }

    @Test
    void sync_whenOperationIsRead_shouldCallCreateProductVector() {
        ProductCdcMessage message = new ProductCdcMessage();
        message.setOp(Operation.READ);
        message.setAfter(product);

        productSyncService.sync(key, message);
        verify(productVectorSyncService).createProductVector(product);
    }

    @Test
    void sync_whenOperationIsUpdate_shouldCallUpdateProductVector() {
        ProductCdcMessage message = new ProductCdcMessage();
        message.setOp(Operation.UPDATE);
        message.setAfter(product);

        productSyncService.sync(key, message);
        verify(productVectorSyncService).updateProductVector(product);
    }

    @Test
    void sync_whenOperationIsUnsupported_shouldLogAndDoNothing() {
        ProductCdcMessage message = new ProductCdcMessage();
        // Null operation or an unhandled one
        message.setOp(null);
        message.setAfter(product);

        productSyncService.sync(key, message);
        verifyNoInteractions(productVectorSyncService);
    }
}
