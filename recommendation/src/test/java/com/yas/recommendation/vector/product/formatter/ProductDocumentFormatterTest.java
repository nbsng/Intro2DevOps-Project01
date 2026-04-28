package com.yas.recommendation.vector.product.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;
import com.yas.recommendation.viewmodel.CategoryVm;
import com.yas.recommendation.viewmodel.ProductAttributeValueVm;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductDocumentFormatterTest {

    private ProductDocumentFormatter formatter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        formatter = new ProductDocumentFormatter();
        objectMapper = mock(ObjectMapper.class);
    }

    @Test
    void format_withValidAttributesAndCategories_shouldFormatCorrectly() {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "Test Product");
        
        // Mock Attributes
        Object attr1 = new Object();
        ProductAttributeValueVm attrVm = mock(ProductAttributeValueVm.class);
        when(attrVm.nameProductAttribute()).thenReturn("Color");
        when(attrVm.value()).thenReturn("Red");
        entityMap.put("attributeValues", List.of(attr1));
        when(objectMapper.convertValue(eq(attr1), eq(ProductAttributeValueVm.class))).thenReturn(attrVm);

        // Mock Categories
        Object cat1 = new Object();
        CategoryVm catVm = mock(CategoryVm.class);
        when(catVm.name()).thenReturn("Electronics");
        entityMap.put("categories", List.of(cat1));
        when(objectMapper.convertValue(eq(cat1), eq(CategoryVm.class))).thenReturn(catVm);

        String template = "Name: {name}, Attributes: {attributeValues}, Categories: {categories}";
        
        String result = formatter.format(entityMap, template, objectMapper);
        
        assertThat(result).isEqualTo("Name: Test Product, Attributes: [Color: Red], Categories: [Electronics]");
    }

    @Test
    void format_withNullAttributesAndCategories_shouldReturnEmptyBrackets() {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "Test Product");
        entityMap.put("attributeValues", null);
        entityMap.put("categories", null);

        String template = "Name: {name}, Attributes: {attributeValues}, Categories: {categories}";
        
        String result = formatter.format(entityMap, template, objectMapper);
        
        assertThat(result).isEqualTo("Name: Test Product, Attributes: [], Categories: []");
    }

    @Test
    void format_shouldRemoveHtmlTags() {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "<b>Test Product</b>");
        entityMap.put("attributeValues", null);
        entityMap.put("categories", null);

        String template = "Name: {name}";
        
        String result = formatter.format(entityMap, template, objectMapper);
        
        assertThat(result).isEqualTo("Name: Test Product");
    }
}
