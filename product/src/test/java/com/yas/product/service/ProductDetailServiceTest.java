package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MediaService mediaService;
    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @InjectMocks
    private ProductDetailService productDetailService;

    @Test
    void getProductDetailById_WhenNotPublished_ShouldThrowNotFoundException() {
        Product product = Product.builder().id(1L).isPublished(false).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_WhenHasOptions_ShouldReturnVariationsAndImages() {
        Brand brand = new Brand();
        brand.setId(10L);
        brand.setName("Brand");

        Category category = new Category();
        category.setId(11L);
        category.setName("Cat");

        Product mainProduct = Product.builder()
            .id(1L)
            .name("Main")
            .slug("main")
            .sku("sku")
            .gtin("gtin")
            .price(10.0)
            .isPublished(true)
            .hasOptions(true)
            .brand(brand)
            .thumbnailMediaId(5L)
            .productImages(List.of(ProductImage.builder().imageId(7L).build()))
            .build();
        mainProduct.setProductCategories(List.of(ProductCategory.builder()
            .product(mainProduct)
            .category(category)
            .build()));

        ProductAttribute attr = ProductAttribute.builder().id(20L).name("Color").build();
        ProductAttributeValue attrValue = new ProductAttributeValue();
        attrValue.setId(21L);
        attrValue.setProduct(mainProduct);
        attrValue.setProductAttribute(attr);
        attrValue.setValue("Red");
        mainProduct.setAttributeValues(List.of(attrValue));

        Product variation = Product.builder()
            .id(2L)
            .name("Var")
            .slug("var")
            .sku("vsku")
            .gtin("vgtin")
            .price(12.0)
            .isPublished(true)
            .thumbnailMediaId(6L)
            .productImages(List.of(ProductImage.builder().imageId(8L).build()))
            .build();
        mainProduct.setProducts(List.of(variation));

        ProductOption option = new ProductOption();
        option.setId(30L);
        ProductOptionCombination combination = ProductOptionCombination.builder()
            .product(variation)
            .productOption(option)
            .value("Red")
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(mainProduct));
        when(productOptionCombinationRepository.findAllByProduct(variation))
            .thenReturn(List.of(combination));
        when(mediaService.getMedia(any()))
            .thenReturn(new NoFileMediaVm(1L, "cap", "file", "type", "url"));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBrandId()).isEqualTo(10L);
        assertThat(result.getCategories()).hasSize(1);
        assertThat(result.getVariations()).hasSize(1);
        assertThat(result.getProductImages()).hasSize(1);
    }
}
