package com.kltn.scsms_api_service.core.dto.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO cho WebSocket booking event messages
 * Chứa thông tin chi tiết về booking event để frontend có thể update smart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEventDto {

    /**
     * Loại event: CREATED, CONFIRMED, CANCELLED, CHECKED_IN, STARTED, COMPLETED, UPDATED
     */
    @JsonProperty("event_type")
    private BookingEventType eventType;

    /**
     * Booking ID
     */
    @JsonProperty("booking_id")
    private UUID bookingId;

    /**
     * Booking code (hiển thị cho user)
     */
    @JsonProperty("booking_code")
    private String bookingCode;

    /**
     * Thông tin booking đầy đủ (optional - để frontend update trực tiếp)
     * Nếu null, frontend cần fetch booking mới
     */
    @JsonProperty("booking_data")
    private BookingInfoDto bookingData;

    /**
     * Timestamp của event
     */
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Message thân thiện với user (optional)
     * Ví dụ: "Booking #BK-20251007-0001 đã được xác nhận"
     */
    @JsonProperty("message")
    private String message;

    /**
     * Enum cho các loại booking events
     */
    public enum BookingEventType {
        CREATED,      // Booking mới được tạo
        CONFIRMED,    // Booking được xác nhận (PENDING → CONFIRMED)
        CANCELLED,    // Booking bị hủy
        CHECKED_IN,   // Booking được check-in (CONFIRMED → CHECKED_IN)
        STARTED,      // Dịch vụ bắt đầu (CHECKED_IN → IN_PROGRESS)
        COMPLETED,    // Dịch vụ hoàn thành (IN_PROGRESS → COMPLETED)
        UPDATED       // Booking được cập nhật (thông tin, schedule, items, etc.)
    }
}

