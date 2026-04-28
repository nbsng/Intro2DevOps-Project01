package com.yas.webhook.model.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yas.webhook.model.Webhook;
import com.yas.webhook.model.WebhookEvent;
import com.yas.webhook.model.viewmodel.webhook.EventVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookDetailVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookListGetVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookPostVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class WebhookMapperTest {

    private WebhookMapper webhookMapper;

    @BeforeEach
    void setUp() {
        try {
            webhookMapper = Mappers.getMapper(WebhookMapper.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    void test_toWebhookVm() {
        Webhook webhook = new Webhook();
        webhook.setId(1L);
        webhook.setPayloadUrl("http://test.com");

        WebhookVm vm = webhookMapper.toWebhookVm(webhook);

        assertNotNull(vm);
        assertEquals(webhook.getId(), vm.getId());
        assertEquals(webhook.getPayloadUrl(), vm.getPayloadUrl());
    }

    @Test
    void test_toWebhookEventVms() {
        WebhookEvent event = new WebhookEvent();
        event.setEventId(10L);

        List<EventVm> vms = webhookMapper.toWebhookEventVms(List.of(event));

        assertNotNull(vms);
        assertEquals(1, vms.size());
        assertEquals(event.getEventId(), vms.get(0).getId());
    }

    @Test
    void test_toWebhookListGetVm() {
        Webhook webhook = new Webhook();
        webhook.setId(1L);
        Page<Webhook> page = new PageImpl<>(List.of(webhook), PageRequest.of(0, 10), 1);

        WebhookListGetVm vm = webhookMapper.toWebhookListGetVm(page, 0, 10);

        assertNotNull(vm);
        assertEquals(1, vm.getWebhooks().size());
        assertEquals(0, vm.getPageNo());
        assertEquals(10, vm.getPageSize());
        assertEquals(1, vm.getTotalElements());
    }

    @Test
    void test_toCreatedWebhook() {
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setPayloadUrl("http://test.com");
        postVm.setSecret("secret");
        postVm.setIsActive(true);

        Webhook webhook = webhookMapper.toCreatedWebhook(postVm);

        assertNotNull(webhook);
        assertEquals(postVm.getPayloadUrl(), webhook.getPayloadUrl());
        assertEquals(postVm.getSecret(), webhook.getSecret());
        assertTrue(webhook.getIsActive());
    }

    @Test
    void test_toUpdatedWebhook() {
        Webhook existing = new Webhook();
        existing.setId(1L);
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setPayloadUrl("http://updated.com");
        postVm.setSecret("new-secret");
        postVm.setIsActive(false);

        Webhook updated = webhookMapper.toUpdatedWebhook(existing, postVm);

        assertNotNull(updated);
        assertEquals(existing.getId(), updated.getId());
        assertEquals(postVm.getPayloadUrl(), updated.getPayloadUrl());
        assertEquals(postVm.getSecret(), updated.getSecret());
        assertEquals(postVm.getIsActive(), updated.getIsActive());
    }

    @Test
    void test_toWebhookDetailVm() {
        Webhook webhook = new Webhook();
        webhook.setId(1L);
        WebhookEvent event = new WebhookEvent();
        event.setEventId(10L);
        webhook.setWebhookEvents(List.of(event));

        WebhookDetailVm vm = webhookMapper.toWebhookDetailVm(webhook);

        assertNotNull(vm);
        assertEquals(webhook.getId(), vm.getId());
        assertEquals(1, vm.getEvents().size());
        assertEquals(event.getEventId(), vm.getEvents().get(0).getId());
    }
}
