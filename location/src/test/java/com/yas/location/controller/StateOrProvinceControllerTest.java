package com.yas.location.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import com.yas.location.model.Country;
import com.yas.location.model.StateOrProvince;
import com.yas.location.service.StateOrProvinceService;
import com.yas.location.utils.Constants;
import com.yas.location.viewmodel.stateorprovince.StateOrProvinceAndCountryGetNameVm;
import com.yas.location.viewmodel.stateorprovince.StateOrProvinceListGetVm;
import com.yas.location.viewmodel.stateorprovince.StateOrProvincePostVm;
import com.yas.location.viewmodel.stateorprovince.StateOrProvinceVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StateOrProvinceController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class StateOrProvinceControllerTest {

    @MockitoBean
    private StateOrProvinceService stateOrProvinceService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Test
    void testCreateStateOrProvince_whenRequestIsValid_thenReturnOk() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("name")
            .code("code")
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);
        StateOrProvince stateOrProvince = StateOrProvince.builder()
            .id(1L)
            .country(Country.builder().id(1L).build())
            .build();
        given(stateOrProvinceService.createStateOrProvince(stateOrProvincePostVm)).willReturn(
            stateOrProvince);

        this.mockMvc.perform(post(Constants.ApiConstant.STATE_OR_PROVINCES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated());
    }

    @Test
    void testCreateStateOrProvince_whenCodeIsOverMaxLength_thenReturnBadRequest() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("name")
            .code("1234567890".repeat(26))
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(post(Constants.ApiConstant.STATE_OR_PROVINCES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateStateOrProvince_whenNameIsBlank_thenReturnBadRequest() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("")
            .code("code")
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(post(Constants.ApiConstant.STATE_OR_PROVINCES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateStateOrProvince_whenRequestIsValid_thenReturnOk() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("name")
            .code("code")
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(put(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateStateOrProvince_whenCodeIsOverMaxLength_thenReturnBadRequest() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("name")
            .code("1234567890".repeat(26))
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(put(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateStateOrProvince_whenNameIsBlank_thenReturnBadRequest() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("")
            .code("code")
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(put(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    // ==================== GET /paging tests ====================

    @Test
    void testGetPageableStateOrProvinces_whenNoParams_thenReturnOk() throws Exception {
        StateOrProvinceListGetVm response = new StateOrProvinceListGetVm(
            Collections.emptyList(), 0, 20, 0, 0, true);
        given(stateOrProvinceService.getPageableStateOrProvinces(0, 20, null)).willReturn(response);

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/paging"))
            .andExpect(status().isOk());
    }

    @Test
    void testGetPageableStateOrProvinces_whenWithCountryId_thenReturnOk() throws Exception {
        StateOrProvinceListGetVm response = new StateOrProvinceListGetVm(
            Collections.emptyList(), 0, 20, 0, 0, true);
        given(stateOrProvinceService.getPageableStateOrProvinces(0, 20, 1L)).willReturn(response);

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/paging")
                .param("countryId", "1"))
            .andExpect(status().isOk());
    }

    @Test
    void testGetPageableStateOrProvinces_whenWithPageNoAndPageSize_thenReturnOk() throws Exception {
        StateOrProvinceListGetVm response = new StateOrProvinceListGetVm(
            Collections.emptyList(), 2, 50, 0, 0, true);
        given(stateOrProvinceService.getPageableStateOrProvinces(2, 50, null)).willReturn(response);

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/paging")
                .param("pageNo", "2")
                .param("pageSize", "50"))
            .andExpect(status().isOk());
    }

    @Test
    void testGetPageableStateOrProvinces_whenWithAllParams_thenReturnOk() throws Exception {
        StateOrProvinceListGetVm response = new StateOrProvinceListGetVm(
            Collections.emptyList(), 1, 10, 0, 0, true);
        given(stateOrProvinceService.getPageableStateOrProvinces(1, 10, 5L)).willReturn(response);

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/paging")
                .param("pageNo", "1")
                .param("pageSize", "10")
                .param("countryId", "5"))
            .andExpect(status().isOk());
    }

    // ==================== GET / tests ====================

    @Test
    void testGetAllByCountryId_whenNoCountryId_thenReturnOk() throws Exception {
        List<StateOrProvinceVm> response = Collections.emptyList();
        given(stateOrProvinceService.getAllByCountryId(null)).willReturn(response);

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL))
            .andExpect(status().isOk());
    }

    @Test
    void testGetAllByCountryId_whenWithCountryId_thenReturnOk() throws Exception {
        StateOrProvinceVm vm = new StateOrProvinceVm(1L, "California", "CA", "state", 1L);
        given(stateOrProvinceService.getAllByCountryId(1L)).willReturn(List.of(vm));

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL)
                .param("countryId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("California"))
            .andExpect(jsonPath("$[0].code").value("CA"));
    }

    @Test
    void testGetAllByCountryId_whenWithInvalidCountryId_thenReturnOk() throws Exception {
        given(stateOrProvinceService.getAllByCountryId(999L)).willReturn(Collections.emptyList());

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL)
                .param("countryId", "999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // ==================== GET /{id} tests ====================

    @Test
    void testGetStateOrProvince_whenIdExists_thenReturnOk() throws Exception {
        StateOrProvinceVm vm = new StateOrProvinceVm(1L, "California", "CA", "state", 1L);
        given(stateOrProvinceService.findById(1L)).willReturn(vm);

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("California"))
            .andExpect(jsonPath("$.code").value("CA"));
    }

    @Test
    void testGetStateOrProvince_whenIdNotExists_thenReturnOk() throws Exception {
        StateOrProvinceVm vm = new StateOrProvinceVm(999L, "Unknown", "UN", "state", null);
        given(stateOrProvinceService.findById(999L)).willReturn(vm);

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(999));
    }

    // ==================== GET state-country-names tests ====================

    @Test
    void testGetStateOrProvinceAndCountryNames_whenValidIds_thenReturnOk() throws Exception {
        StateOrProvinceAndCountryGetNameVm nameVm = new StateOrProvinceAndCountryGetNameVm(
            1L, "California", "United States");
        given(stateOrProvinceService.getStateOrProvinceAndCountryNames(List.of(1L, 2L)))
            .willReturn(List.of(nameVm));

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/state-country-names")
                .param("stateOrProvinceIds", "1", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].stateOrProvinceId").value(1))
            .andExpect(jsonPath("$[0].stateOrProvinceName").value("California"))
            .andExpect(jsonPath("$[0].countryName").value("United States"));
    }

    @Test
    void testGetStateOrProvinceAndCountryNames_whenSingleId_thenReturnOk() throws Exception {
        StateOrProvinceAndCountryGetNameVm nameVm = new StateOrProvinceAndCountryGetNameVm(
            1L, "California", "United States");
        given(stateOrProvinceService.getStateOrProvinceAndCountryNames(List.of(1L)))
            .willReturn(List.of(nameVm));

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/state-country-names")
                .param("stateOrProvinceIds", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].stateOrProvinceId").value(1));
    }

    @Test
    void testGetStateOrProvinceAndCountryNames_whenEmptyIds_thenReturnOk() throws Exception {
        given(stateOrProvinceService.getStateOrProvinceAndCountryNames(Collections.emptyList()))
            .willReturn(Collections.emptyList());

        this.mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/state-country-names")
                .param("stateOrProvinceIds", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // ==================== DELETE /{id} tests ====================

    @Test
    void testDeleteStateOrProvince_whenIdIsValid_thenReturnNoContent() throws Exception {
        this.mockMvc.perform(delete(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1"))
            .andExpect(status().isNoContent());
        verify(stateOrProvinceService).delete(1L);
    }

    @Test
    void testDeleteStateOrProvince_whenIdIsLargeNumber_thenReturnNoContent() throws Exception {
        this.mockMvc.perform(delete(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/999999"))
            .andExpect(status().isNoContent());
        verify(stateOrProvinceService).delete(999999L);
    }
}