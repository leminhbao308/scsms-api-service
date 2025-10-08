package com.kltn.scsms_api_service.core.dto.serviceBayManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO chứa thông tin tính khả dụng của service bay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBayAvailabilityDto {
    
    private UUID bayId;
    private String bayName;
    private String bayCode;
    private String bayType;
    private Boolean isAvailable;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<BookingConflictDto> conflicts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingConflictDto {
        private UUID bookingId;
        private String bookingCode;
        private String customerName;
        private LocalDateTime scheduledStartAt;
        private LocalDateTime scheduledEndAt;
        private String status;
    }
}
