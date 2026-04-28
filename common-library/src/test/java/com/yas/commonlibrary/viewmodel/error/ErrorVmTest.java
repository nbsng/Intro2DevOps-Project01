package com.yas.commonlibrary.viewmodel.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.util.List;

class ErrorVmTest {

    @Test
    void constructor_shouldSetAllFields() {
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Detail message", List.of("error1"));
        assertEquals("400", errorVm.statusCode());
        assertEquals("Bad Request", errorVm.title());
        assertEquals("Detail message", errorVm.detail());
        assertEquals(1, errorVm.fieldErrors().size());
        assertEquals("error1", errorVm.fieldErrors().get(0));
    }

    @Test
    void secondaryConstructor_shouldSetEmptyFieldErrors() {
        ErrorVm errorVm = new ErrorVm("404", "Not Found", "Resource not found");
        assertEquals("404", errorVm.statusCode());
        assertEquals("Not Found", errorVm.title());
        assertEquals("Resource not found", errorVm.detail());
        assertTrue(errorVm.fieldErrors().isEmpty());
    }
}
