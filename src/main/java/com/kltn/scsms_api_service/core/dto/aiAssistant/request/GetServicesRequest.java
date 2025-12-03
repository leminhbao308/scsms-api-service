package com.kltn.scsms_api_service.core.dto.aiAssistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho function getServices()
 * Dùng để tìm kiếm dịch vụ theo keyword
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetServicesRequest {
    
    /**
     * Keyword để tìm kiếm dịch vụ
     * Ví dụ: "Rửa xe", "Bảo dưỡng", "Sửa chữa"
     */
    @JsonProperty("keyword")
    private String keyword;
    
    /**
     * Branch ID (optional)
     * Nếu có, chỉ tìm dịch vụ có sẵn tại chi nhánh này
     */
    @JsonProperty("branch_id")
    private String branchId;
}

