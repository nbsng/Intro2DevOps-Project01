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
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = WebhookController.class, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebhookService webhookService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        when(webhookService.findById(anyLong())).thenReturn(new WebhookDetailVm());
        mockMvc.perform(get("/backoffice/webhooks/1"))
            .andExpect(status().isOk());
    }

    @Test
    void test_createWebhook() throws Exception {
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setPayloadUrl("http://test.com");
        WebhookDetailVm detailVm = new WebhookDetailVm();
        detailVm.setId(1L);
        when(webhookService.create(any(WebhookPostVm.class))).thenReturn(detailVm);
        
        mockMvc.perform(post("/backoffice/webhooks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postVm)))
            .andExpect(status().isCreated());
    }

    @Test
    void test_updateWebhook() throws Exception {
        WebhookPostVm postVm = new WebhookPostVm();
        postVm.setPayloadUrl("http://test.com");
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
