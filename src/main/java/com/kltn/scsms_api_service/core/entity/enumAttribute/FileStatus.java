package com.kltn.scsms_api_service.core.entity.enumAttribute;

public enum FileStatus {
    ACTIVE,      // File đang hoạt động
    DELETED,     // File đã bị xóa (soft delete)
    ARCHIVED,    // File đã được lưu trữ
    PROCESSING,  // File đang được xử lý
    FAILED       // Upload/xử lý thất bại
}
