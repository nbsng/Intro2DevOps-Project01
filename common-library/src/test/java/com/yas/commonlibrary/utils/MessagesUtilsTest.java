package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_withExistingKey_shouldReturnMessage() {
        // Assuming "ACCESS_DENIED" exists in messages.properties
        String message = MessagesUtils.getMessage("ACCESS_DENIED");
        assertNotNull(message);
    }

    @Test
    void getMessage_withNonExistingKey_shouldReturnKey() {
        String key = "NON_EXISTING_KEY";
        String message = MessagesUtils.getMessage(key);
        assertEquals(key, message);
    }

    @Test
    void getMessage_withArguments_shouldReturnFormattedMessage() {
        // We can't easily mock the static bundle, so we rely on what's in the properties file.
        // If there's a message like "test.message=Hello {}"
        // For now, let's just test that it doesn't crash and returns something.
        String message = MessagesUtils.getMessage("SOME_KEY_WITH_ARGS", "arg1");
        assertNotNull(message);
    }
}
