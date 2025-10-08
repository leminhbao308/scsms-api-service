package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO cho thông tin pricing tóm tắt của ServicePackage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackagePricingInfoDto {
    private UUID packageId;
    private String packageName;

    private BigDecimal currentPackagePrice; // packagePrice hiện tại trong DB
    private BigDecimal currentTotalPrice; // currentPackagePrice + markup

    private BigDecimal calculatedPackagePrice; // packagePrice được tính toán lại
    private BigDecimal calculatedTotalPrice; // calculatedPackagePrice + markup

    private Boolean needsUpdate; // Cần cập nhật giá không
    private LocalDateTime lastCalculatedAt; // Thời gian cập nhật cuối cùng (modifiedDate của ServicePackage)
    private String pricingStatus; // UP_TO_DATE, NEEDS_UPDATE, ERROR

    // Service Package Info
    private Integer totalServices;
    private Integer totalDuration;
}
