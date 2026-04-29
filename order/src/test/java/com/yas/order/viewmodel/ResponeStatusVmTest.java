package com.yas.order.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResponeStatusVmTest {

	@Test
	void testRecordAccessors_shouldReturnConstructorValues() {
		ResponeStatusVm vm = new ResponeStatusVm("Title", "Message", "200");

		assertThat(vm.title()).isEqualTo("Title");
		assertThat(vm.message()).isEqualTo("Message");
		assertThat(vm.statusCode()).isEqualTo("200");
	}
}
