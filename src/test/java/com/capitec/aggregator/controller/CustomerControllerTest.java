package com.capitec.aggregator.controller;

import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.dto.response.*;
import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import com.capitec.aggregator.exception.GlobalExceptionHandler;
import com.capitec.aggregator.exception.ResourceNotFoundException;
import com.capitec.aggregator.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CustomerController Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private CustomerDto buildCustomerDto(String customerId, String firstName, String lastName) {
        return new CustomerDto(
                UUID.randomUUID(),
                customerId,
                firstName,
                lastName,
                firstName + " " + lastName,
                firstName.toLowerCase() + "." + lastName.toLowerCase() + "@email.co.za",
                "+27 82 123 4567",
                LocalDateTime.now().minusMonths(6),
                100
        );
    }

    private TransactionDto buildTransactionDto(String customerId) {
        return new TransactionDto(
                UUID.randomUUID(),
                "BANK-" + customerId + "-SAL-001",
                customerId,
                "Sipho Dlamini",
                new BigDecimal("45000.00"),
                "ZAR",
                TransactionType.CREDIT,
                TransactionCategory.SALARY,
                "Monthly Salary",
                "Capitec Bank",
                "BANK",
                LocalDateTime.now().minusDays(5),
                null,
                TransactionStatus.PROCESSED
        );
    }

    // ---- GET /api/v1/customers ----

    @Nested
    @DisplayName("GET /api/v1/customers")
    class GetAllCustomersTests {

        @Test
        @DisplayName("should return 200 with list of all customers")
        void should_return_200_with_customers() throws Exception {
            List<CustomerDto> customers = List.of(
                    buildCustomerDto("CUST001", "Sipho", "Dlamini"),
                    buildCustomerDto("CUST002", "Nomvula", "Khumalo"),
                    buildCustomerDto("CUST003", "Thabo", "Molefe")
            );

            when(customerService.getAllCustomers()).thenReturn(customers);

            mockMvc.perform(get("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].customerId", is("CUST001")))
                    .andExpect(jsonPath("$[0].firstName", is("Sipho")))
                    .andExpect(jsonPath("$[0].lastName", is("Dlamini")))
                    .andExpect(jsonPath("$[0].fullName", is("Sipho Dlamini")));
        }

        @Test
        @DisplayName("should return 200 with empty list when no customers")
        void should_return_empty_list_when_no_customers() throws Exception {
            when(customerService.getAllCustomers()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ---- GET /api/v1/customers/{customerId} ----

    @Nested
    @DisplayName("GET /api/v1/customers/{customerId}")
    class GetCustomerByIdTests {

        @Test
        @DisplayName("should return 200 when customer found")
        void should_return_200_when_customer_found() throws Exception {
            CustomerDto customer = buildCustomerDto("CUST001", "Sipho", "Dlamini");
            when(customerService.getCustomerByCustomerId("CUST001")).thenReturn(customer);

            mockMvc.perform(get("/api/v1/customers/{customerId}", "CUST001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId", is("CUST001")))
                    .andExpect(jsonPath("$.firstName", is("Sipho")))
                    .andExpect(jsonPath("$.email", is("sipho.dlamini@email.co.za")));
        }

        @Test
        @DisplayName("should return 404 when customer not found")
        void should_return_404_when_customer_not_found() throws Exception {
            when(customerService.getCustomerByCustomerId("CUST999"))
                    .thenThrow(new ResourceNotFoundException("Customer", "customerId", "CUST999"));

            mockMvc.perform(get("/api/v1/customers/{customerId}", "CUST999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("CUST999")));
        }
    }

    // ---- GET /api/v1/customers/{customerId}/transactions ----

    @Nested
    @DisplayName("GET /api/v1/customers/{customerId}/transactions")
    class GetCustomerTransactionsTests {

        @Test
        @DisplayName("should return 200 with paginated transactions")
        void should_return_paginated_transactions() throws Exception {
            TransactionDto txn = buildTransactionDto("CUST001");
            PagedResponse<TransactionDto> response = new PagedResponse<>(
                    List.of(txn), 0, 20, 1, 1, false, false, true, true);

            when(customerService.getCustomerTransactions(eq("CUST001"), any(TransactionFilterRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(get("/api/v1/customers/{customerId}/transactions", "CUST001"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].customerId", is("CUST001")));
        }

        @Test
        @DisplayName("should return 404 when customer not found")
        void should_return_404_when_customer_not_found() throws Exception {
            when(customerService.getCustomerTransactions(eq("CUST999"), any()))
                    .thenThrow(new ResourceNotFoundException("Customer", "customerId", "CUST999"));

            mockMvc.perform(get("/api/v1/customers/{customerId}/transactions", "CUST999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should pass filter params to service")
        void should_pass_filter_params_to_service() throws Exception {
            when(customerService.getCustomerTransactions(eq("CUST001"), any(TransactionFilterRequest.class)))
                    .thenReturn(new PagedResponse<>(
                            List.of(), 0, 10, 0, 0, false, false, true, true));

            mockMvc.perform(get("/api/v1/customers/{customerId}/transactions", "CUST001")
                            .param("category", "GROCERIES")
                            .param("sourceSystem", "BANK")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());

            verify(customerService).getCustomerTransactions(eq("CUST001"), any(TransactionFilterRequest.class));
        }
    }

    // ---- GET /api/v1/customers/{customerId}/summary ----

    @Nested
    @DisplayName("GET /api/v1/customers/{customerId}/summary")
    class GetCustomerSummaryTests {

        @Test
        @DisplayName("should return 200 with customer financial summary")
        void should_return_200_with_summary() throws Exception {
            CustomerSummaryDto summary = new CustomerSummaryDto(
                    "CUST001",
                    "Sipho Dlamini",
                    100,
                    new BigDecimal("270000.00"),
                    new BigDecimal("180000.00"),
                    new BigDecimal("90000.00"),
                    new BigDecimal("30000.00"),
                    new BigDecimal("45000.00"),
                    List.of(
                            new CategorySummaryDto("GROCERIES", 30, new BigDecimal("45000.00"),
                                    null, null, new BigDecimal("25.00"), null)
                    )
            );

            when(customerService.getCustomerSummary("CUST001")).thenReturn(summary);

            mockMvc.perform(get("/api/v1/customers/{customerId}/summary", "CUST001"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId", is("CUST001")))
                    .andExpect(jsonPath("$.customerName", is("Sipho Dlamini")))
                    .andExpect(jsonPath("$.totalTransactions", is(100)))
                    .andExpect(jsonPath("$.netPosition", is(90000.00)))
                    .andExpect(jsonPath("$.topCategories", hasSize(1)))
                    .andExpect(jsonPath("$.topCategories[0].category", is("GROCERIES")));
        }

        @Test
        @DisplayName("should return 404 when customer not found")
        void should_return_404_when_customer_not_found() throws Exception {
            when(customerService.getCustomerSummary("CUST999"))
                    .thenThrow(new ResourceNotFoundException("Customer", "customerId", "CUST999"));

            mockMvc.perform(get("/api/v1/customers/{customerId}/summary", "CUST999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    // ---- GET /api/v1/customers/{customerId}/categories/summary ----

    @Nested
    @DisplayName("GET /api/v1/customers/{customerId}/categories/summary")
    class CustomerCategorySummaryTests {

        @Test
        @DisplayName("should return 200 with category breakdown for customer")
        void should_return_200_with_categories() throws Exception {
            List<CategorySummaryDto> categories = List.of(
                    new CategorySummaryDto("GROCERIES", 30, new BigDecimal("45000.00"),
                            null, null, new BigDecimal("25.00"), new BigDecimal("1500.00")),
                    new CategorySummaryDto("UTILITIES", 20, new BigDecimal("25000.00"),
                            null, null, new BigDecimal("13.89"), new BigDecimal("1250.00"))
            );

            when(customerService.getCustomerCategorySummary("CUST001")).thenReturn(categories);

            mockMvc.perform(get("/api/v1/customers/{customerId}/categories/summary", "CUST001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].category", is("GROCERIES")))
                    .andExpect(jsonPath("$[1].category", is("UTILITIES")));
        }

        @Test
        @DisplayName("should return 404 for unknown customer")
        void should_return_404_for_unknown_customer() throws Exception {
            when(customerService.getCustomerCategorySummary("CUST999"))
                    .thenThrow(new ResourceNotFoundException("Customer", "customerId", "CUST999"));

            mockMvc.perform(get("/api/v1/customers/{customerId}/categories/summary", "CUST999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ---- GET /api/v1/customers/{customerId}/trends/monthly ----

    @Nested
    @DisplayName("GET /api/v1/customers/{customerId}/trends/monthly")
    class CustomerMonthlyTrendsTests {

        @Test
        @DisplayName("should return 200 with monthly trends for customer")
        void should_return_200_with_trends() throws Exception {
            List<MonthlyTrendDto> trends = List.of(
                    new MonthlyTrendDto(2024, 1, "January", "2024-01",
                            new BigDecimal("15000.00"), new BigDecimal("45000.00"),
                            new BigDecimal("30000.00"), 20, new BigDecimal("3000.00")),
                    new MonthlyTrendDto(2024, 2, "February", "2024-02",
                            new BigDecimal("18000.00"), new BigDecimal("45000.00"),
                            new BigDecimal("27000.00"), 22, null)
            );

            when(customerService.getCustomerMonthlyTrends("CUST001")).thenReturn(trends);

            mockMvc.perform(get("/api/v1/customers/{customerId}/trends/monthly", "CUST001"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].period", is("2024-01")))
                    .andExpect(jsonPath("$[0].year", is(2024)))
                    .andExpect(jsonPath("$[0].month", is(1)))
                    .andExpect(jsonPath("$[1].period", is("2024-02")));
        }

        @Test
        @DisplayName("should return 404 for unknown customer")
        void should_return_404_for_unknown_customer() throws Exception {
            when(customerService.getCustomerMonthlyTrends("CUST999"))
                    .thenThrow(new ResourceNotFoundException("Customer", "customerId", "CUST999"));

            mockMvc.perform(get("/api/v1/customers/{customerId}/trends/monthly", "CUST999"))
                    .andExpect(status().isNotFound());
        }
    }
}
