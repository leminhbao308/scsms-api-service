package com.kltn.scsms_api_service.core.dto.productTypeManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProductTypeRequest {
    
    @Size(max = 255, message = "Product type name must not exceed 255 characters")
    @JsonProperty("product_type_name")
    private String productTypeName;
    
    @Size(max = 50, message = "Product type code must not exceed 50 characters")
    @JsonProperty("product_type_code")
    private String productTypeCode;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("category_id")
    private UUID categoryId;
}
