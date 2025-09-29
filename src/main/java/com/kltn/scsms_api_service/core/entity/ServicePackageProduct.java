package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_package_products", schema = GeneralConstant.DB_SCHEMA_DEV,
       uniqueConstraints = @UniqueConstraint(columnNames = {"package_id", "product_id"}))
public class ServicePackageProduct extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_package_product_id", nullable = false)
    private UUID servicePackageProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private ServicePackage servicePackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "notes", length = Integer.MAX_VALUE)
    private String notes;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true;

    // Business methods
    /**
     * Tính tổng giá của sản phẩm trong gói dịch vụ
     */
    public BigDecimal calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Cập nhật tổng giá
     */
    public void updateTotalPrice() {
        this.totalPrice = calculateTotalPrice();
    }
}
