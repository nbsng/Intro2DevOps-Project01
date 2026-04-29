package com.yas.rating.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_whenKeyExists_shouldFormatMessage() {
        String message = MessagesUtils.getMessage(Constants.ErrorCode.RATING_NOT_FOUND, 5L);

        assertEquals("RATING 5 is not found", message);
    }

    @Test
    void getMessage_whenKeyMissing_shouldReturnKey() {
        String message = MessagesUtils.getMessage("UNKNOWN_KEY");

        assertEquals("UNKNOWN_KEY", message);
    }
}
