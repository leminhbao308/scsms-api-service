package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO cho thông tin pricing của một Service trong ServicePackage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInPackagePricingDto {
    @JsonProperty("service_id")
    private UUID serviceId;
    
    @JsonProperty("service_name")
    private String serviceName;
    
    @JsonProperty("service_url")
    private String serviceUrl;

    private Integer quantity; // Số lượng service trong package
    
    @JsonProperty("unit_price")
    private BigDecimal unitPrice; // Giá đơn vị của service (basePrice + laborCost)
    
    @JsonProperty("total_price")
    private BigDecimal totalPrice; // unitPrice * quantity

    // Chi tiết giá service
    @JsonProperty("service_base_price")
    private BigDecimal serviceBasePrice; // basePrice của service
    
    @JsonProperty("service_labor_cost")
    private BigDecimal serviceLaborCost; // laborCost của service
    
    @JsonProperty("service_total_price")
    private BigDecimal serviceTotalPrice; // basePrice + laborCost

    @JsonProperty("is_required")
    private Boolean isRequired; // Service có bắt buộc trong package không
    
    private String notes; // Ghi chú
}
