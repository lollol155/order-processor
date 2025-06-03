package ivo.orders_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import ivo.orders_backend.controllers.OrdersController;
import ivo.orders_backend.models.*;
import ivo.orders_backend.services.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OrderService orderService;

	@TestConfiguration
	static class MockConfig {
		@Bean
		OrderService orderService() {
			return Mockito.mock(OrderService.class);
		}
	}

	private static final String BASE_URL = "/orders";
	private static final String VALID_CUSTOMER_ID = "customer123";
	private static final String VALID_PRODUCT_ID = "prod-1";

	private OrderRequest validOrderRequest() {
		return new OrderRequest(
				VALID_CUSTOMER_ID,
				List.of(new Product(VALID_PRODUCT_ID, 2, new BigDecimal("10.99")))
		);
	}

	private OrderResponse validOrderResponse(String orderId) {
		return new OrderResponse(
				orderId,
				VALID_CUSTOMER_ID,
				new BigDecimal("21.98"),
				OrderStatus.ACCEPTED
		);
	}

	@Test
	@DisplayName("POST /orders - Success")
	void createOrderSuccess() throws Exception {
		final OrderRequest request = validOrderRequest();
		final String orderId = UUID.randomUUID().toString();

		Mockito.when(orderService.createNewOrder(any(OrderRequest.class)))
				.thenReturn(validOrderResponse(orderId));

		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.orderId").value(orderId))
				.andExpect(jsonPath("$.customerId").value(VALID_CUSTOMER_ID))
				.andExpect(jsonPath("$.totalAmount").value("21.98"))
				.andExpect(jsonPath("$.status").value("ACCEPTED"));
	}

	@Test @DisplayName("POST /orders - Validation: customerId null")
	void createOrder_CustomerIdNull() throws Exception {
		assertValidationError(
				null,
				List.of(new Product(VALID_PRODUCT_ID, 1, new BigDecimal("1"))),
				"customerId: must not be blank"
		);
	}

	@Test @DisplayName("POST /orders - Validation: customerId empty")
	void createOrder_CustomerIdEmpty() throws Exception {
		assertValidationError(
				"",
				List.of(new Product(VALID_PRODUCT_ID, 1, new BigDecimal("1"))),
				"customerId: must not be blank"
		);
	}

	@Test @DisplayName("POST /orders - Validation: products null")
	void createOrder_ProductsNull() throws Exception {
		assertValidationError(
				VALID_CUSTOMER_ID,
				null,
				"products: must not be empty"
		);
	}

	@Test @DisplayName("POST /orders - Validation: products empty")
	void createOrder_ProductsEmpty() throws Exception {
		assertValidationError(
				VALID_CUSTOMER_ID,
				List.of(),
				"products: At least one product is required"
		);
	}

	@Test @DisplayName("POST /orders - Validation: productId blank")
	void createOrder_ProductIdBlank() throws Exception {
		assertValidationError(
				VALID_CUSTOMER_ID,
				List.of(new Product("", 1, new BigDecimal("1"))),
				"products[0].productId: must not be blank"
		);
	}

	@Test @DisplayName("POST /orders - Validation: productId null")
	void createOrder_ProductIdNull() throws Exception {
		assertValidationError(
				VALID_CUSTOMER_ID,
				List.of(new Product(null, 1, new BigDecimal("1"))),
				"products[0].productId: must not be blank"
		);
	}

	@Test @DisplayName("POST /orders - Validation: quantity 0")
	void createOrder_QuantityZero() throws Exception {
		assertValidationError(
				VALID_CUSTOMER_ID,
				List.of(new Product(VALID_PRODUCT_ID, 0, new BigDecimal("1"))),
				"products[0].quantity: Quantity must be greater than 0."
		);
	}

	@Test @DisplayName("POST /orders - Validation: price 0")
	void createOrder_PriceZero() throws Exception {
		assertValidationError(
				VALID_CUSTOMER_ID,
				List.of(new Product(VALID_PRODUCT_ID, 1, new BigDecimal("0"))),
				"products[0].price: Price must be greater than 0."
		);
	}

	private void assertValidationError(String customerId, List<Product> products, String expectedMessage) throws Exception {
		final OrderRequest request = new OrderRequest(customerId, products);

		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors", org.hamcrest.Matchers.hasItem(expectedMessage)));
	}

	@Test
	@DisplayName("GET /orders/{orderId} - Order Found")
	void getOrderByIdFound() throws Exception {
		final String orderId = "order-123";
		final OrderResponse response = validOrderResponse(orderId);

		Mockito.when(orderService.findOrderById(eq(orderId)))
				.thenReturn(Optional.of(response));

		mockMvc.perform(get(BASE_URL + "/{orderId}", orderId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value(orderId))
				.andExpect(jsonPath("$.customerId").value(VALID_CUSTOMER_ID));
	}

	@Test
	@DisplayName("GET /orders/{orderId} - Order Not Found")
	void getOrderByIdNotFound() throws Exception {
		final String nonExistentOrderId = "non-existent-id";

		Mockito.when(orderService.findOrderById(eq(nonExistentOrderId)))
				.thenReturn(Optional.empty());

		mockMvc.perform(get(BASE_URL + "/{orderId}", nonExistentOrderId))
				.andExpect(status().isNotFound());
	}
}
