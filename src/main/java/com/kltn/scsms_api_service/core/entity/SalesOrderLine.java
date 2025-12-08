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
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_order_line", schema = GeneralConstant.DB_SCHEMA_DEV)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderLine extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sol_so"))
    private SalesOrder salesOrder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_sol_product"))
    private Product product;

    @Column(name = "qty", nullable = false)
    private Long quantity;

    // Giá bán đã tính theo PriceBook tại thời điểm tạo order
    @Column(name = "unit_price", precision = 18, scale = 4, nullable = false)
    private BigDecimal unitPrice;

    // True if this item is free from a promotion (FREE_PRODUCT, BUY_X_GET_Y)
    @Column(name = "is_free_item", nullable = false)
    @Builder.Default
    private Boolean isFreeItem = false;

    // ===== SERVICE ITEM SUPPORT =====
    
    /**
     * Service ID if this line item represents a service (null for product items)
     * Used to distinguish between product items and service items
     */
    @Column(name = "service_id")
    private UUID serviceId;
    
    /**
     * Original booking ID if this service item comes from a booking
     */
    @Column(name = "original_booking_id")
    private UUID originalBookingId;
    
    /**
     * Original booking code for display purposes
     */
    @Column(name = "original_booking_code")
    private String originalBookingCode;
    
    // ===== HELPER METHODS =====
    
    /**
     * Check if this line item represents a service
     * @return true if serviceId is not null
     */
    public boolean isServiceItem() {
        return serviceId != null;
    }
    
    /**
     * Check if this line item represents a product
     * @return true if serviceId is null
     */
    public boolean isProductItem() {
        return serviceId == null;
    }
    
    /**
     * Check if this service item comes from a booking
     * @return true if originalBookingId is not null
     */
    public boolean isFromBooking() {
        return originalBookingId != null;
    }
}
