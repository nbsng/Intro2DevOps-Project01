package com.yas.location.viewmodel.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void testErrorVmFullConstructor() {
        List<String> errors = List.of("Field 'name' is required");
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Validation failed", errors);

        assertThat(errorVm.statusCode()).isEqualTo("400");
        assertThat(errorVm.title()).isEqualTo("Bad Request");
        assertThat(errorVm.detail()).isEqualTo("Validation failed");
        assertThat(errorVm.fieldErrors()).hasSize(1).containsAll(errors);
    }

    @Test
    void testErrorVmShortConstructor() {

        ErrorVm errorVm = new ErrorVm("500", "Internal Server Error", "Something went wrong");

        assertThat(errorVm.statusCode()).isEqualTo("500");
        assertThat(errorVm.title()).isEqualTo("Internal Server Error");
        assertThat(errorVm.detail()).isEqualTo("Something went wrong");
        
        assertThat(errorVm.fieldErrors()).isNotNull();
        assertThat(errorVm.fieldErrors()).isEmpty();
    }
}