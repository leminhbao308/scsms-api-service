package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.InventoryStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.ReferenceType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Inventory Header - Quản lý phiếu nhập/xuất kho
 * Pattern: Header-Detail với InventoryDetail
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventory_headers", schema = GeneralConstant.DB_SCHEMA_DEV)
public class InventoryHeader extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_id", nullable = false)
    private UUID inventoryId;
    
    @Column(name = "inventory_code", unique = true, nullable = false)
    private String inventoryCode;
    
    // Loại phiếu
    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    // Branch quản lý kho này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    
    // Nhà cung cấp (cho phiếu nhập)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    // Reference đến các đơn hàng/phiếu khác
    @Column(name = "reference_type")
    @Enumerated(EnumType.STRING)
    private ReferenceType referenceType;
    
    @Column(name = "reference_id")
    private UUID referenceId;
    
    @Column(name = "reference_code")
    private String referenceCode;
    
    // Ngày giao dịch
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    // Ngày dự kiến nhận hàng (cho phiếu nhập từ supplier)
    @Column(name = "expected_receive_date")
    private LocalDateTime expectedReceiveDate;
    
    // Ngày thực tế nhận hàng
    @Column(name = "actual_receive_date")
    private LocalDateTime actualReceiveDate;
    
    // Ngày dự kiến xuất hàng (cho phiếu xuất)
    @Column(name = "expected_ship_date")
    private LocalDateTime expectedShipDate;
    
    // Ngày thực tế xuất hàng
    @Column(name = "actual_ship_date")
    private LocalDateTime actualShipDate;
    
    // Trạng thái
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InventoryStatus status = InventoryStatus.DRAFT;
    
    // Tổng số lượng items
    @Column(name = "total_items")
    @Builder.Default
    private Integer totalItems = 0;
    
    // Tổng số lượng sản phẩm
    @Column(name = "total_quantity")
    @Builder.Default
    private Integer totalQuantity = 0;
    
    // Tổng giá trị
    @Column(name = "total_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    // Chi phí vận chuyển
    @Column(name = "shipping_cost", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;
    
    // Chi phí khác
    @Column(name = "other_costs", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal otherCosts = BigDecimal.ZERO;
    
    // Tổng chi phí
    @Column(name = "grand_total", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;
    
    // Ghi chú
    @Column(name = "notes", length = Integer.MAX_VALUE)
    private String notes;
    
    // Người yêu cầu
    @Column(name = "requested_by")
    private String requestedBy;
    
    // Người phê duyệt
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    // Người nhận/xuất hàng
    @Column(name = "processed_by")
    private String processedBy;
    
    @Column(name = "processed_date")
    private LocalDateTime processedDate;
    
    // Thông tin vận chuyển
    @Column(name = "shipping_info")
    @JdbcTypeCode(SqlTypes.JSON)
    private String shippingInfo; // JSON: {"carrier": "", "trackingNumber": "", "notes": ""}
    
    // Thông tin thanh toán
    @Column(name = "payment_info")
    @JdbcTypeCode(SqlTypes.JSON)
    private String paymentInfo; // JSON: {"method": "", "status": "", "paidAmount": 0}
    
    // Documents đính kèm
    @Column(name = "attachments")
    @JdbcTypeCode(SqlTypes.JSON)
    private String attachments; // JSON array of file URLs
    
    // One-to-many relationship với inventory details
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<InventoryDetail> inventoryDetails = new ArrayList<>();
    
    // Utility methods
    public void addInventoryDetail(InventoryDetail detail) {
        inventoryDetails.add(detail);
        detail.setInventory(this);
        recalculateTotals();
    }
    
    public void removeInventoryDetail(InventoryDetail detail) {
        inventoryDetails.remove(detail);
        detail.setInventory(null);
        recalculateTotals();
    }
    
    public void recalculateTotals() {
        totalItems = inventoryDetails.size();
        totalQuantity = inventoryDetails.stream()
            .mapToInt(InventoryDetail::getQuantity)
            .sum();
        totalAmount = inventoryDetails.stream()
            .map(InventoryDetail::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        grandTotal = totalAmount.add(shippingCost).add(otherCosts);
    }
    
    public void approve(String approver) {
        this.status = InventoryStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedDate = LocalDateTime.now();
    }
    
    public void complete(String processor) {
        this.status = InventoryStatus.COMPLETED;
        this.processedBy = processor;
        this.processedDate = LocalDateTime.now();
        this.actualReceiveDate = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = InventoryStatus.CANCELLED;
    }
    
    public boolean canEdit() {
        return status == InventoryStatus.DRAFT || status == InventoryStatus.PENDING;
    }
    
    public boolean canApprove() {
        return status == InventoryStatus.PENDING;
    }
    
    public boolean canComplete() {
        return status == InventoryStatus.APPROVED || status == InventoryStatus.IN_TRANSIT;
    }
}
