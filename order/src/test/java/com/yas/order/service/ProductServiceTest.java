package com.yas.order.service;

import static com.yas.order.utils.SecurityContextUtils.setUpSecurityContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.config.ServiceUrlConfig;
import com.yas.order.viewmodel.order.OrderItemVm;
import com.yas.order.viewmodel.order.OrderVm;
import com.yas.order.viewmodel.product.ProductCheckoutListVm;
import com.yas.order.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.order.viewmodel.product.ProductQuantityItem;
import com.yas.order.viewmodel.product.ProductVariationVm;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

class ProductServiceTest {

    private RestClient restClient;

    private ServiceUrlConfig serviceUrlConfig;

    private ProductService productService;

    private RestClient.ResponseSpec responseSpec;

    private static final String PRODUCT_URL = "http://api.yas.local/product";

    @BeforeEach
    void setUp() {
	restClient = mock(RestClient.class);
	serviceUrlConfig = mock(ServiceUrlConfig.class);
	productService = new ProductService(restClient, serviceUrlConfig);
	responseSpec = Mockito.mock(RestClient.ResponseSpec.class);
	setUpSecurityContext("test");
	when(serviceUrlConfig.product()).thenReturn(PRODUCT_URL);
    }

    @Test
    void testGetProductVariations_ifNormalCase_returnProductVariationVms() {
	Long productId = 10L;

	URI url = UriComponentsBuilder
		.fromUriString(serviceUrlConfig.product())
		.path("/backoffice/product-variations/{productId}")
		.buildAndExpand(productId)
		.toUri();

	RestClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
	when(restClient.get()).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

	List<ProductVariationVm> variations = List.of(new ProductVariationVm(1L, "Variation A", "SKU-1"));
	when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
		.thenReturn(ResponseEntity.ok(variations));

	List<ProductVariationVm> result = productService.getProductVariations(productId);

	assertThat(result).hasSize(1);
	assertThat(result.getFirst().id()).isEqualTo(1L);
    }

    @Test
    void testSubtractProductStockQuantity_ifNormalCase_sendQuantityItems() {
	OrderVm orderVm = createOrderVm();

	URI url = UriComponentsBuilder
		.fromUriString(serviceUrlConfig.product())
		.path("/backoffice/products/subtract-quantity")
		.buildAndExpand()
		.toUri();

	RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
	when(restClient.put()).thenReturn(requestBodyUriSpec);
	when(requestBodyUriSpec.uri(url)).thenReturn(requestBodyUriSpec);
	when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);

	ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
	when(requestBodyUriSpec.body(bodyCaptor.capture())).thenReturn(requestBodyUriSpec);
	when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

	productService.subtractProductStockQuantity(orderVm);

	Object capturedBody = bodyCaptor.getValue();
	assertThat(capturedBody).isInstanceOf(List.class);
	@SuppressWarnings("unchecked")
	List<ProductQuantityItem> items = (List<ProductQuantityItem>) capturedBody;
	assertThat(items).containsExactlyInAnyOrder(
		new ProductQuantityItem(1L, 2L),
		new ProductQuantityItem(2L, 1L)
	);
    }

    @Test
    void testGetProductInfomation_ifNormalCase_returnProductCheckoutMap() {
	Set<Long> ids = Set.of(1L, 2L);
	int pageNo = 0;
	int pageSize = 2;

	URI url = UriComponentsBuilder
		.fromUriString(serviceUrlConfig.product())
		.path("/products")
		.queryParam("ids", ids)
		.queryParam("pageNo", pageNo)
		.queryParam("pageSize", pageSize)
		.buildAndExpand()
		.toUri();

	RestClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
	when(restClient.get()).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

	List<ProductCheckoutListVm> list = List.of(
		ProductCheckoutListVm.builder().id(1L).name("Product 1").price(10.0).taxClassId(1L).build(),
		ProductCheckoutListVm.builder().id(2L).name("Product 2").price(20.0).taxClassId(2L).build()
	);
	ProductGetCheckoutListVm response = new ProductGetCheckoutListVm(list, 0, 2, 2, 1, true);
	when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
		.thenReturn(ResponseEntity.ok(response));

	Map<Long, ProductCheckoutListVm> result = productService.getProductInfomation(ids, pageNo, pageSize);

	assertThat(result).hasSize(2);
	assertThat(result.get(1L)).isNotNull();
	assertThat(result.get(2L)).isNotNull();
    }

    @Test
    void testGetProductInfomation_whenResponseNull_throwNotFoundException() {
	Set<Long> ids = Set.of(1L);
	int pageNo = 0;
	int pageSize = 10;

	URI url = UriComponentsBuilder
		.fromUriString(serviceUrlConfig.product())
		.path("/products")
		.queryParam("ids", ids)
		.queryParam("pageNo", pageNo)
		.queryParam("pageSize", pageSize)
		.buildAndExpand()
		.toUri();

	RestClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
	when(restClient.get()).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
	when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
		.thenReturn(ResponseEntity.ok(null));

	assertThrows(NotFoundException.class,
		() -> productService.getProductInfomation(ids, pageNo, pageSize));
    }

    @Test
    void testGetProductInfomation_whenProductListNull_throwNotFoundException() {
	Set<Long> ids = Set.of(1L);
	int pageNo = 0;
	int pageSize = 10;

	URI url = UriComponentsBuilder
		.fromUriString(serviceUrlConfig.product())
		.path("/products")
		.queryParam("ids", ids)
		.queryParam("pageNo", pageNo)
		.queryParam("pageSize", pageSize)
		.buildAndExpand()
		.toUri();

	RestClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
	when(restClient.get()).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
	when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

	ProductGetCheckoutListVm response = new ProductGetCheckoutListVm(null, 0, 10, 0, 0, true);
	when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
		.thenReturn(ResponseEntity.ok(response));

	assertThrows(NotFoundException.class,
		() -> productService.getProductInfomation(ids, pageNo, pageSize));
    }

    private static OrderVm createOrderVm() {
	Set<OrderItemVm> orderItems = new LinkedHashSet<>();
	orderItems.add(OrderItemVm.builder().productId(1L).quantity(2).build());
	orderItems.add(OrderItemVm.builder().productId(2L).quantity(1).build());

	return OrderVm.builder().orderItemVms(orderItems).build();
    }
}
