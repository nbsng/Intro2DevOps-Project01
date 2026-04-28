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
}