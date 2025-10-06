package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

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
    
    @NotNull(message = "Item type is required")
    private BookingItem.ItemType itemType;
    
    @NotNull(message = "Item ID is required")
    private UUID itemId;
    
    @NotNull(message = "Item name is required")
    private String itemName;
    
    private String itemUrl;
    private String itemDescription;
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    @Builder.Default
    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;
    
    @NotNull(message = "Duration minutes is required")
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;
    
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    private String notes;
    
    @Builder.Default
    private Integer displayOrder = 1;
}
