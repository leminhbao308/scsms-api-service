package com.kltn.scsms_api_service.core.dto.branchServiceFilter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Kết quả lọc service/service package theo branch
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchServiceFilterResult {
    
    private UUID branchId;
    private String branchName;
    private UUID warehouseId;
    
    /**
     * Danh sách service/package có thể sử dụng
     */
    private List<ServiceAvailabilityInfo> availableServices;
    
    /**
     * Danh sách service/package không thể sử dụng
     */
    private List<ServiceAvailabilityInfo> unavailableServices;
    
    /**
     * Tổng số service/package được kiểm tra
     */
    private int totalServicesChecked;
    
    /**
     * Số service/package có thể sử dụng
     */
    private int availableServicesCount;
    
    /**
     * Tỷ lệ service/package có thể sử dụng (%)
     */
    public double getAvailabilityPercentage() {
        if (totalServicesChecked == 0) return 100.0;
        return (double) availableServicesCount / totalServicesChecked * 100.0;
    }
    
    /**
     * Có service/package nào khả dụng không
     */
    public boolean hasAvailableServices() {
        return availableServices != null && !availableServices.isEmpty();
    }
    
    /**
     * Có service/package nào không khả dụng không
     */
    public boolean hasUnavailableServices() {
        return unavailableServices != null && !unavailableServices.isEmpty();
    }
}
