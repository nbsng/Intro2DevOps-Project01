package com.yas.order.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentMethod;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.mapper.OrderMapper;
import com.yas.order.repository.OrderItemRepository;
import com.yas.order.repository.OrderRepository;
import com.yas.order.viewmodel.order.*;
import com.yas.order.viewmodel.orderaddress.OrderAddressPostVm;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductService productService;
    @Mock private CartService cartService;
    @Mock private OrderMapper orderMapper;
    @Mock private PromotionService promotionService;

    @InjectMocks
    private OrderService orderService;

    @Nested
    class CreateOrder {

        @Test
        void whenValidOrderPostVm_createsAndReturnsOrderVm() {
            // 1. Setup Address Vm
            OrderAddressPostVm addressVm = OrderAddressPostVm.builder()
                    .phone("0123456789")
                    .contactName("Gemini")
                    .addressLine1("123 Street")
                    .city("Rach Gia")
                    .districtId(1L)
                    .stateOrProvinceId(1L)
                    .countryId(1L)
                    .build();

            // 2. Setup Item Vm
            OrderItemPostVm itemVm = OrderItemPostVm.builder()
                    .productId(10L)
                    .productName("Laptop")
                    .quantity(1)
                    .productPrice(new BigDecimal("1000.0"))
                    .build();

            // 3. Setup OrderPostVm
            OrderPostVm orderPostVm = OrderPostVm.builder()
                    .email("user@test.com")
                    .tax(10.0f)
                    .discount(0.0f)
                    .deliveryFee(new BigDecimal("5.0"))
                    .numberItem(1)
                    .totalPrice(new BigDecimal("1015.0"))
                    .couponCode("PROMO2026")
                    .paymentStatus(PaymentStatus.PENDING)
                    .deliveryMethod(DeliveryMethod.VIETTEL_POST)
                    .paymentMethod(PaymentMethod.COD)
                    .checkoutId("chk_001")
                    .billingAddressPostVm(addressVm)
                    .shippingAddressPostVm(addressVm)
                    .orderItemPostVms(List.of(itemVm))
                    .build();

            Order savedOrder = mock(Order.class);

            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

            OrderVm expectedVm = mock(OrderVm.class);
            when(expectedVm.id()).thenReturn(1L);

            try (MockedStatic<OrderVm> orderVmStatic = mockStatic(OrderVm.class)) {
                orderVmStatic.when(() -> OrderVm.fromModel(any(Order.class), anySet()))
                             .thenReturn(expectedVm);

                // 5. Execute
                OrderVm result = orderService.createOrder(orderPostVm);

                // 6. Verify
                assertThat(result).isEqualTo(expectedVm);
                verify(orderRepository, times(2)).save(any(Order.class)); // gọi trong createOrder + acceptOrder
                verify(orderItemRepository).saveAll(anySet());
                verify(productService).subtractProductStockQuantity(expectedVm);
                verify(cartService).deleteCartItems(expectedVm);
                verify(promotionService).updateUsagePromotion(anyList());
                verify(savedOrder).setOrderStatus(OrderStatus.ACCEPTED);
            }
        }
    }

    @Nested
    class GetOrderWithItemsById {

        @Test
        void whenOrderExists_returnsOrderVm() {
            Order order = mock(Order.class);
            when(order.getId()).thenReturn(1L);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of(mock(OrderItem.class)));

            OrderVm expectedVm = mock(OrderVm.class);
            try (MockedStatic<OrderVm> orderVmStatic = mockStatic(OrderVm.class)) {
                orderVmStatic.when(() -> OrderVm.fromModel(eq(order), any(Set.class)))
                             .thenReturn(expectedVm);
                OrderVm result = orderService.getOrderWithItemsById(1L);
                assertThat(result).isEqualTo(expectedVm);
            }
        }

        @Test
        void whenOrderNotFound_throwsNotFoundException() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> orderService.getOrderWithItemsById(99L));
        }
    }

    @Nested
    class GetAllOrder {

        @Test
        void whenOrdersExist_returnsPopulatedListVm() {
            Order order = mock(Order.class);
            Page<Order> page = new PageImpl<>(List.of(order));
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            try (MockedStatic<OrderBriefVm> briefStatic = mockStatic(OrderBriefVm.class)) {
                briefStatic.when(() -> OrderBriefVm.fromModel(order)).thenReturn(mock(OrderBriefVm.class));
                OrderListVm result = orderService.getAllOrder(
                    Pair.of(ZonedDateTime.now(), ZonedDateTime.now()),
                    null,
                    List.of(),
                    Pair.of("", ""),   // ← String, String
                    null,
                    Pair.of(0, 10)
                );
                assertThat(result.totalElements()).isEqualTo(1);
            }
        }
    }

    @Nested
    class UpdateOrderPaymentStatus {

        @Test
        void whenPaymentCompleted_setsOrderStatusPaid() {
            Order order = mock(Order.class);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);
            when(order.getId()).thenReturn(1L);
            when(order.getOrderStatus()).thenReturn(OrderStatus.ACCEPTED); // tránh NPE khi gọi getOrderStatus().getName()

            PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder()
                .orderId(1L)
                .paymentId(100L)
                .paymentStatus(PaymentStatus.COMPLETED.name())
                .build();

            orderService.updateOrderPaymentStatus(vm);

            verify(order).setOrderStatus(OrderStatus.PAID);
            verify(orderRepository).save(order);
        }
    }

    @Nested
    class RejectOrder {

        @Test
        void whenOrderExists_setsRejectedStatusAndReason() {
            Order order = mock(Order.class);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            orderService.rejectOrder(1L, "Out of stock");

            verify(order).setOrderStatus(OrderStatus.REJECT);
            verify(order).setRejectReason("Out of stock");
            verify(orderRepository).save(order);
        }
    }
}