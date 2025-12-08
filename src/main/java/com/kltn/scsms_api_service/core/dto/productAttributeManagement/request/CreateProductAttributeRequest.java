package com.kltn.scsms_api_service.core.dto.productAttributeManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateProductAttributeRequest {
    
    @NotBlank(message = "Attribute name is required")
    @Size(max = 100, message = "Attribute name must not exceed 100 characters")
    @JsonProperty("attribute_name")
    private String attributeName;
    
    @NotBlank(message = "Attribute code is required")
    @Size(max = 50, message = "Attribute code must not exceed 50 characters")
    @JsonProperty("attribute_code")
    private String attributeCode;
    
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("is_required")
    @Builder.Default
    private Boolean isRequired = false;
    
    @JsonProperty("data_type")
    @Builder.Default
    private String dataType = "STRING";
    
    @JsonProperty("is_active")
    @Builder.Default
    private Boolean isActive = true;
}
