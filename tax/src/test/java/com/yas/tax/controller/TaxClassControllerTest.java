package com.yas.tax.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.constants.ApiConstant;
import com.yas.tax.model.TaxClass;
import com.yas.tax.service.TaxClassService;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
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

@WebMvcTest(controllers = TaxClassController.class, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TaxClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaxClassService taxClassService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TaxClass taxClass;
    private TaxClassVm taxClassVm;
    private TaxClassPostVm taxClassPostVm;

    @BeforeEach
    void setUp() {
        taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard");

        taxClassVm = TaxClassVm.fromModel(taxClass);
        taxClassPostVm = new TaxClassPostVm("1", "Standard");
    }

    @Test
    void getPageableTaxClasses_ShouldReturnPagedTaxClasses() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TaxClass> page = new PageImpl<>(List.of(taxClass), pageable, 1);
        TaxClassListGetVm listGetVm = new TaxClassListGetVm(
            List.of(taxClassVm), 0, 10, 1, 1, true
        );

        when(taxClassService.getPageableTaxClasses(0, 10)).thenReturn(listGetVm);

        mockMvc.perform(get(ApiConstant.TAX_CLASS_URL + "/paging")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageNo").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))
            .andExpect(jsonPath("$.totalElements").value(1));

        verify(taxClassService).getPageableTaxClasses(0, 10);
    }

    @Test
    void getPageableTaxClasses_WithDefaultPagination_ShouldReturnFirstPage() throws Exception {
        TaxClassListGetVm listGetVm = new TaxClassListGetVm(
            List.of(taxClassVm), 0, 10, 1, 1, true
        );

        when(taxClassService.getPageableTaxClasses(0, 10)).thenReturn(listGetVm);

        mockMvc.perform(get(ApiConstant.TAX_CLASS_URL + "/paging")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(taxClassService).getPageableTaxClasses(0, 10);
    }

    @Test
    void listTaxClasses_ShouldReturnAllTaxClasses() throws Exception {
        when(taxClassService.findAllTaxClasses()).thenReturn(List.of(taxClassVm));

        mockMvc.perform(get(ApiConstant.TAX_CLASS_URL)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Standard"));

        verify(taxClassService).findAllTaxClasses();
    }

    @Test
    void getTaxClass_WhenTaxClassExists_ShouldReturnTaxClass() throws Exception {
        when(taxClassService.findById(1L)).thenReturn(taxClassVm);

        mockMvc.perform(get(ApiConstant.TAX_CLASS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Standard"));

        verify(taxClassService).findById(1L);
    }

    @Test
    void getTaxClass_WhenTaxClassDoesNotExist_ShouldReturn404() throws Exception {
        when(taxClassService.findById(999L))
            .thenThrow(new NotFoundException("Tax class not found"));

        mockMvc.perform(get(ApiConstant.TAX_CLASS_URL + "/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void createTaxClass_WithValidData_ShouldReturnCreated() throws Exception {
        when(taxClassService.create(any(TaxClassPostVm.class))).thenReturn(taxClass);

        mockMvc.perform(post(ApiConstant.TAX_CLASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxClassPostVm)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Standard"));

        verify(taxClassService).create(any(TaxClassPostVm.class));
    }

    @Test
    void createTaxClass_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        when(taxClassService.create(any(TaxClassPostVm.class)))
            .thenThrow(new DuplicatedException("Tax class name already exists"));

        mockMvc.perform(post(ApiConstant.TAX_CLASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxClassPostVm)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createTaxClass_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        TaxClassPostVm invalidVm = new TaxClassPostVm(null, null);

        mockMvc.perform(post(ApiConstant.TAX_CLASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidVm)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateTaxClass_WithValidData_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(put(ApiConstant.TAX_CLASS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxClassPostVm)))
            .andExpect(status().isNoContent());

        verify(taxClassService).update(any(TaxClassPostVm.class), eq(1L));
    }

    @Test
    void updateTaxClass_WhenTaxClassDoesNotExist_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("Tax class not found"))
            .when(taxClassService).update(any(TaxClassPostVm.class), eq(999L));

        mockMvc.perform(put(ApiConstant.TAX_CLASS_URL + "/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxClassPostVm)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateTaxClass_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        doThrow(new DuplicatedException("Tax class name already exists"))
            .when(taxClassService).update(any(TaxClassPostVm.class), eq(1L));

        mockMvc.perform(put(ApiConstant.TAX_CLASS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taxClassPostVm)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTaxClass_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete(ApiConstant.TAX_CLASS_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(taxClassService).delete(1L);
    }

    @Test
    void deleteTaxClass_WhenTaxClassDoesNotExist_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("Tax class not found"))
            .when(taxClassService).delete(999L);

        mockMvc.perform(delete(ApiConstant.TAX_CLASS_URL + "/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
