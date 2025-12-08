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
@JsonClassDescription("Lấy danh sách xe của khách hàng đã đăng nhập. KHÔNG CẦN truyền customer_phone hoặc customer_id - function sẽ tự động lấy từ token đăng nhập. Nếu chưa có xe, yêu cầu tạo xe mới.")
public class GetCustomerVehiclesRequest {

    /**
     * Số điện thoại khách hàng - OPTIONAL (tự động lấy từ token nếu không có)
     */
    @JsonProperty("customer_phone")
    private String customerPhone;

    /**
     * Customer ID - OPTIONAL (tự động lấy từ token nếu không có)
     */
    @JsonProperty("customer_id")
    private UUID customerId;
}

