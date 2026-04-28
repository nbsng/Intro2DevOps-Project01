package com.yas.tax.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.repository.TaxRateRepository;
import com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class TaxRateServiceTest {

    private TaxRateRepository taxRateRepository;
    private TaxClassRepository taxClassRepository;
    private LocationService locationService;
    private TaxRateService taxRateService;

    @BeforeEach
    void setUp() {
        taxRateRepository = mock(TaxRateRepository.class);
        taxClassRepository = mock(TaxClassRepository.class);
        locationService = mock(LocationService.class);
        taxRateService = new TaxRateService(locationService, taxRateRepository, taxClassRepository);
    }

    @Test
    void createTaxRate_WhenTaxClassExists_ShouldReturnTaxRate() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "12345", 1L, 1L, 1L);
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);

        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);

        TaxRate savedTaxRate = new TaxRate();
        savedTaxRate.setRate(10.0);
        when(taxRateRepository.save(any(TaxRate.class))).thenReturn(savedTaxRate);

        TaxRate result = taxRateService.createTaxRate(postVm);

        assertEquals(10.0, result.getRate());
    }

    @Test
    void createTaxRate_WhenTaxClassDoesNotExist_ShouldThrowNotFoundException() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "12345", 1L, 1L, 1L);
        when(taxClassRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxRateService.createTaxRate(postVm));
    }

    @Test
    void updateTaxRate_WhenTaxRateAndTaxClassExist_ShouldUpdateTaxRate() {
        TaxRatePostVm postVm = new TaxRatePostVm(15.0, "54321", 1L, 1L, 1L);
        TaxRate taxRate = new TaxRate();
        taxRate.setId(1L);
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);

        taxRateService.updateTaxRate(postVm, 1L);

        verify(taxRateRepository).save(taxRate);
        assertEquals(15.0, taxRate.getRate());
        assertEquals("54321", taxRate.getZipCode());
    }

    @Test
    void updateTaxRate_WhenTaxRateDoesNotExist_ShouldThrowNotFoundException() {
        TaxRatePostVm postVm = new TaxRatePostVm(15.0, "54321", 1L, 1L, 1L);
        when(taxRateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxRateService.updateTaxRate(postVm, 1L));
    }

    @Test
    void delete_WhenTaxRateExists_ShouldDeleteTaxRate() {
        when(taxRateRepository.existsById(1L)).thenReturn(true);

        taxRateService.delete(1L);

        verify(taxRateRepository).deleteById(1L);
    }

    @Test
    void delete_WhenTaxRateDoesNotExist_ShouldThrowNotFoundException() {
        when(taxRateRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxRateService.delete(1L));
    }

    @Test
    void findById_WhenTaxRateExists_ShouldReturnTaxRateVm() {
        TaxRate taxRate = new TaxRate();
        taxRate.setId(1L);
        taxRate.setRate(10.0);
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxRate.setTaxClass(taxClass);

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));

        TaxRateVm result = taxRateService.findById(1L);

        assertEquals(1L, result.id());
        assertEquals(10.0, result.rate());
    }

    @Test
    void findAll_ShouldReturnListOfTaxRateVm() {
        TaxRate taxRate = new TaxRate();
        taxRate.setId(1L);
        taxRate.setRate(10.0);
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxRate.setTaxClass(taxClass);

        when(taxRateRepository.findAll()).thenReturn(List.of(taxRate));

        List<TaxRateVm> result = taxRateService.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void getPageableTaxRates_ShouldReturnTaxRateListGetVm() {
        TaxRate taxRate = new TaxRate();
        taxRate.setId(1L);
        taxRate.setRate(10.0);
        taxRate.setStateOrProvinceId(1L);
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard");
        taxRate.setTaxClass(taxClass);

        Pageable pageable = PageRequest.of(0, 10);
        Page<TaxRate> taxRatePage = new PageImpl<>(List.of(taxRate), pageable, 1);
        when(taxRateRepository.findAll(pageable)).thenReturn(taxRatePage);

        StateOrProvinceAndCountryGetNameVm locationVm = new StateOrProvinceAndCountryGetNameVm(1L, "State", "Country");
        when(locationService.getStateOrProvinceAndCountryNames(anyList())).thenReturn(List.of(locationVm));

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        assertEquals(1, result.taxRateGetDetailContent().size());
        assertEquals("State", result.taxRateGetDetailContent().get(0).stateOrProvinceName());
        assertEquals(0, result.pageNo());
        assertEquals(10, result.pageSize());
        assertEquals(1, result.totalElements());
    }

    @Test
    void getTaxPercent_WhenTaxPercentExists_ShouldReturnTaxPercent() {
        when(taxRateRepository.getTaxPercent(1L, 1L, "12345", 1L)).thenReturn(10.0);

        double result = taxRateService.getTaxPercent(1L, 1L, 1L, "12345");

        assertEquals(10.0, result);
    }

    @Test
    void getTaxPercent_WhenTaxPercentDoesNotExist_ShouldReturnZero() {
        when(taxRateRepository.getTaxPercent(1L, 1L, "12345", 1L)).thenReturn(null);

        double result = taxRateService.getTaxPercent(1L, 1L, 1L, "12345");

        assertEquals(0.0, result);
    }

    @Test
    void getBulkTaxRate_ShouldReturnListOfTaxRateVm() {
        TaxRate taxRate = new TaxRate();
        taxRate.setId(1L);
        taxRate.setRate(10.0);
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxRate.setTaxClass(taxClass);

        when(taxRateRepository.getBatchTaxRates(1L, 1L, "12345", new HashSet<>(List.of(1L))))
            .thenReturn(List.of(taxRate));

        List<TaxRateVm> result = taxRateService.getBulkTaxRate(List.of(1L), 1L, 1L, "12345");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }
}
