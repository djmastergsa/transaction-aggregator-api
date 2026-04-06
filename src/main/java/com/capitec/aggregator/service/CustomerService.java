package com.capitec.aggregator.service;

import com.capitec.aggregator.domain.dto.request.TransactionFilterRequest;
import com.capitec.aggregator.domain.dto.response.*;

import java.util.List;

/**
 * Service for customer-related operations.
 */
public interface CustomerService {

    /**
     * Get all customers.
     */
    List<CustomerDto> getAllCustomers();

    /**
     * Get a customer by their external customerId (e.g. "CUST001").
     */
    CustomerDto getCustomerByCustomerId(String customerId);

    /**
     * Get paginated transactions for a specific customer, with optional filtering.
     */
    PagedResponse<TransactionDto> getCustomerTransactions(String customerId, TransactionFilterRequest filter);

    /**
     * Get a financial summary (income, expenses, net position, top categories) for a customer.
     */
    CustomerSummaryDto getCustomerSummary(String customerId);

    /**
     * Get category-level breakdown for a customer.
     */
    List<CategorySummaryDto> getCustomerCategorySummary(String customerId);

    /**
     * Get monthly trends for a customer over the last 12 months.
     */
    List<MonthlyTrendDto> getCustomerMonthlyTrends(String customerId);
}
