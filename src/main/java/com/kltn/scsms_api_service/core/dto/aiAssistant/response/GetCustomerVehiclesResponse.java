package com.kltn.scsms_api_service.core.dto.aiAssistant.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCustomerVehiclesResponse {

    /**
     * Trạng thái: "HAS_VEHICLES" hoặc "NO_VEHICLES"
     */
    @JsonProperty("status")
    private String status;

    /**
     * Thông báo cho AI
     */
    @JsonProperty("message")
    private String message;

    /**
     * Danh sách xe của khách hàng
     */
    @JsonProperty("vehicles")
    private List<VehicleInfo> vehicles;

    /**
     * Thông tin xe
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        @JsonProperty("vehicle_id")
        private UUID vehicleId;

        @JsonProperty("license_plate")
        private String licensePlate;

        @JsonProperty("description")
        private String description;

        @JsonProperty("vehicle_brand_id")
        private UUID vehicleBrandId;

        @JsonProperty("vehicle_type_id")
        private UUID vehicleTypeId;

        @JsonProperty("vehicle_model_id")
        private UUID vehicleModelId;

        @JsonProperty("vehicle_year")
        private Integer vehicleYear;
    }
}

