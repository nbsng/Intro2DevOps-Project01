package com.yas.product.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ModelEqualsHashCodeTest {

    @Test
    void brandEquals_ShouldHandleBranches() {
        Brand brand = new Brand();
        brand.setId(1L);

        Brand sameId = new Brand();
        sameId.setId(1L);

        Brand differentId = new Brand();
        differentId.setId(2L);

        Brand nullId = new Brand();

        assertThat(brand).isEqualTo(brand);
        assertThat(brand).isEqualTo(sameId);
        assertThat(brand).isNotEqualTo(differentId);
        assertThat(brand).isNotEqualTo(nullId);
        assertThat(brand).isNotEqualTo("brand");
        assertThat(brand.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void categoryEquals_ShouldHandleBranches() {
        Category category = new Category();
        category.setId(1L);

        Category sameId = new Category();
        sameId.setId(1L);

        Category differentId = new Category();
        differentId.setId(2L);

        Category nullId = new Category();

        assertThat(category).isEqualTo(category);
        assertThat(category).isEqualTo(sameId);
        assertThat(category).isNotEqualTo(differentId);
        assertThat(category).isNotEqualTo(nullId);
        assertThat(category).isNotEqualTo("category");
        assertThat(category.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void productEquals_ShouldHandleBranches() {
        Product product = new Product();
        product.setId(1L);

        Product sameId = new Product();
        sameId.setId(1L);

        Product differentId = new Product();
        differentId.setId(2L);

        Product nullId = new Product();

        assertThat(product).isEqualTo(product);
        assertThat(product).isEqualTo(sameId);
        assertThat(product).isNotEqualTo(differentId);
        assertThat(product).isNotEqualTo(nullId);
        assertThat(product).isNotEqualTo("product");
        assertThat(product.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void productOptionEquals_ShouldHandleBranches() {
        ProductOption option = new ProductOption();
        option.setId(1L);

        ProductOption sameId = new ProductOption();
        sameId.setId(1L);

        ProductOption differentId = new ProductOption();
        differentId.setId(2L);

        ProductOption nullId = new ProductOption();

        assertThat(option).isEqualTo(option);
        assertThat(option).isEqualTo(sameId);
        assertThat(option).isNotEqualTo(differentId);
        assertThat(option).isNotEqualTo(nullId);
        assertThat(option).isNotEqualTo("option");
        assertThat(option.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void productOptionValueEquals_ShouldHandleBranches() {
        ProductOptionValue optionValue = new ProductOptionValue();
        optionValue.setId(1L);

        ProductOptionValue sameId = new ProductOptionValue();
        sameId.setId(1L);

        ProductOptionValue differentId = new ProductOptionValue();
        differentId.setId(2L);

        ProductOptionValue nullId = new ProductOptionValue();

        assertThat(optionValue).isEqualTo(optionValue);
        assertThat(optionValue).isEqualTo(sameId);
        assertThat(optionValue).isNotEqualTo(differentId);
        assertThat(optionValue).isNotEqualTo(nullId);
        assertThat(optionValue).isNotEqualTo("option-value");
        assertThat(optionValue.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void productOptionCombinationEquals_ShouldHandleBranches() {
        ProductOptionCombination combination = new ProductOptionCombination();
        combination.setId(1L);

        ProductOptionCombination sameId = new ProductOptionCombination();
        sameId.setId(1L);

        ProductOptionCombination differentId = new ProductOptionCombination();
        differentId.setId(2L);

        ProductOptionCombination nullId = new ProductOptionCombination();

        assertThat(combination).isEqualTo(combination);
        assertThat(combination).isEqualTo(sameId);
        assertThat(combination).isNotEqualTo(differentId);
        assertThat(combination).isNotEqualTo(nullId);
        assertThat(combination).isNotEqualTo("combination");
        assertThat(combination.hashCode()).isEqualTo(sameId.hashCode());
    }

    @Test
    void productRelatedEquals_ShouldHandleBranches() {
        ProductRelated related = new ProductRelated();
        related.setId(1L);

        ProductRelated sameId = new ProductRelated();
        sameId.setId(1L);

        ProductRelated differentId = new ProductRelated();
        differentId.setId(2L);

        ProductRelated nullId = new ProductRelated();

        assertThat(related).isEqualTo(related);
        assertThat(related).isEqualTo(sameId);
        assertThat(related).isNotEqualTo(differentId);
        assertThat(related).isNotEqualTo(nullId);
        assertThat(related).isNotEqualTo("related");
        assertThat(related.hashCode()).isEqualTo(sameId.hashCode());
    }
}
