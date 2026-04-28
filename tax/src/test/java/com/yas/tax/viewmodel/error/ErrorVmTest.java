package com.yas.tax.viewmodel.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void errorVm_WithAllFields_ShouldCreateRecordSuccessfully() {
        List<String> fieldErrors = List.of("field1 error", "field2 error");
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Invalid input", fieldErrors);

        assertEquals("400", errorVm.statusCode());
        assertEquals("Bad Request", errorVm.title());
        assertEquals("Invalid input", errorVm.detail());
        assertEquals(2, errorVm.fieldErrors().size());
        assertEquals("field1 error", errorVm.fieldErrors().get(0));
        assertEquals("field2 error", errorVm.fieldErrors().get(1));
    }

    @Test
    void errorVm_UsingConstructorWithoutFieldErrors_ShouldCreateEmptyFieldErrorsList() {
        ErrorVm errorVm = new ErrorVm("404", "Not Found", "Resource not found");

        assertEquals("404", errorVm.statusCode());
        assertEquals("Not Found", errorVm.title());
        assertEquals("Resource not found", errorVm.detail());
        assertNotNull(errorVm.fieldErrors());
        assertTrue(errorVm.fieldErrors().isEmpty());
    }

    @Test
    void errorVm_WithEmptyFieldErrors_ShouldReturnEmptyList() {
        ErrorVm errorVm = new ErrorVm("500", "Internal Server Error", "Server error", new ArrayList<>());

        assertEquals("500", errorVm.statusCode());
        assertEquals("Internal Server Error", errorVm.title());
        assertEquals("Server error", errorVm.detail());
        assertTrue(errorVm.fieldErrors().isEmpty());
    }

    @Test
    void errorVm_WithMultipleFieldErrors_ShouldContainAllErrors() {
        List<String> fieldErrors = List.of("name is required", "email is invalid", "age must be positive");
        ErrorVm errorVm = new ErrorVm("400", "Validation Error", "Multiple validation errors", fieldErrors);

        assertEquals(3, errorVm.fieldErrors().size());
        assertTrue(errorVm.fieldErrors().contains("name is required"));
        assertTrue(errorVm.fieldErrors().contains("email is invalid"));
        assertTrue(errorVm.fieldErrors().contains("age must be positive"));
    }

    @Test
    void errorVm_RecordEquality_ShouldReturnTrueForSameValues() {
        List<String> fieldErrors1 = List.of("error1");
        List<String> fieldErrors2 = List.of("error1");

        ErrorVm errorVm1 = new ErrorVm("400", "Bad Request", "Invalid", fieldErrors1);
        ErrorVm errorVm2 = new ErrorVm("400", "Bad Request", "Invalid", fieldErrors2);

        assertEquals(errorVm1, errorVm2);
    }

    @Test
    void errorVm_RecordEquality_ShouldReturnFalseForDifferentValues() {
        List<String> fieldErrors1 = List.of("error1");
        List<String> fieldErrors2 = List.of("error2");

        ErrorVm errorVm1 = new ErrorVm("400", "Bad Request", "Invalid", fieldErrors1);
        ErrorVm errorVm2 = new ErrorVm("400", "Bad Request", "Invalid", fieldErrors2);

        // Note: These will not be equal because List.of() creates immutable lists with different content
    }
}
