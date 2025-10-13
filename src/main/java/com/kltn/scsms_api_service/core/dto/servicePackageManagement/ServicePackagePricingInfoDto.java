package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("package_id")
    private UUID packageId;
    
    @JsonProperty("package_name")
    private String packageName;

    @JsonProperty("current_package_price")
    private BigDecimal currentPackagePrice; // packagePrice hiện tại trong DB
    
    @JsonProperty("current_total_price")
    private BigDecimal currentTotalPrice; // currentPackagePrice + markup

    @JsonProperty("calculated_package_price")
    private BigDecimal calculatedPackagePrice; // packagePrice được tính toán lại
    
    @JsonProperty("calculated_total_price")
    private BigDecimal calculatedTotalPrice; // calculatedPackagePrice + markup

    @JsonProperty("needs_update")
    private Boolean needsUpdate; // Cần cập nhật giá không
    
    @JsonProperty("last_calculated_at")
    private LocalDateTime lastCalculatedAt; // Thời gian cập nhật cuối cùng (modifiedDate của ServicePackage)
    
    @JsonProperty("pricing_status")
    private String pricingStatus; // UP_TO_DATE, NEEDS_UPDATE, ERROR

    // Service Package Info
    @JsonProperty("total_services")
    private Integer totalServices;
    
    @JsonProperty("total_duration")
    private Integer totalDuration;
}
