package com.kltn.scsms_api_service.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValueId implements Serializable {
    
    private UUID productId;
    private UUID attributeId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ProductAttributeValueId that = (ProductAttributeValueId) o;
        
        if (productId != null ? !productId.equals(that.productId) : that.productId != null) return false;
        return attributeId != null ? attributeId.equals(that.attributeId) : that.attributeId == null;
    }
    
    @Override
    public int hashCode() {
        int result = productId != null ? productId.hashCode() : 0;
        result = 31 * result + (attributeId != null ? attributeId.hashCode() : 0);
        return result;
    }
}
