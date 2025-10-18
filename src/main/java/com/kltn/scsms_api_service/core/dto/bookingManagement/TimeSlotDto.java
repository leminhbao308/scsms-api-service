package com.kltn.scsms_api_service.core.dto.bookingManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO cho thông tin slot thời gian có thể đặt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDto {
    
    /**
     * ID của bay
     */
    private UUID bayId;
    
    /**
     * Tên bay
     */
    private String bayName;
    
    /**
     * Mã bay
     */
    private String bayCode;
    
    /**
     * Giờ bắt đầu slot
     */
    private LocalTime startTime;
    
    /**
     * Giờ kết thúc slot
     */
    private LocalTime endTime;
    
    /**
     * Thời gian kết thúc dự kiến (bao gồm dịch vụ + buffer)
     */
    private LocalTime estimatedEndTime;
    
    /**
     * Trạng thái slot
     */
    private String status;
    
    /**
     * Ghi chú
     */
    private String notes;
    
    /**
     * Có thể đặt không
     */
    private Boolean isAvailable;
    
    /**
     * Số phút slot
     */
    private Integer durationMinutes;
}
