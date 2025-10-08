package com.kltn.scsms_api_service.core.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event được trigger khi Service được tạo mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCreatedEvent {
    
    private UUID serviceId;
    private String serviceName;
    private UUID serviceProcessId;
    private UUID priceBookId; // Price book được sử dụng để tính giá
    private LocalDateTime createdAt;
    private String createdBy;
}
