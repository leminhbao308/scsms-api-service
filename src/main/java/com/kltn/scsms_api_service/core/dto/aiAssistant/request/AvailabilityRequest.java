package com.kltn.scsms_api_service.core.dto.aiAssistant.request;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Kiểm tra slot trống cho dịch vụ. Tìm các slots phù hợp với service duration và đề xuất slots thay thế nếu hết chỗ.")
public class AvailabilityRequest {
    
    /**
     * Tên dịch vụ hoặc serviceId (UUID)
     * Ví dụ: "Rửa xe", "Ceramic", "Phủ bóng"
     */
    @JsonProperty("service_type")
    private String serviceType;
    
    /**
     * Thời gian mong muốn
     * Có thể là:
     * - ISO format: "2025-11-21T08:00:00"
     * - Ngôn ngữ tự nhiên: "sáng mai", "chiều nay", "ngày 15/11"
     * AI sẽ parse và chuyển thành LocalDateTime
     */
    @JsonProperty("date_time")  
    private String dateTime;
    
    /**
     * Branch ID (UUID format như '7cd17e0d-529d-48ef-9094-67103811651d') hoặc tên chi nhánh - Optional
     * Nếu không có, AI sẽ suggest tất cả branches
     * Lưu ý: Hệ thống sẽ tự động parse - nếu là UUID thì dùng trực tiếp, nếu là tên thì tìm kiếm
     * UUID của chi nhánh (ví dụ: '7cd17e0d-529d-48ef-9094-67103811651d'). Nếu không tìm thấy UUID, có thể để null hoặc để trống.
     */
    @JsonProperty("branch_id")
    private String branchId;
    
    /**
     * Branch name - Optional
     * Tên chi nhánh (ví dụ: "Gò Vấp", "Quận 1")
     * Nếu branch_id null, function sẽ tự động tìm branch theo tên
     */
    @JsonProperty("branch_name")
    private String branchName;
    
    /**
     * Vehicle model - Optional
     * Để xác định bay size phù hợp (ví dụ: "Camry", "SUV")
     */
    @JsonProperty("vehicle_model")
    private String vehicleModel;
    
    /**
     * Danh sách service types - Optional
     * Nếu user muốn đặt nhiều dịch vụ cùng lúc
     */
    @JsonProperty("service_types")
    private List<String> serviceTypes;
}

