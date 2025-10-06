package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "service_process_step_product", schema = "dev")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ServiceProcessStepProduct extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private ServiceProcessStep serviceProcessStep;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;
    
    @Column(name = "unit", length = 20)
    private String unit;
    
    /**
     * Tính tổng chi phí sản phẩm cho bước này
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
}
