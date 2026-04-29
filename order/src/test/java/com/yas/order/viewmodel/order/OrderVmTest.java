package com.yas.order.viewmodel.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.order.model.Order;
import com.yas.order.model.OrderAddress;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OrderVmTest {

	@Test
	void testFromModel_whenItemsPresent_mapsFieldsAndItems() {
		OrderAddress shippingAddress = OrderAddress.builder()
			.id(1L)
			.contactName("John Doe")
			.phone("0123456789")
			.addressLine1("123 Main St")
			.addressLine2("Apt 1")
			.city("City")
			.zipCode("12345")
			.districtId(10L)
			.districtName("District")
			.stateOrProvinceId(20L)
			.stateOrProvinceName("State")
			.countryId(30L)
			.countryName("Country")
			.build();

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

		Order order = Order.builder()
			.id(100L)
			.email("user@test.com")
			.shippingAddressId(shippingAddress)
			.billingAddressId(billingAddress)
			.note("note")
			.tax(1.5f)
			.discount(2.5f)
			.numberItem(2)
			.totalPrice(new BigDecimal("100.00"))
			.deliveryFee(new BigDecimal("5.00"))
			.couponCode("COUPON")
			.orderStatus(OrderStatus.COMPLETED)
			.deliveryMethod(DeliveryMethod.GRAB_EXPRESS)
			.deliveryStatus(DeliveryStatus.DELIVERED)
			.paymentStatus(PaymentStatus.COMPLETED)
			.checkoutId("checkout-1")
			.build();

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

		OrderVm vm = OrderVm.fromModel(order, items);

		assertThat(vm.id()).isEqualTo(100L);
		assertThat(vm.email()).isEqualTo("user@test.com");
		assertThat(vm.shippingAddressVm().id()).isEqualTo(1L);
		assertThat(vm.billingAddressVm().id()).isEqualTo(2L);
		assertThat(vm.note()).isEqualTo("note");
		assertThat(vm.tax()).isEqualTo(1.5f);
		assertThat(vm.discount()).isEqualTo(2.5f);
		assertThat(vm.numberItem()).isEqualTo(2);
		assertThat(vm.totalPrice()).isEqualTo(new BigDecimal("100.00"));
		assertThat(vm.deliveryFee()).isEqualTo(new BigDecimal("5.00"));
		assertThat(vm.couponCode()).isEqualTo("COUPON");
		assertThat(vm.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
		assertThat(vm.deliveryMethod()).isEqualTo(DeliveryMethod.GRAB_EXPRESS);
		assertThat(vm.deliveryStatus()).isEqualTo(DeliveryStatus.DELIVERED);
		assertThat(vm.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
		assertThat(vm.checkoutId()).isEqualTo("checkout-1");
		assertThat(vm.orderItemVms()).hasSize(1);
		OrderItemVm itemVm = vm.orderItemVms().iterator().next();
		assertThat(itemVm.productId()).isEqualTo(10L);
		assertThat(itemVm.productName()).isEqualTo("Product A");
		assertThat(itemVm.quantity()).isEqualTo(2);
		assertThat(itemVm.productPrice()).isEqualTo(new BigDecimal("9.99"));
		assertThat(itemVm.discountAmount()).isEqualTo(new BigDecimal("1.00"));
		assertThat(itemVm.taxAmount()).isEqualTo(new BigDecimal("0.50"));
	}

	@Test
	void testFromModel_whenItemsNull_setsOrderItemVmsNull() {
		Order order = Order.builder()
			.id(200L)
			.email("null-items@test.com")
			.shippingAddressId(OrderAddress.builder().id(3L).build())
			.billingAddressId(OrderAddress.builder().id(4L).build())
			.orderStatus(OrderStatus.PENDING)
			.deliveryMethod(DeliveryMethod.GRAB_EXPRESS)
			.deliveryStatus(DeliveryStatus.PREPARING)
			.paymentStatus(PaymentStatus.PENDING)
			.build();

		OrderVm vm = OrderVm.fromModel(order, null);

		assertThat(vm.id()).isEqualTo(200L);
		assertThat(vm.orderItemVms()).isNull();
	}
}
