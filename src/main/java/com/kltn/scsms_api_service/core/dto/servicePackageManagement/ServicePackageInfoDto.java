package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("package_id")
    private UUID packageId;
    
    @JsonProperty("package_url")
    private String packageUrl;
    
    @JsonProperty("package_name")
    private String packageName;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("category_name")
    private String categoryName;
    
    private String description;
    
    @JsonProperty("total_duration")
    private Integer totalDuration;
    
    @JsonProperty("package_price")
    private BigDecimal packagePrice; // Total price = sum of service prices
    
    @JsonProperty("service_cost")
    private BigDecimal serviceCost; // Sum of service prices
    
    @JsonProperty("service_package_type_id")
    private UUID servicePackageTypeId;
    
    @JsonProperty("service_package_type_name")
    private String servicePackageTypeName;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("package_services")
    private List<ServicePackageServiceDto> packageServices;
    
    @JsonProperty("service_process_id")
    private UUID serviceProcessId;
    
    @JsonProperty("service_process_name")
    private String serviceProcessName;
    
    @JsonProperty("service_process_code")
    private String serviceProcessCode;
    
    @JsonProperty("is_default_process")
    private Boolean isDefaultProcess;
    
    @JsonProperty("service_count")
    private Integer serviceCount;
    
    private AuditDto audit;
}