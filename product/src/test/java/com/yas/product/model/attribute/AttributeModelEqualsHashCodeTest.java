package com.yas.product.model.attribute;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AttributeModelEqualsHashCodeTest {

    @Test
    void productAttributeEquals_ShouldHandleBranches() {
        ProductAttribute attribute = new ProductAttribute();
        attribute.setId(1L);

        ProductAttribute sameId = new ProductAttribute();
        sameId.setId(1L);

        ProductAttribute differentId = new ProductAttribute();
        differentId.setId(2L);

        ProductAttribute nullId = new ProductAttribute();

        assertThat(attribute).isEqualTo(attribute);
        assertThat(attribute).isEqualTo(sameId);
        assertThat(attribute).isNotEqualTo(differentId);
        assertThat(attribute).isNotEqualTo(nullId);
        assertThat(attribute).isNotEqualTo("attribute");
        assertThat(attribute.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void productAttributeGroupEquals_ShouldHandleBranches() {
        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setId(1L);

        ProductAttributeGroup sameId = new ProductAttributeGroup();
        sameId.setId(1L);

        ProductAttributeGroup differentId = new ProductAttributeGroup();
        differentId.setId(2L);

        ProductAttributeGroup nullId = new ProductAttributeGroup();

        assertThat(group).isEqualTo(group);
        assertThat(group).isEqualTo(sameId);
        assertThat(group).isNotEqualTo(differentId);
        assertThat(group).isNotEqualTo(nullId);
        assertThat(group).isNotEqualTo("group");
        assertThat(group.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void productTemplateEquals_ShouldHandleBranches() {
        ProductTemplate template = new ProductTemplate();
        template.setId(1L);

        ProductTemplate sameId = new ProductTemplate();
        sameId.setId(1L);

        ProductTemplate differentId = new ProductTemplate();
        differentId.setId(2L);

        ProductTemplate nullId = new ProductTemplate();

        assertThat(template).isEqualTo(template);
        assertThat(template).isEqualTo(sameId);
        assertThat(template).isNotEqualTo(differentId);
        assertThat(template).isNotEqualTo(nullId);
        assertThat(template).isNotEqualTo("template");
        assertThat(template.hashCode()).isEqualTo(sameId.hashCode());
    }
}
