package com.kltn.scsms_api_service.core.entity.enumAttribute;

public enum PurchaseStatus {
    // DRAFT: Đơn nhập hàng mới tạo, chưa gửi duyệt
    DRAFT,
    
    // PENDING_DELIVERY: Đơn nhập hàng đã được duyệt, đang chờ giao hàng
    PENDING_DELIVERY,
    
    // RECEIVED: Đơn nhập hàng đã được giao hàng đầy đủ
    RECEIVED,
    
    // CANCELLED: Đơn nhập hàng đã bị hủy
    CANCELLED
}
