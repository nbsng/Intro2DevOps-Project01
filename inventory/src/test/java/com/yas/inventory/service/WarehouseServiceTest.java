package com.yas.inventory.service;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.address.AddressDetailVm;
import com.yas.inventory.viewmodel.address.AddressVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseDetailVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseListGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehousePostVm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final Long   WAREHOUSE_ID   = 1L;
    private static final Long   ADDRESS_ID     = 10L;
    private static final String WAREHOUSE_NAME = "Main Warehouse";

    // ── Mocks ─────────────────────────────────────────────────────────────────
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private StockRepository     stockRepository;
    @Mock private ProductService      productService;
    @Mock private LocationService     locationService;

    @InjectMocks
    private WarehouseService warehouseService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Warehouse buildWarehouse(Long id, String name) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setName(name);
        warehouse.setAddressId(ADDRESS_ID);
        return warehouse;
    }

    /**
     * WarehousePostVm fields (in order):
     * id, name, contactName, phone, addressLine1, addressLine2,
     * city, zipCode, districtId, stateOrProvinceId, countryId
     */
    private WarehousePostVm buildPostVm(String name) {
        return new WarehousePostVm(
            null,           // id
            name,           // name
            "Contact",      // contactName
            "0123456789",   // phone
            "123 Street",   // addressLine1
            null,           // addressLine2
            "City",         // city
            "70000",        // zipCode
            1L,             // districtId
            1L,             // stateOrProvinceId
            1L              // countryId
        );
    }

    /**
     * AddressVm fields (in order):
     * id, contactName, phone, addressLine1, city, zipCode,
     * districtId, stateOrProvinceId, countryId
     */
    private AddressVm buildAddressVm() {
        return new AddressVm(
            ADDRESS_ID, "Contact", "0123456789",
            "123 Street", "City", "70000",
            1L, 1L, 1L
        );
    }

    /**
     * Mock AddressDetailVm to avoid depending on its exact constructor signature.
     * The service only reads individual accessor methods from it.
     */
    private AddressDetailVm buildAddressDetailVm() {
        AddressDetailVm mock = mock(AddressDetailVm.class);
        when(mock.contactName()).thenReturn("Contact");
        when(mock.phone()).thenReturn("0123456789");
        when(mock.addressLine1()).thenReturn("123 Street");
        when(mock.addressLine2()).thenReturn(null);
        when(mock.city()).thenReturn("City");
        when(mock.zipCode()).thenReturn("70000");
        when(mock.districtId()).thenReturn(1L);
        when(mock.stateOrProvinceId()).thenReturn(1L);
        when(mock.countryId()).thenReturn(1L);
        return mock;
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("given existing id, returns WarehouseDetailVm with correct fields")
        void givenExistingId_whenFindById_thenReturnWarehouseDetailVm() {
            Warehouse warehouse = buildWarehouse(WAREHOUSE_ID, WAREHOUSE_NAME);
            when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(warehouse));
            AddressDetailVm addressDetailVm = buildAddressDetailVm();
            when(locationService.getAddressById(ADDRESS_ID)).thenReturn(addressDetailVm);

            WarehouseDetailVm result = warehouseService.findById(WAREHOUSE_ID);

            assertThat(result.id()).isEqualTo(WAREHOUSE_ID);
            assertThat(result.name()).isEqualTo(WAREHOUSE_NAME);
            verify(locationService).getAddressById(ADDRESS_ID);
        }

        @Test
        @DisplayName("given non-existent id, throws NotFoundException without calling locationService")
        void givenNonExistentId_whenFindById_thenThrowNotFoundException() {
            when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> warehouseService.findById(WAREHOUSE_ID))
                .isInstanceOf(NotFoundException.class);

            verifyNoInteractions(locationService);
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("given unique name, creates address then saves warehouse")
        void givenUniqueName_whenCreate_thenSavesWarehouse() {
            when(warehouseRepository.existsByName(WAREHOUSE_NAME)).thenReturn(false);
            when(locationService.createAddress(any())).thenReturn(buildAddressVm());
            when(warehouseRepository.save(any(Warehouse.class)))
                .thenAnswer(inv -> inv.getArgument(0));

            Warehouse result = warehouseService.create(buildPostVm(WAREHOUSE_NAME));

            assertThat(result.getName()).isEqualTo(WAREHOUSE_NAME);
            assertThat(result.getAddressId()).isEqualTo(ADDRESS_ID);
            verify(locationService).createAddress(any());
            verify(warehouseRepository).save(any(Warehouse.class));
        }

        @Test
        @DisplayName("given duplicate name, throws DuplicatedException without saving")
        void givenDuplicateName_whenCreate_thenThrowDuplicatedException() {
            when(warehouseRepository.existsByName(WAREHOUSE_NAME)).thenReturn(true);

            assertThatThrownBy(() -> warehouseService.create(buildPostVm(WAREHOUSE_NAME)))
                .isInstanceOf(DuplicatedException.class);

            verifyNoInteractions(locationService);
            verify(warehouseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("given existing id and unique name, updates name and address")
        void givenExistingIdAndUniqueName_whenUpdate_thenUpdatesWarehouse() {
            Warehouse existing = buildWarehouse(WAREHOUSE_ID, "Old Name");
            when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(existing));
            when(warehouseRepository.existsByNameWithDifferentId(WAREHOUSE_NAME, WAREHOUSE_ID))
                .thenReturn(false);

            warehouseService.update(buildPostVm(WAREHOUSE_NAME), WAREHOUSE_ID);

            assertThat(existing.getName()).isEqualTo(WAREHOUSE_NAME);
            verify(locationService).updateAddress(eq(ADDRESS_ID), any());
            verify(warehouseRepository).save(existing);
        }

        @Test
        @DisplayName("given non-existent id, throws NotFoundException without saving")
        void givenNonExistentId_whenUpdate_thenThrowNotFoundException() {
            when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> warehouseService.update(buildPostVm(WAREHOUSE_NAME), WAREHOUSE_ID))
                .isInstanceOf(NotFoundException.class);

            verify(warehouseRepository, never()).save(any());
            verifyNoInteractions(locationService);
        }

        @Test
        @DisplayName("given duplicate name for another warehouse, throws DuplicatedException")
        void givenDuplicateNameForOtherWarehouse_whenUpdate_thenThrowDuplicatedException() {
            Warehouse existing = buildWarehouse(WAREHOUSE_ID, "Old Name");
            when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(existing));
            when(warehouseRepository.existsByNameWithDifferentId(WAREHOUSE_NAME, WAREHOUSE_ID))
                .thenReturn(true);

            assertThatThrownBy(() -> warehouseService.update(buildPostVm(WAREHOUSE_NAME), WAREHOUSE_ID))
                .isInstanceOf(DuplicatedException.class);

            verify(warehouseRepository, never()).save(any());
            verifyNoInteractions(locationService);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("given existing id, deletes warehouse by id and removes its address")
        void givenExistingId_whenDelete_thenDeletesWarehouseAndAddress() {
            Warehouse warehouse = buildWarehouse(WAREHOUSE_ID, WAREHOUSE_NAME);
            when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(warehouse));

            warehouseService.delete(WAREHOUSE_ID);

            verify(warehouseRepository).deleteById(WAREHOUSE_ID);
            verify(locationService).deleteAddress(ADDRESS_ID);
        }

        @Test
        @DisplayName("given non-existent id, throws NotFoundException without deleting")
        void givenNonExistentId_whenDelete_thenThrowNotFoundException() {
            when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> warehouseService.delete(WAREHOUSE_ID))
                .isInstanceOf(NotFoundException.class);

            verify(warehouseRepository, never()).deleteById(any());
            verifyNoInteractions(locationService);
        }
    }

    @Nested
    @DisplayName("findAllWarehouses()")
    class FindAllWarehousesTests {

        @Test
        @DisplayName("given existing warehouses, returns mapped list")
        void givenExistingWarehouses_whenFindAll_thenReturnMappedList() {
            when(warehouseRepository.findAll())
                .thenReturn(List.of(
                    buildWarehouse(1L, "Wh A"),
                    buildWarehouse(2L, "Wh B")
                ));

            List<WarehouseGetVm> result = warehouseService.findAllWarehouses();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("given no warehouses, returns empty list")
        void givenNoWarehouses_whenFindAll_thenReturnEmptyList() {
            when(warehouseRepository.findAll()).thenReturn(List.of());

            assertThat(warehouseService.findAllWarehouses()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPageableWarehouses()")
    class GetPageableWarehousesTests {

        @Test
        @DisplayName("given valid page params, returns correct WarehouseListGetVm")
        void givenValidPageParams_whenGetPageable_thenReturnWarehouseListGetVm() {
            List<Warehouse> warehouses = List.of(
                buildWarehouse(1L, "Wh A"),
                buildWarehouse(2L, "Wh B")
            );
            Page<Warehouse> page = new PageImpl<>(warehouses, PageRequest.of(0, 10), 2);
            when(warehouseRepository.findAll(any(Pageable.class))).thenReturn(page);

            WarehouseListGetVm result = warehouseService.getPageableWarehouses(0, 10);

            assertThat(result.warehouseContent()).hasSize(2);
            assertThat(result.pageNo()).isZero();
            assertThat(result.pageSize()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("given empty repository, returns empty page result")
        void givenEmptyRepository_whenGetPageable_thenReturnEmptyResult() {
            Page<Warehouse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(warehouseRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            WarehouseListGetVm result = warehouseService.getPageableWarehouses(0, 10);

            assertThat(result.warehouseContent()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("given multiple pages, isLast returns false for non-final page")
        void givenMultiplePages_whenGetFirstPage_thenIsLastIsFalse() {
            List<Warehouse> warehouses = List.of(buildWarehouse(1L, "Wh A"));
            Page<Warehouse> page = new PageImpl<>(warehouses, PageRequest.of(0, 1), 3);
            when(warehouseRepository.findAll(any(Pageable.class))).thenReturn(page);

            WarehouseListGetVm result = warehouseService.getPageableWarehouses(0, 1);

            assertThat(result.isLast()).isFalse();
            assertThat(result.totalPages()).isEqualTo(3);
        }
    }
}