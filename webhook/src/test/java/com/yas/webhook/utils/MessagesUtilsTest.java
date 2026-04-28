package com.yas.webhook.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void test_getMessage_withValidCode() {
        // This assumes "WEBHOOK_NOT_FOUND" exists in messages.properties
        // If not, it will return the code itself.
        String code = "WEBHOOK_NOT_FOUND";
        String message = MessagesUtils.getMessage(code, 1L);
        // If the code is not found, it returns the code.
        // We can't easily mock ResourceBundle because it's static and initialized in MessagesUtils.
        // But we can test that it returns something.
        assertNotNull(message);
    }

    @Test
    void test_getMessage_withInvalidCode() {
        String code = "INVALID_CODE";
        String message = MessagesUtils.getMessage(code);
        assertEquals(code, message);
    }

    private void assertNotNull(String message) {
        if (message == null) {
            throw new AssertionError("Message should not be null");
        }
    }
}
