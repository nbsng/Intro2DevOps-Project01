package com.yas.location.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @BeforeEach
    void setUp() {
        MessagesUtils.messageBundle = ResourceBundle.getBundle("messages.messages", Locale.getDefault());
    }

    @Test
    void getMessage_whenErrorCodeExists_shouldReturnMappedMessage() {
        
        String errorCode = "COUNTRY_NOT_FOUND";
        String param = "Vietnam";
        
        String message = MessagesUtils.getMessage(errorCode, param);
         assertThat(message).contains(param);
    }

    @Test
    void getMessage_whenErrorCodeDoesNotExist_shouldReturnErrorCodeItself() {
        String nonExistentCode = "NON_EXISTENT_ERROR_CODE";
        
        String message = MessagesUtils.getMessage(nonExistentCode);
        
        assertThat(message).isEqualTo(nonExistentCode);
    }

    @Test
    void getMessage_withMultipleParameters_shouldFormatCorrectly() {
        String errorCode = "CODE_ALREADY_EXISTED"; 
        Object[] params = {"LOC123", "Hanoi"};
        
        String message = MessagesUtils.getMessage(errorCode, params);
        
        assertThat(message).contains("LOC123");
    }

    @Test
    void testConstructor() {
        MessagesUtils messagesUtils = new MessagesUtils();
        assertThat(messagesUtils).isNotNull();
    }
}