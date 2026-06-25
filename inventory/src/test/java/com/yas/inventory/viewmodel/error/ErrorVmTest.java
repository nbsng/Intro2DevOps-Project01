package com.yas.inventory.viewmodel.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
@DisplayName("ErrorVm Tests")
class ErrorVmTest {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String STATUS_CODE = "404";
    private static final String TITLE = "Not Found";
    private static final String DETAIL = "Resource not found";
    private static final List<String> FIELD_ERRORS = List.of("field1", "field2");

    @Test
    @DisplayName("Should create ErrorVm with all fields")
    void constructor_WithAllFields_ShouldCreateInstance() {
        // Act
        ErrorVm errorVm = new ErrorVm(STATUS_CODE, TITLE, DETAIL, FIELD_ERRORS);

        // Assert
        assertThat(errorVm.statusCode()).isEqualTo(STATUS_CODE);
        assertThat(errorVm.title()).isEqualTo(TITLE);
        assertThat(errorVm.detail()).isEqualTo(DETAIL);
        assertThat(errorVm.fieldErrors()).isEqualTo(FIELD_ERRORS);
    }

    @Test
    @DisplayName("Should create ErrorVm with three parameters and initialize fieldErrors to empty list")
    void constructor_WithThreeParams_ShouldCreateInstanceWithEmptyFieldErrors() {
        // Act
        ErrorVm errorVm = new ErrorVm(STATUS_CODE, TITLE, DETAIL);

        // Assert
        assertThat(errorVm.statusCode()).isEqualTo(STATUS_CODE);
        assertThat(errorVm.title()).isEqualTo(TITLE);
        assertThat(errorVm.detail()).isEqualTo(DETAIL);
        assertThat(errorVm.fieldErrors()).isInstanceOf(ArrayList.class);
        assertThat(errorVm.fieldErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should allow null fieldErrors")
    void constructor_WithNullFieldErrors_ShouldAllowNull() {
        // Act
        ErrorVm errorVm = new ErrorVm(STATUS_CODE, TITLE, DETAIL, null);

        // Assert
        assertThat(errorVm.fieldErrors()).isNull();
    }
}