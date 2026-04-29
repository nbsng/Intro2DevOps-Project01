package com.yas.order.specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.utils.Constants;
import jakarta.persistence.criteria.*;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class OrderSpecificationTest {

    private CriteriaBuilder criteriaBuilder;
    private Root<Order> root;
    private CriteriaQuery<?> query;
    private Path<Object> path;
    private Predicate mockPredicate;
    private Subquery<Long> longSubquery;
    private Root<OrderItem> orderItemRoot;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        criteriaBuilder = mock(CriteriaBuilder.class);
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        path = mock(Path.class);
        mockPredicate = mock(Predicate.class);
        longSubquery = mock(Subquery.class);
        orderItemRoot = mock(Root.class);
        
        when(root.get(anyString())).thenReturn(path);
        when(path.get(anyString())).thenReturn(path);
        
        // Mock Subquery cơ bản
        when(query.subquery(any(Class.class))).thenReturn(longSubquery);
        when(longSubquery.from(any(Class.class))).thenReturn(orderItemRoot);
        when(orderItemRoot.get(anyString())).thenReturn(path);
        when(longSubquery.select(any())).thenReturn(longSubquery);
        when(longSubquery.where(any(Predicate.class))).thenReturn(longSubquery);
        when(longSubquery.where(any(Predicate[].class))).thenReturn(longSubquery);

        // Luôn trả về một predicate thay vì null cho các hàm tạo predicate đơn
        when(criteriaBuilder.conjunction()).thenReturn(mockPredicate);
        when(criteriaBuilder.equal(any(), any())).thenReturn(mockPredicate);
        when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(mockPredicate);
        when(criteriaBuilder.lower(any(Expression.class))).thenReturn(mock(Expression.class));
        when(criteriaBuilder.between(any(Expression.class), any(ZonedDateTime.class), any(ZonedDateTime.class)))
            .thenReturn(mockPredicate);
        
        // Mock cho "In" clause chaining
        CriteriaBuilder.In<Object> inMock = mock(CriteriaBuilder.In.class);
        when(criteriaBuilder.in(any())).thenReturn(inMock);
        when(inMock.value(any())).thenReturn(inMock);
        
        // Mock cho exists subquery
        when(criteriaBuilder.exists(any())).thenReturn(mockPredicate);
    }

    @Test
    void testFindMyOrders_shouldCombineThreePredicates() {
        // Mấu chốt: findMyOrders gọi cb.and(p1, p2, p3) -> Java coi đây là mảng Predicate[]
        // Chúng ta mock phương thức varargs trả về mockPredicate (không được null)
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mockPredicate);

        // Thực thi
        Specification<Order> spec = OrderSpecification.findMyOrders("user-id", "laptop", OrderStatus.COMPLETED);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        
        // Kiểm tra
        assertNotNull(result, "Predicate result should not be null");
        assertEquals(mockPredicate, result);
    }

    @Test
    void testFindOrderByWithMulCriteria_whenValid_shouldReturnPredicate() {
        doReturn(Order.class).when(query).getResultType();
        
        // Tương tự, hàm này truyền 6 tham số vào cb.and(...)
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mockPredicate);

        Specification<Order> spec = OrderSpecification.findOrderByWithMulCriteria(
                List.of(OrderStatus.COMPLETED), "0123", "VN", "a@b.com", "Pro", 
                ZonedDateTime.now().minusDays(1), ZonedDateTime.now());
        
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(root, atLeastOnce()).fetch(anyString(), eq(JoinType.LEFT));
    }

    @Test
    void testFindOrderByWithMulCriteria_whenQueryNull_shouldNotFetch() {
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mockPredicate);

        Specification<Order> spec = OrderSpecification.findOrderByWithMulCriteria(
            List.of(OrderStatus.COMPLETED), "0123", "VN", "a@b.com", "Pro",
            ZonedDateTime.now().minusDays(1), ZonedDateTime.now());

        Predicate result = spec.toPredicate(root, null, criteriaBuilder);

        assertNotNull(result);
        verify(root, never()).fetch(anyString(), eq(JoinType.LEFT));
    }

    @Test
    void testFindOrderByWithMulCriteria_whenResultTypeLong_shouldNotFetch() {
        doReturn(Long.class).when(query).getResultType();
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mockPredicate);

        Specification<Order> spec = OrderSpecification.findOrderByWithMulCriteria(
            List.of(OrderStatus.COMPLETED), "0123", "VN", "a@b.com", "Pro",
            ZonedDateTime.now().minusDays(1), ZonedDateTime.now());

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(root, never()).fetch(anyString(), eq(JoinType.LEFT));
    }

    @Test
    void testHasProductInOrderItems_whenQueryNull_returnConjunction() {
        Specification<Order> spec = OrderSpecification.hasProductInOrderItems(List.of(1L));

        Predicate result = spec.toPredicate(root, null, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
        verify(query, never()).subquery(any(Class.class));
    }

    @Test
    void testHasProductInOrderItems_whenQueryPresent_returnExistsPredicate() {
        Specification<Order> spec = OrderSpecification.hasProductInOrderItems(List.of(1L));

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(query).subquery(OrderItem.class);
        verify(criteriaBuilder).exists(any(Subquery.class));
    }

    @Test
    void testWithProductName_whenEmpty_returnConjunction() {
        Specification<Order> spec = OrderSpecification.withProductName("");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithProductName_whenHasValue_returnExistsPredicate() {
        Specification<Order> spec = OrderSpecification.withProductName("Laptop");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(query).subquery(Long.class);
        verify(criteriaBuilder).exists(any(Subquery.class));
    }

    @Test
    void testHasProductNameInOrderItems_whenQueryNull_returnConjunction() {
        Specification<Order> spec = OrderSpecification.hasProductNameInOrderItems("Phone");

        Predicate result = spec.toPredicate(root, null, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
        verify(query, never()).subquery(any(Class.class));
    }

    @Test
    void testHasProductNameInOrderItems_whenHasValue_returnInPredicate() {
        Specification<Order> spec = OrderSpecification.hasProductNameInOrderItems("Phone");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(query).subquery(Long.class);
        verify(criteriaBuilder).in(any());
    }

    @Test
    void testHasProductNameInOrderItems_whenEmpty_returnInPredicate() {
        Specification<Order> spec = OrderSpecification.hasProductNameInOrderItems("");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(query).subquery(Long.class);
        verify(criteriaBuilder).in(any());
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithCountryName_whenHasValue_returnLikePredicate() {
        Specification<Order> spec = OrderSpecification.withCountryName("VN");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).like(any(Expression.class), eq("%vn%"));
    }

    @Test
    void testWithCountryName_whenEmpty_returnConjunction() {
        Specification<Order> spec = OrderSpecification.withCountryName("");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithCountryName_whenNull_returnConjunction() {
        Specification<Order> spec = OrderSpecification.withCountryName(null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithBillingPhoneNumber_whenHasValue_returnLikePredicate() {
        Specification<Order> spec = OrderSpecification.withBillingPhoneNumber("0123");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).like(any(Expression.class), eq("%0123%"));
    }

    @Test
    void testWithBillingPhoneNumber_whenEmpty_returnConjunction() {
        Specification<Order> spec = OrderSpecification.withBillingPhoneNumber("");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithBillingPhoneNumber_whenNull_returnConjunction() {
        Specification<Order> spec = OrderSpecification.withBillingPhoneNumber(null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithEmail_whenHasValue_returnLikePredicate() {
        Specification<Order> spec = OrderSpecification.withEmail("user@test.com");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).like(any(Expression.class), eq("%user@test.com%"));
    }

    @Test
    void testHasOrderStatus_whenNull_returnConjunction() {
        Specification<Order> spec = OrderSpecification.hasOrderStatus(null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithOrderStatus_whenEmpty_returnConjunction() {
        Specification<Order> spec = OrderSpecification.withOrderStatus(Collections.emptyList());

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithEmail_whenEmpty_returnConjunction() {
        Specification<Order> spec = OrderSpecification.withEmail("");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithDateRange_whenBothPresent_returnBetweenPredicate() {
        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now();
        Specification<Order> spec = OrderSpecification.withDateRange(from, to);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).between(any(Expression.class), eq(from), eq(to));
    }

    @Test
    void testWithDateRange_whenAnyNull_returnConjunction() {
        Specification<Order> spec = OrderSpecification.withDateRange(null, ZonedDateTime.now());

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void testWithDateRange_whenCreatedToNull_returnConjunction() {
        Specification<Order> spec = OrderSpecification.withDateRange(ZonedDateTime.now(), null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        assertEquals(mockPredicate, result);
        verify(criteriaBuilder).conjunction();
    }
}