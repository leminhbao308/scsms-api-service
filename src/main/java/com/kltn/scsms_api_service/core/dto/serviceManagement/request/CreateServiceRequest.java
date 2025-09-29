package com.kltn.scsms_api_service.core.dto.serviceManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.Service;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateServiceRequest {
    
    @NotBlank(message = "Service name is required")
    @Size(min = 2, max = 500, message = "Service name must be between 2 and 500 characters")
    @JsonProperty("service_name")
    private String serviceName;
    
    @NotBlank(message = "Service URL is required")
    @Size(max = 1000, message = "Service URL must not exceed 1000 characters")
    @JsonProperty("service_url")
    private String serviceUrl;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @JsonProperty("description")
    private String description;
    
    @Min(value = 1, message = "Standard duration must be at least 1 minute")
    @JsonProperty("standard_duration")
    private Integer standardDuration;
    
    @JsonProperty("required_skill_level")
    private Service.SkillLevel requiredSkillLevel;
    
    @JsonProperty("is_package")
    private Boolean isPackage;
    
    @DecimalMin(value = "0.0", message = "Base price must be non-negative")
    @JsonProperty("base_price")
    private BigDecimal basePrice; // Total price = product costs + labor costs
    
    @DecimalMin(value = "0.0", message = "Labor cost must be non-negative")
    @JsonProperty("labor_cost")
    private BigDecimal laborCost; // Tiền công lao động
    
    @JsonProperty("service_type")
    private Service.ServiceType serviceType;
    
    @JsonProperty("photo_required")
    private Boolean photoRequired;
    
    
    
    @JsonProperty("image_urls")
    private String imageUrls; // JSON array of image URLs
    
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    @JsonProperty("service_products")
    private List<CreateServiceProductRequest> serviceProducts; // Danh sách sản phẩm trong service
}