package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_attribute", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ProductAttribute extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attribute_id", nullable = false)
    private UUID attributeId;
    
    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;
    
    @Column(name = "attribute_code", unique = true, nullable = false, length = 50)
    private String attributeCode;
    
    @Column(name = "unit", length = 20)
    private String unit;
    
    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = false;
    
    @Column(name = "data_type", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DataType dataType = DataType.STRING;
    
    // Enums
    public enum DataType {
        STRING, NUMBER, BOOLEAN, DATE, DECIMAL, INTEGER, TEXT
    }
    
    // Utility methods
    public boolean isNumericType() {
        return dataType == DataType.NUMBER || 
               dataType == DataType.DECIMAL || 
               dataType == DataType.INTEGER;
    }
    
    public boolean isTextType() {
        return dataType == DataType.STRING || 
               dataType == DataType.TEXT;
    }
    
    public boolean isBooleanType() {
        return dataType == DataType.BOOLEAN;
    }
    
    public boolean isDateType() {
        return dataType == DataType.DATE;
    }
    
    public String getDisplayName() {
        return attributeName + (unit != null ? " (" + unit + ")" : "");
    }
}
