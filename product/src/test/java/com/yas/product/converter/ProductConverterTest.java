package com.yas.product.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProductConverterTest {

    // =========================================================================
    // toSlug
    // =========================================================================

    @Nested
    class ToSlug {

        // -----------------------------------------------------------------
        // Basic happy-path
        // -----------------------------------------------------------------

        @Test
        void simpleWord_returnsLowercase() {
            assertThat(ProductConverter.toSlug("Phone")).isEqualTo("phone");
        }

        @Test
        void alreadyValidSlug_returnsUnchanged() {
            assertThat(ProductConverter.toSlug("my-product")).isEqualTo("my-product");
        }

        @Test
        void lowercaseInput_returnsAsIs() {
            assertThat(ProductConverter.toSlug("laptop")).isEqualTo("laptop");
        }

        // -----------------------------------------------------------------
        // Uppercase / mixed case
        // -----------------------------------------------------------------

        @Test
        void uppercaseInput_convertsToLowercase() {
            assertThat(ProductConverter.toSlug("LAPTOP")).isEqualTo("laptop");
        }

        @Test
        void mixedCase_convertsToLowercase() {
            assertThat(ProductConverter.toSlug("Apple iPhone")).isEqualTo("apple-iphone");
        }

        // -----------------------------------------------------------------
        // Whitespace handling
        // -----------------------------------------------------------------

        @Test
        void leadingAndTrailingSpaces_areTrimmed() {
            assertThat(ProductConverter.toSlug("  phone  ")).isEqualTo("phone");
        }

        @Test
        void internalSpaces_convertedToHyphen() {
            assertThat(ProductConverter.toSlug("gaming laptop")).isEqualTo("gaming-laptop");
        }

        @Test
        void multipleInternalSpaces_collapsedToSingleHyphen() {
            assertThat(ProductConverter.toSlug("gaming   laptop")).isEqualTo("gaming-laptop");
        }

        // -----------------------------------------------------------------
        // Special characters
        // -----------------------------------------------------------------

        @Test
        void specialChars_replacedWithHyphen() {
            assertThat(ProductConverter.toSlug("phone@home!")).isEqualTo("phone-home-");
        }

        @Test
        void dotAndComma_replacedWithHyphen() {
            assertThat(ProductConverter.toSlug("v1.0,release")).isEqualTo("v1-0-release");
        }

        @Test
        void ampersand_replacedWithHyphen() {
            assertThat(ProductConverter.toSlug("salt & pepper")).isEqualTo("salt-pepper");
        }

        @Test
        void consecutiveSpecialChars_collapsedToSingleHyphen() {
            assertThat(ProductConverter.toSlug("hello!!!world")).isEqualTo("hello-world");
        }

        @Test
        void mixedSpecialCharsAndSpaces_collapsedToSingleHyphen() {
            assertThat(ProductConverter.toSlug("a  !!  b")).isEqualTo("a-b");
        }

        // -----------------------------------------------------------------
        // Leading hyphen removal
        // -----------------------------------------------------------------

        @Test
        void leadingSpecialChar_leadingHyphenRemoved() {
            // "!hello" → "-hello" → "hello"
            assertThat(ProductConverter.toSlug("!hello")).isEqualTo("hello");
        }

        @Test
        void leadingHyphenAfterTrim_isRemoved() {
            // " -hello" → trim → "-hello" → "hello"
            assertThat(ProductConverter.toSlug(" -hello")).isEqualTo("hello");
        }

        // -----------------------------------------------------------------
        // Numbers
        // -----------------------------------------------------------------

        @Test
        void numbersOnly_preserved() {
            assertThat(ProductConverter.toSlug("12345")).isEqualTo("12345");
        }

        @Test
        void alphanumericMixed_preserved() {
            assertThat(ProductConverter.toSlug("iphone14")).isEqualTo("iphone14");
        }

        @Test
        void numberWithSpaces_convertsCorrectly() {
            assertThat(ProductConverter.toSlug("product 2024")).isEqualTo("product-2024");
        }

        // -----------------------------------------------------------------
        // Hyphens in input
        // -----------------------------------------------------------------

        @Test
        void singleHyphen_preserved() {
            assertThat(ProductConverter.toSlug("a-b")).isEqualTo("a-b");
        }

        @Test
        void consecutiveHyphens_collapsedToOne() {
            assertThat(ProductConverter.toSlug("a--b")).isEqualTo("a-b");
        }

        @Test
        void manyConsecutiveHyphens_collapsedToOne() {
            assertThat(ProductConverter.toSlug("a----b")).isEqualTo("a-b");
        }

        // -----------------------------------------------------------------
        // Vietnamese / non-ASCII characters
        // -----------------------------------------------------------------

        @Test
        void nonAsciiChars_replacedWithHyphen() {
            // Each non-ASCII char becomes '-', consecutive ones collapse
            assertThat(ProductConverter.toSlug("điện thoại")).doesNotContain("đ", "ệ", "ọ", "ạ");
        }

        // -----------------------------------------------------------------
        // Parameterized: input → expected slug table
        // -----------------------------------------------------------------

        @ParameterizedTest(name = "[{index}] \"{0}\" → \"{1}\"")
        @CsvSource({
            "Hello World,          hello-world",
            "  Trim Me  ,          trim-me",
            "already-slug,         already-slug",
            "UPPER CASE,           upper-case",
            "multi   spaces,       multi-spaces",
            "abc123,               abc123",
            "a--b,                 a-b",
            "a----b,               a-b",
        })
        void parametrized_variousInputs(String input, String expected) {
            assertThat(ProductConverter.toSlug(input.strip())).isEqualTo(expected.strip());
        }
    }
}