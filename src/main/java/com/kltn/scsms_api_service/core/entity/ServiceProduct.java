package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

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
    private UUID serviceProductId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice; // Giá của sản phẩm tại thời điểm thêm vào service
    
    @Column(name = "total_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPrice; // quantity * unitPrice
    
    @Column(name = "notes", length = 1000)
    private String notes; // Ghi chú về việc sử dụng sản phẩm trong service
    
    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true; // Sản phẩm có bắt buộc hay không
    
    
    // Business methods
    /**
     * Tính tổng giá của sản phẩm trong service
     */
    public BigDecimal calculateTotalPrice() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    /**
     * Cập nhật tổng giá khi có thay đổi về giá hoặc số lượng
     */
    public void updateTotalPrice() {
        this.totalPrice = calculateTotalPrice();
    }
    
    // Unique constraint để tránh trùng lặp service-product
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"service_id", "product_id"})
    })
    public static class ServiceProductTable {
    }
}
