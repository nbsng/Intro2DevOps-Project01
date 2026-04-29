package com.yas.order.viewmodel.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.order.model.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderItemGetVmTest {

	@Test
	void testFromModel_shouldMapAllFields() {
		OrderItem orderItem = OrderItem.builder()
			.id(1L)
			.productId(10L)
			.productName("Product A")
			.quantity(2)
			.productPrice(new BigDecimal("9.99"))
			.discountAmount(new BigDecimal("1.00"))
			.taxAmount(new BigDecimal("0.50"))
			.build();

		OrderItemGetVm vm = OrderItemGetVm.fromModel(orderItem);

		assertThat(vm.id()).isEqualTo(1L);
		assertThat(vm.productId()).isEqualTo(10L);
		assertThat(vm.productName()).isEqualTo("Product A");
		assertThat(vm.quantity()).isEqualTo(2);
		assertThat(vm.productPrice()).isEqualTo(new BigDecimal("9.99"));
		assertThat(vm.discountAmount()).isEqualTo(new BigDecimal("1.00"));
		assertThat(vm.taxAmount()).isEqualTo(new BigDecimal("0.50"));
	}

	@Test
	void testFromModels_whenNull_returnEmptyList() {
		List<OrderItemGetVm> result = OrderItemGetVm.fromModels(null);

		assertThat(result).isEmpty();
	}

	@Test
	void testFromModels_whenEmpty_returnEmptyList() {
		List<OrderItemGetVm> result = OrderItemGetVm.fromModels(List.of());

		assertThat(result).isEmpty();
	}

	@Test
	void testFromModels_whenHasItems_returnMappedList() {
		OrderItem first = OrderItem.builder()
			.id(1L)
			.productId(11L)
			.productName("Product 1")
			.quantity(1)
			.productPrice(new BigDecimal("10.00"))
			.discountAmount(new BigDecimal("0.00"))
			.taxAmount(new BigDecimal("0.80"))
			.build();
		OrderItem second = OrderItem.builder()
			.id(2L)
			.productId(12L)
			.productName("Product 2")
			.quantity(3)
			.productPrice(new BigDecimal("15.00"))
			.discountAmount(new BigDecimal("1.50"))
			.taxAmount(new BigDecimal("1.20"))
			.build();

		List<OrderItemGetVm> result = OrderItemGetVm.fromModels(List.of(first, second));

		assertThat(result).hasSize(2);
		assertThat(result.get(0).id()).isEqualTo(1L);
		assertThat(result.get(1).id()).isEqualTo(2L);
	}
}
