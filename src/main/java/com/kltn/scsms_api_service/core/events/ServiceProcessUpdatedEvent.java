package com.kltn.scsms_api_service.core.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event được trigger khi Service Process được cập nhật
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProcessUpdatedEvent {
    
    private UUID serviceProcessId;
    private String serviceProcessName;
    private List<UUID> affectedServiceIds; // Danh sách service bị ảnh hưởng
    private UUID priceBookId; // Price book được sử dụng để tính giá
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String updateReason; // Lý do cập nhật
}
