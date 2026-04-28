package com.yas.recommendation.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ViewModelTest {

    @Test
    void testCategoryVm() {
        CategoryVm vm = new CategoryVm(1L, "name", "desc", "slug", "keyword", "metaDesc", (short) 1, true);
        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.name()).isEqualTo("name");
    }

    @Test
    void testImageVm() {
        ImageVm vm = new ImageVm(1L, "url");
        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.url()).isEqualTo("url");
    }

    @Test
    void testProductAttributeValueVm() {
        ProductAttributeValueVm vm = new ProductAttributeValueVm(1L, "name", "value");
        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.nameProductAttribute()).isEqualTo("name");
    }

    @Test
    void testProductVariationVm() {
        ProductVariationVm vm = new ProductVariationVm(1L, "name", "slug", "sku", "gtin", 10.0, null);
        assertThat(vm.id()).isEqualTo(1L);
    }

    @Test
    void testRelatedProductVm() {
        RelatedProductVm vm = new RelatedProductVm();
        vm.setProductId(1L);
        vm.setName("name");
        vm.setPrice(BigDecimal.TEN);
        vm.setBrand("brand");
        vm.setTitle("title");
        vm.setDescription("desc");
        vm.setMetaDescription("metaDesc");
        vm.setSpecification("spec");
        vm.setSlug("slug");
        
        assertThat(vm.getProductId()).isEqualTo(1L);
        assertThat(vm.getName()).isEqualTo("name");
        assertThat(vm.getPrice()).isEqualTo(BigDecimal.TEN);
    }
    
    @Test
    void testProductDetailVm() {
        ProductDetailVm vm = new ProductDetailVm(
                1L, "name", "shortDesc", "desc", "spec", "sku", "gtin", "slug",
                true, true, true, true, true, 10.0, 1L, List.of(), "metaTitle",
                "metaKeyword", "metaDesc", 1L, "brandName", List.of(), List.of(),
                null, List.of()
        );
        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.name()).isEqualTo("name");
    }
}
