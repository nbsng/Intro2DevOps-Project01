package com.yas.delivery.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeliveryControllerTest {

    @Test
    void testControllerInitialization() {
        DeliveryController controller = new DeliveryController();
        assertNotNull(controller);
    }
}
