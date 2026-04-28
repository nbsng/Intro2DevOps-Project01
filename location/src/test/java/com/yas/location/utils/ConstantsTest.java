package com.yas.location.utils;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import static org.assertj.core.api.Assertions.assertThat;

class ConstantsTest {

    @Test
    void testErrorCodeConstants() {
        assertThat(Constants.ErrorCode.COUNTRY_NOT_FOUND).isEqualTo("COUNTRY_NOT_FOUND");
        assertThat(Constants.ErrorCode.NAME_ALREADY_EXITED).isEqualTo("NAME_ALREADY_EXITED");
        assertThat(Constants.ErrorCode.STATE_OR_PROVINCE_NOT_FOUND).isEqualTo("STATE_OR_PROVINCE_NOT_FOUND");
        assertThat(Constants.ErrorCode.ADDRESS_NOT_FOUND).isEqualTo("ADDRESS_NOT_FOUND");
        assertThat(Constants.ErrorCode.CODE_ALREADY_EXISTED).isEqualTo("CODE_ALREADY_EXISTED");
    }

    @Test
    void testPageableConstants() {
        assertThat(Constants.PageableConstant.DEFAULT_PAGE_SIZE).isEqualTo("10");
        assertThat(Constants.PageableConstant.DEFAULT_PAGE_NUMBER).isEqualTo("0");
    }

    @Test
    void testApiConstants() {
        assertThat(Constants.ApiConstant.STATE_OR_PROVINCES_URL).isEqualTo("/backoffice/state-or-provinces");
        assertThat(Constants.ApiConstant.COUNTRIES_URL).isEqualTo("/backoffice/countries");
        assertThat(Constants.ApiConstant.CODE_200).isEqualTo("200");
        assertThat(Constants.ApiConstant.OK).isEqualTo("Ok");
    }

    @Test
    void testConstructorIsPrivate() throws Exception {
        Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Constants instance = constructor.newInstance();
        assertThat(instance).isNotNull();

        Constructor<Constants.ErrorCode> errorCodeConstructor = Constants.ErrorCode.class.getDeclaredConstructor(Constants.class);
        errorCodeConstructor.setAccessible(true);
        Constants.ErrorCode errorCodeInstance = errorCodeConstructor.newInstance(instance);
        assertThat(errorCodeInstance).isNotNull();

        Constructor<Constants.PageableConstant> pageableConstructor = Constants.PageableConstant.class.getDeclaredConstructor(Constants.class);
        pageableConstructor.setAccessible(true);
        Constants.PageableConstant pageableInstance = pageableConstructor.newInstance(instance);
        assertThat(pageableInstance).isNotNull();

        Constructor<Constants.ApiConstant> apiConstructor = Constants.ApiConstant.class.getDeclaredConstructor(Constants.class);
        apiConstructor.setAccessible(true);
        Constants.ApiConstant apiInstance = apiConstructor.newInstance(instance);
        assertThat(apiInstance).isNotNull();
    }
}