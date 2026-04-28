package com.yas.commonlibrary.kafka.cdc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;

class BaseCdcConsumerTest {

    private static class TestCdcConsumer extends BaseCdcConsumer<String, String> {
    }

    @Test
    @SuppressWarnings("unchecked")
    void processMessage_withConsumer_shouldCallAccept() {
        TestCdcConsumer consumer = new TestCdcConsumer();
        String record = "test-record";
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(KafkaHeaders.RECEIVED_KEY, "test-key");
        MessageHeaders headers = new MessageHeaders(headerMap);
        Consumer<String> mockConsumer = mock(Consumer.class);

        consumer.processMessage(record, headers, mockConsumer);

        verify(mockConsumer).accept(record);
    }

    @Test
    @SuppressWarnings("unchecked")
    void processMessage_withBiConsumer_shouldCallAccept() {
        TestCdcConsumer consumer = new TestCdcConsumer();
        String key = "test-key";
        String value = "test-value";
        Map<String, Object> headerMap = new HashMap<>();
        MessageHeaders headers = new MessageHeaders(headerMap);
        BiConsumer<String, String> mockBiConsumer = mock(BiConsumer.class);

        consumer.processMessage(key, value, headers, mockBiConsumer);

        verify(mockBiConsumer).accept(key, value);
    }
}
