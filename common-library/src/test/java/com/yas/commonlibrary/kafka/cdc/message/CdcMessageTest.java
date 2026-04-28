package com.yas.commonlibrary.kafka.cdc.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CdcMessageTest {

    @Test
    void productCdcMessage_shouldWork() {
        Product before = Product.builder().id(1L).isPublished(false).build();
        Product after = Product.builder().id(1L).isPublished(true).build();
        ProductCdcMessage message = ProductCdcMessage.builder()
                .before(before)
                .after(after)
                .op(Operation.UPDATE)
                .build();

        assertEquals(before, message.getBefore());
        assertEquals(after, message.getAfter());
        assertEquals(Operation.UPDATE, message.getOp());
    }

    @Test
    void product_shouldWork() {
        Product product = new Product();
        product.setId(2L);
        product.setPublished(true);

        assertEquals(2L, product.getId());
        assertTrue(product.isPublished());
    }

    @Test
    void productMsgKey_shouldWork() {
        ProductMsgKey key = new ProductMsgKey(3L);
        assertEquals(3L, key.getId());
        
        key.setId(4L);
        assertEquals(4L, key.getId());
    }

    @Test
    void operation_shouldReturnCorrectName() {
        assertEquals("r", Operation.READ.getName());
        assertEquals("c", Operation.CREATE.getName());
        assertEquals("u", Operation.UPDATE.getName());
        assertEquals("d", Operation.DELETE.getName());
    }
}
