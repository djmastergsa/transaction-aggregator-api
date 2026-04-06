package com.capitec.aggregator.service;

import com.capitec.aggregator.domain.enums.TransactionCategory;
import com.capitec.aggregator.service.impl.TransactionCategorizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionCategorizationService Tests")
class TransactionCategorizationServiceTest {

    private TransactionCategorizationService categorizationService;

    @BeforeEach
    void setUp() {
        categorizationService = new TransactionCategorizationServiceImpl();
    }

    // ---- SALARY ----
    @Nested
    @DisplayName("Salary categorization")
    class SalaryTests {

        @Test
        void salary_keyword_in_description_should_categorize_as_salary() {
            assertThat(categorizationService.categorize("Monthly Salary Payment", "Employer")).isEqualTo(TransactionCategory.SALARY);
        }

        @Test
        void payroll_keyword_should_categorize_as_salary() {
            assertThat(categorizationService.categorize("PAYROLL DEPOSIT", "ABC Corp")).isEqualTo(TransactionCategory.SALARY);
        }

        @Test
        void remuneration_should_categorize_as_salary() {
            assertThat(categorizationService.categorize("Remuneration for services", null)).isEqualTo(TransactionCategory.SALARY);
        }

        @Test
        void salary_is_case_insensitive() {
            assertThat(categorizationService.categorize("SALARY CREDIT", null)).isEqualTo(TransactionCategory.SALARY);
            assertThat(categorizationService.categorize("salary credit", null)).isEqualTo(TransactionCategory.SALARY);
        }
    }

    // ---- GROCERIES ----
    @Nested
    @DisplayName("Groceries categorization")
    class GroceriesTests {

        @ParameterizedTest(name = "Merchant ''{0}'' should be GROCERIES")
        @CsvSource({
                "Purchase at Woolworths food, Woolworths Food",
                "Pick n Pay Supermarket, Pick n Pay",
                "Checkers Hypermarket, Checkers",
                "SPAR Convenience, SPAR",
                "Food Lovers Market, Food Lovers",
                "FreshStop at Caltex, FreshStop"
        })
        void common_grocery_merchants_should_be_categorized_as_groceries(String description, String merchant) {
            assertThat(categorizationService.categorize(description, merchant)).isEqualTo(TransactionCategory.GROCERIES);
        }

        @Test
        void checkers_sixty60_delivery_should_be_groceries() {
            assertThat(categorizationService.categorize("Checkers Sixty60 Delivery", "Checkers")).isEqualTo(TransactionCategory.GROCERIES);
        }
    }

    // ---- UTILITIES ----
    @Nested
    @DisplayName("Utilities categorization")
    class UtilitiesTests {

        @ParameterizedTest(name = "Description ''{0}'' should be UTILITIES")
        @CsvSource({
                "Eskom Prepaid Electricity Token, Eskom",
                "City Power Electricity Bill, City Power",
                "Telkom ADSL Line Rental, Telkom",
                "Vodacom Airtime Purchase, Vodacom",
                "MTN Data Bundle, MTN",
                "Rain Fibre Internet Monthly, Rain",
                "Fibre Internet Subscription, Vumatel"
        })
        void utility_providers_should_be_categorized_as_utilities(String description, String merchant) {
            assertThat(categorizationService.categorize(description, merchant)).isEqualTo(TransactionCategory.UTILITIES);
        }

        @Test
        void electricity_keyword_should_be_utilities() {
            assertThat(categorizationService.categorize("Prepaid electricity purchase", "Eskom")).isEqualTo(TransactionCategory.UTILITIES);
        }
    }

    // ---- ENTERTAINMENT ----
    @Nested
    @DisplayName("Entertainment categorization")
    class EntertainmentTests {

