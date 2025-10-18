package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request để lấy các slot available
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotsRequest {
    
    /**
     * ID của chi nhánh
     */
    @NotNull(message = "Branch ID is required")
    private UUID branchId;
    
    /**
     * Ngày muốn đặt
     */
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    /**
     * Thời gian dịch vụ (phút)
     */
    @NotNull(message = "Service duration is required")
    private Integer serviceDurationMinutes;
    
    /**
     * ID của bay cụ thể (optional)
     */
    private UUID bayId;
    
    /**
     * Chỉ lấy slot từ giờ này trở đi (optional)
     */
    private Integer fromHour;
    
    /**
     * Chỉ lấy slot đến giờ này (optional)
     */
    private Integer toHour;
}
