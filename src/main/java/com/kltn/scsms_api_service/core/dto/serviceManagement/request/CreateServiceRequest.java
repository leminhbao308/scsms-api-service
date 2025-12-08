package com.kltn.scsms_api_service.core.dto.serviceManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.CreateServiceProcessRequest;
import com.kltn.scsms_api_service.core.entity.Service;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

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
    
    @Min(value = 1, message = "Estimated duration must be at least 1 minute")
    @JsonProperty("estimated_duration")
    private Integer estimatedDuration;
    
    @JsonProperty("required_skill_level")
    private Service.SkillLevel requiredSkillLevel;
    
    
    
    @JsonProperty("service_type_id")
    private UUID serviceTypeId;
    
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    @JsonProperty("service_process_id")
    private UUID serviceProcessId;
    
    @JsonProperty("is_default_process")
    private Boolean isDefaultProcess;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    // Danh sách sản phẩm cho service
    @JsonProperty("service_products")
    @Valid
    private List<CreateServiceProductRequest> serviceProducts;
    
    // Thông tin quy trình dịch vụ (nếu muốn tạo mới)
    @JsonProperty("service_process")
    @Valid
    private CreateServiceProcessRequest serviceProcess;
    
}