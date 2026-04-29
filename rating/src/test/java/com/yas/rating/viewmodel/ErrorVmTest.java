package com.yas.rating.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void constructor_withoutFieldErrors_shouldDefaultToEmptyList() {
        ErrorVm errorVm = new ErrorVm("400 BAD_REQUEST", "Bad Request", "Missing field");

        assertEquals(List.of(), errorVm.fieldErrors());
    }
}
