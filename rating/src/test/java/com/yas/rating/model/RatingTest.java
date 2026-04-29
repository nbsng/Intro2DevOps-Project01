package com.yas.rating.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RatingTest {

    @Test
    void equals_sameReference_shouldBeTrue() {
        Rating rating = new Rating();

        assertTrue(rating.equals(rating));
    }

    @Test
    void equals_differentType_shouldBeFalse() {
        Rating rating = new Rating();

        assertFalse(rating.equals("not-a-rating"));
    }

    @Test
    void equals_nullId_shouldBeFalse() {
        Rating first = new Rating();
        Rating second = new Rating();

        assertFalse(first.equals(second));
    }

    @Test
    void equals_sameId_shouldBeTrue() {
        Rating first = new Rating();
        first.setId(10L);
        Rating second = new Rating();
        second.setId(10L);

        assertTrue(first.equals(second));
    }

    @Test
    void equals_differentId_shouldBeFalse() {
        Rating first = new Rating();
        first.setId(10L);
        Rating second = new Rating();
        second.setId(11L);

        assertFalse(first.equals(second));
    }

    @Test
    void hashCode_shouldMatchClassHash() {
        Rating rating = new Rating();

        assertEquals(Rating.class.hashCode(), rating.hashCode());
        assertNotEquals(0, rating.hashCode());
    }
}
