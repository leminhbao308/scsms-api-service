package com.kltn.scsms_api_service.core.dto.productManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class BulkUpdateProductAttributeValueRequest {
    
    @NotEmpty(message = "Product IDs list cannot be empty")
    @JsonProperty("product_ids")
    private List<UUID> productIds;
    
    @NotNull(message = "Attribute ID is required")
    @JsonProperty("attribute_id")
    private UUID attributeId;
    
    @JsonProperty("value_text")
    private String valueText;
    
    @JsonProperty("value_number")
    private BigDecimal valueNumber;
}
