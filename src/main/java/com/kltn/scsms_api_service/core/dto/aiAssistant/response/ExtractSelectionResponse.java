package com.kltn.scsms_api_service.core.dto.aiAssistant.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO cho function extractUserSelection()
 * Chứa dữ liệu đã extract và validation results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractSelectionResponse {

    /**
     * Trạng thái extraction: "SUCCESS", "NEEDS_CLARIFICATION", "NO_SELECTION", "ERROR"
     */
    @JsonProperty("status")
    private String status;

    /**
     * Confidence score (0.0 - 1.0)
     * >= 0.8: High confidence, có thể dùng
     * < 0.8: Low confidence, cần clarification
     */
    @JsonProperty("confidence")
    private Double confidence;

    /**
     * Có cần clarification không
     */
    @JsonProperty("needs_clarification")
    private Boolean needsClarification;

    /**
     * Thông báo cho AI (nếu cần clarification)
     */
    @JsonProperty("message")
    private String message;

    /**
     * Extracted data
     */
    @JsonProperty("extracted_data")
    private ExtractedData extractedData;

    /**
     * Validation errors (nếu có)
     */
    @JsonProperty("validation_errors")
    @Builder.Default
    private List<String> validationErrors = new ArrayList<>();

    /**
     * Extracted data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedData {
        /**
         * Vehicle selection
         */
        @JsonProperty("vehicle")
        private VehicleSelection vehicle;

        /**
         * Date selection
         */
        @JsonProperty("date")
        private DateSelection date;

        /**
         * Branch selection
         */
        @JsonProperty("branch")
        private BranchSelection branch;

        /**
         * Service selection
         */
        @JsonProperty("service")
        private ServiceSelection service;

        /**
         * Bay selection
         */
        @JsonProperty("bay")
        private BaySelection bay;

        /**
         * Time selection
         */
        @JsonProperty("time")
        private TimeSelection time;

        /**
         * Intent: "SELECT", "CHANGE", "CONFIRM", "CANCEL", "UNKNOWN"
         */
        @JsonProperty("intent")
        private String intent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleSelection {
        @JsonProperty("vehicle_id")
        private UUID vehicleId;

        @JsonProperty("license_plate")
        private String licensePlate;

        @JsonProperty("selection_type")
        private String selectionType; // "LICENSE_PLATE", "INDEX", "DESCRIPTION"

        @JsonProperty("raw_text")
        private String rawText; // Text từ user message

        @JsonProperty("confidence")
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateSelection {
        @JsonProperty("date_time")
        private LocalDateTime dateTime;

        @JsonProperty("raw_text")
        private String rawText;

        @JsonProperty("confidence")
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchSelection {
        @JsonProperty("branch_id")
        private UUID branchId;

        @JsonProperty("branch_name")
        private String branchName;

        @JsonProperty("selection_type")
        private String selectionType; // "NAME", "INDEX", "ADDRESS"

        @JsonProperty("raw_text")
        private String rawText;

        @JsonProperty("confidence")
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceSelection {
        @JsonProperty("service_id")
        private UUID serviceId;

        @JsonProperty("service_name")
        private String serviceName;

        @JsonProperty("selection_type")
        private String selectionType; // "NAME", "INDEX", "KEYWORD"

        @JsonProperty("raw_text")
        private String rawText;

        @JsonProperty("confidence")
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaySelection {
        @JsonProperty("bay_id")
        private UUID bayId;

        @JsonProperty("bay_name")
        private String bayName;

        @JsonProperty("selection_type")
        private String selectionType; // "NAME", "INDEX"

        @JsonProperty("raw_text")
        private String rawText;

        @JsonProperty("confidence")
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSelection {
        @JsonProperty("time_slot")
        private String timeSlot; // Format: "HH:mm"

        @JsonProperty("raw_text")
        private String rawText;

        @JsonProperty("confidence")
        private Double confidence;
    }

    /**
     * Helper method để tạo response thành công
     */
    public static ExtractSelectionResponse success(ExtractedData data, Double confidence) {
        return ExtractSelectionResponse.builder()
                .status("SUCCESS")
                .confidence(confidence)
                .needsClarification(false)
                .extractedData(data)
                .build();
    }

    /**
     * Helper method để tạo response cần clarification
     */
    public static ExtractSelectionResponse needsClarification(String message, List<String> errors) {
        return ExtractSelectionResponse.builder()
                .status("NEEDS_CLARIFICATION")
                .confidence(0.0)
                .needsClarification(true)
                .message(message)
                .validationErrors(errors != null ? errors : new ArrayList<>())
                .build();
    }

    /**
     * Helper method để tạo response không có selection
     */
    public static ExtractSelectionResponse noSelection() {
        return ExtractSelectionResponse.builder()
                .status("NO_SELECTION")
                .confidence(0.0)
                .needsClarification(false)
                .message("No selection detected in user message")
                .build();
    }
}

