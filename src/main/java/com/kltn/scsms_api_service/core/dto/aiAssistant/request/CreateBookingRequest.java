package com.kltn.scsms_api_service.core.dto.aiAssistant.request;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Tạo booking thực sự trong database. Chỉ được gọi khi user xác nhận đặt lịch.")
public class CreateBookingRequest {

    /**
     * Số điện thoại khách hàng - Optional (chỉ dùng khi không có token authentication)
     * Format: "0123456789" (không có dấu cách, dấu gạch ngang)
     * Lưu ý: Nếu đã đăng nhập, hệ thống sẽ tự động lấy thông tin từ token, không cần truyền field này.
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
     * Branch ID (UUID format như '7cd17e0d-529d-48ef-9094-67103811651d') hoặc tên chi nhánh - Optional
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
     * Service Bay ID (UUID format) hoặc tên bay - Optional
     * Lưu ý: Hệ thống sẽ tự động parse - nếu là UUID thì dùng trực tiếp, nếu là tên thì tìm kiếm
     * UUID của service bay. Nếu không tìm thấy UUID, có thể để null hoặc để trống.
     */
    @JsonProperty("bay_id")
    private String bayId;

    /**
     * Service Bay name - Optional
     * Tên bay (ví dụ: "Bay rửa xe 1", "Bệ sửa chữa 2")
     * Nếu bay_id null, function sẽ tự động tìm bay theo tên (có thể kèm branch_id hoặc branch_name để tìm chính xác hơn)
     */
    @JsonProperty("bay_name")
    private String bayName;

    /**
     * Vehicle ID (UUID format) - Optional
     * Nếu không có, sẽ tìm theo licensePlate
     * Lưu ý: Hệ thống sẽ tự động parse - nếu là UUID thì dùng trực tiếp
     * UUID của vehicle. Nếu không tìm thấy UUID, có thể để null hoặc để trống.
     */
    @JsonProperty("vehicle_id")
    private String vehicleId;

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

