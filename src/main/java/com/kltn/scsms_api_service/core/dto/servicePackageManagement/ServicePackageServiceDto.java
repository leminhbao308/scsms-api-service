package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageServiceDto {
    
    private UUID servicePackageServiceId;
    
    private UUID packageId;
    
    private UUID serviceId;
    
    private String serviceName;
    
    private String serviceUrl;
    
    private String serviceDescription;
    
    private Integer serviceStandardDuration;
    
    private BigDecimal serviceBasePrice;
    
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private BigDecimal totalPrice;
    
    private String notes;
    
    private Boolean isRequired;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
