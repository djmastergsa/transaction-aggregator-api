package com.capitec.aggregator.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response object")
public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "Error type", example = "Not Found")
        String error,

        @Schema(description = "Human-readable error message", example = "Transaction not found with id: 'abc123'")
        String message,

        @Schema(description = "Request path that caused the error", example = "/api/v1/transactions/abc123")
        String path,

        @Schema(description = "Timestamp when the error occurred")
        LocalDateTime timestamp,

        @Schema(description = "List of field-level validation errors")
        List<FieldError> fieldErrors
) {
    @Schema(description = "Field-level validation error detail")
    public record FieldError(
            @Schema(description = "Field that failed validation", example = "amount")
            String field,

            @Schema(description = "Rejected value", example = "-100")
            Object rejectedValue,

            @Schema(description = "Validation message", example = "must be greater than 0")
            String message
    ) {}
}
