package com.kltn.scsms_api_service.core.dto.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.ServiceProcessTrackingInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO cho WebSocket tracking event messages
 * Chứa thông tin chi tiết về tracking event để frontend có thể update smart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventDto {

    /**
     * Loại event: CREATED, STARTED, UPDATED, COMPLETED, CANCELLED
     */
    @JsonProperty("event_type")
    private TrackingEventType eventType;

    /**
     * Tracking ID
     */
    @JsonProperty("tracking_id")
    private UUID trackingId;

    /**
     * Booking ID (để frontend biết tracking thuộc booking nào)
     */
    @JsonProperty("booking_id")
    private UUID bookingId;

    /**
     * Booking code (hiển thị cho user)
     */
    @JsonProperty("booking_code")
    private String bookingCode;

    /**
     * Thông tin tracking đầy đủ (optional - để frontend update trực tiếp)
     * Nếu null, frontend cần fetch tracking mới
     */
    @JsonProperty("tracking_data")
    private ServiceProcessTrackingInfoDto trackingData;

    /**
     * Timestamp của event
     */
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Message thân thiện với user (optional)
     * Ví dụ: "Bước 'Rửa xe' đã bắt đầu cho booking #BK-20251007-0001"
     */
    @JsonProperty("message")
    private String message;

    /**
     * Enum cho các loại tracking events
     */
    public enum TrackingEventType {
        CREATED,      // Tracking mới được tạo
        STARTED,      // PENDING → IN_PROGRESS
        UPDATED,      // Tracking được cập nhật (notes, progress, etc.)
        COMPLETED,    // IN_PROGRESS → COMPLETED
        CANCELLED     // Tracking bị hủy
    }
}

