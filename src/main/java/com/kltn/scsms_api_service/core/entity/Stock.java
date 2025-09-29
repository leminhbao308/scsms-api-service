package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stock - Quản lý tồn kho thực tế theo từng chi nhánh
 * Entity này lưu trữ số lượng hiện có của từng sản phẩm tại mỗi chi nhánh
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stocks", schema = GeneralConstant.DB_SCHEMA_DEV,
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"branch_id", "product_id", "batch_number"})
    },
    indexes = {
        @Index(name = "idx_stock_branch_product", columnList = "branch_id, product_id"),
        @Index(name = "idx_stock_product", columnList = "product_id"),
        @Index(name = "idx_stock_expiry", columnList = "expiry_date")
    })
public class Stock extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "stock_id", nullable = false)
    private UUID stockId;
    
    // Chi nhánh
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    
    // Sản phẩm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    // Thông tin sản phẩm (snapshot để query nhanh)
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "product_sku")
    private String productSku;
    
    // Số lượng tồn kho
    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;
    
    // Số lượng đã đặt (reserved cho đơn hàng chưa xuất)
    @Column(name = "quantity_reserved")
    @Builder.Default
    private Integer quantityReserved = 0;
    
    // Số lượng có thể bán = quantityOnHand - quantityReserved
    @Column(name = "quantity_available")
    @Builder.Default
    private Integer quantityAvailable = 0;
    
    // Số lượng đang chờ nhập (từ purchase order)
    @Column(name = "quantity_on_order")
    @Builder.Default
    private Integer quantityOnOrder = 0;
    
    // Số lượng bị hư hỏng/lỗi
    @Column(name = "quantity_damaged")
    @Builder.Default
    private Integer quantityDamaged = 0;
    
    // Thông tin lô hàng
    @Column(name = "batch_number")
    private String batchNumber;
    
    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    // Vị trí kho
    @Column(name = "storage_location")
    private String storageLocation;
    
    @Column(name = "bin_location")
    private String binLocation;
    
    // Giá trị tồn kho
    @Column(name = "unit_cost", precision = 15, scale = 2)
    private BigDecimal unitCost; // Giá vốn trung bình
    
    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost; // quantityOnHand * unitCost
    
    // Thông tin nhập kho cuối cùng
    @Column(name = "last_inbound_date")
    private LocalDateTime lastInboundDate;
    
    @Column(name = "last_inbound_quantity")
    private Integer lastInboundQuantity;
    
    // Thông tin xuất kho cuối cùng
    @Column(name = "last_outbound_date")
    private LocalDateTime lastOutboundDate;
    
    @Column(name = "last_outbound_quantity")
    private Integer lastOutboundQuantity;
    
    // Ngày kiểm kê cuối cùng
    @Column(name = "last_stock_check_date")
    private LocalDateTime lastStockCheckDate;
    
    // Trạng thái
    @Column(name = "stock_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StockStatus stockStatus = StockStatus.AVAILABLE;
    
    // Cảnh báo tồn kho
    @Column(name = "is_low_stock")
    @Builder.Default
    private Boolean isLowStock = false;
    
    @Column(name = "is_out_of_stock")
    @Builder.Default
    private Boolean isOutOfStock = false;
    
    @Column(name = "is_overstocked")
    @Builder.Default
    private Boolean isOverstocked = false;
    
    // Min/Max stock levels (override từ Product nếu cần)
    @Column(name = "min_stock_level")
    private Integer minStockLevel;
    
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;
    
    @Column(name = "reorder_point")
    private Integer reorderPoint;
    
    // Utility methods
    public void calculateAvailableQuantity() {
        quantityAvailable = quantityOnHand - (quantityReserved != null ? quantityReserved : 0);
        if (quantityAvailable < 0) {
            quantityAvailable = 0;
        }
    }
    
    public void calculateTotalCost() {
        if (unitCost != null && quantityOnHand != null) {
            totalCost = unitCost.multiply(new BigDecimal(quantityOnHand));
        }
    }
    
    public void updateStockStatus() {
        // Kiểm tra hết hạn
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            stockStatus = StockStatus.EXPIRED;
            isOutOfStock = true;
            return;
        }
        
        // Kiểm tra hư hỏng
        if (quantityDamaged != null && quantityDamaged.equals(quantityOnHand)) {
            stockStatus = StockStatus.DAMAGED;
            isOutOfStock = true;
            return;
        }
        
        // Kiểm tra tồn kho
        if (quantityAvailable == 0) {
            stockStatus = StockStatus.OUT_OF_STOCK;
            isOutOfStock = true;
            isLowStock = false;
        } else if (reorderPoint != null && quantityAvailable <= reorderPoint) {
            stockStatus = StockStatus.LOW_STOCK;
            isLowStock = true;
            isOutOfStock = false;
        } else if (minStockLevel != null && quantityAvailable <= minStockLevel) {
            stockStatus = StockStatus.LOW_STOCK;
            isLowStock = true;
            isOutOfStock = false;
        } else {
            stockStatus = StockStatus.AVAILABLE;
            isLowStock = false;
            isOutOfStock = false;
        }
        
        // Kiểm tra overstocked
        if (maxStockLevel != null && quantityOnHand > maxStockLevel) {
            isOverstocked = true;
        } else {
            isOverstocked = false;
        }
    }
    
    public void addStock(int quantity, BigDecimal cost) {
        // Update quantity
        quantityOnHand += quantity;
        
        // Update weighted average cost
        if (cost != null && unitCost != null) {
            BigDecimal totalExistingCost = unitCost.multiply(new BigDecimal(quantityOnHand - quantity));
            BigDecimal newCost = cost.multiply(new BigDecimal(quantity));
            BigDecimal combinedCost = totalExistingCost.add(newCost);
            unitCost = combinedCost.divide(new BigDecimal(quantityOnHand), 2, java.math.RoundingMode.HALF_UP);
        } else if (cost != null) {
            unitCost = cost;
        }
        
        lastInboundDate = LocalDateTime.now();
        lastInboundQuantity = quantity;
        
        calculateAvailableQuantity();
        calculateTotalCost();
        updateStockStatus();
    }
    
    public void removeStock(int quantity) {
        if (quantityAvailable < quantity) {
            throw new IllegalStateException("Not enough available stock. Available: " + quantityAvailable + ", Requested: " + quantity);
        }
        
        quantityOnHand -= quantity;
        lastOutboundDate = LocalDateTime.now();
        lastOutboundQuantity = quantity;
        
        calculateAvailableQuantity();
        calculateTotalCost();
        updateStockStatus();
    }
    
    public void reserveStock(int quantity) {
        if (quantityAvailable < quantity) {
            throw new IllegalStateException("Not enough available stock to reserve");
        }
        
        quantityReserved += quantity;
        calculateAvailableQuantity();
        updateStockStatus();
    }
    
    public void releaseReservedStock(int quantity) {
        if (quantityReserved < quantity) {
            throw new IllegalStateException("Cannot release more than reserved");
        }
        
        quantityReserved -= quantity;
        calculateAvailableQuantity();
        updateStockStatus();
    }
    
    public boolean isExpiringSoon(int daysThreshold) {
        if (expiryDate != null) {
            LocalDate threshold = LocalDate.now().plusDays(daysThreshold);
            return expiryDate.isBefore(threshold);
        }
        return false;
    }
    
    public boolean needsReorder() {
        return isLowStock || isOutOfStock;
    }
    
    public void calculateStocks() {
        calculateAvailableQuantity();
        calculateTotalCost();
        updateStockStatus();
    }
    
    @Override
    protected void onCreate() {
        super.onCreate();
        calculateStocks();
    }
    
    @Override
    protected void onUpdate() {
        super.onUpdate();
        calculateStocks();
    }
}
