package com.capitec.aggregator.controller;

import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.dto.response.*;
import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.domain.enums.TransactionStatus;
import com.capitec.aggregator.domain.enums.TransactionType;
import com.capitec.aggregator.exception.GlobalExceptionHandler;
import com.capitec.aggregator.exception.ResourceNotFoundException;
import com.capitec.aggregator.service.TransactionAggregatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("TransactionController Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionAggregatorService aggregatorService;

    private TransactionDto buildTransactionDto() {
        return new TransactionDto(
                UUID.randomUUID(),
                "BANK-CUST001-SAL-001",
                "CUST001",
                "Sipho Dlamini",
                new BigDecimal("45000.00"),
                "ZAR",
                TransactionType.CREDIT,
                TransactionCategory.SALARY,
                "Monthly Salary",
                "Capitec Bank",
                "BANK",
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(5),
                TransactionStatus.PROCESSED
        );
    }

    // ---- GET /api/v1/transactions ----

    @Nested
    @DisplayName("GET /api/v1/transactions")
    class GetTransactionsTests {

        @Test
        @DisplayName("should return 200 with paged response of transactions")
        void should_return_200_with_transactions() throws Exception {
            TransactionDto dto = buildTransactionDto();
            PagedResponse<TransactionDto> response = new PagedResponse<>(
                    List.of(dto), 0, 20, 1, 1, false, false, true, true);

            when(aggregatorService.getTransactions(any(TransactionFilterRequest.class))).thenReturn(response);

            mockMvc.perform(get("/api/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.page", is(0)))
                    .andExpect(jsonPath("$.content[0].transactionRef", is("BANK-CUST001-SAL-001")))
                    .andExpect(jsonPath("$.content[0].customerId", is("CUST001")))
                    .andExpect(jsonPath("$.content[0].sourceSystem", is("BANK")));
        }

        @Test
        @DisplayName("should return 200 with pagination params")
        void should_return_200_with_custom_pagination() throws Exception {
            PagedResponse<TransactionDto> response = new PagedResponse<>(
                    List.of(), 2, 10, 0, 0, false, true, false, true);

            when(aggregatorService.getTransactions(any())).thenReturn(response);

            mockMvc.perform(get("/api/v1/transactions")
                            .param("page", "2")
                            .param("size", "10")
                            .param("sortBy", "amount")
                            .param("sortDirection", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page", is(2)))
                    .andExpect(jsonPath("$.size", is(10)));
        }

        @Test
        @DisplayName("should apply category filter")
        void should_apply_category_filter() throws Exception {
            when(aggregatorService.getTransactions(any())).thenReturn(
                    new PagedResponse<>(List.of(), 0, 20, 0, 0, false, false, true, true));

            mockMvc.perform(get("/api/v1/transactions")
                            .param("category", "GROCERIES")
                            .param("customerId", "CUST001"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 400 for invalid category value")
        void should_return_400_for_invalid_category() throws Exception {
            mockMvc.perform(get("/api/v1/transactions")
                            .param("category", "INVALID_CATEGORY"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ---- GET /api/v1/transactions/{id} ----

    @Nested
    @DisplayName("GET /api/v1/transactions/{id}")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("should return 200 when transaction found")
        void should_return_200_when_found() throws Exception {
            TransactionDto dto = buildTransactionDto();
            String id = dto.id().toString();
            when(aggregatorService.getTransactionById(id)).thenReturn(dto);

            mockMvc.perform(get("/api/v1/transactions/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionRef", is("BANK-CUST001-SAL-001")))
                    .andExpect(jsonPath("$.customerId", is("CUST001")));
        }

        @Test
        @DisplayName("should return 404 when transaction not found")
        void should_return_404_when_not_found() throws Exception {
            String id = UUID.randomUUID().toString();
            when(aggregatorService.getTransactionById(id))
                    .thenThrow(new ResourceNotFoundException("Transaction", "id", id));

            mockMvc.perform(get("/api/v1/transactions/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")));
        }

        @Test
        @DisplayName("should return 400 for invalid UUID format")
        void should_return_400_for_invalid_uuid() throws Exception {
            when(aggregatorService.getTransactionById("not-a-valid-uuid"))
                    .thenThrow(new IllegalArgumentException("Invalid UUID format: not-a-valid-uuid"));

            mockMvc.perform(get("/api/v1/transactions/{id}", "not-a-valid-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ---- GET /api/v1/transactions/ref/{transactionRef} ----

    @Nested
    @DisplayName("GET /api/v1/transactions/ref/{transactionRef}")
    class GetTransactionByRefTests {

        @Test
        @DisplayName("should return 200 when transaction found by ref")
        void should_return_200_when_ref_found() throws Exception {
            TransactionDto dto = buildTransactionDto();
            when(aggregatorService.getTransactionByRef("BANK-CUST001-SAL-001")).thenReturn(dto);

            mockMvc.perform(get("/api/v1/transactions/ref/{ref}", "BANK-CUST001-SAL-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionRef", is("BANK-CUST001-SAL-001")));
        }

        @Test
        @DisplayName("should return 404 when ref not found")
        void should_return_404_when_ref_not_found() throws Exception {
            when(aggregatorService.getTransactionByRef("UNKNOWN-REF"))
                    .thenThrow(new ResourceNotFoundException("Transaction", "transactionRef", "UNKNOWN-REF"));

            mockMvc.perform(get("/api/v1/transactions/ref/{ref}", "UNKNOWN-REF"))
                    .andExpect(status().isNotFound());
        }
    }

    // ---- POST /api/v1/transactions/sync ----

    @Nested
    @DisplayName("POST /api/v1/transactions/sync")
    class SyncTests {

        @Test
        @DisplayName("should return 200 with sync result")
        void should_return_200_on_sync() throws Exception {
            when(aggregatorService.syncAllSources()).thenReturn(500);

            mockMvc.perform(post("/api/v1/transactions/sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.newTransactionsPersisted", is(500)));
        }
    }

    // ---- GET /api/v1/transactions/aggregate ----

    @Nested
    @DisplayName("GET /api/v1/transactions/aggregate")
    class AggregateTests {

        @Test
        @DisplayName("should return 200 with aggregation summary")
        void should_return_200_with_summary() throws Exception {
            AggregationSummaryDto summary = new AggregationSummaryDto(
                    500,
                    new BigDecimal("250000.00"),
                    new BigDecimal("300000.00"),
                    new BigDecimal("50000.00"),
                    new BigDecimal("1100.00"),
                    Map.of("GROCERIES", 100L, "SALARY", 6L),
                    Map.of(),
                    Map.of("BANK", 250L, "CREDIT_CARD", 150L),
                    Map.of(),
                    Map.of("DEBIT", 470L, "CREDIT", 30L),
                    Map.of("PROCESSED", 495L),
                    null,
                    null,
                    LocalDateTime.now()
            );

            when(aggregatorService.getAggregationSummary(any())).thenReturn(summary);

            mockMvc.perform(get("/api/v1/transactions/aggregate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalTransactions", is(500)))
                    .andExpect(jsonPath("$.netPosition", is(50000.00)))
                    .andExpect(jsonPath("$.transactionsBySource.BANK", is(250)));
        }
    }

    // ---- GET /api/v1/transactions/categories/summary ----

    @Nested
    @DisplayName("GET /api/v1/transactions/categories/summary")
    class CategorySummaryTests {

        @Test
        @DisplayName("should return 200 with list of category summaries")
        void should_return_200_with_categories() throws Exception {
            List<CategorySummaryDto> summaries = List.of(
                    new CategorySummaryDto("GROCERIES", 100, new BigDecimal("50000.00"), null, null, new BigDecimal("20.00"), null),
                    new CategorySummaryDto("UTILITIES", 50, new BigDecimal("30000.00"), null, null, new BigDecimal("12.00"), null)
            );

            when(aggregatorService.getCategorySummary(null)).thenReturn(summaries);

            mockMvc.perform(get("/api/v1/transactions/categories/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].category", is("GROCERIES")))
                    .andExpect(jsonPath("$[0].transactionCount", is(100)));
        }
    }

    // ---- GET /api/v1/transactions/trends/monthly ----

    @Nested
    @DisplayName("GET /api/v1/transactions/trends/monthly")
    class MonthlyTrendsTests {

        @Test
        @DisplayName("should return 200 with monthly trend data")
        void should_return_200_with_trends() throws Exception {
            List<MonthlyTrendDto> trends = List.of(
                    new MonthlyTrendDto(2024, 1, "January", "2024-01",
                            new BigDecimal("15000.00"), new BigDecimal("45000.00"),
                            new BigDecimal("30000.00"), 100, null)
            );

            when(aggregatorService.getMonthlyTrends(null)).thenReturn(trends);

            mockMvc.perform(get("/api/v1/transactions/trends/monthly"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].period", is("2024-01")))
                    .andExpect(jsonPath("$[0].year", is(2024)));
        }
    }

    // ---- GET /api/v1/transactions/sources/summary ----

    @Nested
    @DisplayName("GET /api/v1/transactions/sources/summary")
    class SourceSummaryTests {

        @Test
        @DisplayName("should return 200 with source summaries")
        void should_return_200_with_sources() throws Exception {
            List<SourceSummaryDto> sources = List.of(
                    new SourceSummaryDto("BANK", 250,
                            new BigDecimal("120000.00"), new BigDecimal("225000.00"),
                            new BigDecimal("105000.00"), new BigDecimal("50.00"))
            );

            when(aggregatorService.getSourceSummary()).thenReturn(sources);

            mockMvc.perform(get("/api/v1/transactions/sources/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].sourceSystem", is("BANK")))
                    .andExpect(jsonPath("$[0].transactionCount", is(250)));
        }
    }
}
