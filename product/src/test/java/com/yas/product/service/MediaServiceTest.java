package com.yas.product.service;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.ProductApplication;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeTemplate;
import com.yas.product.model.attribute.ProductTemplate;
import com.yas.product.repository.ProductAttributeRepository;
import com.yas.product.repository.ProductAttributeTemplateRepository;
import com.yas.product.repository.ProductTemplateRepository;
import com.yas.product.utils.Constants;
import com.yas.product.viewmodel.producttemplate.ProductAttributeTemplatePostVm;
import com.yas.product.viewmodel.producttemplate.ProductTemplateListGetVm;
import com.yas.product.viewmodel.producttemplate.ProductTemplatePostVm;
import com.yas.product.viewmodel.producttemplate.ProductTemplateVm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ProductApplication.class)
@ActiveProfiles("test") // Đảm bảo sử dụng profile test (thường là H2 database)
class ProductTemplateServiceTest {

    @Autowired
    private ProductTemplateService productTemplateService;
    @Autowired
    private ProductAttributeRepository productAttributeRepository;
    @Autowired
    private ProductAttributeTemplateRepository productAttributeTemplateRepository;
    @Autowired
    private ProductTemplateRepository productTemplateRepository;

    private ProductAttribute productAttribute1;
    private ProductAttribute productAttribute2;
    private ProductTemplate productTemplate1;
    private ProductTemplate productTemplate2;

    @BeforeEach
    void setUp() {
        // Xóa sạch dữ liệu cũ để tránh xung đột gây NPE khi save trùng unique key
        productAttributeTemplateRepository.deleteAll();
        productAttributeRepository.deleteAll();
        productTemplateRepository.deleteAll();

        // Khởi tạo và lưu Attribute
        productAttribute1 = ProductAttribute.builder().name("productAttribute1").build();
        productAttribute1 = productAttributeRepository.save(productAttribute1);

        productAttribute2 = ProductAttribute.builder().name("productAttribute2").build();
        productAttribute2 = productAttributeRepository.save(productAttribute2);

        // Khởi tạo và lưu Template
        productTemplate1 = ProductTemplate.builder().name("productTemplate1").build();
        productTemplate1 = productTemplateRepository.save(productTemplate1);

        productTemplate2 = ProductTemplate.builder().name("productTemplate2").build();
        productTemplate2 = productTemplateRepository.save(productTemplate2);

        // Khởi tạo mối quan hệ
        ProductAttributeTemplate pat1 = ProductAttributeTemplate.builder()
                .productTemplate(productTemplate1)
                .productAttribute(productAttribute1)
                .build();
        ProductAttributeTemplate pat2 = ProductAttributeTemplate.builder()
                .productTemplate(productTemplate2)
                .productAttribute(productAttribute2)
                .build();

        productAttributeTemplateRepository.saveAll(List.of(pat1, pat2));
    }

    @AfterEach
    void tearDown() {
        productAttributeTemplateRepository.deleteAll();
        productAttributeRepository.deleteAll();
        productTemplateRepository.deleteAll();
    }

    @Test
    void getPageableProductTemplate_WhenGetPageable_thenSuccess() {
        ProductTemplateListGetVm actualResponse = productTemplateService.getPageableProductTemplate(0, 10);
        assertThat(actualResponse.productTemplateVms()).hasSize(2);
        assertThat(actualResponse.pageNo()).isEqualTo(0);
    }

    @Test
    void saveProductTemplate_WhenDuplicateName_ThenThrowDuplicatedException() {
        ProductTemplatePostVm postVm = new ProductTemplatePostVm("productTemplate1", null);
        DuplicatedException exception = assertThrows(DuplicatedException.class, 
            () -> productTemplateService.saveProductTemplate(postVm));
        
        assertThat(exception.getMessage()).contains("productTemplate1");
    }

    @Test
    void getProductTemplate_WhenIdValid_thenSuccess() {
        ProductTemplateVm actualResponse = productTemplateService.getProductTemplate(productTemplate1.getId());
        assertEquals(productTemplate1.getName(), actualResponse.name());
    }
}