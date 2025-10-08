package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity quản lý bệ dịch vụ tại chi nhánh
 * Mỗi bay đại diện cho 1 vị trí vật lý có thể phục vụ khách hàng
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_bays", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ServiceBay extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bay_id", nullable = false)
    private UUID bayId;
    
    /**
     * Chi nhánh sở hữu bay này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    
    /**
     * Tên bay (vd: "Bệ rửa xe 1", "Bệ sửa chữa 2")
     */
    @Column(name = "bay_name", nullable = false, length = 255)
    private String bayName;
    
    /**
     * Mã bay (vd: "BAY-001", "WASH-01")
     */
    @Column(name = "bay_code", length = 50)
    private String bayCode;
    
    /**
     * Loại bay
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "bay_type", nullable = false)
    private BayType bayType;
    
    /**
     * Mô tả bay
     */
    @Column(name = "description", length = 1000)
    private String description;
    
    /**
     * Số lượng xe có thể phục vụ cùng lúc
     */
    @Column(name = "capacity")
    @Builder.Default
    private Integer capacity = 1;
    
    /**
     * Trạng thái bay
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BayStatus status = BayStatus.ACTIVE;
    
    /**
     * Thứ tự ưu tiên hiển thị
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 1;
    
    /**
     * Ghi chú
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    /**
     * Các booking sử dụng bay này
     */
    @OneToMany(mappedBy = "serviceBay", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();
    
    // Business methods
    
    /**
     * Kiểm tra bay có hoạt động không
     */
    public boolean isActive() {
        return status == BayStatus.ACTIVE;
    }
    
    /**
     * Kiểm tra bay có bị bảo trì không
     */
    public boolean isMaintenance() {
        return status == BayStatus.MAINTENANCE;
    }
    
    /**
     * Kiểm tra bay có bị đóng không
     */
    public boolean isClosed() {
        return status == BayStatus.CLOSED;
    }
    
    /**
     * Kiểm tra bay có phải bệ rửa xe không
     */
    public boolean isWashBay() {
        return bayType == BayType.WASH_BAY;
    }
    
    /**
     * Kiểm tra bay có phải bệ sửa chữa không
     */
    public boolean isRepairBay() {
        return bayType == BayType.REPAIR_BAY;
    }
    
    /**
     * Kiểm tra bay có phải bệ nâng xe không
     */
    public boolean isLiftBay() {
        return bayType == BayType.LIFT_BAY;
    }
    
    /**
     * Đặt bay vào trạng thái bảo trì
     */
    public void setMaintenance(String reason) {
        this.status = BayStatus.MAINTENANCE;
        this.notes = reason;
    }
    
    /**
     * Đóng bay
     */
    public void closeBay(String reason) {
        this.status = BayStatus.CLOSED;
        this.notes = reason;
    }
    
    /**
     * Kích hoạt bay
     */
    public void activateBay() {
        this.status = BayStatus.ACTIVE;
        this.notes = null;
    }
    
    /**
     * Enum cho loại bay
     */
    public enum BayType {
        WASH_BAY,       // Bệ rửa xe
        REPAIR_BAY,     // Bệ sửa chữa
        LIFT_BAY,       // Bệ nâng xe
        INSPECTION_BAY, // Bệ kiểm tra
        PAINT_BAY,      // Bệ sơn xe
        DETAILING_BAY,  // Bệ chăm sóc chi tiết
        TIRE_BAY,       // Bệ thay lốp
        GENERAL_BAY     // Bệ tổng hợp
    }
    
    /**
     * Enum cho trạng thái bay
     */
    public enum BayStatus {
        ACTIVE,         // Hoạt động bình thường
        MAINTENANCE,    // Đang bảo trì
        CLOSED,         // Tạm đóng
        INACTIVE        // Không hoạt động
    }
}
