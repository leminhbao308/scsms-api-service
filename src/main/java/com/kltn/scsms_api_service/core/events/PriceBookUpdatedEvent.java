package com.kltn.scsms_api_service.core.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event được trigger khi Price Book được cập nhật
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBookUpdatedEvent {
    
    private UUID priceBookId;
    private String priceBookName;
    private List<UUID> affectedServiceIds; // Danh sách service bị ảnh hưởng
    private List<UUID> affectedProductIds; // Danh sách sản phẩm bị ảnh hưởng
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String updateReason; // Lý do cập nhật
    private String updateType; // "PRICE_CHANGE", "PRODUCT_ADDED", "PRODUCT_REMOVED", "POLICY_CHANGE"
}
