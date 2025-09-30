package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePackageInfoDto {
    
    private UUID packageId;
    private String packageUrl;
    private String packageName;
    private UUID categoryId;
    private String categoryName;
    private String description;
    private Integer totalDuration;
    private BigDecimal packagePrice; // Total price = sum of service prices + sum of product prices
    private BigDecimal serviceCost; // Sum of service prices
    private BigDecimal productCost; // Sum of product prices
    private ServicePackage.PackageType packageType;
    private String imageUrls;
    private Boolean isActive;
    private List<ServicePackageProductDto> packageProducts;
    private List<ServicePackageServiceDto> packageServices;
    private AuditDto audit;
}