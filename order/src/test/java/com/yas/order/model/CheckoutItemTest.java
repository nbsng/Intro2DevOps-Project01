package com.yas.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.order.model.CheckoutItem;
import org.junit.jupiter.api.Test;

class CheckoutItemTest {

	@Test
	void testEquals_sameInstance_returnsTrue() {
		CheckoutItem item = CheckoutItem.builder().id(1L).build();

		assertThat(item.equals(item)).isTrue();
	}

	@Test
	void testEquals_differentType_returnsFalse() {
		CheckoutItem item = CheckoutItem.builder().id(1L).build();

		assertThat(item.equals("not-a-checkout-item")).isFalse();
	}

	@Test
	void testEquals_sameId_returnsTrue() {
		CheckoutItem first = CheckoutItem.builder().id(1L).build();
		CheckoutItem second = CheckoutItem.builder().id(1L).build();

		assertThat(first).isEqualTo(second);
	}

	@Test
	void testEquals_differentId_returnsFalse() {
		CheckoutItem first = CheckoutItem.builder().id(1L).build();
		CheckoutItem second = CheckoutItem.builder().id(2L).build();

		assertThat(first).isNotEqualTo(second);
	}

	@Test
	void testEquals_nullId_returnsFalse() {
		CheckoutItem first = CheckoutItem.builder().id(null).build();
		CheckoutItem second = CheckoutItem.builder().id(1L).build();

		assertThat(first).isNotEqualTo(second);
	}

	@Test
	void testHashCode_sameClass_returnsSameValue() {
		CheckoutItem first = CheckoutItem.builder().id(1L).build();
		CheckoutItem second = CheckoutItem.builder().id(2L).build();

		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}
}
