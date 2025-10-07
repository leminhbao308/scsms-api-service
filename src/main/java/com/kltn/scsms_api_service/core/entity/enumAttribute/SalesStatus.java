package com.kltn.scsms_api_service.core.entity.enumAttribute;

public enum SalesStatus {
    // DRAFT: Đơn bán hàng tạm thời, chưa được xác nhận
    DRAFT,
    
    // CONFIRMED: Đơn bán hàng đã được xác nhận
    CONFIRMED,
    
    // FULFILLED: Đơn bán hàng đã được hoàn tất
    FULFILLED,
    
    // PARTIALLY_RETURNED: Đơn bán hàng đã được trả hàng một phần
    PARTIALLY_RETURNED,
    
    // RETURNED: Đơn bán hàng đã được trả hàng đầy đủ
    RETURNED,
    
    // CANCELLED: Đơn bán hàng đã bị hủy
    CANCELLED
}
