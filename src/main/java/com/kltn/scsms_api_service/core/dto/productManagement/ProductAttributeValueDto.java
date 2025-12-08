package com.kltn.scsms_api_service.core.dto.productManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductAttributeValueDto {
    
    @JsonProperty("product_id")
    private UUID productId;
    
    @JsonProperty("attribute_id")
    private UUID attributeId;
    
    @JsonProperty("attribute_name")
    private String attributeName;
    
    @JsonProperty("attribute_code")
    private String attributeCode;
    
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("data_type")
    private String dataType;
    
    @JsonProperty("value_text")
    private String valueText;
    
    @JsonProperty("value_number")
    private BigDecimal valueNumber;
    
    @JsonProperty("display_value")
    private String displayValue;
}
