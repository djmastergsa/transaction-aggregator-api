package com.capitec.aggregator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transaction Aggregator API")
                        .description("""
                                A production-grade REST API that aggregates customer financial transaction data
                                from multiple mock data sources (Bank, Credit Card, Mobile Payment) and provides
                                rich categorization and analytics capabilities.

                                **Features:**
                                - Multi-source data aggregation using the Adapter pattern
                                - Rule-based transaction categorization using the Strategy pattern
                                - Extensive filtering, sorting, and pagination
                                - Aggregation analytics: category breakdowns, monthly trends, source summaries

                                **Data Sources:**
                                - BANK: ~50 transactions/customer (salary, groceries, utilities)
                                - CREDIT_CARD: ~30 transactions/customer (dining, entertainment, shopping)
                                - MOBILE_PAYMENT: ~20 transactions/customer (transport, small transfers, dining)

                                Built for Capitec Bank technical assessment.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Capitec Assessment")
                                .email("assessment@capitecbank.co.za"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}
