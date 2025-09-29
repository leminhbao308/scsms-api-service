package com.kltn.scsms_api_service.core.entity.enumAttribute;

public enum InventoryListStatus {
    PENDING,        // Chờ xử lý
    RECEIVED,       // Đã nhận
    ISSUED,         // Đã xuất
    REJECTED,       // Bị từ chối
    CANCELLED       // Đã hủy
}
