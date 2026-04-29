package com.yas.recommendation.kafka.consumer;

import static org.mockito.Mockito.verify;

import com.yas.commonlibrary.kafka.cdc.message.ProductCdcMessage;
import com.yas.commonlibrary.kafka.cdc.message.ProductMsgKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ProductSyncDataConsumerTest {

    @Mock
    private ProductSyncService productSyncService;

    @InjectMocks
    private ProductSyncDataConsumer productSyncDataConsumer;

    @Test
    void processMessage_shouldDelegateToProductSyncService() {
        ProductMsgKey key = new ProductMsgKey();
        ProductCdcMessage message = new ProductCdcMessage();
        MessageHeaders headers = new MessageHeaders(Map.of());

        productSyncDataConsumer.processMessage(key, message, headers);

        // Verify that the consumer properly delegated sync operation to ProductSyncService
        verify(productSyncService).sync(key, message);
    }

    @Test
    void processMessage_whenPayloadIsNull_shouldDelegateToProductSyncService() {
        ProductMsgKey key = new ProductMsgKey();
        MessageHeaders headers = new MessageHeaders(Map.of());

        productSyncDataConsumer.processMessage(key, null, headers);

        verify(productSyncService).sync(key, null);
    }
}