        @ParameterizedTest(name = "''{0}'' should be ENTERTAINMENT")
        @CsvSource({
                "Netflix Monthly Subscription, Netflix",
                "DStv Premium Bill, DStv",
                "Showmax Streaming, Showmax",
                "Spotify Premium, Spotify",
                "Apple Music Subscription, Apple Music",
                "Ster-Kinekor Cinema Tickets, Ster-Kinekor"
        })
        void streaming_and_entertainment_services_should_be_entertainment(String description, String merchant) {
            assertThat(categorizationService.categorize(description, merchant)).isEqualTo(TransactionCategory.ENTERTAINMENT);
        }

        @Test
        void cinema_keyword_in_description_should_be_entertainment() {
            assertThat(categorizationService.categorize("Cinema movie tickets Eastgate", "Nu Metro Cinemas")).isEqualTo(TransactionCategory.ENTERTAINMENT);
        }
    }

    // ---- TRANSPORT ----
    @Nested
    @DisplayName("Transport categorization")
    class TransportTests {

        @ParameterizedTest(name = "''{0}'' should be TRANSPORT")
        @CsvSource({
                "Uber Trip Sandton, Uber",
                "Bolt Taxi to Airport, Bolt",
                "Shell Garage Fuel Payment, Shell",
                "Engen Petrol Station, Engen",
                "BP Petrol Payment, BP",
                "Caltex Garage Fuel, Caltex",
                "Gautrain Monthly Card, Gautrain"
        })
        void transport_merchants_should_be_categorized_as_transport(String description, String merchant) {
            assertThat(categorizationService.categorize(description, merchant)).isEqualTo(TransactionCategory.TRANSPORT);
        }

        @Test
        void fuel_keyword_should_be_transport() {
            assertThat(categorizationService.categorize("Fuel purchase at garage", "Sasol")).isEqualTo(TransactionCategory.TRANSPORT);
        }
    }

    // ---- DINING ----
    @Nested
    @DisplayName("Dining categorization")
    class DiningTests {

        @ParameterizedTest(name = "''{0}'' should be DINING")
        @CsvSource({
                "KFC Burger Meal, KFC",
                "McDonald's Drive Thru, McDonald's",
                "Steers Burger Melrose, Steers",
                "Wimpy Breakfast Rosebank, Wimpy",
                "Nando's Chicken Sandton, Nando's",
                "Spur Steak Ranch, Spur",
                "Ocean Basket Restaurant, Ocean Basket"
        })
        void sa_fast_food_chains_should_be_dining(String description, String merchant) {
            assertThat(categorizationService.categorize(description, merchant)).isEqualTo(TransactionCategory.DINING);
        }

        @Test
        void uber_eats_should_be_dining() {
            assertThat(categorizationService.categorize("Uber Eats delivery order", "Uber Eats")).isEqualTo(TransactionCategory.DINING);
        }

        @Test
        void coffee_keyword_should_be_dining() {
            assertThat(categorizationService.categorize("Mug & Bean Coffee Menlyn", "Mug & Bean")).isEqualTo(TransactionCategory.DINING);
        }

        @Test
        void restaurant_keyword_should_be_dining() {
            assertThat(categorizationService.categorize("Restaurant booking - Cape Town", "Boschendal")).isEqualTo(TransactionCategory.DINING);
        }
    }

    // ---- HEALTHCARE ----
    @Nested
    @DisplayName("Healthcare categorization")
    class HealthcareTests {

        @ParameterizedTest(name = "''{0}'' should be HEALTHCARE")
        @CsvSource({
                "Clicks Pharmacy Purchase, Clicks Pharmacy",
                "Dischem Pharmacy Products, Dischem",
                "Mediclinic Hospital Bill, Mediclinic",
                "Doctor Consultation Fee, Dr Smith",
                "Medical Aid Contribution, Discovery"
        })
        void healthcare_providers_should_be_categorized_as_healthcare(String description, String merchant) {
            assertThat(categorizationService.categorize(description, merchant)).isEqualTo(TransactionCategory.HEALTHCARE);
        }

