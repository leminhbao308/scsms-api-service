package com.kltn.scsms_api_service.core.dto.servicePackageManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateServicePackageRequest {
    
    @NotBlank(message = "Package name is required")
    @Size(min = 2, max = 500, message = "Package name must be between 2 and 500 characters")
    @JsonProperty("package_name")
    private String packageName;
    
    @NotBlank(message = "Package URL is required")
    @Size(max = 1000, message = "Package URL must not exceed 1000 characters")
    @JsonProperty("package_url")
    private String packageUrl;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @JsonProperty("description")
    private String description;
    
    @Min(value = 1, message = "Total duration must be at least 1 minute")
    @JsonProperty("total_duration")
    private Integer totalDuration;
    
    @DecimalMin(value = "0.0", message = "Package price must be non-negative")
    @JsonProperty("package_price")
    private BigDecimal packagePrice;
    
    @DecimalMin(value = "0.0", message = "Original price must be non-negative")
    @JsonProperty("original_price")
    private BigDecimal originalPrice;
    
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100%")
    @JsonProperty("discount_percentage")
    private BigDecimal discountPercentage;
    
    @DecimalMin(value = "0.0", message = "Savings amount must be non-negative")
    @JsonProperty("savings_amount")
    private BigDecimal savingsAmount;
    
    @JsonProperty("package_type")
    private ServicePackage.PackageType packageType;
    
    @JsonProperty("target_vehicle_types")
    private String targetVehicleTypes;
    
    @Min(value = 1, message = "Validity period must be at least 1 day")
    @JsonProperty("validity_period_days")
    private Integer validityPeriodDays;
    
    @Min(value = 1, message = "Maximum usage count must be at least 1")
    @JsonProperty("max_usage_count")
    private Integer maxUsageCount;
    
    @JsonProperty("is_limited_time")
    private Boolean isLimitedTime;
    
    @JsonProperty("start_date")
    private LocalDate startDate;
    
    @JsonProperty("end_date")
    private LocalDate endDate;
    
    @JsonProperty("is_popular")
    private Boolean isPopular;
    
    @JsonProperty("is_recommended")
    private Boolean isRecommended;
    
    @JsonProperty("image_urls")
    private String imageUrls;
    
    @JsonProperty("tags")
    private String tags;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @JsonProperty("sort_order")
    private Integer sortOrder;
    
    @Size(max = 2000, message = "Terms and conditions must not exceed 2000 characters")
    @JsonProperty("terms_and_conditions")
    private String termsAndConditions;
}
