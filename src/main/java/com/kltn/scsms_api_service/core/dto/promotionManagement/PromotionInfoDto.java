package com.kltn.scsms_api_service.core.dto.promotionManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.Promotion;
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
    
    @JsonProperty("promotion_name")
    private String promotionName;
    
    @JsonProperty("promotion_type")
    private String promotionType;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("category")
    private CategoryFlatDto category;
    
    @JsonProperty("discount_type")
    private Promotion.DiscountType discountType;
    
    @JsonProperty("discount_value")
    private BigDecimal discountValue;
    
    @JsonProperty("max_discount_amount")
    private BigDecimal maxDiscountAmount;
    
    @JsonProperty("min_order_amount")
    private BigDecimal minOrderAmount;
    
    @JsonProperty("start_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    
    @JsonProperty("end_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    
    @JsonProperty("usage_limit")
    private Integer usageLimit;
    
    @JsonProperty("used_count")
    private Integer usedCount;
    
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
    
    @JsonProperty("free_item_quantity")
    private Integer freeItemQuantity;
    
    @JsonProperty("free_product")
    private ProductFlatDto freeProduct;
    
    @JsonProperty("free_service")
    private ServiceFlatDto freeService;
    
    @JsonProperty("buy_quantity")
    private Integer buyQuantity;
    
    @JsonProperty("get_quantity")
    private Integer getQuantity;
    
    @JsonProperty("buy_product")
    private ProductFlatDto buyProduct;
    
    @JsonProperty("get_product")
    private ProductFlatDto getProduct;
    
    @JsonProperty("is_visible")
    private Boolean isVisible;
    
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
    
    @JsonProperty("promotion_lines")
    private List<PromotionLineDto> promotionLines;
    
    @JsonProperty("total_usage_count")
    private Long totalUsageCount;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
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
        @JsonProperty("line_id")
        private UUID lineId;
        
        @JsonProperty("target_vehicle_types")
        private List<String> targetVehicleTypes;
        
        @JsonProperty("target_services")
        private List<UUID> targetServices;
        
        @JsonProperty("target_products")
        private List<UUID> targetProducts;
        
        @JsonProperty("required_quantity")
        private Integer requiredQuantity;
        
        @JsonProperty("required_amount")
        private BigDecimal requiredAmount;
        
        @JsonProperty("item_id")
        private UUID itemId;
        
        @JsonProperty("item_type")
        private String itemType;
    }
}
