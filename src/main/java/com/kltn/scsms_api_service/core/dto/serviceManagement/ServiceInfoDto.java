package com.kltn.scsms_api_service.core.dto.serviceManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessInfoDto;
import com.kltn.scsms_api_service.core.entity.Service;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceInfoDto {
    
    @JsonProperty("service_id")
    private UUID serviceId;
    
    @JsonProperty("service_url")
    private String serviceUrl;
    
    @JsonProperty("service_name")
    private String serviceName;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("category_name")
    private String categoryName;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("required_skill_level")
    private Service.SkillLevel requiredSkillLevel;
    
    @JsonProperty("service_type_id")
    private UUID serviceTypeId;
    
    @JsonProperty("service_type_name")
    private String serviceTypeName;
    
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("service_process_id")
    private UUID serviceProcessId;
    
    @JsonProperty("service_process_name")
    private String serviceProcessName;
    
    @JsonProperty("service_process_code")
    private String serviceProcessCode;
    
    @JsonProperty("is_default_process")
    private Boolean isDefaultProcess;
    
    @JsonProperty("estimated_duration")
    private Integer estimatedDuration;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("branch_name")
    private String branchName;
    
    // Danh sách sản phẩm của service
    @JsonProperty("service_products")
    private List<ServiceProductInfoDto> serviceProducts;
    
    // Thông tin quy trình dịch vụ (nếu có)
    @JsonProperty("service_process")
    private ServiceProcessInfoDto serviceProcess;
    
    @Builder.Default
    private AuditDto audit = AuditDto.builder().build();
}