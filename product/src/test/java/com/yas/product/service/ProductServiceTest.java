package com.yas.product.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.*;
import com.yas.product.model.enumeration.FilterExistInWhSelection;
import com.yas.product.repository.*;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private MediaService mediaService;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private Product buildProduct(Long id, String name, String slug) {
        Product p = mock(Product.class);
        lenient().when(p.getId()).thenReturn(id);
        lenient().when(p.getName()).thenReturn(name);
        lenient().when(p.getSlug()).thenReturn(slug);
        lenient().when(p.getPrice()).thenReturn(10.0);
        lenient().when(p.isAllowedToOrder()).thenReturn(true);
        lenient().when(p.isPublished()).thenReturn(true);
        lenient().when(p.isFeatured()).thenReturn(false);
        lenient().when(p.isVisibleIndividually()).thenReturn(true);
        lenient().when(p.isHasOptions()).thenReturn(false);
        return p;
    }

    private NoFileMediaVm mediaVm(String url) {
        // Record NoFileMediaVm(Long id, String caption, String fileName, String mediaType, String url)
        return new NoFileMediaVm(1L, "No caption", "file.jpg", "image/jpeg", url);
    }

    // =========================================================================
    // getLatestProducts
    // =========================================================================

    @Nested
    class GetLatestProducts {
        @Test
        void whenCountIsZero_returnsEmptyList() {
            assertThat(productService.getLatestProducts(0)).isEmpty();
            verifyNoInteractions(productRepository);
        }

        @Test
        void whenProductsExist_returnsMappedVms() {
            Product product = buildProduct(1L, "Phone", "phone");
            
            // ProductListVm(Long id, String name, String slug, Boolean isAllowedToOrder, 
            // Boolean isPublished, Boolean isFeatured, Boolean isVisibleIndividually, 
            // Double price, ZonedDateTime createdOn, Long taxClassId, Long parentId)
            ProductListVm vm = new ProductListVm(
                1L, "Phone", "phone", true, true, false, true, 10.0, 
                ZonedDateTime.now(), null, null
            );

            when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(List.of(product));

            try (MockedStatic<ProductListVm> s = mockStatic(ProductListVm.class)) {
                s.when(() -> ProductListVm.fromModel(product)).thenReturn(vm);
                List<ProductListVm> result = productService.getLatestProducts(3);
                assertThat(result).hasSize(1).containsExactly(vm);
            }
        }
    }

    // =========================================================================
    // getProductsByBrand
    // =========================================================================

    @Nested
    class GetProductsByBrand {
        @Test
        void whenBrandFound_returnsThumbnailVms() {
            Brand brand = mock(Brand.class);
            Product product = buildProduct(1L, "Shoe", "shoe");
            lenient().when(product.getThumbnailMediaId()).thenReturn(10L);
            
            when(brandRepository.findBySlug("nike")).thenReturn(Optional.of(brand));
            when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
            when(mediaService.getMedia(10L)).thenReturn(mediaVm("http://img.url"));

            List<ProductThumbnailVm> result = productService.getProductsByBrand("nike");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).thumbnailUrl()).isEqualTo("http://img.url");
        }
    }

    // =========================================================================
    // Stock Operations
    // =========================================================================

    @Nested
    class StockQuantityOperations {
        @Test
        void subtract_reducesCorrectly() {
            Product p = mock(Product.class);
            lenient().when(p.getId()).thenReturn(1L);
            lenient().when(p.isStockTrackingEnabled()).thenReturn(true);
            lenient().when(p.getStockQuantity()).thenReturn(20L);

            when(productRepository.findAllByIdIn(any())).thenReturn(List.of(p));
            productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1L, 5L)));
            
            verify(p).setStockQuantity(15L);
        }

        @Test
        void whenTrackingDisabled_doesNotUpdate() {
            Product p = mock(Product.class);
            lenient().when(p.getId()).thenReturn(1L);
            lenient().when(p.isStockTrackingEnabled()).thenReturn(false);

            when(productRepository.findAllByIdIn(any())).thenReturn(List.of(p));
            productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1L, 5L)));
            
            verify(p, never()).setStockQuantity(anyLong());
        }
    }

    // =========================================================================
    // getProductsForWarehouse
    // =========================================================================

    @Nested
    class GetProductsForWarehouse {
        @Test
        void whenProductsFound_returnsInfoVms() {
            Product p = buildProduct(1L, "Phone", "phone");
            lenient().when(p.getSku()).thenReturn("SKU-001");
            
            // ProductInfoVm(Long id, String name, String sku)
            ProductInfoVm infoVm = new ProductInfoVm(1L, "Phone", "SKU-001");
            
            when(productRepository.findProductForWarehouse(any(), any(), any(), any()))
                .thenReturn(List.of(p));

            try (MockedStatic<ProductInfoVm> s = mockStatic(ProductInfoVm.class)) {
                s.when(() -> ProductInfoVm.fromProduct(p)).thenReturn(infoVm);
                List<ProductInfoVm> result = productService.getProductsForWarehouse(
                    "phone", "SKU", List.of(1L), FilterExistInWhSelection.YES);
                assertThat(result).hasSize(1).containsExactly(infoVm);
            }
        }
    }

    // =========================================================================
    // getFeaturedProductsById
    // =========================================================================

    @Nested
    class GetFeaturedProductsById {
        @Test
        void whenProductHasThumbnail_returnsThumbnailUrl() {
            Product p = buildProduct(1L, "P", "p");
            lenient().when(p.getThumbnailMediaId()).thenReturn(5L);
            lenient().when(p.getParent()).thenReturn(null);
            
            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(p));
            when(mediaService.getMedia(5L)).thenReturn(mediaVm("http://thumb.url"));

            List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));
            assertThat(result.get(0).thumbnailUrl()).isEqualTo("http://thumb.url");
        }
    }
}