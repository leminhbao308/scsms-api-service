package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.compositId.ProductAttributeValueId;
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
@Table(name = "product_attribute_value", schema = GeneralConstant.DB_SCHEMA_DEV)
@IdClass(ProductAttributeValueId.class)
public class ProductAttributeValue extends AuditEntity {
    
    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Id
    @Column(name = "attribute_id", nullable = false)
    private UUID attributeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", insertable = false, updatable = false)
    private ProductAttribute productAttribute;
    
    @Column(name = "value_text", length = 255)
    private String valueText; // For text/number values stored as string
    
    @Column(name = "value_number", precision = 18, scale = 2)
    private BigDecimal valueNumber; // For numeric values
    
    // Utility methods
    public String getDisplayValue() {
        if (valueNumber != null) {
            return valueNumber.toString();
        }
        return valueText;
    }
    
    public void setValue(String value) {
        if (productAttribute != null && productAttribute.isNumericType()) {
            try {
                this.valueNumber = new BigDecimal(value);
                this.valueText = null;
            } catch (NumberFormatException e) {
                this.valueText = value;
                this.valueNumber = null;
            }
        } else {
            this.valueText = value;
            this.valueNumber = null;
        }
    }
    
    public void setValue(BigDecimal value) {
        this.valueNumber = value;
        this.valueText = null;
    }
}
