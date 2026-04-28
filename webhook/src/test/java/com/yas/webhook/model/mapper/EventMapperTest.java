package com.yas.webhook.model.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.yas.webhook.model.Event;
import com.yas.webhook.model.viewmodel.webhook.EventVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class EventMapperTest {

    private EventMapper eventMapper;

    @BeforeEach
    void setUp() {
        eventMapper = Mappers.getMapper(EventMapper.class);
    }

    @Test
    void test_toEventVm() {
        Event event = new Event();
        event.setId(1L);
        event.setName("test-event");

        EventVm vm = eventMapper.toEventVm(event);

        assertNotNull(vm);
        assertEquals(event.getId(), vm.getId());
        assertEquals(event.getName(), vm.getName());
    }
}
