package com.kltn.scsms_api_service.core.dto.promotionManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.branchManagement.BranchFlatDto;
import com.kltn.scsms_api_service.core.dto.promotionTypeManagement.PromotionTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.PromotionLine;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromotionInfoDto extends AuditDto {
    
    @JsonProperty("promotion_id")
    private UUID promotionId;
    
    @JsonProperty("promotion_code")
    private String promotionCode;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("promotion_type")
    private PromotionTypeInfoDto promotionType;
    
    @JsonProperty("start_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startAt;
    
    @JsonProperty("end_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endAt;
    
    @JsonProperty("usage_limit")
    private Long usageLimit;
    
    @JsonProperty("per_customer_limit")
    private Long perCustomerLimit;
    
    @JsonProperty("priority")
    private Integer priority;
    
    @JsonProperty("is_stackable")
    private Boolean isStackable;
    
    @JsonProperty("coupon_redeem_once")
    private Boolean couponRedeemOnce;
    
    @JsonProperty("branch")
    private BranchFlatDto branch;
    
    
    @JsonProperty("promotion_lines")
    private List<PromotionLineDto> promotionLines;
    
    @JsonProperty("total_usage_count")
    private Long totalUsageCount;
    
    @JsonProperty("is_expired")
    private Boolean isExpired;
    
    @JsonProperty("is_available")
    private Boolean isAvailable;
    
    // === NESTED DTOs ===
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryFlatDto {
        @JsonProperty("category_id")
        private UUID categoryId;
        
        @JsonProperty("category_name")
        private String categoryName;
        
        @JsonProperty("category_url")
        private String categoryUrl;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductFlatDto {
        @JsonProperty("product_id")
        private UUID productId;
        
        @JsonProperty("product_name")
        private String productName;
        
        @JsonProperty("product_url")
        private String productUrl;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceFlatDto {
        @JsonProperty("service_id")
        private UUID serviceId;
        
        @JsonProperty("service_name")
        private String serviceName;
        
        @JsonProperty("service_url")
        private String serviceUrl;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PromotionLineDto {
        @JsonProperty("promotion_line_id")
        private UUID promotionLineId;
        
        @JsonProperty("line_type")
        private PromotionLine.LineType lineType;
        
        @JsonProperty("target_id")
        private UUID targetId;
        
        @JsonProperty("branch")
        private BranchFlatDto branch;
        
        @JsonProperty("discount_type")
        private PromotionLine.DiscountType discountType;
        
        @JsonProperty("discount_value")
        private BigDecimal discountValue;
        
        @JsonProperty("max_discount_amount")
        private BigDecimal maxDiscountAmount;
        
        @JsonProperty("min_order_value")
        private BigDecimal minOrderValue;
        
        @JsonProperty("min_quantity")
        private Integer minQuantity;
        
        @JsonProperty("buy_qty")
        private Integer buyQty;
        
        @JsonProperty("get_qty")
        private Integer getQty;
        
        @JsonProperty("free_product")
        private ProductFlatDto freeProduct;
        
        @JsonProperty("free_quantity")
        private Integer freeQuantity;
        
        @JsonProperty("start_at")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime startAt;
        
        @JsonProperty("end_at")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime endAt;
        
        @JsonProperty("line_priority")
        private Integer linePriority;
        
        @JsonProperty("is_active")
        private Boolean isActive;
        
        @JsonProperty("created_at")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime createdAt;
    }
}
