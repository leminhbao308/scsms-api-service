package com.kltn.scsms_api_service.core.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    @JsonProperty("content")
    private List<T> content;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("size")
    private Integer size;

    @JsonProperty("total_elements")
    private Long totalElements;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("first")
    private Boolean first;

    @JsonProperty("last")
    private Boolean last;

    @JsonProperty("has_next")
    private Boolean hasNext;

    @JsonProperty("has_previous")
    private Boolean hasPrevious;
}

