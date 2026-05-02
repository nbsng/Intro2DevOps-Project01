package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.InternalServerErrorException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.ProductOptionValue;
import com.yas.product.model.ProductRelated;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.ImageVm;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductFeatureGetVm;
import com.yas.product.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.product.viewmodel.product.ProductListGetFromCategoryVm;
import com.yas.product.viewmodel.product.ProductListGetVm;
import com.yas.product.viewmodel.product.ProductListVm;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductPutVm;
import com.yas.product.viewmodel.product.ProductGetDetailVm;
import com.yas.product.viewmodel.product.ProductDetailGetVm;
import com.yas.product.viewmodel.product.ProductCheckoutListVm;
import com.yas.product.viewmodel.product.ProductEsDetailVm;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductSlugGetVm;
import com.yas.product.viewmodel.product.ProductVariationGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailGetVm;
import com.yas.product.viewmodel.product.ProductThumbnailVm;
import com.yas.product.viewmodel.product.ProductVariationPostVm;
import com.yas.product.viewmodel.product.ProductsGetVm;
import com.yas.product.viewmodel.productattribute.ProductAttributeGroupGetVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePostVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePutVm;
import com.yas.product.viewmodel.product.ProductOptionValueDisplay;
import com.yas.product.viewmodel.product.ProductVariationPutVm;
import java.util.Map;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MediaService mediaService;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private ProductOptionValueRepository productOptionValueRepository;
    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock
    private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Brand brand;
    private Category category;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("Brand 1");
        brand.setSlug("brand-1");

        category = new Category();
        category.setId(1L);
        category.setName("Category 1");
        category.setSlug("category-1");
        product = Product.builder()
                .id(1L)
                .name("Product 1")
                .slug("product-1")
                .sku("SKU1")
                .brand(brand)
                .build();
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldThrowNotFoundException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProductDetailVm() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailVm result = productService.getProductById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Product 1");
        assertThat(result.slug()).isEqualTo("product-1");
        assertThat(result.sku()).isEqualTo("SKU1");
        assertThat(result.brandId()).isEqualTo(1L);
    }

    @Test
    void getLatestProducts_WhenCountIsLessThanOrEqualZero_ShouldReturnEmptyList() {
        List<ProductListVm> result = productService.getLatestProducts(0);
        assertThat(result).isEmpty();
    }

    @Test
    void getLatestProducts_WhenCountIsValid_ShouldReturnProductListVm() {
        when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getLatestProducts(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    @Test
    void getProductsByBrand_WhenBrandNotFound_ShouldThrowNotFoundException() {
        when(brandRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("brand-slug"));
    }

    @Test
    void getProductsByBrand_WhenBrandExists_ShouldReturnProductList() {
        when(brandRepository.findBySlug("brand-1")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
        when(mediaService.getMedia(any())).thenReturn(new NoFileMediaVm(1L, "caption", "file", "type", "url"));

        List<ProductThumbnailVm> result = productService.getProductsByBrand("brand-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Product 1");
        assertThat(result.get(0).thumbnailUrl()).isEqualTo("url");
    }

    @Test
    void getProductsFromCategory_WhenCategoryNotFound_ShouldThrowNotFoundException() {
        when(categoryRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductsFromCategory(0, 10, "category-1"));
    }

    @Test
    void getProductsFromCategory_WhenCategoryExists_ShouldReturnProducts() {
        when(categoryRepository.findBySlug("category-1")).thenReturn(Optional.of(category));
        ProductCategory productCategory = ProductCategory.builder().product(product).category(category).build();
        Page<ProductCategory> page = new PageImpl<>(List.of(productCategory));
        when(productCategoryRepository.findAllByCategory(any(Pageable.class), any(Category.class))).thenReturn(page);
        when(mediaService.getMedia(any())).thenReturn(new NoFileMediaVm(1L, "caption", "file", "type", "url"));

        ProductListGetFromCategoryVm result = productService.getProductsFromCategory(0, 10, "category-1");

        assertThat(result.productContent()).hasSize(1);
        assertThat(result.productContent().get(0).id()).isEqualTo(1L);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void getProductsWithFilter_ShouldReturnProductListGetVm() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.getProductsWithFilter(anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        ProductListGetVm result = productService.getProductsWithFilter(0, 10, "Product", "Brand");

        assertThat(result.productContent()).hasSize(1);
        assertThat(result.productContent().get(0).id()).isEqualTo(1L);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void getListFeaturedProducts_ShouldReturnFeaturedProducts() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.getFeaturedProduct(any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(any())).thenReturn(new NoFileMediaVm(1L, "caption", "file", "type", "url"));

        ProductFeatureGetVm result = productService.getListFeaturedProducts(0, 10);

        assertThat(result.productList()).hasSize(1);
        assertThat(result.productList().get(0).id()).isEqualTo(1L);
        assertThat(result.totalPage()).isEqualTo(1);
    }
    @Test
    void createProduct_WhenLengthLessThanWidth_ShouldThrowBadRequestException() {
        ProductPostVm productPostVm = new ProductPostVm(
                "Name", "slug", 1L, List.of(1L), "short", "desc", "spec", "sku", "gtin",
                1.0, null, 1.0, 2.0, 1.0, 10.0, true, true, false, true, true,
                "meta", "meta", "meta", 1L, List.of(), List.of(), List.of(), List.of(), List.of(), 1L);

        assertThrows(BadRequestException.class, () -> productService.createProduct(productPostVm));
    }

    @Test
    void createProduct_WhenSlugExists_ShouldThrowDuplicatedException() {
        ProductPostVm productPostVm = new ProductPostVm(
                "Name", "slug", 1L, List.of(1L), "short", "desc", "spec", "sku", "gtin",
                1.0, null, 2.0, 1.0, 1.0, 10.0, true, true, false, true, true,
                "meta", "meta", "meta", 1L, List.of(), List.of(), List.of(), List.of(), List.of(), 1L);

        when(productRepository.findBySlugAndIsPublishedTrue("slug")).thenReturn(Optional.of(product));

        assertThrows(DuplicatedException.class, () -> productService.createProduct(productPostVm));
    }

    @Test
    void createProduct_WhenCategoryIdsMissing_ShouldThrowBadRequestException() {
        List<Long> categoryIds = new ArrayList<>(List.of(1L, 2L));
        ProductPostVm productPostVm = new ProductPostVm(
                "Name", "new-slug", 1L, categoryIds, "short", "desc", "spec", "new-sku", "new-gtin",
                1.0, null, 2.0, 1.0, 1.0, 10.0, true, true, false, true, true,
                "meta", "meta", "meta", 1L, List.of(), List.of(), List.of(), List.of(), List.of(), 1L);

        when(productRepository.findBySlugAndIsPublishedTrue("new-slug")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("new-gtin")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("new-sku")).thenReturn(Optional.empty());
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(categoryRepository.findAllById(categoryIds)).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> productService.createProduct(productPostVm));
    }

    @Test
    void createProduct_WhenCategoryIdsPartiallyMissing_ShouldThrowBadRequestException() {
        List<Long> categoryIds = new ArrayList<>(List.of(1L, 2L));
        ProductPostVm productPostVm = new ProductPostVm(
                "Name", "new-slug", 1L, categoryIds, "short", "desc", "spec", "new-sku", "new-gtin",
                1.0, null, 2.0, 1.0, 1.0, 10.0, true, true, false, true, true,
                "meta", "meta", "meta", 1L, List.of(), List.of(), List.of(), List.of(), List.of(), 1L);

        when(productRepository.findBySlugAndIsPublishedTrue("new-slug")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("new-gtin")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("new-sku")).thenReturn(Optional.empty());
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(categoryRepository.findAllById(categoryIds)).thenReturn(List.of(category));

        assertThrows(BadRequestException.class, () -> productService.createProduct(productPostVm));
    }

    @Test
    void createProduct_ShouldSaveAndReturnProductDetail() {
        ProductPostVm productPostVm = new ProductPostVm(
                "Name", "new-slug", 1L, List.of(1L), "short", "desc", "spec", "new-sku", "new-gtin",
                1.0, null, 2.0, 1.0, 1.0, 10.0, true, true, false, true, true,
                "meta", "meta", "meta", 1L, List.of(1L, 2L), List.of(), List.of(), List.of(), List.of(2L), 1L);

        when(productRepository.findBySlugAndIsPublishedTrue("new-slug")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("new-gtin")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("new-sku")).thenReturn(Optional.empty());

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));

        Product product2 = Product.builder().id(2L).build();
        when(productRepository.findAllById(anyList())).thenReturn(List.of(product2));

        ProductGetDetailVm result = productService.createProduct(productPostVm);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(productImageRepository).saveAll(anyList());
        verify(productCategoryRepository).saveAll(anyList());
        verify(productRelatedRepository).saveAll(anyList());
    }
    @Test
    void createProduct_WithVariationsAndOptions_ShouldSaveAll() {
        ProductVariationPostVm variationVm = new ProductVariationPostVm(
                "Var1", "var-slug", "var-sku", "var-gtin", 10.0, 1L, List.of(1L), Map.of(1L, "Value1"));
        ProductOptionValuePostVm optionValueVm = new ProductOptionValuePostVm(1L, "type", 1, List.of("Value1"));
        ProductOptionValueDisplay displayVm = new ProductOptionValueDisplay(1L, "type", 1, "Value1");

        ProductPostVm productPostVm = new ProductPostVm(
                "Name", "new-slug", 1L, List.of(1L), "short", "desc", "spec", "new-sku", "new-gtin",
                1.0, null, 2.0, 1.0, 1.0, 10.0, true, true, false, true, true,
                "meta", "meta", "meta", 1L, List.of(1L), 
                List.of(variationVm), List.of(optionValueVm), List.of(displayVm), List.of(), 1L);

        when(productRepository.findBySlugAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        // gtin and sku only if not empty
        when(productRepository.findByGtinAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        Product savedVariation = Product.builder().id(2L).slug("var-slug").build();
        when(productRepository.saveAll(anyList())).thenReturn(List.of(savedVariation));
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));

        ProductOption option = new ProductOption();
        option.setId(1L);
        option.setName("Opt1");
        when(productOptionRepository.findAllByIdIn(anyList())).thenReturn(List.of(option));

        ProductOptionValue pov = ProductOptionValue.builder().productOption(option).value("Value1").build();
        when(productOptionValueRepository.saveAll(anyList())).thenReturn(List.of(pov));

        ProductGetDetailVm result = productService.createProduct(productPostVm);

        assertThat(result).isNotNull();
        verify(productRepository).saveAll(anyList());
        verify(productOptionValueRepository).saveAll(anyList());
        verify(productOptionCombinationRepository).saveAll(anyList());
    }

        @Test
        void createProduct_WhenCombinationVariationMissing_ShouldThrowInternalServerErrorException() {
        ProductVariationPostVm variationVm = new ProductVariationPostVm(
            "Var1", "var-slug", "var-sku", "var-gtin", 10.0, 1L, List.of(1L), Map.of(1L, "Value1"));
        ProductOptionValuePostVm optionValueVm = new ProductOptionValuePostVm(1L, "type", 1, List.of("Value1"));
        ProductOptionValueDisplay displayVm = new ProductOptionValueDisplay(1L, "type", 1, "Value1");

        ProductPostVm productPostVm = new ProductPostVm(
            "Name", "new-slug", 1L, List.of(1L), "short", "desc", "spec", "new-sku", "new-gtin",
            1.0, null, 2.0, 1.0, 1.0, 10.0, true, true, false, true, true,
            "meta", "meta", "meta", 1L, List.of(1L),
            List.of(variationVm), List.of(optionValueVm), List.of(displayVm), List.of(), 1L);

        when(productRepository.findBySlugAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));

        Product savedVariation = Product.builder().id(2L).slug("other-slug").build();
        when(productRepository.saveAll(anyList())).thenReturn(List.of(savedVariation));

        ProductOption option = new ProductOption();
        option.setId(1L);
        when(productOptionRepository.findAllByIdIn(anyList())).thenReturn(List.of(option));

        ProductOptionValue pov = ProductOptionValue.builder().productOption(option).value("Value1").build();
        when(productOptionValueRepository.saveAll(anyList())).thenReturn(List.of(pov));

        assertThrows(InternalServerErrorException.class, () -> productService.createProduct(productPostVm));
        }

        @Test
        void createProduct_WhenOptionValueMissing_ShouldThrowBadRequestException() {
        ProductVariationPostVm variationVm = new ProductVariationPostVm(
            "Var1", "var-slug", "var-sku", "var-gtin", 10.0, 1L, List.of(1L), Map.of(1L, "Value1"));
        ProductOptionValuePostVm optionValueVm = new ProductOptionValuePostVm(1L, "type", 1, List.of("Value1"));
        ProductOptionValueDisplay displayVm = new ProductOptionValueDisplay(1L, "type", 1, "Value1");

        ProductPostVm productPostVm = new ProductPostVm(
            "Name", "new-slug", 1L, List.of(1L), "short", "desc", "spec", "new-sku", "new-gtin",
            1.0, null, 2.0, 1.0, 1.0, 10.0, true, true, false, true, true,
            "meta", "meta", "meta", 1L, List.of(1L),
            List.of(variationVm), List.of(optionValueVm), List.of(displayVm), List.of(), 1L);

        when(productRepository.findBySlugAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));

        Product savedVariation = Product.builder().id(2L).slug("var-slug").build();
        when(productRepository.saveAll(anyList())).thenReturn(List.of(savedVariation));

        ProductOption option = new ProductOption();
        option.setId(1L);
        when(productOptionRepository.findAllByIdIn(anyList())).thenReturn(List.of(option));
        when(productOptionValueRepository.saveAll(anyList())).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> productService.createProduct(productPostVm));
        }

    @Test
    void getProductDetail_ShouldReturnDetail() {
        product.setThumbnailMediaId(1L);
        when(productRepository.findBySlugAndIsPublishedTrue("product-1")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(any())).thenReturn(new NoFileMediaVm(1L, "caption", "file", "type", "url"));
        
        ProductDetailGetVm result = productService.getProductDetail("product-1");
        
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getProductDetail_WhenAttributesHaveGroupAndNone_ShouldReturnGroups() {
        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setId(9L);
        group.setName("Specs");

        ProductAttribute attrWithGroup = ProductAttribute.builder().id(1L).name("Color")
            .productAttributeGroup(group).build();
        ProductAttribute attrWithoutGroup = ProductAttribute.builder().id(2L).name("Size")
            .productAttributeGroup(null).build();

        ProductAttributeValue value1 = new ProductAttributeValue();
        value1.setProduct(product);
        value1.setProductAttribute(attrWithGroup);
        value1.setValue("Red");

        ProductAttributeValue value2 = new ProductAttributeValue();
        value2.setProduct(product);
        value2.setProductAttribute(attrWithoutGroup);
        value2.setValue("M");

        product.setAttributeValues(List.of(value1, value2));
        product.setProductCategories(List.of(ProductCategory.builder().product(product).category(category).build()));
        product.setThumbnailMediaId(1L);
        product.setProductImages(List.of(ProductImage.builder().imageId(2L).product(product).build()));

        when(productRepository.findBySlugAndIsPublishedTrue("product-1")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(new NoFileMediaVm(1L, "cap", "file", "type", "url"));
        when(mediaService.getMedia(2L)).thenReturn(new NoFileMediaVm(2L, "cap", "file", "type", "url2"));

        ProductDetailGetVm result = productService.getProductDetail("product-1");

        List<String> groupNames = result.productAttributeGroups().stream()
            .map(ProductAttributeGroupGetVm::name)
            .toList();
        assertThat(groupNames).contains("Specs", "None group");
        assertThat(result.productImageMediaUrls()).hasSize(1);
    }

    @Test
    void updateProduct_WhenProductNotFound_ShouldThrowNotFoundException() {
        ProductPutVm productPutVm = new ProductPutVm(
                "Name", "slug", 10.0, true, true, false, true, true, 1L, List.of(), "short", "desc", "spec",
                "sku", "gtin", 1.0, null, 1.0, 2.0, 1.0, "meta", "meta", "meta", 1L, List.of(), List.of(), List.of(), List.of(), List.of(), 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.updateProduct(1L, productPutVm));
    }

    @Test
    void updateProduct_WhenLengthLessThanWidth_ShouldThrowBadRequestException() {
        ProductPutVm productPutVm = new ProductPutVm(
                "Name", "slug", 10.0, true, true, false, true, true, 1L, List.of(), "short", "desc", "spec",
                "sku", "gtin", 1.0, null, 1.0, 2.0, 1.0, "meta", "meta", "meta", 1L, List.of(), List.of(), List.of(), List.of(), List.of(), 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> productService.updateProduct(1L, productPutVm));
    }

    @Test
    void updateProduct_ShouldUpdateProduct() {
        ProductPutVm productPutVm = new ProductPutVm(
                "Name", "new-slug", 10.0, true, true, false, true, true, 1L, List.of(1L), "short", "desc", "spec",
                "new-sku", "new-gtin", 1.0, null, 2.0, 1.0, 1.0, "meta", "meta", "meta", 1L, List.of(1L), List.of(), List.of(), List.of(), List.of(), 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySlugAndIsPublishedTrue("new-slug")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("new-gtin")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("new-sku")).thenReturn(Optional.empty());

        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));

        ProductOption option = new ProductOption();
        option.setId(1L);
        when(productOptionRepository.findAllByIdIn(anyList())).thenReturn(List.of(option));

        productService.updateProduct(1L, productPutVm);

        verify(productImageRepository).saveAll(anyList());
        verify(productCategoryRepository).saveAll(anyList());
    }

    @Test
    void updateProduct_WithNewVariation_ShouldSaveProduct() {
        ProductVariationPutVm newVariation = new ProductVariationPutVm(
                null, "new-var", "new-var-slug", "new-var-sku", "new-var-gtin", 10.0, 1L, List.of(1L), Map.of(1L, "Value1"));
        
        ProductOptionValueDisplay displayVm = new ProductOptionValueDisplay(1L, "type", 1, "Value1");
        
        ProductPutVm productPutVm = new ProductPutVm(
                "Name", "new-slug", 10.0, true, true, false, true, true, 1L, List.of(1L), "short", "desc", "spec",
                "new-sku", "new-gtin", 1.0, null, 2.0, 1.0, 1.0, "meta", "meta", "meta", 1L, List.of(1L), 
                List.of(newVariation), List.of(), List.of(displayVm), List.of(), 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySlugAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());

        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));

        ProductOption option = new ProductOption();
        option.setId(1L);
        when(productOptionRepository.findAllByIdIn(anyList())).thenReturn(List.of(option));
        
        ProductOptionValue pov = ProductOptionValue.builder().productOption(option).value("Value1").build();
        when(productOptionValueRepository.saveAll(anyList())).thenReturn(List.of(pov));
        
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.updateProduct(1L, productPutVm);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenRelatedProductsChanged_ShouldUpdateRelations() {
        Product relatedOld = Product.builder().id(2L).build();
        ProductRelated oldRelation = ProductRelated.builder().product(product).relatedProduct(relatedOld).build();
        product.setRelatedProducts(List.of(oldRelation));

        ProductOptionValuePutVm optionValueVm = new ProductOptionValuePutVm(1L, "type", 1, List.of("Value1"));
        ProductOptionValueDisplay displayVm = new ProductOptionValueDisplay(1L, "type", 1, "Value1");
        ProductPutVm productPutVm = new ProductPutVm(
                "Name", "new-slug", 10.0, true, true, false, true, true, 1L, List.of(1L), "short", "desc", "spec",
                "new-sku", "new-gtin", 1.0, null, 2.0, 1.0, 1.0, "meta", "meta", "meta", 1L, List.of(),
                List.of(), List.of(optionValueVm), List.of(displayVm), List.of(3L), 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySlugAndIsPublishedTrue("new-slug")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("new-gtin")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("new-sku")).thenReturn(Optional.empty());
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category));

        ProductOption option = new ProductOption();
        option.setId(1L);
        when(productOptionRepository.findAllByIdIn(anyList())).thenReturn(List.of(option));

        Product relatedNew = Product.builder().id(3L).build();
        when(productRepository.findAllById(anyList())).thenReturn(List.of(relatedNew));

        productService.updateProduct(1L, productPutVm);

        verify(productRelatedRepository).deleteAll(anyList());
        verify(productRelatedRepository).saveAll(anyList());
    }

    @Test
    void deleteProduct_WhenProductNotFound_ShouldThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.deleteProduct(1L));
    }

    @Test
    void deleteProduct_WithParent_ShouldDeleteCombinations() {
        Product parent = new Product();
        parent.setId(2L);
        product.setParent(parent);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(product)).thenReturn(List.of(new ProductOptionCombination()));

        productService.deleteProduct(1L);

        assertThat(product.isPublished()).isFalse();
        verify(productOptionCombinationRepository).deleteAll(anyList());
        verify(productRepository).save(product);
    }

    @Test
    void getProductsByBrand_ShouldReturnProducts() {
        when(brandRepository.findBySlug("brand-slug")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
        when(mediaService.getMedia(any())).thenReturn(new NoFileMediaVm(1L, "cap", "file", "type", "url"));

        List<ProductThumbnailVm> result = productService.getProductsByBrand("brand-slug");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).slug()).isEqualTo("product-1");
    }

    @Test
    void getProductsFromCategory_ShouldReturnProducts() {
        when(categoryRepository.findBySlug("cat-slug")).thenReturn(Optional.of(category));
        ProductCategory productCategory = new ProductCategory();
        productCategory.setProduct(product);
        Page<ProductCategory> page = new PageImpl<>(List.of(productCategory));
        when(productCategoryRepository.findAllByCategory(any(Pageable.class), eq(category))).thenReturn(page);
        when(mediaService.getMedia(any())).thenReturn(new NoFileMediaVm(1L, "cap", "file", "type", "url"));

        ProductListGetFromCategoryVm result = productService.getProductsFromCategory(0, 5, "cat-slug");

        assertThat(result.productContent()).hasSize(1);
    }

    @Test
    void getFeaturedProductsById_WithThumbnail_ShouldReturnProducts() {
        product.setThumbnailMediaId(1L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));
        when(mediaService.getMedia(1L)).thenReturn(new NoFileMediaVm(1L, "cap", "file", "type", "url"));

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).thumbnailUrl()).isEqualTo("url");
    }

    @Test
    void getFeaturedProductsById_NoThumbnailWithParent_ShouldReturnParentThumbnail() {
        product.setThumbnailMediaId(null);
        Product parent = new Product();
        parent.setId(2L);
        parent.setThumbnailMediaId(2L);
        product.setParent(parent);
        
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));
        when(mediaService.getMedia(null)).thenReturn(new NoFileMediaVm(null, "cap", "file", "type", ""));
        when(productRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(mediaService.getMedia(2L)).thenReturn(new NoFileMediaVm(2L, "cap", "file", "type", "parent-url"));

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).thumbnailUrl()).isEqualTo("parent-url");
    }

    @Test
    void getFeaturedProductsById_NoThumbnailNoParent_ShouldReturnEmptyThumbnail() {
        product.setThumbnailMediaId(null);
        product.setParent(null);

        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));
        when(mediaService.getMedia(null)).thenReturn(new NoFileMediaVm(null, "cap", "file", "type", ""));

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).thumbnailUrl()).isEqualTo("");
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    void setProductImages_WhenEmptyList_ShouldDeleteImages() {
        product.setId(1L);

        List<ProductImage> result = productService.setProductImages(Collections.emptyList(), product);

        assertThat(result).isEmpty();
        verify(productImageRepository).deleteByProductId(1L);
    }

    @Test
    void setProductImages_WhenExistingImages_ShouldReturnNewOnesAndDeleteRemoved() {
        product.setId(1L);
        ProductImage image1 = ProductImage.builder().imageId(1L).product(product).build();
        ProductImage image2 = ProductImage.builder().imageId(2L).product(product).build();
        product.setProductImages(List.of(image1, image2));

        List<ProductImage> result = productService.setProductImages(List.of(2L, 3L), product);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getImageId()).isEqualTo(3L);
        verify(productImageRepository).deleteByImageIdInAndProductId(List.of(1L), 1L);
    }

    @Test
    void setProductImages_WhenNoChanges_ShouldReturnEmptyList() {
        product.setId(1L);
        ProductImage image1 = ProductImage.builder().imageId(1L).product(product).build();
        ProductImage image2 = ProductImage.builder().imageId(2L).product(product).build();
        product.setProductImages(List.of(image1, image2));

        List<ProductImage> result = productService.setProductImages(List.of(1L, 2L), product);

        assertThat(result).isEmpty();
        verify(productImageRepository, never()).deleteByImageIdInAndProductId(anyList(), anyLong());
    }

    @Test
    void setProductImages_WhenNoExistingImages_ShouldCreateNewList() {
        product.setId(1L);
        product.setProductImages(null);

        List<ProductImage> result = productService.setProductImages(List.of(2L, 3L), product);

        assertThat(result).hasSize(2);
        verify(productImageRepository, never()).deleteByProductId(anyLong());
    }

    @Test
    void getProductSlug_WhenHasParent_ShouldReturnParentSlug() {
        Product parent = new Product();
        parent.setId(2L);
        parent.setSlug("parent-slug");
        product.setParent(parent);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductSlugGetVm result = productService.getProductSlug(1L);

        assertThat(result.slug()).isEqualTo("parent-slug");
        assertThat(result.productVariantId()).isEqualTo(1L);
    }

    @Test
    void getProductSlug_WhenNoParent_ShouldReturnProductSlug() {
        product.setParent(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductSlugGetVm result = productService.getProductSlug(1L);

        assertThat(result.slug()).isEqualTo("product-1");
        assertThat(result.productVariantId()).isNull();
    }

    @Test
    void getProductEsDetailById_WhenBrandNull_ShouldReturnNullBrandName() {
        product.setBrand(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductEsDetailVm result = productService.getProductEsDetailById(1L);

        assertThat(result.brand()).isNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getProductVariationsByParentId_WhenHasOptions_ShouldReturnVariations() {
        product.setId(1L);
        product.setHasOptions(true);

        Product variation = Product.builder()
            .id(2L)
            .name("Var")
            .slug("var")
            .sku("sku")
            .gtin("gtin")
            .price(10.0)
            .thumbnailMediaId(3L)
            .isPublished(true)
            .productImages(List.of(ProductImage.builder().imageId(5L).product(product).build()))
            .build();
        product.setProducts(List.of(variation));

        ProductOption option = new ProductOption();
        option.setId(11L);
        ProductOptionCombination combination = ProductOptionCombination.builder()
            .product(variation)
            .productOption(option)
            .value("Red")
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combination));
        when(mediaService.getMedia(3L)).thenReturn(new NoFileMediaVm(3L, "cap", "file", "type", "url"));
        when(mediaService.getMedia(5L)).thenReturn(new NoFileMediaVm(5L, "cap", "file", "type", "img-url"));

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).options()).containsEntry(11L, "Red");
        assertThat(result.get(0).thumbnail()).isNotNull();
        assertThat(result.get(0).productImages()).hasSize(1);
    }

    @Test
    void getProductVariationsByParentId_WhenNoOptions_ShouldReturnEmptyList() {
        product.setId(1L);
        product.setHasOptions(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void updateProductQuantity_ShouldUpdateStockQuantities() {
        Product product1 = Product.builder().id(1L).stockQuantity(5L).build();
        Product product2 = Product.builder().id(2L).stockQuantity(10L).build();
        when(productRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(product1, product2));

        productService.updateProductQuantity(List.of(
            new ProductQuantityPostVm(1L, 7L),
            new ProductQuantityPostVm(2L, 3L)
        ));

        assertThat(product1.getStockQuantity()).isEqualTo(7L);
        assertThat(product2.getStockQuantity()).isEqualTo(3L);
        verify(productRepository).saveAll(anyList());
    }

    @Test
    void subtractStockQuantity_ShouldNotGoBelowZero() {
        Product product1 = Product.builder().id(1L).stockQuantity(3L).stockTrackingEnabled(true).build();
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product1));

        productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1L, 5L)));

        assertThat(product1.getStockQuantity()).isEqualTo(0L);
        verify(productRepository).saveAll(anyList());
    }

    @Test
    void subtractStockQuantity_WhenTrackingDisabled_ShouldNotUpdateQuantity() {
        Product product1 = Product.builder().id(1L).stockQuantity(3L).stockTrackingEnabled(false).build();
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product1));

        productService.subtractStockQuantity(List.of(new ProductQuantityPutVm(1L, 2L)));

        assertThat(product1.getStockQuantity()).isEqualTo(3L);
        verify(productRepository).saveAll(anyList());
    }

    @Test
    void restoreStockQuantity_ShouldAddAmount() {
        Product product1 = Product.builder().id(1L).stockQuantity(3L).stockTrackingEnabled(true).build();
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product1));

        productService.restoreStockQuantity(List.of(new ProductQuantityPutVm(1L, 5L)));

        assertThat(product1.getStockQuantity()).isEqualTo(8L);
        verify(productRepository).saveAll(anyList());
    }

    @Test
    void getRelatedProductsStorefront_ShouldFilterPublished() {
        product.setId(1L);
        Product relatedPublished = Product.builder().id(2L).isPublished(true).thumbnailMediaId(10L).price(10.0)
            .name("Rel1").slug("rel1").build();
        Product relatedHidden = Product.builder().id(3L).isPublished(false).thumbnailMediaId(11L).price(20.0)
            .name("Rel2").slug("rel2").build();
        ProductRelated rel1 = ProductRelated.builder().product(product).relatedProduct(relatedPublished).build();
        ProductRelated rel2 = ProductRelated.builder().product(product).relatedProduct(relatedHidden).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRelatedRepository.findAllByProduct(eq(product), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(rel1, rel2)));
        when(mediaService.getMedia(10L)).thenReturn(new NoFileMediaVm(10L, "cap", "file", "type", "url"));

        ProductsGetVm result = productService.getRelatedProductsStorefront(1L, 0, 10);

        assertThat(result.productContent()).hasSize(1);
        assertThat(result.productContent().get(0).id()).isEqualTo(2L);
    }

    @Test
    void getRelatedProductsBackoffice_ShouldReturnAllRelated() {
        Product related = Product.builder().id(2L).name("Rel").slug("rel").price(10.0)
            .isAllowedToOrder(true).isPublished(true).isFeatured(false).isVisibleIndividually(true).taxClassId(1L)
            .build();
        ProductRelated relation = ProductRelated.builder().product(product).relatedProduct(related).build();
        product.setRelatedProducts(List.of(relation));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<ProductListVm> result = productService.getRelatedProductsBackoffice(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(2L);
    }

    @Test
    void getProductCheckoutList_ShouldSetThumbnailUrlWhenPresent() {
        Brand productBrand = new Brand();
        productBrand.setId(9L);
        Product checkoutProduct = Product.builder().id(1L).name("P1").brand(productBrand).price(10.0)
            .thumbnailMediaId(5L).taxClassId(1L).build();
        Page<Product> page = new PageImpl<>(List.of(checkoutProduct));
        when(productRepository.findAllPublishedProductsByIds(anyList(), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(5L)).thenReturn(new NoFileMediaVm(5L, "cap", "file", "type", "thumb"));

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

        assertThat(result.productCheckoutListVms()).hasSize(1);
        ProductCheckoutListVm item = result.productCheckoutListVms().get(0);
        assertThat(item.thumbnailUrl()).isEqualTo("thumb");
    }

    @Test
    void getProductCheckoutList_WhenThumbnailMissing_ShouldKeepEmptyUrl() {
        Brand productBrand = new Brand();
        productBrand.setId(9L);
        Product checkoutProduct = Product.builder().id(1L).name("P1").brand(productBrand).price(10.0)
            .thumbnailMediaId(5L).taxClassId(1L).build();
        Page<Product> page = new PageImpl<>(List.of(checkoutProduct));
        when(productRepository.findAllPublishedProductsByIds(anyList(), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(5L)).thenReturn(new NoFileMediaVm(5L, "cap", "file", "type", ""));

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

        assertThat(result.productCheckoutListVms()).hasSize(1);
        ProductCheckoutListVm item = result.productCheckoutListVms().get(0);
        assertThat(item.thumbnailUrl()).isEqualTo("");
    }
}
