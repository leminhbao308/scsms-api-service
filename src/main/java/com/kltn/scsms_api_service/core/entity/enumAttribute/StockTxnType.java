package com.kltn.scsms_api_service.core.entity.enumAttribute;

public enum StockTxnType {
    // PURCHASE_RECEIPT: Nhập kho từ đơn mua hàng
    PURCHASE_RECEIPT,
    
    // SALE: Xuất kho từ đơn bán hàng
    SALE,
    
    // RETURN: Trả hàng (cả nhập và xuất)
    RETURN,
    
    // ADJUSTMENT: Điều chỉnh tồn kho (cả tăng và giảm)
    ADJUSTMENT,
    
    // RESERVATION: Dự trữ kho
    RESERVATION,
    
    // RELEASE: Giải phóng kho
    RELEASE
}
