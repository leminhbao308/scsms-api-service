package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.ItemType;
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
@Table(name = "price_list_details", schema = GeneralConstant.DB_SCHEMA_DEV,
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"price_list_id", "item_type", "item_id"})
    })
public class PriceListDetail extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "price_list_detail_id", nullable = false)
    private UUID priceListDetailId;
    
    // Many-to-one với Price List Header
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PriceListHeader priceListHeader;
    
    // Loại item (Service, Product, ServicePackage)
    @Column(name = "item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemType itemType;
    
    // ID của item (có thể là serviceId, productId, hoặc packageId)
    @Column(name = "item_id", nullable = false)
    private UUID itemId;
    
    // Reference đến các entity cụ thể (optional, để query dễ dàng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private ServicePackage servicePackage;
    
    // Tên item (để tránh phải join khi display)
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    // Giá gốc (giá cơ sở từ Service/Product/Package)
    @Column(name = "base_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal basePrice;
    
    // Giá bán trong bảng giá này
    @Column(name = "selling_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal sellingPrice;
    
    // Giá tối thiểu được phép bán
    @Column(name = "min_price", precision = 15, scale = 2)
    private BigDecimal minPrice;
    
    // Giá tối đa
    @Column(name = "max_price", precision = 15, scale = 2)
    private BigDecimal maxPrice;
    
    // Unit of measure (từ Product hoặc Service)
    @Column(name = "unit_of_measure")
    private String unitOfMeasure;
    
    // Số lượng tối thiểu để áp dụng giá này
    @Column(name = "min_quantity")
    @Builder.Default
    private Integer minQuantity = 1;
    
    // Số lượng tối đa áp dụng giá này
    @Column(name = "max_quantity")
    private Integer maxQuantity;
    
    // Giá theo cấp bậc (tier pricing)
    @Column(name = "tier_pricing")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tierPricing; // JSON: [{"minQty": 1, "maxQty": 10, "price": 100}, ...]
    
    // Điều kiện áp dụng
    @Column(name = "conditions")
    @JdbcTypeCode(SqlTypes.JSON)
    private String conditions; // JSON: {"vehicleType": ["SEDAN", "SUV"], "dayOfWeek": ["MON", "TUE"]}
    
    // Ghi chú
    @Column(name = "notes", length = 1000)
    private String notes;
    
    // Có cho phép thương lượng giá không
    @Column(name = "is_negotiable")
    @Builder.Default
    private Boolean isNegotiable = false;
    
    // Margin (lợi nhuận)
    @Column(name = "margin_percentage", precision = 5, scale = 2)
    private BigDecimal marginPercentage;
    
    @Column(name = "margin_amount", precision = 15, scale = 2)
    private BigDecimal marginAmount;
    
    // Utility methods
    public BigDecimal calculateFinalPrice() {
        BigDecimal finalPrice = sellingPrice;
        
        // Đảm bảo không thấp hơn minPrice
        if (minPrice != null && finalPrice.compareTo(minPrice) < 0) {
            finalPrice = minPrice;
        }
        
        return finalPrice;
    }
    
    public void calculateMargin() {
        if (basePrice != null && sellingPrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0) {
            marginAmount = sellingPrice.subtract(basePrice);
            marginPercentage = marginAmount.multiply(new BigDecimal("100"))
                .divide(basePrice, 2, java.math.RoundingMode.HALF_UP);
        }
    }
    
    @Override
    protected void onCreate() {
        calculateMargin();
        super.onCreate();
    }
    
    @Override
    protected void onUpdate() {
        calculateMargin();
        super.onUpdate();
    }
}
