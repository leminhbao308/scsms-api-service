package com.kltn.scsms_api_service.core.dto.aiAssistant.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO cho function getServices()
 * Trả về danh sách dịch vụ tìm được
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetServicesResponse {
    
    /**
     * Trạng thái: "FOUND", "NOT_FOUND", "MULTIPLE_FOUND"
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * Thông báo cho AI
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Danh sách dịch vụ tìm được
     */
    @JsonProperty("services")
    private List<ServiceInfo> services;
    
    /**
     * Thông tin dịch vụ
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {
        @JsonProperty("service_id")
        private UUID serviceId;
        
        @JsonProperty("service_name")
        private String serviceName;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("estimated_duration")
        private Integer estimatedDuration; // Phút
        
        @JsonProperty("price")
        private BigDecimal price;
        
        @JsonProperty("service_type_id")
        private UUID serviceTypeId;
        
        @JsonProperty("service_type_name")
        private String serviceTypeName;
    }
}

