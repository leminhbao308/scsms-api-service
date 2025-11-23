package com.kltn.scsms_api_service.core.dto.aiAssistant.request;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Tạo booking thực sự trong database. Chỉ được gọi khi user xác nhận đặt lịch.")
public class CreateBookingRequest {
    
    /**
     * Số điện thoại khách hàng - BẮT BUỘC
     * Format: "0123456789" (không có dấu cách, dấu gạch ngang)
     */
    @JsonProperty("customer_phone")
    private String customerPhone;
    
    /**
     * Tên dịch vụ hoặc danh sách dịch vụ
     * Có thể là:
     * - String: "Rửa xe"
     * - Array: ["Rửa xe", "Ceramic"]
     */
    @JsonProperty("service_type")
    private Object serviceType; // String hoặc List<String>
    
    /**
     * Thời gian đặt lịch
     * Format: ISO "2025-11-21T08:00:00" hoặc ngôn ngữ tự nhiên
     */
    @JsonProperty("date_time")
    private String dateTime;
    
    /**
     * Branch ID - Optional (nếu có branch_name thì không cần)
     */
    @JsonProperty("branch_id")
    private UUID branchId;
    
    /**
     * Branch name - Optional
     * Tên chi nhánh (ví dụ: "Gò Vấp", "Quận 1")
     * Nếu branch_id null, function sẽ tự động tìm branch theo tên
     */
    @JsonProperty("branch_name")
    private String branchName;
    
    /**
     * Service Bay ID - Optional (nếu có bay_name thì không cần)
     */
    @JsonProperty("bay_id")
    private UUID bayId;
    
    /**
     * Service Bay name - Optional
     * Tên bay (ví dụ: "Bay rửa xe 1", "Bệ sửa chữa 2")
     * Nếu bay_id null, function sẽ tự động tìm bay theo tên (có thể kèm branch_id hoặc branch_name để tìm chính xác hơn)
     */
    @JsonProperty("bay_name")
    private String bayName;
    
    /**
     * Vehicle ID - Optional
     * Nếu không có, sẽ tìm theo licensePlate
     */
    @JsonProperty("vehicle_id")
    private UUID vehicleId;
    
    /**
     * Biển số xe - Optional
     * Nếu không có vehicleId, sẽ dùng licensePlate để tìm xe
     */
    @JsonProperty("vehicle_license_plate")
    private String vehicleLicensePlate;
    
    /**
     * Ghi chú của khách hàng - Optional
     */
    @JsonProperty("notes")
    private String notes;
}

