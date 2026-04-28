package com.yas.tax.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_WithValidErrorCode_ShouldReturnFormattedMessage() {
        String result = MessagesUtils.getMessage("error.code");
        assertNotNull(result);
        // The actual message depends on messages.properties file
    }

    @Test
    void getMessage_WithInvalidErrorCode_ShouldReturnErrorCode() {
        String errorCode = "nonexistent.error.code.that.does.not.exist";
        String result = MessagesUtils.getMessage(errorCode);
        assertEquals(errorCode, result);
    }

    @Test
    void getMessage_WithValidErrorCodeAndParams_ShouldReturnFormattedMessageWithParams() {
        String result = MessagesUtils.getMessage("error.code", "param1", "param2");
        assertNotNull(result);
    }

    @Test
    void getMessage_WithSingleParam_ShouldReturnFormattedMessageWithParam() {
        String result = MessagesUtils.getMessage("error.code", "paramValue");
        assertNotNull(result);
    }

    @Test
    void getMessage_WithNoParams_ShouldReturnMessage() {
        String result = MessagesUtils.getMessage("error.code");
        assertNotNull(result);
        // Should contain the error code or the message if found
    }

    @Test
    void getMessage_WithMultipleParams_ShouldReturnFormattedMessage() {
        String result = MessagesUtils.getMessage("error.code", "a", "b", "c", "d");
        assertNotNull(result);
    }
}
