package com.kltn.scsms_api_service.core.dto.promotionManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CreatePromotionRequest {
    
    @Size(max = 100, message = "Promotion code must not exceed 100 characters")
    @JsonProperty("promotion_code")
    private String promotionCode;
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @JsonProperty("name")
    private String name;
    
    @Size(max = Integer.MAX_VALUE, message = "Description must not exceed maximum length")
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("promotion_type_id")
    private UUID promotionTypeId;
    
    @JsonProperty("start_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startAt;
    
    @JsonProperty("end_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endAt;
    
    @Min(value = 1, message = "Usage limit must be at least 1")
    @JsonProperty("usage_limit")
    private Long usageLimit;
    
    @Min(value = 1, message = "Per customer limit must be at least 1")
    @JsonProperty("per_customer_limit")
    private Long perCustomerLimit;
    
    @Min(value = 0, message = "Priority must be at least 0")
    @JsonProperty("priority")
    private Integer priority = 100;
    
    @JsonProperty("is_stackable")
    private Boolean isStackable = false;
    
    @JsonProperty("coupon_redeem_once")
    private Boolean couponRedeemOnce = false;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("promotion_lines")
    private List<CreatePromotionLineRequest> promotionLines;
    
    // === VALIDATION METHODS ===
    
    @AssertTrue(message = "End date must be after start date")
    private boolean isValidDateRange() {
        return endAt == null || startAt == null || endAt.isAfter(startAt);
    }
}
