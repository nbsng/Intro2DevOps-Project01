package com.yas.product.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRelatedTest {

    // =========================================================================
    // equals()
    // =========================================================================

    @Nested
    class Equals {

        @Test
        void equals_SameInstance_ReturnsTrue() {
            ProductRelated pr = ProductRelated.builder().id(1L).build();

            assertThat(pr.equals(pr)).isTrue();
        }

        @Test
        void equals_SameId_ReturnsTrue() {
            ProductRelated pr1 = ProductRelated.builder().id(1L).build();
            ProductRelated pr2 = ProductRelated.builder().id(1L).build();

            assertThat(pr1.equals(pr2)).isTrue();
        }

        @Test
        void equals_DifferentId_ReturnsFalse() {
            ProductRelated pr1 = ProductRelated.builder().id(1L).build();
            ProductRelated pr2 = ProductRelated.builder().id(2L).build();

            assertThat(pr1.equals(pr2)).isFalse();
        }

        @Test
        void equals_NullId_ReturnsFalse() {
            ProductRelated pr1 = ProductRelated.builder().id(null).build();
            ProductRelated pr2 = ProductRelated.builder().id(1L).build();

            assertThat(pr1.equals(pr2)).isFalse();
        }

        @Test
        void equals_BothNullId_ReturnsFalse() {
            ProductRelated pr1 = ProductRelated.builder().id(null).build();
            ProductRelated pr2 = ProductRelated.builder().id(null).build();

            // id == null → equals returns false per JPA convention
            assertThat(pr1.equals(pr2)).isFalse();
        }

        @Test
        void equals_NullObject_ReturnsFalse() {
            ProductRelated pr = ProductRelated.builder().id(1L).build();

            assertThat(pr.equals(null)).isFalse();
        }

        @Test
        void equals_DifferentType_ReturnsFalse() {
            ProductRelated pr = ProductRelated.builder().id(1L).build();

            assertThat(pr.equals("not-a-product-related")).isFalse();
            assertThat(pr.equals(1L)).isFalse();
        }

        @Test
        void equals_SameIdDifferentProductFields_ReturnsTrue() {
            // equals only compares id — other fields should not affect the result
            Product productA = new Product();
            Product productB = new Product();

            ProductRelated pr1 = ProductRelated.builder().id(5L).product(productA).relatedProduct(productA).build();
            ProductRelated pr2 = ProductRelated.builder().id(5L).product(productB).relatedProduct(productB).build();

            assertThat(pr1.equals(pr2)).isTrue();
        }
    }

    // =========================================================================
    // hashCode()
    // =========================================================================

    @Nested
    class HashCode {

        @Test
        void hashCode_IsConsistentAcrossMultipleCalls() {
            ProductRelated pr = ProductRelated.builder().id(1L).build();

            assertThat(pr.hashCode()).isEqualTo(pr.hashCode());
        }

        @Test
        void hashCode_TwoInstancesSameClass_ReturnSameValue() {
            // hashCode returns getClass().hashCode() — always equal for same class
            ProductRelated pr1 = ProductRelated.builder().id(1L).build();
            ProductRelated pr2 = ProductRelated.builder().id(2L).build();

            assertThat(pr1.hashCode()).isEqualTo(pr2.hashCode());
        }
    }

    // =========================================================================
    // Builder / Getter / Setter
    // =========================================================================

    @Nested
    class BuilderAndAccessors {

        @Test
        void builder_SetsAllFields() {
            Product product = new Product();
            Product related = new Product();

            ProductRelated pr = ProductRelated.builder()
                    .id(10L)
                    .product(product)
                    .relatedProduct(related)
                    .build();

            assertThat(pr.getId()).isEqualTo(10L);
            assertThat(pr.getProduct()).isSameAs(product);
            assertThat(pr.getRelatedProduct()).isSameAs(related);
        }

        @Test
        void noArgsConstructor_CreatesInstanceWithNullFields() {
            ProductRelated pr = new ProductRelated();

            assertThat(pr.getId()).isNull();
            assertThat(pr.getProduct()).isNull();
            assertThat(pr.getRelatedProduct()).isNull();
        }

        @Test
        void allArgsConstructor_SetsAllFields() {
            Product product = new Product();
            Product related = new Product();

            ProductRelated pr = new ProductRelated(3L, product, related);

            assertThat(pr.getId()).isEqualTo(3L);
            assertThat(pr.getProduct()).isSameAs(product);
            assertThat(pr.getRelatedProduct()).isSameAs(related);
        }

        @Test
        void setters_UpdateFieldsCorrectly() {
            ProductRelated pr = new ProductRelated();
            Product product = new Product();
            Product related = new Product();

            pr.setId(99L);
            pr.setProduct(product);
            pr.setRelatedProduct(related);

            assertThat(pr.getId()).isEqualTo(99L);
            assertThat(pr.getProduct()).isSameAs(product);
            assertThat(pr.getRelatedProduct()).isSameAs(related);
        }
    }
}