package com.kltn.scsms_api_service.core.dto.servicePackageManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateServicePackageRequest {
    
    @Size(min = 2, max = 500, message = "Package name must be between 2 and 500 characters")
    @JsonProperty("package_name")
    private String packageName;
    
    @Size(max = 1000, message = "Package URL must not exceed 1000 characters")
    @JsonProperty("package_url")
    private String packageUrl;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @JsonProperty("description")
    private String description;
    
    @Min(value = 1, message = "Total duration must be at least 1 minute")
    @JsonProperty("total_duration")
    private Integer totalDuration;
    
    @JsonProperty("service_package_type_id")
    private UUID servicePackageTypeId;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    
    @Valid
    @JsonProperty("package_services")
    private List<UpdateServicePackageServiceRequest> packageServices;
    
    @JsonProperty("service_process_id")
    private UUID serviceProcessId;
    
    @JsonProperty("is_default_process")
    private Boolean isDefaultProcess;
    
    @JsonProperty("branch_id")
    private UUID branchId;
}