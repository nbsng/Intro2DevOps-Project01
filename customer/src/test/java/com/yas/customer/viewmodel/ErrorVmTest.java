package com.yas.customer.viewmodel;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorVmTest {

    @Test
    void testErrorVmFullConstructor() {
        List<String> errors = List.of("Field is required");
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Detail error", errors);

        assertThat(errorVm.statusCode()).isEqualTo("400");
        assertThat(errorVm.title()).isEqualTo("Bad Request");
        assertThat(errorVm.detail()).isEqualTo("Detail error");
        assertThat(errorVm.fieldErrors()).containsAll(errors);
    }

    @Test
    void testErrorVmShortConstructor() {
        ErrorVm errorVm = new ErrorVm("500", "Internal Server Error", "Something went wrong");

        assertThat(errorVm.statusCode()).isEqualTo("500");
        assertThat(errorVm.fieldErrors()).isNotNull();
        assertThat(errorVm.fieldErrors()).isEmpty(); 
    }
}