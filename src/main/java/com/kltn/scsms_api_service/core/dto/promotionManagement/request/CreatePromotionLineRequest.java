package com.kltn.scsms_api_service.core.dto.promotionManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.PromotionLine;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePromotionLineRequest {
    
    @NotNull(message = "Line type is required")
    @JsonProperty("line_type")
    private PromotionLine.LineType lineType;
    
    @JsonProperty("target_id")
    private UUID targetId;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @NotNull(message = "Discount type is required")
    @JsonProperty("discount_type")
    private PromotionLine.DiscountType discountType;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    @JsonProperty("discount_value")
    private BigDecimal discountValue;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Max discount amount must be greater than or equal to 0")
    @JsonProperty("max_discount_amount")
    private BigDecimal maxDiscountAmount;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Min order value must be greater than or equal to 0")
    @JsonProperty("min_order_value")
    private BigDecimal minOrderValue;
    
    @Min(value = 1, message = "Min quantity must be at least 1")
    @JsonProperty("min_quantity")
    private Integer minQuantity;
    
    @Min(value = 1, message = "Buy quantity must be at least 1")
    @JsonProperty("buy_qty")
    private Integer buyQty;
    
    @Min(value = 1, message = "Get quantity must be at least 1")
    @JsonProperty("get_qty")
    private Integer getQty;
    
    @JsonProperty("free_product_id")
    private UUID freeProductId;
    
    @Min(value = 1, message = "Free quantity must be at least 1")
    @JsonProperty("free_quantity")
    private Integer freeQuantity = 1;
    
    @JsonProperty("start_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startAt;
    
    @JsonProperty("end_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endAt;
    
    @Min(value = 0, message = "Line priority must be at least 0")
    @JsonProperty("line_priority")
    private Integer linePriority = 100;
    
    @JsonProperty("is_active")
    private Boolean isActive = true;
    
    // === VALIDATION METHODS ===
    
    @AssertTrue(message = "End date must be after start date")
    private boolean isValidDateRange() {
        return endAt == null || startAt == null || endAt.isAfter(startAt);
    }
    
    @AssertTrue(message = "For percentage discount, discount value must be between 0 and 100")
    private boolean isValidPercentageDiscount() {
        if (discountType == PromotionLine.DiscountType.PERCENT) {
            return discountValue != null && 
                   discountValue.compareTo(BigDecimal.ZERO) > 0 && 
                   discountValue.compareTo(BigDecimal.valueOf(100)) <= 0;
        }
        return true;
    }
    
    @AssertTrue(message = "For buy X get Y, both buy_qty and get_qty must be specified")
    private boolean isValidBuyXGetY() {
        if (discountType == PromotionLine.DiscountType.BUY_X_GET_Y) {
            return buyQty != null && buyQty > 0 && getQty != null && getQty > 0;
        }
        return true;
    }
    
    @AssertTrue(message = "For free product, free_product_id must be specified")
    private boolean isValidFreeProduct() {
        if (discountType == PromotionLine.DiscountType.FREE_PRODUCT) {
            return freeProductId != null;
        }
        return true;
    }
}
