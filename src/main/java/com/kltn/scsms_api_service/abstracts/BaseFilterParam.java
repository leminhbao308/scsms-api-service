package com.kltn.scsms_api_service.abstracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.interfaces.FilterStandardlize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseFilterParam<T extends BaseFilterParam<T>> implements FilterStandardlize<T> {
    @Min(value = 1, message = "Page must be greater than 0")
    private int page = 1;
    
    @Min(value = 1, message = "Size must be greater than 0")
    @Max(value = 100, message = "Size must not exceed 100")
    private int size = 10;
    
    private String sort = "createdDate";
    private String direction = "DESC";
    
    // Filters
    private Boolean active;
    private Boolean deleted;
    
    // Search
    @Size(min = 2, message = "Search term must be at least 2 characters")
    private String search;
    
    // Date ranges
    @JsonProperty("created_date_from")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdDateFrom;
    
    @JsonProperty("created_date_to")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdDateTo;
    
    @JsonProperty("modified_date_from")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime modifiedDateFrom;
    
    @JsonProperty("modified_date_to")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime modifiedDateTo;
    
    @Override
    public final T standardizeFilterRequest(T request) {
        // Standardize pagination
        request.setPage(Math.max(request.getPage() - 1, 0)); // Convert to 0-based
        request.setSize(Math.max(1, Math.min(request.getSize(), 100)));
        
        // Standardize sort and direction
        if (request.getSort() == null || request.getSort().trim().isEmpty()) {
            request.setSort(getDefaultSortField());
        }
        request.setDirection("ASC".equalsIgnoreCase(request.getDirection()) ? "ASC" : "DESC");
        
        // Standardize search
        request.setSearch(trimAndNullify(request.getSearch()));
        
        // Call subclass-specific standardization
        standardizeSpecificFields(request);
        
        return request;
    }
    
    /**
     * Template method for subclasses to implement
     * their specific field standardization logic
     */
    protected void standardizeSpecificFields(T request) {
        // Default implementation - do nothing
        // Subclasses can override this to add their specific standardization logic
    }
    
    /**
     * Template method for subclasses to specify
     * their default sort field if different from "createdDate"
     */
    protected String getDefaultSortField() {
        return "createdDate";
    }
    
    protected static String trimAndNullify(String str) {
        return (str != null && !str.trim().isEmpty()) ? str.trim() : null;
    }
    
    protected static String standardizeEnumField(String field) {
        return (field != null) ? field.trim().toUpperCase() : null;
    }
    
    protected static String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;
        String cleanPhone = phoneNumber.replaceAll("[^0-9+]", "");
        return cleanPhone.length() >= 10 ? cleanPhone : null;
    }
}
