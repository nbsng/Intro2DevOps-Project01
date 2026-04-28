package com.yas.sampledata.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @BeforeEach
    void setUp() {
        MessagesUtils.messageBundle = ResourceBundle.getBundle("messages.messages", Locale.ENGLISH);
    }

    @Test
    void getMessage_formatsMessageWithArgs() {
        String result = MessagesUtils.getMessage("sample.greeting", "World");
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    void getMessage_returnsCodeWhenMissing() {
        String result = MessagesUtils.getMessage("missing.code");
        assertThat(result).isEqualTo("missing.code");
    }
}
