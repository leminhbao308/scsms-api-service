package com.kltn.scsms_api_service.core.dto.serviceManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProductDto {
    
    private UUID serviceProductId;
    private UUID serviceId;
    private UUID productId;
    private String productName;
    private String productUrl;
    private String productSku;
    private String productBrand;
    private String productModel;
    private String unitOfMeasure;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String notes;
    private Boolean isRequired;
    private Boolean isActive;
    private AuditDto audit;
}
