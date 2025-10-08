package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO cho thông tin pricing chi tiết của ServicePackage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackagePricingDto {
    private UUID packageId;
    private String packageName;
    private String packageUrl;

    private BigDecimal packagePrice; // Tổng chi phí từ các service con (tự động tính)
    private BigDecimal finalPrice; // packagePrice + markup (nếu có)

    private LocalDateTime lastPriceCalculatedAt; // Thời gian cập nhật giá cuối cùng (modifiedDate của ServicePackage)
    private UUID priceBookId;
    private String priceBookName;

    private List<ServiceInPackagePricingDto> services;

    // Summary stats
    private BigDecimal totalServiceCosts;
    private Integer totalServices;
    private Integer totalDuration;
}
