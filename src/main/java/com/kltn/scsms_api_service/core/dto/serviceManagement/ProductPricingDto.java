package com.kltn.scsms_api_service.core.dto.serviceManagement;

import com.kltn.scsms_api_service.core.enums.PricingPolicyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO cho thông tin pricing của sản phẩm trong Service Process Step
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPricingDto {
    
    private UUID productId;
    private String productName;
    private String sku;
    private String productType;
    private String brand;
    private String model;
    
    // Quantity and unit information
    private BigDecimal quantity;
    private String unit;
    
    // Pricing information
    private BigDecimal unitPrice; // Giá đơn vị từ PriceBook
    private BigDecimal totalPrice; // unitPrice * quantity
    private PricingPolicyType policyType; // FIXED, MARKUP_ON_PEAK
    private String priceSource; // "PRICE_BOOK", "DEFAULT", "OVERRIDE"
    
    // Metadata
    private UUID priceBookId;
    private UUID priceBookItemId;
    private String priceBookName;
    private LocalDateTime priceCalculatedAt;
}
