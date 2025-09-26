package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private BigDecimal packagePrice;
    private BigDecimal originalPrice;
    private BigDecimal discountPercentage;
    private BigDecimal savingsAmount;
    private ServicePackage.PackageType packageType;
    private String targetVehicleTypes;
    private Integer validityPeriodDays;
    private Integer maxUsageCount;
    private Boolean isLimitedTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isPopular;
    private Boolean isRecommended;
    private String imageUrls;
    private String tags;
    private Integer sortOrder;
    private String termsAndConditions;
    private Boolean isActive;
    private List<ServicePackageStepInfoDto> packageSteps;
    private AuditDto audit;
}