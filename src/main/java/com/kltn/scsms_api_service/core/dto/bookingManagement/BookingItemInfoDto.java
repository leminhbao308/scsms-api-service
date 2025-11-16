package com.kltn.scsms_api_service.core.dto.bookingManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO chứa thông tin chi tiết của booking item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemInfoDto {

    @JsonProperty("booking_item_id")
    private UUID bookingItemId;
    private UUID bookingId;

    // Service information
    @JsonProperty("service_id")
    private UUID serviceId;
    
    @JsonProperty("service_name")
    private String serviceName;
    
    @JsonProperty("service_description")
    private String serviceDescription;

    // Pricing information
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    // Duration information
    @JsonProperty("duration_minutes")
    private Integer durationMinutes;

    // Status
    @JsonProperty("item_status")
    private BookingItem.ItemStatus itemStatus;

    // Additional information
    private String notes;
    @JsonProperty("display_order")
    private Integer displayOrder;

    // Computed fields
    @JsonProperty("is_completed")
    private Boolean isCompleted;
    @JsonProperty("is_in_progress")
    private Boolean isInProgress;
}
