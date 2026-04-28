package com.yas.inventory.service;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.StockExistingException;
import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockPostVm;
import com.yas.inventory.viewmodel.stock.StockQuantityUpdateVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final Long PRODUCT_ID   = 100L;
    private static final Long WAREHOUSE_ID = 10L;
    private static final Long STOCK_ID     = 1L;

    // ── Mocks ─────────────────────────────────────────────────────────────────
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private StockRepository     stockRepository;
    @Mock private ProductService      productService;
    @Mock private WarehouseService    warehouseService;
    @Mock private StockHistoryService stockHistoryService;

    @InjectMocks
    private StockService stockService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Warehouse buildWarehouse(Long id) {
        return Warehouse.builder().id(id).name("Warehouse " + id).build();
    }

    private Stock buildStock(Long stockId, Long productId, Long quantity) {
        return Stock.builder()
            .id(stockId)
            .productId(productId)
            .quantity(quantity)
            .reservedQuantity(0L)
            .warehouse(buildWarehouse(WAREHOUSE_ID))
            .build();
    }

    private ProductInfoVm buildProductInfoVm(Long productId) {
        return new ProductInfoVm(productId, "Product " + productId, "SKU-" + productId, true);
    }

    private StockPostVm buildStockPostVm(Long productId, Long warehouseId) {
        return new StockPostVm(productId, warehouseId);
    }

    private StockQuantityVm buildQuantityVm(Long stockId, Long quantity) {
        return new StockQuantityVm(stockId, quantity, "Test note");
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addProductIntoWarehouse()")
    class AddProductIntoWarehouseTests {

        @Test
        @DisplayName("given valid product and warehouse, saves stock with zero quantity")
        void givenValidProductAndWarehouse_whenAdd_thenSavesStockWithZeroQuantity() {
            when(stockRepository.existsByWarehouseIdAndProductId(WAREHOUSE_ID, PRODUCT_ID))
                .thenReturn(false);
            when(productService.getProduct(PRODUCT_ID))
                .thenReturn(buildProductInfoVm(PRODUCT_ID));
            when(warehouseRepository.findById(WAREHOUSE_ID))
                .thenReturn(Optional.of(buildWarehouse(WAREHOUSE_ID)));

            stockService.addProductIntoWarehouse(List.of(buildStockPostVm(PRODUCT_ID, WAREHOUSE_ID)));

            ArgumentCaptor<List<Stock>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockRepository).saveAll(captor.capture());

            List<Stock> saved = captor.getValue();
            assertThat(saved).hasSize(1);
            assertThat(saved.get(0).getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(saved.get(0).getQuantity()).isZero();
            assertThat(saved.get(0).getReservedQuantity()).isZero();
            assertThat(saved.get(0).getWarehouse().getId()).isEqualTo(WAREHOUSE_ID);
        }

        @Test
        @DisplayName("given stock already exists, throws StockExistingException without saving")
        void givenStockAlreadyExists_whenAdd_thenThrowStockExistingException() {
            when(stockRepository.existsByWarehouseIdAndProductId(WAREHOUSE_ID, PRODUCT_ID))
                .thenReturn(true);

            assertThatThrownBy(() ->
                stockService.addProductIntoWarehouse(List.of(buildStockPostVm(PRODUCT_ID, WAREHOUSE_ID)))
            ).isInstanceOf(StockExistingException.class);

            verify(stockRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("given product not found, throws NotFoundException without saving")
        void givenProductNotFound_whenAdd_thenThrowNotFoundException() {
            when(stockRepository.existsByWarehouseIdAndProductId(WAREHOUSE_ID, PRODUCT_ID))
                .thenReturn(false);
            when(productService.getProduct(PRODUCT_ID)).thenReturn(null);

            assertThatThrownBy(() ->
                stockService.addProductIntoWarehouse(List.of(buildStockPostVm(PRODUCT_ID, WAREHOUSE_ID)))
            ).isInstanceOf(NotFoundException.class);

            verify(stockRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("given warehouse not found, throws NotFoundException without saving")
        void givenWarehouseNotFound_whenAdd_thenThrowNotFoundException() {
            when(stockRepository.existsByWarehouseIdAndProductId(WAREHOUSE_ID, PRODUCT_ID))
                .thenReturn(false);
            when(productService.getProduct(PRODUCT_ID))
                .thenReturn(buildProductInfoVm(PRODUCT_ID));
            when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                stockService.addProductIntoWarehouse(List.of(buildStockPostVm(PRODUCT_ID, WAREHOUSE_ID)))
            ).isInstanceOf(NotFoundException.class);

            verify(stockRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("given multiple valid products, saves all stocks")
        void givenMultipleValidProducts_whenAdd_thenSavesAllStocks() {
            Warehouse warehouse = buildWarehouse(WAREHOUSE_ID);
            when(stockRepository.existsByWarehouseIdAndProductId(eq(WAREHOUSE_ID), anyLong()))
                .thenReturn(false);
            when(productService.getProduct(101L)).thenReturn(buildProductInfoVm(101L));
            when(productService.getProduct(102L)).thenReturn(buildProductInfoVm(102L));
            when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(warehouse));

            stockService.addProductIntoWarehouse(List.of(
                buildStockPostVm(101L, WAREHOUSE_ID),
                buildStockPostVm(102L, WAREHOUSE_ID)
            ));

            ArgumentCaptor<List<Stock>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("updateProductQuantityInStock()")
    class UpdateProductQuantityInStockTests {

        @Test
        @DisplayName("given positive adjustment, adds to stock quantity and notifies all services")
        void givenPositiveAdjustment_whenUpdate_thenAddsToQuantityAndCallsServices() {
            Stock stock = buildStock(STOCK_ID, PRODUCT_ID, 100L);
            StockQuantityVm quantityVm = buildQuantityVm(STOCK_ID, 50L);
            StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(quantityVm));

            when(stockRepository.findAllById(anyList())).thenReturn(List.of(stock));

            stockService.updateProductQuantityInStock(updateVm);

            // 100 (original) + 50 (adjusted) = 150
            assertThat(stock.getQuantity()).isEqualTo(150L);
            verify(stockRepository).saveAll(List.of(stock));
            verify(stockHistoryService).createStockHistories(List.of(stock), List.of(quantityVm));
            verify(productService).updateProductQuantity(anyList());
        }

        @Test
        @DisplayName("given null quantity, treats as zero and does not change stock quantity")
        void givenNullQuantity_whenUpdate_thenTreatsAsZeroAndKeepsOriginalQuantity() {
            Stock stock = buildStock(STOCK_ID, PRODUCT_ID, 100L);
            StockQuantityVm quantityVm = new StockQuantityVm(STOCK_ID, null, "note");
            StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(quantityVm));

            when(stockRepository.findAllById(anyList())).thenReturn(List.of(stock));

            stockService.updateProductQuantityInStock(updateVm);

            // 100 + 0 = 100 (unchanged)
            assertThat(stock.getQuantity()).isEqualTo(100L);
            verify(stockRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("given negative adjustment exceeding stock, throws BadRequestException")
        void givenNegativeAdjustmentExceedingStock_whenUpdate_thenThrowBadRequestException() {
            // Condition: adjustedQuantity < 0 && adjustedQuantity > stock.getQuantity()
            // stock.quantity = -200, adjusted = -100 → -100 < 0 && -100 > -200 → true
            Stock stock = buildStock(STOCK_ID, PRODUCT_ID, -200L);
            StockQuantityVm quantityVm = buildQuantityVm(STOCK_ID, -100L);
            StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(quantityVm));

            when(stockRepository.findAllById(anyList())).thenReturn(List.of(stock));

            assertThatThrownBy(() -> stockService.updateProductQuantityInStock(updateVm))
                .isInstanceOf(BadRequestException.class);

            verify(stockRepository, never()).saveAll(any());
            verify(stockHistoryService, never()).createStockHistories(any(), any());
        }

        @Test
        @DisplayName("given stock not matched by any quantityVm, skips setQuantity for that stock")
        void givenUnmatchedStock_whenUpdate_thenSkipsQuantityUpdate() {
            Stock stock = buildStock(STOCK_ID, PRODUCT_ID, 100L);
            // vm references different stockId → no match
            StockQuantityVm quantityVm = buildQuantityVm(999L, 50L);
            StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(quantityVm));

            when(stockRepository.findAllById(anyList())).thenReturn(List.of(stock));

            stockService.updateProductQuantityInStock(updateVm);

            // quantity must remain unchanged
            assertThat(stock.getQuantity()).isEqualTo(100L);
            verify(stockRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("given empty stock list returned, skips history creation and product update")
        void givenEmptyStockList_whenUpdate_thenSkipsHistoryAndProductUpdate() {
            StockQuantityVm quantityVm = buildQuantityVm(STOCK_ID, 10L);
            StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(quantityVm));

            when(stockRepository.findAllById(anyList())).thenReturn(Collections.emptyList());

            stockService.updateProductQuantityInStock(updateVm);

            verify(stockRepository).saveAll(Collections.emptyList());
            verify(stockHistoryService).createStockHistories(Collections.emptyList(), List.of(quantityVm));
            verify(productService, never()).updateProductQuantity(any());
        }

        @Test
        @DisplayName("given zero adjustment, quantity stays the same")
        void givenZeroAdjustment_whenUpdate_thenQuantityUnchanged() {
            Stock stock = buildStock(STOCK_ID, PRODUCT_ID, 100L);
            StockQuantityVm quantityVm = buildQuantityVm(STOCK_ID, 0L);
            StockQuantityUpdateVm updateVm = new StockQuantityUpdateVm(List.of(quantityVm));

            when(stockRepository.findAllById(anyList())).thenReturn(List.of(stock));

            stockService.updateProductQuantityInStock(updateVm);

            assertThat(stock.getQuantity()).isEqualTo(100L);
        }
    }
}