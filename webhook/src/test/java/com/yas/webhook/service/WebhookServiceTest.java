package com.yas.webhook.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.webhook.integration.api.WebhookApi;
import com.yas.webhook.model.Webhook;
import com.yas.webhook.model.WebhookEvent;
import com.yas.webhook.model.WebhookEventNotification;
import com.yas.webhook.model.dto.WebhookEventNotificationDto;
import com.yas.webhook.model.enums.NotificationStatus;
import com.yas.webhook.model.mapper.WebhookMapper;
import com.yas.webhook.model.viewmodel.webhook.EventVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookDetailVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookListGetVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookPostVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookVm;
import com.yas.webhook.repository.EventRepository;
import com.yas.webhook.repository.WebhookEventNotificationRepository;
import com.yas.webhook.repository.WebhookEventRepository;
import com.yas.webhook.repository.WebhookRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    WebhookRepository webhookRepository;
    @Mock
    EventRepository eventRepository;
    @Mock
    WebhookEventRepository webhookEventRepository;
    @Mock
    WebhookEventNotificationRepository webhookEventNotificationRepository;
    @Mock
    WebhookMapper webhookMapper;
    @Mock
    WebhookApi webHookApi;

    @InjectMocks
    WebhookService webhookService;

    @Test
    void test_getPageableWebhooks() {
        Page<Webhook> webhooks = new PageImpl<>(List.of(new Webhook()));
        when(webhookRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))))
            .thenReturn(webhooks);
        when(webhookMapper.toWebhookListGetVm(webhooks, 0, 10)).thenReturn(WebhookListGetVm.builder().build());

        WebhookListGetVm result = webhookService.getPageableWebhooks(0, 10);

        assertNotNull(result);
        verify(webhookRepository).findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));
    }

    @Test
    void test_findAllWebhooks() {
        Webhook webhook = new Webhook();
        when(webhookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))).thenReturn(List.of(webhook));
        when(webhookMapper.toWebhookVm(webhook)).thenReturn(new WebhookVm());

        List<WebhookVm> result = webhookService.findAllWebhooks();

        assertEquals(1, result.size());
        verify(webhookRepository).findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Test
    void test_findById_success() {
        Webhook webhook = new Webhook();
        when(webhookRepository.findById(1L)).thenReturn(Optional.of(webhook));
        when(webhookMapper.toWebhookDetailVm(webhook)).thenReturn(new WebhookDetailVm());

        WebhookDetailVm result = webhookService.findById(1L);

        assertNotNull(result);
    }

    @Test
    void test_findById_notFound_shouldThrowException() {
        when(webhookRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> webhookService.findById(1L));
    }

    @Test
    void test_create_withEvents() {
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setEvents(List.of(EventVm.builder().id(10L).build()));
        Webhook webhook = new Webhook();
        webhook.setId(1L);
        
        when(webhookMapper.toCreatedWebhook(postVm)).thenReturn(webhook);
        when(webhookRepository.save(webhook)).thenReturn(webhook);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(new com.yas.webhook.model.Event()));
        when(webhookMapper.toWebhookDetailVm(webhook)).thenReturn(new WebhookDetailVm());

        WebhookDetailVm result = webhookService.create(postVm);

        assertNotNull(result);
        verify(webhookEventRepository).saveAll(anyList());
    }

    @Test
    void test_create_eventNotFound_shouldThrowException() {
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setEvents(List.of(EventVm.builder().id(10L).build()));
        Webhook webhook = new Webhook();
        webhook.setId(1L);

        when(webhookMapper.toCreatedWebhook(postVm)).thenReturn(webhook);
        when(webhookRepository.save(webhook)).thenReturn(webhook);
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> webhookService.create(postVm));
    }

    @Test
    void test_update_success() {
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setEvents(List.of(EventVm.builder().id(10L).build()));
        Webhook existing = new Webhook();
        existing.setWebhookEvents(List.of(new WebhookEvent()));
        
        when(webhookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(webhookMapper.toUpdatedWebhook(existing, postVm)).thenReturn(existing);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(new com.yas.webhook.model.Event()));

        webhookService.update(postVm, 1L);

        verify(webhookRepository).save(existing);
        verify(webhookEventRepository).deleteAll(anyList());
        verify(webhookEventRepository).saveAll(anyList());
    }

    @Test
    void test_delete_success() {
        when(webhookRepository.existsById(1L)).thenReturn(true);

        webhookService.delete(1L);

        verify(webhookEventRepository).deleteByWebhookId(1L);
        verify(webhookRepository).deleteById(1L);
    }

    @Test
    void test_delete_notFound_shouldThrowException() {
        when(webhookRepository.existsById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> webhookService.delete(1L));
    }

    @Test
    void test_notifyToWebhook_ShouldNotException() {

        WebhookEventNotificationDto notificationDto = WebhookEventNotificationDto
            .builder()
            .notificationId(1L)
            .url("http://test.com")
            .secret("secret")
            .build();

        WebhookEventNotification notification = new WebhookEventNotification();
        when(webhookEventNotificationRepository.findById(notificationDto.getNotificationId()))
            .thenReturn(Optional.of(notification));

        webhookService.notifyToWebhook(notificationDto);

        verify(webhookEventNotificationRepository).save(notification);
        verify(webHookApi).notify(notificationDto.getUrl(), notificationDto.getSecret(), notificationDto.getPayload());
        assertEquals(NotificationStatus.NOTIFIED, notification.getNotificationStatus());
    }
}
