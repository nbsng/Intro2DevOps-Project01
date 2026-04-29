package com.yas.model.enumeration;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.order.model.enumeration.CheckoutState;
import org.junit.jupiter.api.Test;

class CheckoutStateTest {

	@Test
	void testGetName_shouldReturnDisplayName() {
		assertThat(CheckoutState.COMPLETED.getName()).isEqualTo("Completed");
		assertThat(CheckoutState.PENDING.getName()).isEqualTo("Pending");
		assertThat(CheckoutState.LOCK.getName()).isEqualTo("LOCK");
		assertThat(CheckoutState.CHECKED_OUT.getName()).isEqualTo("Checked Out");
		assertThat(CheckoutState.PAYMENT_PROCESSING.getName()).isEqualTo("Payment Processing");
		assertThat(CheckoutState.PAYMENT_FAILED.getName()).isEqualTo("Payment Failed");
		assertThat(CheckoutState.PAYMENT_CONFIRMED.getName()).isEqualTo("Payment Confirmed");
		assertThat(CheckoutState.FULFILLED.getName()).isEqualTo("Fulfilled");
	}
}
