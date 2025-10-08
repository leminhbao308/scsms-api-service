package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO để tạo booking item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingItemRequest {
    
    // @NotNull(message = "Item type is required")
    @JsonProperty("item_type")
    private BookingItem.ItemType itemType;
    
    // @NotNull(message = "Item ID is required")
    @JsonProperty("item_id")
    private UUID itemId;
    
    // @NotNull(message = "Item name is required")
    @JsonProperty("item_name")
    private String itemName;
    
    @JsonProperty("item_url")
    private String itemUrl;
    @JsonProperty("item_description")
    private String itemDescription;
    
    // @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    @Builder.Default
    @Positive(message = "Quantity must be positive")
    @JsonProperty("quantity")
    private Integer quantity = 1;
    
    // @NotNull(message = "Duration minutes is required")
    @Positive(message = "Duration must be positive")
    @JsonProperty("duration_minutes")
    private Integer durationMinutes;
    
    @Builder.Default
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Builder.Default
    @JsonProperty("tax_amount")
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @JsonProperty("notes")
    private String notes;
    
    @Builder.Default
    @JsonProperty("display_order")
    private Integer displayOrder = 1;
}
