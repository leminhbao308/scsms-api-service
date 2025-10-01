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
@Table(name = "promotion_lines", schema = GeneralConstant.DB_SCHEMA_DEV)
public class PromotionLine extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "line_id", nullable = false)
    private UUID lineId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;
    
    // === TARGET CONDITIONS ===
    @Column(name = "target_vehicle_types")
    @JdbcTypeCode(SqlTypes.JSON)
    private String targetVehicleTypes; // JSON array: ["SEDAN", "SUV"]
    
    @Column(name = "target_services")
    @JdbcTypeCode(SqlTypes.JSON)
    private String targetServices; // JSON array: service IDs
    
    @Column(name = "target_products")
    @JdbcTypeCode(SqlTypes.JSON)
    private String targetProducts; // JSON array: product IDs
    
    // === QUANTITY & AMOUNT CONDITIONS ===
    @Column(name = "required_quantity")
    private Integer requiredQuantity; // Số lượng yêu cầu
    
    @Column(name = "required_amount", precision = 15, scale = 2)
    private BigDecimal requiredAmount; // Số tiền yêu cầu
    
    // === ITEM REFERENCES ===
    @Column(name = "item_id")
    private UUID itemId; // References products or services
    
    @Column(name = "item_type", length = 20)
    @Enumerated(EnumType.STRING)
    private ItemType itemType; // PRODUCT, SERVICE, ANY
    
    // === ENUMS ===
    public enum ItemType {
        PRODUCT, SERVICE, ANY
    }
}
