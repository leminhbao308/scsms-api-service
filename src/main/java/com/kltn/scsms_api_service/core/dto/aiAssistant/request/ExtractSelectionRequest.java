package com.kltn.scsms_api_service.core.dto.aiAssistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.BookingDraft;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO cho function extractUserSelection()
 * Chứa thông tin cần thiết để AI extract lựa chọn từ user message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractSelectionRequest {

    /**
     * User message cần extract
     */
    @JsonProperty("user_message")
    private String userMessage;

    /**
     * Draft context hiện tại (để AI biết đã có gì)
     */
    @JsonProperty("draft_context")
    private DraftContext draftContext;

    /**
     * Available options từ các function calls trước đó
     */
    @JsonProperty("available_options")
    private AvailableOptions availableOptions;

    /**
     * Current step trong booking process (1-7)
     */
    @JsonProperty("current_step")
    private Integer currentStep;

    /**
     * Draft context summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DraftContext {
        @JsonProperty("draft_id")
        private UUID draftId;

        @JsonProperty("has_vehicle")
        private Boolean hasVehicle;

        @JsonProperty("has_date")
        private Boolean hasDate;

        @JsonProperty("has_branch")
        private Boolean hasBranch;

        @JsonProperty("has_service")
        private Boolean hasService;

        @JsonProperty("has_bay")
        private Boolean hasBay;

        @JsonProperty("has_time")
        private Boolean hasTime;

        @JsonProperty("current_step")
        private Integer currentStep;
    }

    /**
     * Available options từ tool responses
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableOptions {
        /**
         * Danh sách vehicles (từ getCustomerVehicles response)
         */
        @JsonProperty("vehicles")
        private List<VehicleOption> vehicles;

        /**
         * Danh sách branches (từ getBranches response)
         */
        @JsonProperty("branches")
        private List<BranchOption> branches;

        /**
         * Danh sách services (từ getServices response)
         */
        @JsonProperty("services")
        private List<ServiceOption> services;

        /**
         * Danh sách bays (từ checkAvailability response)
         */
        @JsonProperty("bays")
        private List<BayOption> bays;

        /**
         * Danh sách time slots (từ checkAvailability response)
         */
        @JsonProperty("time_slots")
        private List<String> timeSlots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleOption {
        @JsonProperty("vehicle_id")
        private UUID vehicleId;

        @JsonProperty("license_plate")
        private String licensePlate;

        @JsonProperty("index")
        private Integer index; // Số thứ tự (1-based) để hỗ trợ "xe đầu tiên", "xe thứ 2"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchOption {
        @JsonProperty("branch_id")
        private UUID branchId;

        @JsonProperty("branch_name")
        private String branchName;

        @JsonProperty("address")
        private String address;

        @JsonProperty("index")
        private Integer index;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceOption {
        @JsonProperty("service_id")
        private UUID serviceId;

        @JsonProperty("service_name")
        private String serviceName;

        @JsonProperty("index")
        private Integer index;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BayOption {
        @JsonProperty("bay_id")
        private UUID bayId;

        @JsonProperty("bay_name")
        private String bayName;

        @JsonProperty("index")
        private Integer index;
    }

    /**
     * Factory method để tạo request từ BookingDraft và available options
     */
    public static ExtractSelectionRequest fromDraft(
            BookingDraft draft,
            String userMessage,
            AvailableOptions availableOptions) {
        DraftContext context = DraftContext.builder()
                .draftId(draft.getDraftId())
                .hasVehicle(draft.hasVehicle())
                .hasDate(draft.hasDate())
                .hasBranch(draft.hasBranch())
                .hasService(draft.hasService())
                .hasBay(draft.hasBay())
                .hasTime(draft.hasTime())
                .currentStep(draft.getCurrentStep())
                .build();

        return ExtractSelectionRequest.builder()
                .userMessage(userMessage)
                .draftContext(context)
                .availableOptions(availableOptions)
                .currentStep(draft.getCurrentStep())
                .build();
    }
}

