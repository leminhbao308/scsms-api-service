package com.kltn.scsms_api_service.core.entity.enumAttribute;

public enum PricingPolicyType {
    FIXED, // trực tiếp nhập giá bán
    
    MARKUP_ON_PEAK, // markup theo peakPurchasePrice (ProductCostStats)
}
