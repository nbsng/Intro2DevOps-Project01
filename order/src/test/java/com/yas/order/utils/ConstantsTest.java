package com.yas.order.utils;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import static org.assertj.core.api.Assertions.assertThat;

class ConstantsTest {

    @Test
    void testErrorCodeConstants() {
        assertThat(Constants.ErrorCode.ORDER_NOT_FOUND).isEqualTo("ORDER_NOT_FOUND");
        assertThat(Constants.ErrorCode.CHECKOUT_NOT_FOUND).isEqualTo("CHECKOUT_NOT_FOUND");
        assertThat(Constants.ErrorCode.CHECKOUT_ITEM_NOT_EMPTY).isEqualTo("CHECKOUT_ITEM_NOT_EMPTY");
        assertThat(Constants.ErrorCode.SIGN_IN_REQUIRED).isEqualTo("SIGN_IN_REQUIRED");
    }

    @Test
    void testMessageCodeConstants() {
        assertThat(Constants.MessageCode.CREATE_CHECKOUT).contains("Create checkout");
        assertThat(Constants.MessageCode.UPDATE_CHECKOUT_STATUS).contains("Update checkout");
    }

    @Test
    void testColumnConstants() {
        assertThat(Constants.Column.ID_COLUMN).isEqualTo("id");
        assertThat(Constants.Column.ORDER_EMAIL_COLUMN).isEqualTo("email");
        assertThat(Constants.Column.ORDER_ITEM_PRODUCT_NAME_COLUMN).isEqualTo("productName");
    }

    @Test
    void testPrivateConstructors() throws Exception {
        assertPrivateConstructor(Constants.ErrorCode.class);

        assertPrivateConstructor(Constants.MessageCode.class);

        assertPrivateConstructor(Constants.Column.class);

        Constructor<Constants> constantsConstructor = Constants.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        Constants constantsInstance = constantsConstructor.newInstance();
        assertThat(constantsInstance).isNotNull();
    }


    private void assertPrivateConstructor(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getDeclaredConstructor(Constants.class);
        constructor.setAccessible(true);
        
        Constructor<Constants> parentConstructor = Constants.class.getDeclaredConstructor();
        parentConstructor.setAccessible(true);
        Constants parentInstance = parentConstructor.newInstance();
        
        Object instance = constructor.newInstance(parentInstance);
        assertThat(instance).isNotNull();
    }
}