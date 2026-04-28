package com.yas.tax.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.constants.ApiConstant;
import com.yas.tax.model.TaxRate;
import com.yas.tax.service.TaxRateService;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TaxRateController.class, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TaxRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaxRateService taxRateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TaxRate taxRate;
    private TaxRateVm taxRateVm;
    private TaxRatePostVm taxRatePostVm;
    private com.yas.tax.viewmodel.taxrate.TaxRateGetDetailVm taxRateGetDetailVm;

    @BeforeEach
    void setUp() {
        taxRate = new TaxRate();
        taxRate.setId(1L);
        taxRate.setRate(10.0);
        taxRate.setTaxClass(new com.yas.tax.model.TaxClass());
        taxRate.getTaxClass().setId(1L);

        taxRateVm = TaxRateVm.fromModel(taxRate);
        taxRatePostVm = new TaxRatePostVm(10.0, "12345", 1L, 1L, 1L);
        taxRateGetDetailVm = new com.yas.tax.viewmodel.taxrate.TaxRateGetDetailVm(1L, 10.0, "12345", "Standard", "State", "Country");
    }

    @Test
    void getPageableTaxRates_ShouldReturnPagedTaxRates() throws Exception {
        TaxRateListGetVm listGetVm = new TaxRateListGetVm(
            List.of(taxRateGetDetailVm), 0, 10, 1, 1, true
        );

        when(taxRateService.getPageableTaxRates(0, 10)).thenReturn(listGetVm);

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/paging")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageNo").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))
            .andExpect(jsonPath("$.totalElements").value(1));

        verify(taxRateService).getPageableTaxRates(0, 10);
    }

    @Test
    void getPageableTaxRates_WithDefaultPagination_ShouldReturnFirstPage() throws Exception {
        TaxRateListGetVm listGetVm = new TaxRateListGetVm(
            List.of(taxRateGetDetailVm), 0, 10, 1, 1, true
        );

        when(taxRateService.getPageableTaxRates(0, 10)).thenReturn(listGetVm);

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/paging")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(taxRateService).getPageableTaxRates(0, 10);
    }

    @Test
    void getTaxRate_WhenTaxRateExists_ShouldReturnTaxRate() throws Exception {
        when(taxRateService.findById(1L)).thenReturn(taxRateVm);

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.rate").value(10.0));

        verify(taxRateService).findById(1L);
    }

    @Test
    void getTaxRate_WhenTaxRateDoesNotExist_ShouldReturn404() throws Exception {
        when(taxRateService.findById(999L))
            .thenThrow(new NotFoundException("Tax rate not found"));

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void createTaxRate_WithValidData_ShouldReturnCreated() throws Exception {
        when(taxRateService.createTaxRate(any(TaxRatePostVm.class))).thenReturn(taxRate);

        mockMvc.perform(post(ApiConstant.TAX_RATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxRatePostVm)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.rate").value(10.0));

        verify(taxRateService).createTaxRate(any(TaxRatePostVm.class));
    }

    @Test
    void createTaxRate_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        TaxRatePostVm invalidVm = new TaxRatePostVm(null, null, null, null, null);

        mockMvc.perform(post(ApiConstant.TAX_RATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidVm)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateTaxRate_WithValidData_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(put(ApiConstant.TAX_RATE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxRatePostVm)))
            .andExpect(status().isNoContent());

        verify(taxRateService).updateTaxRate(any(TaxRatePostVm.class), eq(1L));
    }

    @Test
    void updateTaxRate_WhenTaxRateDoesNotExist_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("Tax rate not found"))
            .when(taxRateService).updateTaxRate(any(TaxRatePostVm.class), eq(999L));

        mockMvc.perform(put(ApiConstant.TAX_RATE_URL + "/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxRatePostVm)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteTaxRate_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete(ApiConstant.TAX_RATE_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(taxRateService).delete(1L);
    }

    @Test
    void deleteTaxRate_WhenTaxRateDoesNotExist_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("Tax rate not found"))
            .when(taxRateService).delete(999L);

        mockMvc.perform(delete(ApiConstant.TAX_RATE_URL + "/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void getTaxPercentByAddress_WithRequiredParams_ShouldReturnTaxPercent() throws Exception {
        when(taxRateService.getTaxPercent(1L, 1L, null, null)).thenReturn(10.0);

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/tax-percent")
                .param("taxClassId", "1")
                .param("countryId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(10.0));

        verify(taxRateService).getTaxPercent(1L, 1L, null, null);
    }

    @Test
    void getTaxPercentByAddress_WithAllParams_ShouldReturnTaxPercent() throws Exception {
        when(taxRateService.getTaxPercent(1L, 1L, 1L, "12345")).thenReturn(15.0);

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/tax-percent")
                .param("taxClassId", "1")
                .param("countryId", "1")
                .param("stateOrProvinceId", "1")
                .param("zipCode", "12345")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(15.0));

        verify(taxRateService).getTaxPercent(1L, 1L, 1L, "12345");
    }

    @Test
    void getBatchTaxPercentsByAddress_WithRequiredParams_ShouldReturnTaxRates() throws Exception {
        when(taxRateService.getBulkTaxRate(List.of(1L), 1L, null, null))
            .thenReturn(List.of(taxRateVm));

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/location-based-batch")
                .param("taxClassIds", "1")
                .param("countryId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));

        verify(taxRateService).getBulkTaxRate(List.of(1L), 1L, null, null);
    }

    @Test
    void getBatchTaxPercentsByAddress_WithAllParams_ShouldReturnTaxRates() throws Exception {
        when(taxRateService.getBulkTaxRate(List.of(1L, 2L), 1L, 1L, "12345"))
            .thenReturn(List.of(taxRateVm));

        mockMvc.perform(get(ApiConstant.TAX_RATE_URL + "/location-based-batch")
                .param("taxClassIds", "1,2")
                .param("countryId", "1")
                .param("stateOrProvinceId", "1")
                .param("zipCode", "12345")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));

        verify(taxRateService).getBulkTaxRate(List.of(1L, 2L), 1L, 1L, "12345");
    }
}
