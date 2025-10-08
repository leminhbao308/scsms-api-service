package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

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
    private UUID serviceId;
    private String serviceName;
    private String serviceUrl;

    private Integer quantity; // Số lượng service trong package
    private BigDecimal unitPrice; // Giá đơn vị của service (basePrice + laborCost)
    private BigDecimal totalPrice; // unitPrice * quantity

    // Chi tiết giá service
    private BigDecimal serviceBasePrice; // basePrice của service
    private BigDecimal serviceLaborCost; // laborCost của service
    private BigDecimal serviceTotalPrice; // basePrice + laborCost

    private Boolean isRequired; // Service có bắt buộc trong package không
    private String notes; // Ghi chú
}
