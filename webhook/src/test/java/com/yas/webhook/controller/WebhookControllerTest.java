package com.yas.webhook.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.webhook.model.viewmodel.webhook.WebhookDetailVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookListGetVm;
import com.yas.webhook.model.viewmodel.webhook.WebhookPostVm;
import com.yas.webhook.service.WebhookService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebhookService webhookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test_getPageableWebhooks() throws Exception {
        when(webhookService.getPageableWebhooks(anyInt(), anyInt())).thenReturn(WebhookListGetVm.builder().build());
        mockMvc.perform(get("/backoffice/webhooks/paging"))
            .andExpect(status().isOk());
    }

    @Test
    void test_listWebhooks() throws Exception {
        when(webhookService.findAllWebhooks()).thenReturn(List.of());
        mockMvc.perform(get("/backoffice/webhooks"))
            .andExpect(status().isOk());
    }

    @Test
    void test_getWebhook() throws Exception {
        when(webhookService.findById(anyLong())).thenReturn(WebhookDetailVm.builder().build());
        mockMvc.perform(get("/backoffice/webhooks/1"))
            .andExpect(status().isOk());
    }

    @Test
    void test_createWebhook() throws Exception {
        WebhookPostVm postVm = WebhookPostVm.builder()
            .payloadUrl("http://test.com")
            .build();
        when(webhookService.create(any(WebhookPostVm.class))).thenReturn(WebhookDetailVm.builder().id(1L).build());
        
        mockMvc.perform(post("/backoffice/webhooks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postVm)))
            .andExpect(status().isCreated());
    }

    @Test
    void test_updateWebhook() throws Exception {
        WebhookPostVm postVm = WebhookPostVm.builder()
            .payloadUrl("http://test.com")
            .build();
        doNothing().when(webhookService).update(any(WebhookPostVm.class), anyLong());

        mockMvc.perform(put("/backoffice/webhooks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postVm)))
            .andExpect(status().isNoContent());
    }

    @Test
    void test_deleteWebhook() throws Exception {
        doNothing().when(webhookService).delete(anyLong());
        mockMvc.perform(delete("/backoffice/webhooks/1"))
            .andExpect(status().isNoContent());
    }
}
