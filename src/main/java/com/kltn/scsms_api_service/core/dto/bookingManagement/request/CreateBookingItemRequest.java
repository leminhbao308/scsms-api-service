package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO cho booking item
 * Giá cả được lấy từ price book, không cho phép client truyền vào
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingItemRequest {
    
    @JsonProperty("service_id")
    private UUID serviceId;
    
    @JsonProperty("item_name")
    private String itemName;
    
    @JsonProperty("item_description")
    private String itemDescription;
    
    // Removed: quantity - services are always quantity 1
    // Removed: unit_price - always fetched from price book
    // Removed: total_amount - calculated from price book
    
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;
    
    @JsonProperty("tax_amount")
    private BigDecimal taxAmount;
}