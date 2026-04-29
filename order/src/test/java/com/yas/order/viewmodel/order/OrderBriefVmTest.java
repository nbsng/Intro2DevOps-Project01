package com.yas.order.viewmodel.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.order.model.Order;
import com.yas.order.model.OrderAddress;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class OrderBriefVmTest {

	@Test
	void testFromModel_shouldMapAllFields() {
		OrderAddress billingAddress = OrderAddress.builder()
			.id(2L)
			.contactName("Jane Smith")
			.phone("0987654321")
			.addressLine1("456 Side St")
			.addressLine2("Suite 2")
			.city("Town")
			.zipCode("54321")
			.districtId(11L)
			.districtName("District 2")
			.stateOrProvinceId(21L)
			.stateOrProvinceName("State 2")
			.countryId(31L)
			.countryName("Country 2")
			.build();

		ZonedDateTime createdOn = ZonedDateTime.parse("2024-01-15T10:00:00Z");

		Order order = Order.builder()
			.id(100L)
			.email("user@test.com")
			.billingAddressId(billingAddress)
			.totalPrice(new BigDecimal("100.00"))
			.orderStatus(OrderStatus.COMPLETED)
			.deliveryMethod(DeliveryMethod.GRAB_EXPRESS)
			.deliveryStatus(DeliveryStatus.DELIVERED)
			.paymentStatus(PaymentStatus.COMPLETED)
			.build();
		order.setCreatedOn(createdOn);

		OrderBriefVm vm = OrderBriefVm.fromModel(order);

		assertThat(vm.id()).isEqualTo(100L);
		assertThat(vm.email()).isEqualTo("user@test.com");
		assertThat(vm.billingAddressVm().id()).isEqualTo(2L);
		assertThat(vm.totalPrice()).isEqualTo(new BigDecimal("100.00"));
		assertThat(vm.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
		assertThat(vm.deliveryMethod()).isEqualTo(DeliveryMethod.GRAB_EXPRESS);
		assertThat(vm.deliveryStatus()).isEqualTo(DeliveryStatus.DELIVERED);
		assertThat(vm.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
		assertThat(vm.createdOn()).isEqualTo(createdOn);
	}
}
