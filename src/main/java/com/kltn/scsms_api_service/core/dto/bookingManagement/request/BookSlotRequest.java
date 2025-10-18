package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request để đặt slot cho booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSlotRequest {
    
    /**
     * ID của bay
     */
    @NotNull(message = "Bay ID is required")
    private UUID bayId;
    
    /**
     * Ngày đặt
     */
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    /**
     * Giờ bắt đầu slot
     */
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    /**
     * ID của booking
     */
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;
    
    /**
     * Thời gian dịch vụ (phút)
     */
    @NotNull(message = "Service duration is required")
    private Integer serviceDurationMinutes;
    
    /**
     * Ghi chú
     */
    private String notes;
}
