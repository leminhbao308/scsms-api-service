package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ServicePackageServiceDto {
    
    @JsonProperty("service_package_service_id")
    private UUID servicePackageServiceId;
    
    @JsonProperty("package_id")
    private UUID packageId;
    
    @JsonProperty("service_id")
    private UUID serviceId;
    
    @JsonProperty("service_name")
    private String serviceName;
    
    @JsonProperty("service_url")
    private String serviceUrl;
    
    @JsonProperty("service_description")
    private String serviceDescription;
    
    @JsonProperty("service_standard_duration")
    private Integer serviceStandardDuration;
    
    @JsonProperty("service_base_price")
    private BigDecimal serviceBasePrice;
    
    private Integer quantity;
    
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    
    private String notes;
    
    @JsonProperty("is_required")
    private Boolean isRequired;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
