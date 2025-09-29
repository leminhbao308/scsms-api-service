package com.kltn.scsms_api_service.core.entity.enumAttribute;

public enum InventoryStatus {
    DRAFT,                // Nháp
    PENDING,              // Chờ xử lý
    APPROVED,             // Đã phê duyệt
    IN_TRANSIT,           // Đang vận chuyển
    PARTIALLY_RECEIVED,   // Nhận một phần
    COMPLETED,            // Hoàn thành
    CANCELLED,            // Đã hủy
    REJECTED              // Bị từ chối
}
