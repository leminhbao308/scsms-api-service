package com.kltn.scsms_api_service.core.dto.productManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkUpdateProductAttributeValuesRequest {
    
    @NotEmpty(message = "Attribute values list cannot be empty")
    @Valid
    @JsonProperty("attribute_values")
    private List<ProductAttributeValueUpdateRequest> attributeValues;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductAttributeValueUpdateRequest {
        
        @NotNull(message = "Attribute ID is required")
        @JsonProperty("attribute_id")
        private UUID attributeId;
        
        @JsonProperty("value_text")
        private String valueText;
        
        @JsonProperty("value_number")
        private BigDecimal valueNumber;
        
        @JsonProperty("operation")
        private String operation; // "CREATE", "UPDATE", hoặc "DELETE"
    }
}
