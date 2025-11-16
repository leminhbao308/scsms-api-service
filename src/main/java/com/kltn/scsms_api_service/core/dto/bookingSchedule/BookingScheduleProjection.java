package com.kltn.scsms_api_service.core.dto.bookingSchedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Projection DTO cho query schedule bookings
 * Chỉ lấy các field cần thiết để tính toán available time ranges
 * Tối ưu hiệu năng - giảm I/O và memory usage
 */
@Data
@AllArgsConstructor
public class BookingScheduleProjection {
    @JsonProperty("booking_id")
    private UUID bookingId;
    @JsonProperty("bay_id")
    private UUID bayId;
    @JsonProperty("branch_id")
    private UUID branchId;
    @JsonProperty("scheduled_start_at")
    private LocalDateTime scheduledStartAt;
    @JsonProperty("scheduled_end_at")
    private LocalDateTime scheduledEndAt;
    private Booking.BookingStatus status;
}
