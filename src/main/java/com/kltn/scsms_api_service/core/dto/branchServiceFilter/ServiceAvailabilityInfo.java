package com.kltn.scsms_api_service.core.dto.branchServiceFilter;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Thông tin tính khả dụng của service hoặc service package
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAvailabilityInfo {
    
    private UUID id;
    private String name;
    private String type; // "SERVICE"
    
    /**
     * Có thể sử dụng không
     */
    private boolean available;
    
    /**
     * Tổng số sản phẩm cần thiết
     */
    private int totalProductsRequired;
    
    /**
     * Số sản phẩm có sẵn
     */
    private int availableProductsCount;
    
    /**
     * Danh sách sản phẩm không có trong kho
     */
    private List<String> missingProducts;
    
    /**
     * Danh sách sản phẩm không đủ số lượng
     */
    private List<String> insufficientProducts;
    
    /**
     * Lý do không khả dụng (nếu có)
     */
    private String reason;
    
    /**
     * Thông tin chi tiết service (nếu là service)
     */
    private ServiceInfoDto serviceInfo;
    
    
    /**
     * Tỷ lệ sản phẩm có sẵn (%)
     */
    public double getProductAvailabilityPercentage() {
        if (totalProductsRequired == 0) return 100.0;
        return (double) availableProductsCount / totalProductsRequired * 100.0;
    }
    
    /**
     * Có sản phẩm nào thiếu không
     */
    public boolean hasMissingProducts() {
        return missingProducts != null && !missingProducts.isEmpty();
    }
    
    /**
     * Có sản phẩm nào không đủ số lượng không
     */
    public boolean hasInsufficientProducts() {
        return insufficientProducts != null && !insufficientProducts.isEmpty();
    }
    
    /**
     * Lấy tên hiển thị
     */
    public String getDisplayName() {
        return name != null ? name : (type + " - " + id.toString().substring(0, 8));
    }
}
