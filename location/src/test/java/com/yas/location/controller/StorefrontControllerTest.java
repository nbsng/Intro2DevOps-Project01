package com.yas.location.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import com.yas.location.service.CountryService;
import com.yas.location.service.DistrictService;
import com.yas.location.service.StateOrProvinceService;
import com.yas.location.utils.Constants;
import com.yas.location.viewmodel.country.CountryVm;
import com.yas.location.viewmodel.district.DistrictGetVm;
import com.yas.location.viewmodel.stateorprovince.StateOrProvinceVm;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
    CountryStorefrontController.class,
    DistrictStorefrontController.class,
    StateOrProvinceStoreFrontController.class
}, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class StorefrontControllerTest {

    @MockitoBean
    private CountryService countryService;

    @MockitoBean
    private DistrictService districtService;

    @MockitoBean
    private StateOrProvinceService stateOrProvinceService;

    @Autowired
    private MockMvc mockMvc;

    // ==================== CountryStorefrontController tests ====================

    @Test
    void testListCountries_whenCountriesExist_thenReturnOk() throws Exception {
        CountryVm country = new CountryVm(1L, "US", "United States", "USA", true, true, true, true, true);
        given(countryService.findAllCountries()).willReturn(List.of(country));

        this.mockMvc.perform(get(Constants.ApiConstant.COUNTRIES_STOREFRONT_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].code2").value("US"))
            .andExpect(jsonPath("$[0].name").value("United States"));
    }

    @Test
    void testListCountries_whenNoCountries_thenReturnOk() throws Exception {
        given(countryService.findAllCountries()).willReturn(Collections.emptyList());

        this.mockMvc.perform(get(Constants.ApiConstant.COUNTRIES_STOREFRONT_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testListCountries_whenMultipleCountries_thenReturnOk() throws Exception {
        CountryVm us = new CountryVm(1L, "US", "United States", "USA", true, true, true, true, true);
        CountryVm vn = new CountryVm(2L, "VN", "Vietnam", "VNM", true, true, true, true, true);
        given(countryService.findAllCountries()).willReturn(List.of(us, vn));

        this.mockMvc.perform(get(Constants.ApiConstant.COUNTRIES_STOREFRONT_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("United States"))
            .andExpect(jsonPath("$[1].name").value("Vietnam"));
    }

    // ==================== StateOrProvinceStoreFrontController tests ====================

    @Test
    void testGetStateOrProvinceByCountryId_whenStateOrProvincesExist_thenReturnOk() throws Exception {
        StateOrProvinceVm stateOrProvince = new StateOrProvinceVm(1L, "California", "CA", "state", 1L);
        given(stateOrProvinceService.getAllByCountryId(1L)).willReturn(List.of(stateOrProvince));

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_STOREFRONT_URL + "/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("California"))
            .andExpect(jsonPath("$[0].code").value("CA"));
    }

    @Test
    void testGetStateOrProvinceByCountryId_whenNoStateOrProvinces_thenReturnOk() throws Exception {
        given(stateOrProvinceService.getAllByCountryId(999L)).willReturn(Collections.emptyList());

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_STOREFRONT_URL + "/999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetStateOrProvinceByCountryId_whenMultipleStateOrProvinces_thenReturnOk() throws Exception {
        StateOrProvinceVm ca = new StateOrProvinceVm(1L, "California", "CA", "state", 1L);
        StateOrProvinceVm tx = new StateOrProvinceVm(2L, "Texas", "TX", "state", 1L);
        given(stateOrProvinceService.getAllByCountryId(1L)).willReturn(List.of(ca, tx));

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_STOREFRONT_URL + "/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].code").value("CA"))
            .andExpect(jsonPath("$[1].code").value("TX"));
    }

    // ==================== DistrictStorefrontController tests ====================

    @Test
    void testGetDistrictsByStateOrProvinceId_whenDistrictsExist_thenReturnOk() throws Exception {
        DistrictGetVm district = new DistrictGetVm(1L, "Los Angeles");
        given(districtService.getList(1L)).willReturn(List.of(district));

        this.mockMvc.perform(get("/storefront/district/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Los Angeles"));
    }

    @Test
    void testGetDistrictsByStateOrProvinceId_whenNoDistricts_thenReturnOk() throws Exception {
        given(districtService.getList(999L)).willReturn(Collections.emptyList());

        this.mockMvc.perform(get("/storefront/district/999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetDistrictsByStateOrProvinceId_whenMultipleDistricts_thenReturnOk() throws Exception {
        DistrictGetVm d1 = new DistrictGetVm(1L, "District 1");
        DistrictGetVm d2 = new DistrictGetVm(2L, "District 2");
        given(districtService.getList(1L)).willReturn(List.of(d1, d2));

        this.mockMvc.perform(get("/storefront/district/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("District 1"))
            .andExpect(jsonPath("$[1].name").value("District 2"));
    }

    @Test
    void testGetDistrictsByStateOrProvinceId_viaBackofficePath_thenReturnOk() throws Exception {
        DistrictGetVm district = new DistrictGetVm(1L, "Test District");
        given(districtService.getList(5L)).willReturn(List.of(district));

        this.mockMvc.perform(get("/backoffice/district/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Test District"));
    }
}