package com.yas.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

	private static final class TestHandler extends AbstractCircuitBreakFallbackHandler {
		void callBodiless(Throwable throwable) throws Throwable {
			handleBodilessFallback(throwable);
		}

		<T> T callTyped(Throwable throwable) throws Throwable {
			return handleTypedFallback(throwable);
		}
	}

	@Test
	void handleBodilessFallback_rethrowsThrowable() {
		TestHandler handler = new TestHandler();
		RuntimeException error = new RuntimeException("boom");

		Throwable thrown = assertThrows(RuntimeException.class, () -> handler.callBodiless(error));

		assertThat(thrown).isSameAs(error);
	}

	@Test
	void handleTypedFallback_rethrowsThrowable() {
		TestHandler handler = new TestHandler();
		RuntimeException error = new RuntimeException("boom");

		Throwable thrown = assertThrows(RuntimeException.class, () -> handler.callTyped(error));

		assertThat(thrown).isSameAs(error);
	}
}
