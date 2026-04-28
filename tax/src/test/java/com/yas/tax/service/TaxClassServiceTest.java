package com.yas.tax.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class TaxClassServiceTest {

    private TaxClassRepository taxClassRepository;
    private TaxClassService taxClassService;

    @BeforeEach
    void setUp() {
        taxClassRepository = mock(TaxClassRepository.class);
        taxClassService = new TaxClassService(taxClassRepository);
    }

    @Test
    void findAllTaxClasses_ShouldReturnListOfTaxClassVm() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard");
        when(taxClassRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of(taxClass));

        List<TaxClassVm> result = taxClassService.findAllTaxClasses();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Standard", result.get(0).name());
    }

    @Test
    void findById_WhenTaxClassExists_ShouldReturnTaxClassVm() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard");
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));

        TaxClassVm result = taxClassService.findById(1L);

        assertEquals(1L, result.id());
        assertEquals("Standard", result.name());
    }

    @Test
    void findById_WhenTaxClassDoesNotExist_ShouldThrowNotFoundException() {
        when(taxClassRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxClassService.findById(1L));
    }

    @Test
    void create_WhenNameIsUnique_ShouldReturnTaxClass() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Standard");
        TaxClass taxClass = new TaxClass();
        taxClass.setName("Standard");
        when(taxClassRepository.existsByName("Standard")).thenReturn(false);
        when(taxClassRepository.save(any(TaxClass.class))).thenReturn(taxClass);

        TaxClass result = taxClassService.create(postVm);

        assertEquals("Standard", result.getName());
    }

    @Test
    void create_WhenNameAlreadyExists_ShouldThrowDuplicatedException() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Standard");
        when(taxClassRepository.existsByName("Standard")).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> taxClassService.create(postVm));
    }

    @Test
    void update_WhenTaxClassExistsAndNameIsUnique_ShouldUpdateTaxClass() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Updated");
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard");

        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass("Updated", 1L)).thenReturn(false);

        taxClassService.update(postVm, 1L);

        verify(taxClassRepository).save(taxClass);
        assertEquals("Updated", taxClass.getName());
    }

    @Test
    void update_WhenTaxClassDoesNotExist_ShouldThrowNotFoundException() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Updated");
        when(taxClassRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taxClassService.update(postVm, 1L));
    }

    @Test
    void update_WhenNameAlreadyExistsForOtherTaxClass_ShouldThrowDuplicatedException() {
        TaxClassPostVm postVm = new TaxClassPostVm("1", "Updated");
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard");

        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass("Updated", 1L)).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> taxClassService.update(postVm, 1L));
    }

    @Test
    void delete_WhenTaxClassExists_ShouldDeleteTaxClass() {
        when(taxClassRepository.existsById(1L)).thenReturn(true);

        taxClassService.delete(1L);

        verify(taxClassRepository).deleteById(1L);
    }

    @Test
    void delete_WhenTaxClassDoesNotExist_ShouldThrowNotFoundException() {
        when(taxClassRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taxClassService.delete(1L));
    }

    @Test
    void getPageableTaxClasses_ShouldReturnTaxClassListGetVm() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("Standard");

        Pageable pageable = PageRequest.of(0, 10);
        Page<TaxClass> taxClassPage = new PageImpl<>(List.of(taxClass), pageable, 1);
        when(taxClassRepository.findAll(pageable)).thenReturn(taxClassPage);

        TaxClassListGetVm result = taxClassService.getPageableTaxClasses(0, 10);

        assertEquals(1, result.taxClassContent().size());
        assertEquals(0, result.pageNo());
        assertEquals(10, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(true, result.isLast());
    }
}
