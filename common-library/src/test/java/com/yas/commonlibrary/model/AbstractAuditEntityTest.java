package com.yas.commonlibrary.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;

class AbstractAuditEntityTest {

    private static class TestAuditEntity extends AbstractAuditEntity {
    }

    @Test
    void gettersAndSetters_shouldWork() {
        TestAuditEntity entity = new TestAuditEntity();
        ZonedDateTime now = ZonedDateTime.now();
        
        entity.setCreatedOn(now);
        entity.setCreatedBy("admin");
        entity.setLastModifiedOn(now);
        entity.setLastModifiedBy("admin");

        assertEquals(now, entity.getCreatedOn());
        assertEquals("admin", entity.getCreatedBy());
        assertEquals(now, entity.getLastModifiedOn());
        assertEquals("admin", entity.getLastModifiedBy());
    }
}
