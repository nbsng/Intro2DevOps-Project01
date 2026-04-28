package com.yas.recommendation.vector.common.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import tools.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultDocumentFormatterTest {

    private DefaultDocumentFormatter formatter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        formatter = new DefaultDocumentFormatter();
        objectMapper = mock(ObjectMapper.class);
    }

    @Test
    void format_shouldSubstituteAndRemoveHtmlTags() {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("title", "<b>Product Title</b>");
        entityMap.put("description", "<p>This is a <i>great</i> product.</p>");

        String template = "Title: {title}, Description: {description}";
        
        String result = formatter.format(entityMap, template, objectMapper);
        
        assertThat(result).isEqualTo("Title: Product Title, Description: This is a great product.");
    }
}
