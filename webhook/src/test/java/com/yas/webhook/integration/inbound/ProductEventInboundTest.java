package com.yas.webhook.integration.inbound;

import static org.mockito.Mockito.verify;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.yas.webhook.service.ProductEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductEventInboundTest {

    @Mock
    private ProductEventService productEventService;

    @InjectMocks
    private ProductEventInbound productEventInbound;

    @Test
    void test_onProductEvent() {
        JsonNode payload = new ObjectMapper().createObjectNode();
        productEventInbound.onProductEvent(payload);
        verify(productEventService).onProductEvent(payload);
    }
}
