package com.kltn.scsms_api_service.core.entity.enumAttribute;

/**
 * Trạng thái của booking draft
 */
public enum DraftStatus {
    /**
     * Đang trong quá trình đặt lịch (chưa hoàn thành)
     */
    IN_PROGRESS,
    
    /**
     * Đã hoàn thành (đã tạo booking thành công)
     */
    COMPLETED,
    
    /**
     * Bị bỏ dở (user không hoàn thành)
     */
    ABANDONED
}

