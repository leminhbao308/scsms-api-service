package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request để hoàn thành dịch vụ sớm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteEarlyRequest {
    
    /**
     * ID của booking
     */
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;
    
    /**
     * Thời gian hoàn thành thực tế
     */
    @NotNull(message = "Actual completion time is required")
    private LocalDateTime actualCompletionTime;
    
    /**
     * Ghi chú
     */
    private String notes;
}
