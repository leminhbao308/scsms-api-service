package com.kltn.scsms_api_service.core.entity.enumAttribute;

public enum StockStatus {
    AVAILABLE,      // Có sẵn
    LOW_STOCK,      // Sắp hết
    OUT_OF_STOCK,   // Hết hàng
    ON_ORDER,       // Đang đặt hàng
    QUARANTINE,     // Cách ly (chờ kiểm tra)
    DAMAGED,        // Hư hỏng
    EXPIRED,        // Hết hạn
    LOCKED          // Khóa (không cho phép giao dịch)
}
