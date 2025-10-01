package com.kltn.scsms_api_service.core.dto.promotionManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.Promotion;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePromotionRequest {
    
    @Size(max = 50, message = "Promotion code must not exceed 50 characters")
    @JsonProperty("promotion_code")
    private String promotionCode;
    
    @Size(max = 255, message = "Promotion name must not exceed 255 characters")
    @JsonProperty("promotion_name")
    private String promotionName;
    
    @Size(max = 255, message = "Promotion type must not exceed 255 characters")
    @JsonProperty("promotion_type")
    private String promotionType;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("discount_type")
    private Promotion.DiscountType discountType;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    @JsonProperty("discount_value")
    private BigDecimal discountValue;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Max discount amount must be greater than or equal to 0")
    @JsonProperty("max_discount_amount")
    private BigDecimal maxDiscountAmount;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Min order amount must be greater than or equal to 0")
    @JsonProperty("min_order_amount")
    private BigDecimal minOrderAmount;
    
    @Future(message = "Start date must be in the future")
    @JsonProperty("start_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    
    @Future(message = "End date must be in the future")
    @JsonProperty("end_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    
    @Min(value = 1, message = "Usage limit must be at least 1")
    @JsonProperty("usage_limit")
    private Integer usageLimit;
    
    @Min(value = 1, message = "Usage limit per customer must be at least 1")
    @JsonProperty("usage_limit_per_customer")
    private Integer usageLimitPerCustomer;
    
    @JsonProperty("target_customer_ranks")
    private List<String> targetCustomerRanks;
    
    @JsonProperty("target_vehicle_types")
    private List<String> targetVehicleTypes;
    
    @JsonProperty("target_services")
    private List<UUID> targetServices;
    
    @JsonProperty("target_products")
    private List<UUID> targetProducts;
    
    @JsonProperty("target_branches")
    private List<UUID> targetBranches;
    
    @Min(value = 1, message = "Free item quantity must be at least 1")
    @JsonProperty("free_item_quantity")
    private Integer freeItemQuantity;
    
    @JsonProperty("free_product_id")
    private UUID freeProductId;
    
    @JsonProperty("free_service_id")
    private UUID freeServiceId;
    
    @Min(value = 1, message = "Buy quantity must be at least 1")
    @JsonProperty("buy_quantity")
    private Integer buyQuantity;
    
    @Min(value = 1, message = "Get quantity must be at least 1")
    @JsonProperty("get_quantity")
    private Integer getQuantity;
    
    @JsonProperty("buy_product_id")
    private UUID buyProductId;
    
    @JsonProperty("get_product_id")
    private UUID getProductId;
    
    @JsonProperty("is_visible")
    private Boolean isVisible;
    
    @Min(value = 0, message = "Priority must be at least 0")
    @JsonProperty("priority")
    private Integer priority;
    
    @JsonProperty("image_urls")
    private List<String> imageUrls;
    
    @JsonProperty("banner_url")
    private String bannerUrl;
    
    @JsonProperty("stackable")
    private Boolean stackable;
    
    @JsonProperty("auto_apply")
    private Boolean autoApply;
    
    @JsonProperty("require_coupon_code")
    private Boolean requireCouponCode;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    // === VALIDATION METHODS ===
    
    @AssertTrue(message = "End date must be after start date")
    private boolean isValidDateRange() {
        return endDate == null || startDate == null || endDate.isAfter(startDate);
    }
    
    @AssertTrue(message = "For percentage discount, discount value must be between 0 and 100")
    private boolean isValidPercentageDiscount() {
        if (discountType == Promotion.DiscountType.PERCENTAGE) {
            return discountValue != null && 
                   discountValue.compareTo(BigDecimal.ZERO) > 0 && 
                   discountValue.compareTo(BigDecimal.valueOf(100)) <= 0;
        }
        return true;
    }
    
    @AssertTrue(message = "For fixed amount discount, discount value must be greater than 0")
    private boolean isValidFixedAmountDiscount() {
        if (discountType == Promotion.DiscountType.FIXED_AMOUNT) {
            return discountValue != null && discountValue.compareTo(BigDecimal.ZERO) > 0;
        }
        return true;
    }
    
    @AssertTrue(message = "For free item promotion, free item quantity must be specified")
    private boolean isValidFreeItemPromotion() {
        if (promotionType != null && promotionType.toLowerCase().contains("free")) {
            return freeItemQuantity != null && freeItemQuantity > 0;
        }
        return true;
    }
    
    @AssertTrue(message = "For buy X get Y promotion, buy and get quantities must be specified")
    private boolean isValidBuyXGetYPromotion() {
        if (promotionType != null && promotionType.toLowerCase().contains("buy")) {
            return buyQuantity != null && buyQuantity > 0 && 
                   getQuantity != null && getQuantity > 0;
        }
        return true;
    }
}
