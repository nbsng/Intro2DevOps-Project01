package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class DateTimeUtilsTest {

    @Test
    void format_withDefaultPattern_shouldReturnFormattedString() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 27, 10, 30, 0);
        String formatted = DateTimeUtils.format(dateTime);
        assertEquals("27-10-2023_10-30-00", formatted);
    }

    @Test
    void format_withCustomPattern_shouldReturnFormattedString() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 27, 10, 30, 0);
        String formatted = DateTimeUtils.format(dateTime, "yyyy-MM-dd HH:mm:ss");
        assertEquals("2023-10-27 10:30:00", formatted);
    }
}
