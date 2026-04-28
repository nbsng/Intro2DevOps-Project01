package com.yas.inventory.service;

import com.yas.inventory.model.Stock;
import com.yas.inventory.model.StockHistory;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockHistoryRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryListVm;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryVm;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockHistoryServiceTest {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final Long   STOCK_ID     = 1L;
    private static final Long   PRODUCT_ID   = 100L;
    private static final Long   WAREHOUSE_ID = 10L;
    private static final String NOTE         = "Restock";
    private static final Long   QUANTITY     = 50L;

    // ── Mocks ─────────────────────────────────────────────────────────────────
    @Mock private StockHistoryRepository stockHistoryRepository;
    @Mock private ProductService         productService;

    @InjectMocks
    private StockHistoryService stockHistoryService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Warehouse buildWarehouse(Long id) {
        return Warehouse.builder().id(id).name("Warehouse " + id).build();
    }

    private Stock buildStock(Long stockId, Long productId) {
        return Stock.builder()
            .id(stockId)
            .productId(productId)
            .quantity(100L)
            .warehouse(buildWarehouse(WAREHOUSE_ID))
            .build();
    }

    private StockHistory buildStockHistory(Long id) {
        return StockHistory.builder()
            .id(id)
            .productId(PRODUCT_ID)
            .adjustedQuantity(QUANTITY)
            .note(NOTE)
            .warehouse(buildWarehouse(WAREHOUSE_ID))
            .build();
    }

    private StockQuantityVm buildQuantityVm(Long stockId, Long quantity, String note) {
        return new StockQuantityVm(stockId, quantity, note);
    }

    private ProductInfoVm buildProductInfoVm() {
        return new ProductInfoVm(PRODUCT_ID, "iPhone 15", "IP15", true);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createStockHistories()")
    class CreateStockHistoriesTests {

        @Test
        @DisplayName("given matching stocks and quantities, saves StockHistory with correct fields")
        void givenMatchingStocksAndQuantities_whenCreate_thenSavesHistoriesWithCorrectFields() {
            Stock stock = buildStock(STOCK_ID, PRODUCT_ID);
            StockQuantityVm quantityVm = buildQuantityVm(STOCK_ID, QUANTITY, NOTE);

            stockHistoryService.createStockHistories(List.of(stock), List.of(quantityVm));

            ArgumentCaptor<List<StockHistory>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockHistoryRepository).saveAll(captor.capture());

            List<StockHistory> saved = captor.getValue();
            assertThat(saved).hasSize(1);

            StockHistory history = saved.get(0);
            assertThat(history.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(history.getNote()).isEqualTo(NOTE);
            assertThat(history.getAdjustedQuantity()).isEqualTo(QUANTITY);
            assertThat(history.getWarehouse()).isEqualTo(stock.getWarehouse());
        }

        @Test
        @DisplayName("given stock with no matching StockQuantityVm, skips that stock")
        void givenUnmatchedStock_whenCreate_thenSkipsAndSavesEmptyList() {
            Stock stock = buildStock(STOCK_ID, PRODUCT_ID);
            StockQuantityVm quantityVm = buildQuantityVm(999L, QUANTITY, NOTE);

            stockHistoryService.createStockHistories(List.of(stock), List.of(quantityVm));

            ArgumentCaptor<List<StockHistory>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockHistoryRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("given empty stock list, saves empty history list")
        void givenEmptyStocks_whenCreate_thenSavesEmptyList() {
            stockHistoryService.createStockHistories(Collections.emptyList(), Collections.emptyList());

            ArgumentCaptor<List<StockHistory>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockHistoryRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("given multiple matching stocks, saves a history entry per matched stock")
        void givenMultipleMatchingStocks_whenCreate_thenSavesAllHistories() {
            Stock stock1 = buildStock(1L, 101L);
            Stock stock2 = buildStock(2L, 102L);
            StockQuantityVm vm1 = buildQuantityVm(1L, 10L, "Note 1");
            StockQuantityVm vm2 = buildQuantityVm(2L, 20L, "Note 2");

            stockHistoryService.createStockHistories(List.of(stock1, stock2), List.of(vm1, vm2));

            ArgumentCaptor<List<StockHistory>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockHistoryRepository).saveAll(captor.capture());

            List<StockHistory> saved = captor.getValue();
            assertThat(saved).hasSize(2);
            assertThat(saved.get(0).getProductId()).isEqualTo(101L);
            assertThat(saved.get(1).getProductId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("given partially matched stocks, saves only matched entries")
        void givenPartiallyMatchedStocks_whenCreate_thenSavesOnlyMatched() {
            Stock matched   = buildStock(1L, 101L);
            Stock unmatched = buildStock(2L, 102L);
            StockQuantityVm vm = buildQuantityVm(1L, 10L, "Note");

            stockHistoryService.createStockHistories(List.of(matched, unmatched), List.of(vm));

            ArgumentCaptor<List<StockHistory>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockHistoryRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
            assertThat(captor.getValue().get(0).getProductId()).isEqualTo(101L);
        }
    }

    @Nested
    @DisplayName("getStockHistories()")
    class GetStockHistoriesTests {

        @Test
        @DisplayName("given valid ids, returns StockHistoryListVm with correctly mapped fields")
        void givenValidIds_whenGetStockHistories_thenReturnMappedVm() {
            StockHistory history      = buildStockHistory(1L);
            ProductInfoVm productInfo = buildProductInfoVm();

            when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(List.of(history));
            when(productService.getProduct(PRODUCT_ID)).thenReturn(productInfo);

            StockHistoryListVm result = stockHistoryService.getStockHistories(PRODUCT_ID, WAREHOUSE_ID);

            assertThat(result.data()).hasSize(1);
            StockHistoryVm vm = result.data().get(0);
            assertThat(vm.id()).isEqualTo(1L);
            assertThat(vm.productName()).isEqualTo(productInfo.name());
            assertThat(vm.adjustedQuantity()).isEqualTo(QUANTITY);
            assertThat(vm.note()).isEqualTo(NOTE);
        }

        @Test
        @DisplayName("given no history records, returns empty data list")
        void givenNoHistory_whenGetStockHistories_thenReturnEmptyList() {
            when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Collections.emptyList());
            when(productService.getProduct(PRODUCT_ID)).thenReturn(buildProductInfoVm());

            StockHistoryListVm result = stockHistoryService.getStockHistories(PRODUCT_ID, WAREHOUSE_ID);

            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("given multiple history records, returns all in repository order")
        void givenMultipleHistories_whenGetStockHistories_thenReturnAllInOrder() {
            StockHistory h1 = buildStockHistory(1L);
            StockHistory h2 = buildStockHistory(2L);

            when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(List.of(h1, h2));
            when(productService.getProduct(PRODUCT_ID)).thenReturn(buildProductInfoVm());

            StockHistoryListVm result = stockHistoryService.getStockHistories(PRODUCT_ID, WAREHOUSE_ID);

            assertThat(result.data()).hasSize(2);
            assertThat(result.data().get(0).id()).isEqualTo(1L);
            assertThat(result.data().get(1).id()).isEqualTo(2L);
        }
    }
}