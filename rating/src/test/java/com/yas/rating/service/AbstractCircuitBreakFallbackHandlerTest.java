package com.yas.rating.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

    private final TestHandler handler = new TestHandler();

    @Test
    void handleBodilessFallback_shouldRethrowThrowable() {
        RuntimeException exception = new RuntimeException("boom");

        assertThrows(RuntimeException.class, () -> handler.callHandleBodilessFallback(exception));
    }

    @Test
    void handleFallback_shouldRethrowThrowable() {
        RuntimeException exception = new RuntimeException("boom");

        assertThrows(RuntimeException.class, () -> handler.callHandleFallback(exception));
    }

    private static class TestHandler extends AbstractCircuitBreakFallbackHandler {
        void callHandleBodilessFallback(Throwable throwable) throws Throwable {
            handleBodilessFallback(throwable);
        }

        Object callHandleFallback(Throwable throwable) throws Throwable {
            return handleFallback(throwable);
        }
    }
}
