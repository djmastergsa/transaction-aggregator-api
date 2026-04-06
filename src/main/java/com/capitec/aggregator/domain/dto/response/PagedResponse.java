package com.capitec.aggregator.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Generic paginated response wrapper")
public record PagedResponse<T>(
        @Schema(description = "List of items in the current page")
        List<T> content,

        @Schema(description = "Current page number (zero-based)", example = "0")
        int page,

        @Schema(description = "Number of items per page", example = "20")
        int size,

        @Schema(description = "Total number of elements across all pages", example = "500")
        long totalElements,

        @Schema(description = "Total number of pages", example = "25")
        int totalPages,

        @Schema(description = "Whether there is a next page")
        boolean hasNext,

        @Schema(description = "Whether there is a previous page")
        boolean hasPrevious,

        @Schema(description = "Whether this is the first page")
        boolean first,

        @Schema(description = "Whether this is the last page")
        boolean last
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast()
        );
    }
}
