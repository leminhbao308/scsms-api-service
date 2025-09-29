package com.kltn.scsms_api_service.core.entity.enumAttribute;

public enum TransactionType {
    INBOUND,              // Nhập kho
    OUTBOUND,             // Xuất kho
    TRANSFER,             // Chuyển kho
    ADJUSTMENT,           // Điều chỉnh
    RETURN_TO_SUPPLIER,   // Trả hàng cho nhà cung cấp
    RETURN_FROM_CUSTOMER, // Nhận hàng trả từ khách
    DAMAGE,               // Hàng hư hỏng
    LOSS,                 // Mất mát
    DISPOSAL              // Thanh lý
}
