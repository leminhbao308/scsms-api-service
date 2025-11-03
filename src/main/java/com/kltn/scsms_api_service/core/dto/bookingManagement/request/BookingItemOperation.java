package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

/**
 * Enum định nghĩa các operation có thể thực hiện trên booking item khi update booking
 */
public enum BookingItemOperation {
    /**
     * Item mới được tạo
     */
    CREATED,
    
    /**
     * Item đã tồn tại và được cập nhật
     */
    UPDATED,
    
    /**
     * Item đã tồn tại nhưng bị xóa
     */
    DELETED
}

