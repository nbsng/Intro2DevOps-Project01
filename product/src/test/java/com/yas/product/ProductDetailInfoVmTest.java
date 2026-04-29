package com.yas.product.viewmodel.product;

import com.yas.product.model.Category;
import com.yas.product.viewmodel.ImageVm;
import com.yas.product.viewmodel.productattribute.ProductAttributeValueGetVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ProductDetailInfoVmTest {

    // -------------------------------------------------------------------------
    // Helper: build a fully-populated instance
    // -------------------------------------------------------------------------
    private static final long   ID          = 1L;
    private static final String NAME        = "iPhone 15";
    private static final String SHORT_DESC  = "short";
    private static final String DESC        = "description";
    private static final String SPEC        = "specification";
    private static final String SKU         = "SKU-001";
    private static final String GTIN        = "GTIN-001";
    private static final String SLUG        = "iphone-15";
    private static final Double PRICE       = 999.99;
    private static final Long   BRAND_ID    = 10L;
    private static final String META_TITLE  = "meta title";
    private static final String META_KW     = "meta keyword";
    private static final String META_DESC   = "meta desc";
    private static final Long   TAX_ID      = 5L;
    private static final String BRAND_NAME  = "Apple";

    private Category                    category;
    private ProductAttributeValueGetVm  attrValue;
    private ProductVariationGetVm       variation;
    private ImageVm                     thumbnail;
    private ImageVm                     productImage;

    @BeforeEach
    void setUp() {
        category     = mock(Category.class);
        attrValue    = mock(ProductAttributeValueGetVm.class);
        variation    = mock(ProductVariationGetVm.class);
        thumbnail    = mock(ImageVm.class);
        productImage = mock(ImageVm.class);
    }

    private ProductDetailInfoVm buildFull() {
        return new ProductDetailInfoVm(
            ID, NAME, SHORT_DESC, DESC, SPEC, SKU, GTIN, SLUG,
            true, true, false, true, true,
            PRICE, BRAND_ID, List.of(category),
            META_TITLE, META_KW, META_DESC, TAX_ID, BRAND_NAME,
            List.of(attrValue), List.of(variation),
            thumbnail, List.of(productImage)
        );
    }

    // =========================================================================
    // Constructor — all fields set correctly
    // =========================================================================

    @Nested
    class Constructor {

        @Test
        void allFields_mappedCorrectly() {
            ProductDetailInfoVm vm = buildFull();

            assertThat(vm.getId()).isEqualTo(ID);
            assertThat(vm.getName()).isEqualTo(NAME);
            assertThat(vm.getShortDescription()).isEqualTo(SHORT_DESC);
            assertThat(vm.getDescription()).isEqualTo(DESC);
            assertThat(vm.getSpecification()).isEqualTo(SPEC);
            assertThat(vm.getSku()).isEqualTo(SKU);
            assertThat(vm.getGtin()).isEqualTo(GTIN);
            assertThat(vm.getSlug()).isEqualTo(SLUG);
            assertThat(vm.getIsAllowedToOrder()).isTrue();
            assertThat(vm.getIsPublished()).isTrue();
            assertThat(vm.getIsFeatured()).isFalse();
            assertThat(vm.getIsVisible()).isTrue();
            assertThat(vm.getStockTrackingEnabled()).isTrue();
            assertThat(vm.getPrice()).isEqualTo(PRICE);
            assertThat(vm.getBrandId()).isEqualTo(BRAND_ID);
            assertThat(vm.getMetaTitle()).isEqualTo(META_TITLE);
            assertThat(vm.getMetaKeyword()).isEqualTo(META_KW);
            assertThat(vm.getMetaDescription()).isEqualTo(META_DESC);
            assertThat(vm.getTaxClassId()).isEqualTo(TAX_ID);
            assertThat(vm.getBrandName()).isEqualTo(BRAND_NAME);
            assertThat(vm.getThumbnail()).isSameAs(thumbnail);
        }

        @Test
        void categories_mappedCorrectly() {
            ProductDetailInfoVm vm = buildFull();
            assertThat(vm.getCategories()).hasSize(1).containsExactly(category);
        }

        @Test
        void attributeValues_mappedCorrectly() {
            ProductDetailInfoVm vm = buildFull();
            assertThat(vm.getAttributeValues()).hasSize(1).containsExactly(attrValue);
        }

        @Test
        void variations_mappedCorrectly() {
            ProductDetailInfoVm vm = buildFull();
            assertThat(vm.getVariations()).hasSize(1).containsExactly(variation);
        }

        @Test
        void productImages_mappedCorrectly() {
            ProductDetailInfoVm vm = buildFull();
            assertThat(vm.getProductImages()).hasSize(1).containsExactly(productImage);
        }
    }

    // =========================================================================
    // Null-safety — categories / attributeValues / variations
    // =========================================================================

    @Nested
    class NullSafety {

        @Test
        void nullCategories_defaultsToEmptyList() {
            ProductDetailInfoVm vm = new ProductDetailInfoVm(
                ID, NAME, null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, null,
                null, null, null, null, null,
                null, null, null, null
            );
            assertThat(vm.getCategories()).isNotNull().isEmpty();
        }

        @Test
        void nullAttributeValues_defaultsToEmptyList() {
            ProductDetailInfoVm vm = new ProductDetailInfoVm(
                ID, NAME, null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, null,
                null, null, null, null, null,
                null, null, null, null
            );
            assertThat(vm.getAttributeValues()).isNotNull().isEmpty();
        }

        @Test
        void nullVariations_defaultsToEmptyList() {
            ProductDetailInfoVm vm = new ProductDetailInfoVm(
                ID, NAME, null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, null,
                null, null, null, null, null,
                null, null, null, null
            );
            assertThat(vm.getVariations()).isNotNull().isEmpty();
        }

        @Test
        void nonNullCategories_notReplacedWithEmptyList() {
            List<Category> cats = List.of(category);
            ProductDetailInfoVm vm = new ProductDetailInfoVm(
                ID, NAME, null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, cats,
                null, null, null, null, null,
                null, null, null, null
            );
            assertThat(vm.getCategories()).isSameAs(cats);
        }

        @Test
        void nullThumbnail_remainsNull() {
            ProductDetailInfoVm vm = new ProductDetailInfoVm(
                ID, NAME, null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, null,
                null, null, null, null, null,
                null, null, null, null
            );
            assertThat(vm.getThumbnail()).isNull();
        }

        @Test
        void nullProductImages_remainsNull() {
            ProductDetailInfoVm vm = new ProductDetailInfoVm(
                ID, NAME, null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, null,
                null, null, null, null, null,
                null, null, null, null
            );
            assertThat(vm.getProductImages()).isNull();
        }
    }

    // =========================================================================
    // Setters — update fields correctly
    // =========================================================================

    @Nested
    class Setters {

        @Test
        void setters_updateAllFields() {
            ProductDetailInfoVm vm = buildFull();

            vm.setId(99L);
            vm.setName("Galaxy S24");
            vm.setShortDescription("new short");
            vm.setDescription("new desc");
            vm.setSpecification("new spec");
            vm.setSku("SKU-NEW");
            vm.setGtin("GTIN-NEW");
            vm.setSlug("galaxy-s24");
            vm.setIsAllowedToOrder(false);
            vm.setIsPublished(false);
            vm.setIsFeatured(true);
            vm.setIsVisible(false);
            vm.setStockTrackingEnabled(false);
            vm.setPrice(1299.99);
            vm.setBrandId(20L);
            vm.setMetaTitle("new meta");
            vm.setMetaKeyword("new kw");
            vm.setMetaDescription("new meta desc");
            vm.setTaxClassId(7L);
            vm.setBrandName("Samsung");

            assertThat(vm.getId()).isEqualTo(99L);
            assertThat(vm.getName()).isEqualTo("Galaxy S24");
            assertThat(vm.getShortDescription()).isEqualTo("new short");
            assertThat(vm.getDescription()).isEqualTo("new desc");
            assertThat(vm.getSku()).isEqualTo("SKU-NEW");
            assertThat(vm.getGtin()).isEqualTo("GTIN-NEW");
            assertThat(vm.getSlug()).isEqualTo("galaxy-s24");
            assertThat(vm.getIsAllowedToOrder()).isFalse();
            assertThat(vm.getIsPublished()).isFalse();
            assertThat(vm.getIsFeatured()).isTrue();
            assertThat(vm.getIsVisible()).isFalse();
            assertThat(vm.getStockTrackingEnabled()).isFalse();
            assertThat(vm.getPrice()).isEqualTo(1299.99);
            assertThat(vm.getBrandId()).isEqualTo(20L);
            assertThat(vm.getBrandName()).isEqualTo("Samsung");
            assertThat(vm.getTaxClassId()).isEqualTo(7L);
        }

        @Test
        void setCategories_updatesCorrectly() {
            ProductDetailInfoVm vm = buildFull();
            Category newCat = mock(Category.class);
            vm.setCategories(List.of(newCat));
            assertThat(vm.getCategories()).containsExactly(newCat);
        }

        @Test
        void setAttributeValues_updatesCorrectly() {
            ProductDetailInfoVm vm = buildFull();
            ProductAttributeValueGetVm newAttr = mock(ProductAttributeValueGetVm.class);
            vm.setAttributeValues(List.of(newAttr));
            assertThat(vm.getAttributeValues()).containsExactly(newAttr);
        }

        @Test
        void setVariations_updatesCorrectly() {
            ProductDetailInfoVm vm = buildFull();
            ProductVariationGetVm newVar = mock(ProductVariationGetVm.class);
            vm.setVariations(List.of(newVar));
            assertThat(vm.getVariations()).containsExactly(newVar);
        }

        @Test
        void setThumbnail_updatesCorrectly() {
            ProductDetailInfoVm vm = buildFull();
            ImageVm newThumb = mock(ImageVm.class);
            vm.setThumbnail(newThumb);
            assertThat(vm.getThumbnail()).isSameAs(newThumb);
        }

        @Test
        void setProductImages_updatesCorrectly() {
            ProductDetailInfoVm vm = buildFull();
            ImageVm newImg = mock(ImageVm.class);
            vm.setProductImages(List.of(newImg));
            assertThat(vm.getProductImages()).containsExactly(newImg);
        }
    }
}