package com.yas.location.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.yas.location.model.Country;
import com.yas.location.service.CountryService;
import com.yas.location.utils.Constants;
import com.yas.location.viewmodel.country.CountryListGetVm;
import com.yas.location.viewmodel.country.CountryPostVm;
import com.yas.location.viewmodel.country.CountryVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CountryController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class CountryControllerTest {

    @MockitoBean
    private CountryService countryService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    // ==================== GET Methods ====================

    @Test
    void testGetPageableCountries_thenReturnOk() throws Exception {
        CountryListGetVm countryListGetVm = new CountryListGetVm(List.of(), 0, 10, 0, 1, true);
        given(countryService.getPageableCountries(anyInt(), anyInt())).willReturn(countryListGetVm);

        this.mockMvc.perform(get(Constants.ApiConstant.COUNTRIES_URL + "/paging")
                .param("pageNo", "0")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageNo").value(0));
    }

    @Test
    void testListCountries_thenReturnOk() throws Exception {
        CountryVm countryVm = new CountryVm(1L, "VN", "Vietnam", "VNM", true, true, true, true, true);
        given(countryService.findAllCountries()).willReturn(List.of(countryVm));

        this.mockMvc.perform(get(Constants.ApiConstant.COUNTRIES_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Vietnam"));
    }

    @Test
    void testGetCountry_whenIdExists_thenReturnOk() throws Exception {
        CountryVm countryVm = new CountryVm(1L, "VN", "Vietnam", "VNM", true, true, true, true, true);
        given(countryService.findById(1L)).willReturn(countryVm);

        this.mockMvc.perform(get(Constants.ApiConstant.COUNTRIES_URL + "/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Vietnam"));
    }

    // ==================== POST Methods ====================

    @Test
    void testCreateCountry_whenRequestIsValid_thenReturnCreated() throws Exception {
        CountryPostVm countryPostVm = CountryPostVm.builder()
            .id("VN")
            .code2("VN")
            .name("Vietnam")
            .build();

        Country country = new Country();
        country.setId(1L);
        country.setName("Vietnam");

        String request = objectWriter.writeValueAsString(countryPostVm);
        given(countryService.create(countryPostVm)).willReturn(country);

        this.mockMvc.perform(post(Constants.ApiConstant.COUNTRIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Vietnam"));
    }

    @Test
    void testCreateCountry_whenValidationFails_thenReturnBadRequest() throws Exception {
        // id is blank
        CountryPostVm countryPostVm = CountryPostVm.builder().id("").name("Name").build();
        String request = objectWriter.writeValueAsString(countryPostVm);

        this.mockMvc.perform(post(Constants.ApiConstant.COUNTRIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    // ==================== PUT Methods ====================

    @Test
    void testUpdateCountry_whenRequestIsValid_thenReturnNoContent() throws Exception {
        CountryPostVm countryPostVm = CountryPostVm.builder()
            .id("VN")
            .code2("VN")
            .name("Vietnam Updated")
            .build();

        String request = objectWriter.writeValueAsString(countryPostVm);

        this.mockMvc.perform(put(Constants.ApiConstant.COUNTRIES_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isNoContent());
    }

    // ==================== DELETE Methods ====================

    @Test
    void testDeleteCountry_thenReturnNoContent() throws Exception {
        doNothing().when(countryService).delete(1L);

        this.mockMvc.perform(delete(Constants.ApiConstant.COUNTRIES_URL + "/1"))
            .andExpect(status().isNoContent());
    }
}