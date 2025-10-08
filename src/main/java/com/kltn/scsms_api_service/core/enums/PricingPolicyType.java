package com.kltn.scsms_api_service.core.enums;

/**
 * Enum định nghĩa các loại chính sách pricing
 */
public enum PricingPolicyType {
    
    /**
     * Giá cố định - nhập trực tiếp giá bán
     */
    FIXED,
    
    /**
     * Markup trên giá nhập cao nhất - tính giá bán dựa trên markup percentage
     */
    MARKUP_ON_PEAK,
    
    /**
     * Giá theo bậc thang - giá khác nhau theo số lượng
     */
    TIERED_PRICING,
    
    /**
     * Giá động - thay đổi theo thời gian, mùa vụ
     */
    DYNAMIC_PRICING
}
