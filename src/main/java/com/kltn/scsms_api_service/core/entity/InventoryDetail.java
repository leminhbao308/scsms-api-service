package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.InventoryListStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.QualityStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Inventory Detail - Chi tiết từng sản phẩm trong phiếu nhập/xuất kho
 * Pattern: Header-Detail với Inventory
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventory_details", schema = GeneralConstant.DB_SCHEMA_DEV)
public class InventoryDetail extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_detail_id", nullable = false)
    private UUID inventoryDetailId;
    
    // Many-to-one với Inventory Header
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private InventoryHeader inventory;
    
    // Reference đến Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    // Thông tin sản phẩm (snapshot tại thời điểm giao dịch)
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "product_sku")
    private String productSku;
    
    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure;
    
    // Số lượng
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    // Ngày sản xuất
    @Column(name = "production_date")
    private LocalDate productionDate;
    
    // Số lượng thực tế nhận được (có thể khác với quantity đặt)
    @Column(name = "received_quantity")
    private Integer receivedQuantity;
    
    // Số lượng bị reject
    @Column(name = "rejected_quantity")
    @Builder.Default
    private Integer rejectedQuantity = 0;
    
    // Giá
    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    // Tổng giá trị = quantity * unitPrice
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    // Thuế VAT
    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    // Tổng sau chiết khấu và thuế
    @Column(name = "final_amount", precision = 15, scale = 2)
    private BigDecimal finalAmount;
    
    // Thông tin lô hàng
    @Column(name = "batch_number")
    private String batchNumber;
    
    @Column(name = "serial_numbers")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> serialNumbers; // JSON array cho sản phẩm cần tracking serial
    
    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    // Vị trí trong kho
    @Column(name = "storage_location")
    private String storageLocation;
    
    @Column(name = "bin_location")
    private String binLocation;
    
    // Chất lượng khi nhận
    @Column(name = "quality_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private QualityStatus qualityStatus = QualityStatus.GOOD;
    
    // Lý do reject (nếu có)
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
    
    // Ghi chú
    @Column(name = "notes", length = 1000)
    private String notes;
    
    // Warranty information
    @Column(name = "warranty_months")
    private Integer warrantyMonths;
    
    @Column(name = "warranty_activation_date")
    private LocalDate warrantyActivationDate;
    
    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;
    
    // Cost allocation (phân bổ chi phí)
    @Column(name = "allocated_shipping_cost", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allocatedShippingCost = BigDecimal.ZERO;
    
    @Column(name = "allocated_other_costs", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allocatedOtherCosts = BigDecimal.ZERO;
    
    // Landed cost (giá thành cuối cùng bao gồm tất cả chi phí)
    @Column(name = "landed_cost", precision = 15, scale = 2)
    private BigDecimal landedCost;
    
    // Line number trong phiếu
    @Column(name = "line_number")
    private Integer lineNumber;
    
    // Trạng thái dòng
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InventoryListStatus status = InventoryListStatus.PENDING;
    
    // Enum
    
    // Utility methods
    public void calculateTotalAmount() {
        if (quantity != null && unitPrice != null) {
            totalAmount = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
    
    public void calculateFinalAmount() {
        if (totalAmount == null) {
            calculateTotalAmount();
        }
        
        BigDecimal amount = totalAmount;
        
        // Cộng thuế
        if (taxAmount != null) {
            amount = amount.add(taxAmount);
        }
        if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tax = amount.multiply(taxPercentage)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            amount = amount.add(tax);
            taxAmount = tax;
        }
        
        finalAmount = amount;
    }
    
    public void calculateLandedCost() {
        if (finalAmount == null) {
            calculateFinalAmount();
        }
        
        landedCost = finalAmount
            .add(allocatedShippingCost != null ? allocatedShippingCost : BigDecimal.ZERO)
            .add(allocatedOtherCosts != null ? allocatedOtherCosts : BigDecimal.ZERO);
    }
    
    public BigDecimal getUnitLandedCost() {
        if (landedCost != null && receivedQuantity != null && receivedQuantity > 0) {
            return landedCost.divide(new BigDecimal(receivedQuantity), 2, java.math.RoundingMode.HALF_UP);
        }
        if (landedCost != null && quantity != null && quantity > 0) {
            return landedCost.divide(new BigDecimal(quantity), 2, java.math.RoundingMode.HALF_UP);
        }
        return unitPrice;
    }
    
    public void setWarrantyExpiryDate() {
        if (warrantyMonths != null && warrantyActivationDate != null) {
            warrantyExpiryDate = warrantyActivationDate.plusMonths(warrantyMonths);
        }
    }
    
    public boolean isExpiringSoon(int daysThreshold) {
        if (expiryDate != null) {
            LocalDate threshold = LocalDate.now().plusDays(daysThreshold);
            return expiryDate.isBefore(threshold);
        }
        return false;
    }
    
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
    
    public void calculateInventoryDetailAmounts() {
        calculateTotalAmount();
        calculateFinalAmount();
        calculateLandedCost();
        
        if (receivedQuantity == null) {
            receivedQuantity = quantity;
        }
        
        if (rejectedQuantity == null) {
            rejectedQuantity = 0;
        }
    }
    
    @Override
    protected void onCreate() {
        super.onCreate();
        calculateInventoryDetailAmounts();
    }
    
    @Override
    protected void onUpdate() {
        super.onUpdate();
        calculateInventoryDetailAmounts();
    }
}
