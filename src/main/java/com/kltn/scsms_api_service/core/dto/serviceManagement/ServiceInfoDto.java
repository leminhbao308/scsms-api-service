package com.kltn.scsms_api_service.core.dto.serviceManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.Service;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceInfoDto {
    
    private UUID serviceId;
    private String serviceUrl;
    private String serviceName;
    private UUID categoryId;
    private String categoryName;
    private String description;
    private Integer standardDuration;
    private Service.SkillLevel requiredSkillLevel;
    private Boolean isPackage;
    private BigDecimal basePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Service.ComplexityLevel complexityLevel;
    private Service.ServiceType serviceType;
    private String vehicleTypes;
    private String requiredTools;
    private String safetyNotes;
    private String qualityCriteria;
    private Boolean photoRequired;
    private Boolean customerApprovalRequired;
    private Boolean isExpressService;
    private Boolean isPremiumService;
    private String imageUrls;
    private String tags;
    private Integer sortOrder;
    private Boolean isFeatured;
    private Boolean isActive;
    private AuditDto audit;
}