package com.yas.product.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionValueTest {

    // =========================================================================
    // equals()
    // =========================================================================

    @Nested
    class Equals {

        @Test
        void sameInstance_returnsTrue() {
            ProductOptionValue pov = ProductOptionValue.builder().id(1L).build();
            assertThat(pov.equals(pov)).isTrue();
        }

        @Test
        void sameId_returnsTrue() {
            ProductOptionValue pov1 = ProductOptionValue.builder().id(1L).build();
            ProductOptionValue pov2 = ProductOptionValue.builder().id(1L).build();
            assertThat(pov1.equals(pov2)).isTrue();
        }

        @Test
        void differentId_returnsFalse() {
            ProductOptionValue pov1 = ProductOptionValue.builder().id(1L).build();
            ProductOptionValue pov2 = ProductOptionValue.builder().id(2L).build();
            assertThat(pov1.equals(pov2)).isFalse();
        }

        @Test
        void nullId_returnsFalse() {
            ProductOptionValue pov1 = ProductOptionValue.builder().id(null).build();
            ProductOptionValue pov2 = ProductOptionValue.builder().id(1L).build();
            assertThat(pov1.equals(pov2)).isFalse();
        }

        @Test
        void bothNullId_returnsFalse() {
            ProductOptionValue pov1 = ProductOptionValue.builder().id(null).build();
            ProductOptionValue pov2 = ProductOptionValue.builder().id(null).build();
            assertThat(pov1.equals(pov2)).isFalse();
        }

        @Test
        void nullObject_returnsFalse() {
            ProductOptionValue pov = ProductOptionValue.builder().id(1L).build();
            assertThat(pov.equals(null)).isFalse();
        }

        @Test
        void differentType_returnsFalse() {
            ProductOptionValue pov = ProductOptionValue.builder().id(1L).build();
            assertThat(pov.equals("string")).isFalse();
            assertThat(pov.equals(1L)).isFalse();
        }

        @Test
        void sameIdDifferentFields_returnsTrue() {
            // equals only compares id — other fields must not affect result
            ProductOptionValue pov1 = ProductOptionValue.builder()
                    .id(5L).value("Red").displayType("color").displayOrder(1).build();
            ProductOptionValue pov2 = ProductOptionValue.builder()
                    .id(5L).value("Blue").displayType("size").displayOrder(2).build();
            assertThat(pov1.equals(pov2)).isTrue();
        }
    }

    // =========================================================================
    // hashCode()
    // =========================================================================

    @Nested
    class HashCode {

        @Test
        void isConsistentAcrossMultipleCalls() {
            ProductOptionValue pov = ProductOptionValue.builder().id(1L).build();
            assertThat(pov.hashCode()).isEqualTo(pov.hashCode());
        }

        @Test
        void allInstancesOfSameClass_returnSameHashCode() {
            ProductOptionValue pov1 = ProductOptionValue.builder().id(1L).build();
            ProductOptionValue pov2 = ProductOptionValue.builder().id(99L).build();
            assertThat(pov1.hashCode()).isEqualTo(pov2.hashCode());
        }
    }

    // =========================================================================
    // Builder / Getter / Setter
    // =========================================================================

    @Nested
    class BuilderAndAccessors {

        @Test
        void builder_setsAllFields() {
            Product product = new Product();
            ProductOption productOption = new ProductOption();

            ProductOptionValue pov = ProductOptionValue.builder()
                    .id(10L)
                    .product(product)
                    .productOption(productOption)
                    .displayType("color")
                    .displayOrder(3)
                    .value("Red")
                    .build();

            assertThat(pov.getId()).isEqualTo(10L);
            assertThat(pov.getProduct()).isSameAs(product);
            assertThat(pov.getProductOption()).isSameAs(productOption);
            assertThat(pov.getDisplayType()).isEqualTo("color");
            assertThat(pov.getDisplayOrder()).isEqualTo(3);
            assertThat(pov.getValue()).isEqualTo("Red");
        }

        @Test
        void noArgsConstructor_createsInstanceWithDefaultValues() {
            ProductOptionValue pov = new ProductOptionValue();

            assertThat(pov.getId()).isNull();
            assertThat(pov.getProduct()).isNull();
            assertThat(pov.getProductOption()).isNull();
            assertThat(pov.getDisplayType()).isNull();
            assertThat(pov.getDisplayOrder()).isZero();
            assertThat(pov.getValue()).isNull();
        }

        @Test
        void allArgsConstructor_setsAllFields() {
            Product product = new Product();
            ProductOption productOption = new ProductOption();

            ProductOptionValue pov = new ProductOptionValue(
                    2L, product, productOption, "size", 1, "XL");

            assertThat(pov.getId()).isEqualTo(2L);
            assertThat(pov.getProduct()).isSameAs(product);
            assertThat(pov.getProductOption()).isSameAs(productOption);
            assertThat(pov.getDisplayType()).isEqualTo("size");
            assertThat(pov.getDisplayOrder()).isEqualTo(1);
            assertThat(pov.getValue()).isEqualTo("XL");
        }

        @Test
        void setters_updateFieldsCorrectly() {
            ProductOptionValue pov = new ProductOptionValue();
            Product product = new Product();
            ProductOption productOption = new ProductOption();

            pov.setId(7L);
            pov.setProduct(product);
            pov.setProductOption(productOption);
            pov.setDisplayType("texture");
            pov.setDisplayOrder(5);
            pov.setValue("Smooth");

            assertThat(pov.getId()).isEqualTo(7L);
            assertThat(pov.getProduct()).isSameAs(product);
            assertThat(pov.getProductOption()).isSameAs(productOption);
            assertThat(pov.getDisplayType()).isEqualTo("texture");
            assertThat(pov.getDisplayOrder()).isEqualTo(5);
            assertThat(pov.getValue()).isEqualTo("Smooth");
        }
    }
}