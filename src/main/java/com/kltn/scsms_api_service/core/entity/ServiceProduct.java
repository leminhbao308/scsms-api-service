package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity quản lý mối quan hệ many-to-many giữa Service và Product
 * Lưu trữ thông tin sản phẩm được sử dụng trong dịch vụ với số lượng tương ứng
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_products", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ServiceProduct extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_product_id", nullable = false)
    private UUID id;
    
    /**
     * Dịch vụ sử dụng sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;
    
    /**
     * Sản phẩm được sử dụng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    /**
     * Số lượng sản phẩm cần thiết cho dịch vụ
     */
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;
    
    /**
     * Đơn vị tính
     */
    @Column(name = "unit", length = 20)
    private String unit;
    
    /**
     * Ghi chú về việc sử dụng sản phẩm trong dịch vụ
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    /**
     * Có bắt buộc sử dụng sản phẩm này không
     */
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;
    
    /**
     * Thứ tự ưu tiên sử dụng sản phẩm
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
    
    // Business methods
    
    /**
     * Tính tổng chi phí sản phẩm cho dịch vụ
     */
    public BigDecimal calculateProductCost() {
        if (product == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        
        // Giả sử Product có trường price, nếu không có thì trả về 0
        // Bạn có thể cần điều chỉnh logic này tùy theo cấu trúc Product entity
        return quantity; // Tạm thời trả về quantity, cần cập nhật khi có thông tin về price
    }
    
    /**
     * Kiểm tra xem có đủ số lượng sản phẩm không
     */
    public boolean hasEnoughQuantity(BigDecimal requiredQuantity) {
        if (quantity == null || requiredQuantity == null) {
            return false;
        }
        return quantity.compareTo(requiredQuantity) >= 0;
    }
    
    /**
     * Lấy tên sản phẩm
     */
    public String getProductName() {
        return product != null ? product.getProductName() : null;
    }
    
    /**
     * Lấy mã sản phẩm
     */
    public String getProductCode() {
        return product != null ? product.getSku() : null;
    }
    
    /**
     * Lấy tên dịch vụ
     */
    public String getServiceName() {
        return service != null ? service.getServiceName() : null;
    }
    
    /**
     * Lấy URL dịch vụ
     */
    public String getServiceUrl() {
        return service != null ? service.getServiceUrl() : null;
    }
}
