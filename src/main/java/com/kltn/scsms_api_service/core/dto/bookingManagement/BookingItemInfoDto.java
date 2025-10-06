package com.kltn.scsms_api_service.core.dto.bookingManagement;

import com.kltn.scsms_api_service.core.entity.BookingItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO chứa thông tin chi tiết của booking item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemInfoDto {
    
    private UUID bookingItemId;
    private UUID bookingId;
    
    // Item information
    private BookingItem.ItemType itemType;
    private UUID itemId;
    private String itemName;
    private String itemUrl;
    private String itemDescription;
    
    // Pricing information
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    
    // Duration information
    private Integer durationMinutes;
    private Long actualDurationMinutes;
    
    // Status and timing
    private BookingItem.ItemStatus itemStatus;
    private LocalDateTime actualStartAt;
    private LocalDateTime actualEndAt;
    
    // Additional information
    private String notes;
    private Integer displayOrder;
    
    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String modifiedBy;
    
    // Computed fields
    private Boolean isCompleted;
    private Boolean isInProgress;
}
