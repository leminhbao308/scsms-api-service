package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
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
    private BigDecimal packagePrice; // Total price = sum of service prices
    private BigDecimal serviceCost; // Sum of service prices
    private UUID servicePackageTypeId;
    private String servicePackageTypeName;
    private Boolean isActive;
    private List<ServicePackageServiceDto> packageServices;
    private UUID serviceProcessId;
    private String serviceProcessName;
    private String serviceProcessCode;
    private Boolean isDefaultProcess;
    private Integer serviceCount;
    private AuditDto audit;
}