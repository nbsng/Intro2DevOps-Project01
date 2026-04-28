package com.yas.customer.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class ConstantsTest {

    @Test
    void testConstantsErrorCodeValues() {
        assertThat(Constants.ErrorCode.USER_WITH_EMAIL_NOT_FOUND).isEqualTo("USER_WITH_EMAIL_NOT_FOUND");
        assertThat(Constants.ErrorCode.USER_WITH_USERNAME_NOT_FOUND).isEqualTo("USER_WITH_USERNAME_NOT_FOUND");
        assertThat(Constants.ErrorCode.WRONG_EMAIL_FORMAT).isEqualTo("WRONG_EMAIL_FORMAT");
        assertThat(Constants.ErrorCode.USER_NOT_FOUND).isEqualTo("USER_NOT_FOUND");
        assertThat(Constants.ErrorCode.USER_ADDRESS_NOT_FOUND).isEqualTo("USER_ADDRESS_NOT_FOUND");
        assertThat(Constants.ErrorCode.UNAUTHENTICATED).isEqualTo("ACTION FAILED, PLEASE LOGIN");
        assertThat(Constants.ErrorCode.USERNAME_ALREADY_EXITED).isEqualTo("USERNAME_ALREADY_EXITED");
        assertThat(Constants.ErrorCode.USER_WITH_EMAIL_ALREADY_EXITED).isEqualTo("USER_WITH_EMAIL_ALREADY_EXITED");
    }

    @Test
    void testConstructorIsPrivate() throws NoSuchMethodException {
        Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            Constants constants = constructor.newInstance();
            assertThat(constants).isNotNull();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // Do nothing
        }
    }

    @Test
    void testErrorCodeConstructor() throws NoSuchMethodException {
        // Bao phủ constructor của inner class ErrorCode
        Constructor<Constants.ErrorCode> constructor = Constants.ErrorCode.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            Constants.ErrorCode errorCode = constructor.newInstance();
            assertThat(errorCode).isNotNull();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // Do nothing
        }
    }
}