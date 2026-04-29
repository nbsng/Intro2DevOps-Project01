package com.yas.order.viewmodel.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OrderGetVmTest {

	@Test
	void testFromModel_whenItemsPresent_mapsFieldsAndItems() {
		Order order = Order.builder()
			.id(101L)
			.orderStatus(OrderStatus.COMPLETED)
			.totalPrice(new BigDecimal("120.00"))
			.deliveryStatus(DeliveryStatus.DELIVERED)
			.deliveryMethod(DeliveryMethod.GRAB_EXPRESS)
			.build();
		ZonedDateTime createdOn = ZonedDateTime.parse("2024-02-01T10:00:00Z");
		order.setCreatedOn(createdOn);

		OrderItem orderItem = OrderItem.builder()
			.id(1L)
			.productId(10L)
			.productName("Product A")
			.quantity(2)
			.productPrice(new BigDecimal("9.99"))
			.discountAmount(new BigDecimal("1.00"))
			.taxAmount(new BigDecimal("0.50"))
			.build();

		Set<OrderItem> items = new LinkedHashSet<>();
		items.add(orderItem);

		OrderGetVm vm = OrderGetVm.fromModel(order, items);

		assertThat(vm.id()).isEqualTo(101L);
		assertThat(vm.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
		assertThat(vm.totalPrice()).isEqualTo(new BigDecimal("120.00"));
		assertThat(vm.deliveryStatus()).isEqualTo(DeliveryStatus.DELIVERED);
		assertThat(vm.deliveryMethod()).isEqualTo(DeliveryMethod.GRAB_EXPRESS);
		assertThat(vm.createdOn()).isEqualTo(createdOn);
		assertThat(vm.orderItems()).hasSize(1);
		OrderItemGetVm itemVm = vm.orderItems().getFirst();
		assertThat(itemVm.productId()).isEqualTo(10L);
		assertThat(itemVm.productName()).isEqualTo("Product A");
		assertThat(itemVm.quantity()).isEqualTo(2);
		assertThat(itemVm.productPrice()).isEqualTo(new BigDecimal("9.99"));
		assertThat(itemVm.discountAmount()).isEqualTo(new BigDecimal("1.00"));
		assertThat(itemVm.taxAmount()).isEqualTo(new BigDecimal("0.50"));
	}

	@Test
	void testFromModel_whenItemsNull_returnsEmptyOrderItemsList() {
		Order order = Order.builder()
			.id(102L)
			.orderStatus(OrderStatus.PENDING)
			.totalPrice(new BigDecimal("0.00"))
			.deliveryStatus(DeliveryStatus.PREPARING)
			.deliveryMethod(DeliveryMethod.GRAB_EXPRESS)
			.build();
		order.setCreatedOn(ZonedDateTime.parse("2024-03-01T00:00:00Z"));

		OrderGetVm vm = OrderGetVm.fromModel(order, null);

		assertThat(vm.id()).isEqualTo(102L);
		assertThat(vm.orderItems()).isEmpty();
	}

	@Test
	void testFromModel_whenItemsEmpty_returnsEmptyOrderItemsList() {
		Order order = Order.builder()
			.id(103L)
			.orderStatus(OrderStatus.PENDING)
			.totalPrice(new BigDecimal("10.00"))
			.deliveryStatus(DeliveryStatus.PREPARING)
			.deliveryMethod(DeliveryMethod.GRAB_EXPRESS)
			.build();
		order.setCreatedOn(ZonedDateTime.parse("2024-03-02T00:00:00Z"));

		OrderGetVm vm = OrderGetVm.fromModel(order, Set.of());

		assertThat(vm.id()).isEqualTo(103L);
		assertThat(vm.orderItems()).isEmpty();
	}
}
