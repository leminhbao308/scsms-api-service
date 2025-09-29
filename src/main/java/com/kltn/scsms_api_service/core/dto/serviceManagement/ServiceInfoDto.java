package com.kltn.scsms_api_service.core.dto.serviceManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.Service;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceInfoDto {
    
    private UUID serviceId;
    private String serviceUrl;
    private String serviceName;
    private UUID categoryId;
    private String categoryName;
    private String description;
    private Integer standardDuration;
    private Service.SkillLevel requiredSkillLevel;
    private Boolean isPackage;
    private BigDecimal basePrice; // Total price = product costs + labor costs
    private BigDecimal laborCost; // Tiền công lao động
    private BigDecimal productCost; // Tổng giá các sản phẩm
    private Service.ServiceType serviceType;
    private Boolean photoRequired;
    private String imageUrls; // JSON array of image URLs
    private Boolean isFeatured;
    private Boolean isActive;
    private List<ServiceProductDto> serviceProducts; // Danh sách sản phẩm trong service
    private AuditDto audit;
}