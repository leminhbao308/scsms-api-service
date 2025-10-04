package com.kltn.scsms_api_service.core.dto.promotionTypeManagement.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePromotionTypeRequest {
    
    @Size(max = 50, message = "Type code must not exceed 50 characters")
    private String typeCode;
    
    @Size(max = 150, message = "Type name must not exceed 150 characters")
    private String typeName;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
