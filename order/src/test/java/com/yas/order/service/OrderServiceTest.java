package com.yas.order.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.csv.BaseCsv;
import com.yas.commonlibrary.csv.CsvExporter;
import com.yas.commonlibrary.utils.AuthenticationUtils;
import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.csv.OrderItemCsv;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentMethod;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.mapper.OrderMapper;
import com.yas.order.model.request.OrderRequest;
import com.yas.order.repository.OrderItemRepository;
import com.yas.order.repository.OrderRepository;
import com.yas.order.viewmodel.product.ProductVariationVm;
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
import org.springframework.data.domain.Sort;
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

        @Test
        void whenOrdersEmpty_returnsNullListAndZeroTotals() {
            Page<Order> page = new PageImpl<>(List.of());
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            OrderListVm result = orderService.getAllOrder(
                Pair.of(ZonedDateTime.now(), ZonedDateTime.now()),
                "Phone",
                List.of(OrderStatus.PENDING),
                Pair.of("VN", "0123"),
                "user@test.com",
                Pair.of(0, 10)
            );

            assertThat(result.orderList()).isNull();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
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

        @Test
        void whenPaymentNotCompleted_doesNotChangeOrderStatus() {
            Order order = mock(Order.class);
            when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);
            when(order.getId()).thenReturn(2L);
            when(order.getOrderStatus()).thenReturn(OrderStatus.ACCEPTED);

            PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder()
                .orderId(2L)
                .paymentId(101L)
                .paymentStatus(PaymentStatus.PENDING.name())
                .build();

            orderService.updateOrderPaymentStatus(vm);

            verify(order, never()).setOrderStatus(OrderStatus.PAID);
            verify(orderRepository).save(order);
        }

        @Test
        void whenOrderNotFound_throwsNotFoundException() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder()
                .orderId(99L)
                .paymentId(101L)
                .paymentStatus(PaymentStatus.PENDING.name())
                .build();

            assertThrows(NotFoundException.class, () -> orderService.updateOrderPaymentStatus(vm));
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

        @Test
        void whenOrderNotFound_throwsNotFoundException() {
            when(orderRepository.findById(404L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> orderService.rejectOrder(404L, "Out of stock"));
        }
    }

    @Nested
    class AcceptOrder {

        @Test
        void whenOrderNotFound_throwsNotFoundException() {
            when(orderRepository.findById(405L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> orderService.acceptOrder(405L));
        }
    }

    @Nested
    class GetLatestOrders {

        @Test
        void whenCountIsZero_returnsEmptyAndSkipsRepository() {
            List<OrderBriefVm> result = orderService.getLatestOrders(0);

            assertThat(result).isEmpty();
            verify(orderRepository, never()).getLatestOrders(any(Pageable.class));
        }

        @Test
        void whenRepositoryReturnsEmpty_returnsEmptyList() {
            when(orderRepository.getLatestOrders(any(Pageable.class))).thenReturn(List.of());

            List<OrderBriefVm> result = orderService.getLatestOrders(5);

            assertThat(result).isEmpty();
            verify(orderRepository).getLatestOrders(argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 5));
        }

        @Test
        void whenOrdersExist_mapsToBriefVmList() {
            Order order = mock(Order.class);
            OrderBriefVm briefVm = mock(OrderBriefVm.class);
            when(orderRepository.getLatestOrders(any(Pageable.class))).thenReturn(List.of(order));

            try (MockedStatic<OrderBriefVm> briefStatic = mockStatic(OrderBriefVm.class)) {
                briefStatic.when(() -> OrderBriefVm.fromModel(order)).thenReturn(briefVm);

                List<OrderBriefVm> result = orderService.getLatestOrders(3);

                assertThat(result).containsExactly(briefVm);
                verify(orderRepository).getLatestOrders(argThat(pageable ->
                    pageable.getPageNumber() == 0 && pageable.getPageSize() == 3));
            }
        }
    }

    @Nested
    class IsOrderCompletedWithUserIdAndProductId {

        @Test
        void whenNoVariations_andOrderExists_returnsPresentTrue() {
            when(productService.getProductVariations(1L)).thenReturn(List.of());
            when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mock(Order.class)));

            try (MockedStatic<AuthenticationUtils> authStatic = mockStatic(AuthenticationUtils.class)) {
                authStatic.when(AuthenticationUtils::extractUserId).thenReturn("user-1");

                OrderExistsByProductAndUserGetVm result = orderService.isOrderCompletedWithUserIdAndProductId(1L);

                assertThat(result.isPresent()).isTrue();
                verify(productService).getProductVariations(1L);
                verify(orderRepository).findOne(any(Specification.class));
            }
        }

        @Test
        void whenVariationsExist_andNoOrder_returnsPresentFalse() {
            when(productService.getProductVariations(2L))
                .thenReturn(List.of(new ProductVariationVm(21L, "v1", "SKU-1")));
            when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

            try (MockedStatic<AuthenticationUtils> authStatic = mockStatic(AuthenticationUtils.class)) {
                authStatic.when(AuthenticationUtils::extractUserId).thenReturn("user-2");

                OrderExistsByProductAndUserGetVm result = orderService.isOrderCompletedWithUserIdAndProductId(2L);

                assertThat(result.isPresent()).isFalse();
                verify(productService).getProductVariations(2L);
                verify(orderRepository).findOne(any(Specification.class));
            }
        }
    }

    @Nested
    class GetMyOrders {

        @Test
        void whenOrdersExist_returnsMappedOrderGetVms() {
            Order order = mock(Order.class);
            OrderGetVm orderGetVm = mock(OrderGetVm.class);
            when(orderRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(order));

            try (MockedStatic<AuthenticationUtils> authStatic = mockStatic(AuthenticationUtils.class);
                 MockedStatic<OrderGetVm> orderGetVmStatic = mockStatic(OrderGetVm.class)) {
                authStatic.when(AuthenticationUtils::extractUserId).thenReturn("user-3");
                orderGetVmStatic.when(() -> OrderGetVm.fromModel(order, null)).thenReturn(orderGetVm);

                List<OrderGetVm> result = orderService.getMyOrders("Laptop", OrderStatus.COMPLETED);

                assertThat(result).containsExactly(orderGetVm);
                verify(orderRepository).findAll(any(Specification.class), any(Sort.class));
            }
        }
    }

    @Nested
    class FindOrderVmByCheckoutId {

        @Test
        void whenCheckoutIdExists_returnsOrderGetVm() {
            Order order = mock(Order.class);
            when(order.getId()).thenReturn(10L);
            when(orderRepository.findByCheckoutId("chk-123")).thenReturn(Optional.of(order));
            when(orderItemRepository.findAllByOrderId(10L)).thenReturn(List.of(mock(OrderItem.class)));

            OrderGetVm expected = mock(OrderGetVm.class);
            try (MockedStatic<OrderGetVm> orderGetVmStatic = mockStatic(OrderGetVm.class)) {
                orderGetVmStatic.when(() -> OrderGetVm.fromModel(eq(order), any(Set.class)))
                    .thenReturn(expected);

                OrderGetVm result = orderService.findOrderVmByCheckoutId("chk-123");

                assertThat(result).isEqualTo(expected);
                verify(orderRepository).findByCheckoutId("chk-123");
                verify(orderItemRepository).findAllByOrderId(10L);
            }
        }

        @Test
        void whenCheckoutIdNotFound_throwsNotFoundException() {
            when(orderRepository.findByCheckoutId("missing")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> orderService.findOrderVmByCheckoutId("missing"));
        }
    }

    @Nested
    class ExportCsv {

        @Test
        void whenOrderListIsNull_exportsEmptyCsv() throws Exception {
            OrderRequest request = OrderRequest.builder()
                .createdFrom(ZonedDateTime.now())
                .createdTo(ZonedDateTime.now())
                .productName("Laptop")
                .orderStatus(List.of(OrderStatus.PENDING))
                .billingCountry("VN")
                .billingPhoneNumber("0123456789")
                .email("user@test.com")
                .pageNo(0)
                .pageSize(10)
                .build();

            OrderListVm orderListVm = new OrderListVm(null, 0, 0);
            byte[] expected = new byte[] {1, 2, 3};

            OrderService spyService = spy(orderService);
            doReturn(orderListVm).when(spyService).getAllOrder(any(), any(), any(), any(), any(), any());

            try (MockedStatic<CsvExporter> csvExporterStatic = mockStatic(CsvExporter.class)) {
                csvExporterStatic
                    .when(() -> CsvExporter.exportToCsv(eq(List.of()), eq(OrderItemCsv.class)))
                    .thenReturn(expected);

                byte[] result = spyService.exportCsv(request);

                assertThat(result).isEqualTo(expected);
                csvExporterStatic.verify(() -> CsvExporter.exportToCsv(eq(List.of()), eq(OrderItemCsv.class)));
                verify(orderMapper, never()).toCsv(any());
            }
        }

        @Test
        void whenOrderListExists_mapsAndExportsCsv() throws Exception {
            OrderRequest request = OrderRequest.builder()
                .createdFrom(ZonedDateTime.now())
                .createdTo(ZonedDateTime.now())
                .productName("Laptop")
                .orderStatus(List.of(OrderStatus.PENDING))
                .billingCountry("VN")
                .billingPhoneNumber("0123456789")
                .email("user@test.com")
                .pageNo(0)
                .pageSize(10)
                .build();

            OrderBriefVm briefVm = mock(OrderBriefVm.class);
            OrderItemCsv csvRow = mock(OrderItemCsv.class);
            when(orderMapper.toCsv(briefVm)).thenReturn(csvRow);

            OrderListVm orderListVm = new OrderListVm(List.of(briefVm), 1, 1);
            byte[] expected = new byte[] {9, 8, 7};

            OrderService spyService = spy(orderService);
            doReturn(orderListVm).when(spyService).getAllOrder(any(), any(), any(), any(), any(), any());

            try (MockedStatic<CsvExporter> csvExporterStatic = mockStatic(CsvExporter.class)) {
                csvExporterStatic
                    .when(() -> CsvExporter.exportToCsv(anyList(), eq(OrderItemCsv.class)))
                    .thenReturn(expected);

                byte[] result = spyService.exportCsv(request);

                assertThat(result).isEqualTo(expected);
                verify(orderMapper).toCsv(briefVm);
                csvExporterStatic.verify(() -> CsvExporter.exportToCsv(
                    argThat(list -> list.size() == 1 && list.get(0) == csvRow),
                    eq(OrderItemCsv.class)
                ));
            }
        }
    }
}