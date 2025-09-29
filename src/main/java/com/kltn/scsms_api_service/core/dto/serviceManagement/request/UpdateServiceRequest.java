package com.kltn.scsms_api_service.core.dto.serviceManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.Service;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateServiceRequest {
    
    @Size(min = 2, max = 500, message = "Service name must be between 2 and 500 characters")
    @JsonProperty("service_name")
    private String serviceName;
    
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
    private BigDecimal basePrice;
    
    @DecimalMin(value = "0.0", message = "Minimum price must be non-negative")
    @JsonProperty("min_price")
    private BigDecimal minPrice;
    
    @DecimalMin(value = "0.0", message = "Maximum price must be non-negative")
    @JsonProperty("max_price")
    private BigDecimal maxPrice;
    
    @JsonProperty("complexity_level")
    private Service.ComplexityLevel complexityLevel;
    
    @JsonProperty("service_type")
    private Service.ServiceType serviceType;
    
    @JsonProperty("vehicle_types")
    private String vehicleTypes;
    
    @JsonProperty("required_tools")
    private String requiredTools;
    
    @Size(max = 2000, message = "Safety notes must not exceed 2000 characters")
    @JsonProperty("safety_notes")
    private String safetyNotes;
    
    @Size(max = 2000, message = "Quality criteria must not exceed 2000 characters")
    @JsonProperty("quality_criteria")
    private String qualityCriteria;
    
    @JsonProperty("photo_required")
    private Boolean photoRequired;
    
    @JsonProperty("customer_approval_required")
    private Boolean customerApprovalRequired;
    
    @JsonProperty("is_express_service")
    private Boolean isExpressService;
    
    @JsonProperty("is_premium_service")
    private Boolean isPremiumService;
    
    @JsonProperty("image_urls")
    private String imageUrls;
    
    @JsonProperty("tags")
    private String tags;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @JsonProperty("sort_order")
    private Integer sortOrder;
    
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    @JsonProperty("is_active")
    private Boolean isActive;
}
