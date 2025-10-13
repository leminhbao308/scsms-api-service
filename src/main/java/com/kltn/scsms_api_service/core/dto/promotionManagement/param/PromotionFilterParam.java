package com.kltn.scsms_api_service.core.dto.promotionManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.PromotionLine;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromotionFilterParam extends BaseFilterParam<PromotionFilterParam> {
    
    // === PROMOTION-SPECIFIC FILTERS ===
    
    @JsonProperty("promotion_code")
    @Size(max = 100, message = "Promotion code must not exceed 100 characters")
    private String promotionCode;
    
    @JsonProperty("name")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @JsonProperty("promotion_type_id")
    private UUID promotionTypeId;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("is_stackable")
    private Boolean isStackable;
    
    @JsonProperty("coupon_redeem_once")
    private Boolean couponRedeemOnce;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    // Manual getter/setter for isActive (Lombok issue)
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Manual getter/setter for isExpired (Lombok issue)
    public Boolean getIsExpired() {
        return isExpired;
    }
    
    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }
    
    // === DISCOUNT VALUE RANGE ===
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Min discount value must be greater than or equal to 0")
    @JsonProperty("min_discount_value")
    private BigDecimal minDiscountValue;
    
    @DecimalMax(value = "999999999.99", inclusive = true, message = "Max discount value must be less than or equal to 999999999.99")
    @JsonProperty("max_discount_value")
    private BigDecimal maxDiscountValue;
    
    // === ORDER AMOUNT RANGE ===
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Min order amount must be greater than or equal to 0")
    @JsonProperty("min_order_amount")
    private BigDecimal minOrderAmount;
    
    @DecimalMax(value = "999999999.99", inclusive = true, message = "Max order amount must be less than or equal to 999999999.99")
    @JsonProperty("max_order_amount")
    private BigDecimal maxOrderAmount;
    
    // === USAGE LIMITS ===
    
    @JsonProperty("has_usage_limit")
    private Boolean hasUsageLimit;
    
    @JsonProperty("usage_limit_exceeded")
    private Boolean usageLimitExceeded;
    
    @JsonProperty("min_usage_count")
    private Integer minUsageCount;
    
    @JsonProperty("max_usage_count")
    private Integer maxUsageCount;
    
    // === DATE RANGES ===
    
    @JsonProperty("start_at_from")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startAtFrom;
    
    @JsonProperty("start_at_to")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startAtTo;
    
    @JsonProperty("end_at_from")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endAtFrom;
    
    @JsonProperty("end_at_to")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endAtTo;
    
    // === STATUS FILTERS ===
    
    @JsonProperty("is_expired")
    private Boolean isExpired;
    
    @JsonProperty("is_available")
    private Boolean isAvailable;
    
    @JsonProperty("is_starting_soon")
    private Boolean isStartingSoon;
    
    @JsonProperty("is_ending_soon")
    private Boolean isEndingSoon;
    
    // === TARGET FILTERS ===
    
    @JsonProperty("target_customer_rank")
    @Size(max = 50, message = "Target customer rank must not exceed 50 characters")
    private String targetCustomerRank;
    
    @JsonProperty("target_vehicle_type")
    @Size(max = 50, message = "Target vehicle type must not exceed 50 characters")
    private String targetVehicleType;
    
    @JsonProperty("target_service_id")
    private UUID targetServiceId;
    
    @JsonProperty("target_product_id")
    private UUID targetProductId;
    
    @JsonProperty("target_branch_id")
    private UUID targetBranchId;
    
    // === FREE ITEM FILTERS ===
    
    @JsonProperty("has_free_item")
    private Boolean hasFreeItem;
    
    @JsonProperty("free_product_id")
    private UUID freeProductId;
    
    @JsonProperty("free_service_id")
    private UUID freeServiceId;
    
    // === BUY X GET Y FILTERS ===
    
    @JsonProperty("has_buy_x_get_y")
    private Boolean hasBuyXGetY;
    
    @JsonProperty("buy_product_id")
    private UUID buyProductId;
    
    @JsonProperty("get_product_id")
    private UUID getProductId;
    
    // === PRIORITY FILTERS ===
    
    @JsonProperty("min_priority")
    private Integer minPriority;
    
    @JsonProperty("max_priority")
    private Integer maxPriority;
    
    // === SEARCH FILTERS ===
    
    @JsonProperty("search")
    @Size(max = 255, message = "Search term must not exceed 255 characters")
    private String search;
    
    @JsonProperty("description")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    // === STATISTICAL FILTERS ===
    
    @JsonProperty("most_used")
    private Boolean mostUsed;
    
    @JsonProperty("least_used")
    private Boolean leastUsed;
    
    @JsonProperty("never_used")
    private Boolean neverUsed;
    
    // === STANDARDIZATION METHODS ===
    
    public static PromotionFilterParam standardize(PromotionFilterParam filterParam) {
        return filterParam.standardizeFilterRequest(filterParam);
    }
    
    @Override
    protected String getDefaultSortField() {
        return "createdDate"; // Promotion specific default sort field
    }
    
    @Override
    protected void standardizeSpecificFields(PromotionFilterParam request) {
        // Chuẩn hóa search terms
        request.setPromotionCode(trimAndNullify(request.getPromotionCode()));
        // Note: name, description, search fields will be handled by Lombok getters/setters
        
        // Chuẩn hóa numeric fields (no standardization needed for BigDecimal)
        // request.setMinDiscountValue(request.getMinDiscountValue());
        // request.setMaxDiscountValue(request.getMaxDiscountValue());
        // request.setMinOrderAmount(request.getMinOrderAmount());
        // request.setMaxOrderAmount(request.getMaxOrderAmount());
        
        // Chuẩn hóa integer fields (no standardization needed for Integer)
        // request.setMinUsageCount(request.getMinUsageCount());
        // request.setMaxUsageCount(request.getMaxUsageCount());
        // request.setMinPriority(request.getMinPriority());
        // request.setMaxPriority(request.getMaxPriority());
        
        // Chuẩn hóa date fields (no standardization needed for LocalDateTime)
        // request.setStartDateFrom(request.getStartDateFrom());
        // request.setStartDateTo(request.getStartDateTo());
        // request.setEndDateFrom(request.getEndDateFrom());
        // request.setEndDateTo(request.getEndDateTo());
    }
    
    // === HELPER METHODS ===
    
    /**
     * Check if filter has any date range filters
     */
    public boolean hasDateRangeFilters() {
        return startAtFrom != null || startAtTo != null ||
               endAtFrom != null || endAtTo != null;
    }
    
    /**
     * Check if filter has any value range filters
     */
    public boolean hasValueRangeFilters() {
        return minDiscountValue != null || maxDiscountValue != null ||
               minOrderAmount != null || maxOrderAmount != null;
    }
    
    /**
     * Check if filter has any usage filters
     */
    public boolean hasUsageFilters() {
        return hasUsageLimit != null || usageLimitExceeded != null ||
               minUsageCount != null || maxUsageCount != null;
    }
    
    /**
     * Check if filter has any target filters
     */
    public boolean hasTargetFilters() {
        return targetCustomerRank != null || targetVehicleType != null ||
               targetServiceId != null || targetProductId != null || targetBranchId != null;
    }
    
    /**
     * Check if filter has any free item filters
     */
    public boolean hasFreeItemFilters() {
        return hasFreeItem != null || freeProductId != null || freeServiceId != null;
    }
    
    /**
     * Check if filter has any buy X get Y filters
     */
    public boolean hasBuyXGetYFilters() {
        return hasBuyXGetY != null || buyProductId != null || getProductId != null;
    }
}