        @Test
        void dis_chem_with_hyphen_should_be_healthcare() {
            assertThat(categorizationService.categorize("Dis-Chem Pharmacy Products", "Dis-Chem")).isEqualTo(TransactionCategory.HEALTHCARE);
        }
    }

    // ---- SHOPPING ----
    @Nested
    @DisplayName("Shopping categorization")
    class ShoppingTests {

        @ParameterizedTest(name = "''{0}'' should be SHOPPING")
        @CsvSource({
                "Takealot Online Purchase, Takealot",
                "Amazon Order ZA, Amazon",
                "Mr Price Fashion Sale, Mr Price",
                "Woolworths Fashion Online, Woolworths Fashion",
                "H&M Clothing Purchase, H&M",
                "Zara Sandton City, Zara",
                "Edgars Clearance Sale, Edgars"
        })
        void shopping_retailers_should_be_categorized_as_shopping(String description, String merchant) {
            assertThat(categorizationService.categorize(description, merchant)).isEqualTo(TransactionCategory.SHOPPING);
        }
    }

    // ---- TRANSFER ----
    @Nested
    @DisplayName("Transfer categorization")
    class TransferTests {

        @Test
        void eft_keyword_should_be_transfer() {
            assertThat(categorizationService.categorize("EFT to Savings Account", "FNB")).isEqualTo(TransactionCategory.TRANSFER);
        }

        @Test
        void home_loan_should_be_transfer() {
            assertThat(categorizationService.categorize("Monthly Home Loan Payment", "ABSA")).isEqualTo(TransactionCategory.TRANSFER);
        }

        @Test
        void payshap_should_be_transfer() {
            assertThat(categorizationService.categorize("PayShap send money", "PayShap")).isEqualTo(TransactionCategory.TRANSFER);
        }

        @Test
        void snapscan_should_be_transfer() {
            assertThat(categorizationService.categorize("SnapScan Payment to Friend", "SnapScan")).isEqualTo(TransactionCategory.TRANSFER);
        }
    }

    // ---- EDGE CASES ----
    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        void null_description_and_merchant_should_return_other() {
            assertThat(categorizationService.categorize(null, null)).isEqualTo(TransactionCategory.OTHER);
        }

        @Test
        void empty_description_and_merchant_should_return_other() {
            assertThat(categorizationService.categorize("", "")).isEqualTo(TransactionCategory.OTHER);
        }

        @Test
        void blank_description_and_merchant_should_return_other() {
            assertThat(categorizationService.categorize("   ", "   ")).isEqualTo(TransactionCategory.OTHER);
        }

        @Test
        void unrecognized_merchant_should_return_other() {
            assertThat(categorizationService.categorize("Some random transaction", "Unknown Merchant")).isEqualTo(TransactionCategory.OTHER);
        }

        @Test
        void null_description_with_valid_merchant_should_still_categorize() {
            assertThat(categorizationService.categorize(null, "Netflix")).isEqualTo(TransactionCategory.ENTERTAINMENT);
        }

        @Test
        void valid_description_with_null_merchant_should_still_categorize() {
            assertThat(categorizationService.categorize("Monthly salary payment", null)).isEqualTo(TransactionCategory.SALARY);
        }

        @Test
        void mixed_case_should_be_handled_correctly() {
            assertThat(categorizationService.categorize("WOOLWORTHS FOOD", "WOOLWORTHS")).isEqualTo(TransactionCategory.GROCERIES);
        }

        @Test
        void categorization_is_case_insensitive_for_all_categories() {
            assertThat(categorizationService.categorize("NETFLIX SUBSCRIPTION", "NETFLIX")).isEqualTo(TransactionCategory.ENTERTAINMENT);
            assertThat(categorizationService.categorize("eskom electricity", "ESKOM")).isEqualTo(TransactionCategory.UTILITIES);
            assertThat(categorizationService.categorize("UBER TRIP HOME", "UBER")).isEqualTo(TransactionCategory.TRANSPORT);
        }
    }
}
