package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("package_id")
    private UUID packageId;
    
    @JsonProperty("package_name")
    private String packageName;
    
    @JsonProperty("package_url")
    private String packageUrl;

    @JsonProperty("package_price")
    private BigDecimal packagePrice; // Tổng chi phí từ các service con (tự động tính)
    
    @JsonProperty("final_price")
    private BigDecimal finalPrice; // packagePrice + markup (nếu có)

    @JsonProperty("last_price_calculated_at")
    private LocalDateTime lastPriceCalculatedAt; // Thời gian cập nhật giá cuối cùng (modifiedDate của ServicePackage)
    
    @JsonProperty("price_book_id")
    private UUID priceBookId;
    
    @JsonProperty("price_book_name")
    private String priceBookName;

    private List<ServiceInPackagePricingDto> services;

    // Summary stats
    @JsonProperty("total_service_costs")
    private BigDecimal totalServiceCosts;
    
    @JsonProperty("total_services")
    private Integer totalServices;
    
    @JsonProperty("total_duration")
    private Integer totalDuration;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("branch_name")
    private String branchName;
}
