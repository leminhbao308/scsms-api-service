package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO cho booking item
 * Giá cả được lấy từ price book, không cho phép client truyền vào
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingItemRequest {
    
    @JsonProperty("service_id")
    private UUID serviceId;
    
    @JsonProperty("service_name")
    private String serviceName;
    
    @JsonProperty("service_description")
    private String serviceDescription;
    
    /**
     * Giá đơn vị của dịch vụ (từ price book hoặc snapshot tại thời điểm booking)
     */
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    /**
     * Thời gian dự kiến hoàn thành dịch vụ (phút)
     */
    @JsonProperty("duration_minutes")
    private Integer durationMinutes;
    
    /**
     * Operation type cho update booking items
     * - DELETE: Xóa item này khỏi booking (cần có booking_item_id hoặc service_id)
     * - null hoặc không có: Thêm mới hoặc cập nhật item (tùy theo item đã tồn tại hay chưa)
     */
    @JsonProperty("operation")
    private ItemOperation operation;
    
    /**
     * Booking item ID - chỉ cần khi operation = DELETE
     * Nếu không có, sẽ tìm theo service_id
     */
    @JsonProperty("booking_item_id")
    private UUID bookingItemId;
    
    /**
     * Enum cho các operations trên booking item
     */
    public enum ItemOperation {
        DELETE  // Xóa item khỏi booking
        // Có thể mở rộng thêm: UPDATE, REPLACE, etc.
    }
}