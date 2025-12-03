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
public class GetBranchesResponse {

    /**
     * Trạng thái: "SUCCESS" hoặc "NO_BRANCHES"
     */
    @JsonProperty("status")
    private String status;

    /**
     * Thông báo cho AI
     */
    @JsonProperty("message")
    private String message;

    /**
     * Danh sách chi nhánh
     */
    @JsonProperty("branches")
    private List<BranchInfo> branches;

    /**
     * Thông tin chi nhánh
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchInfo {
        @JsonProperty("branch_id")
        private UUID branchId;

        @JsonProperty("branch_name")
        private String branchName;

        @JsonProperty("branch_code")
        private String branchCode;

        @JsonProperty("address")
        private String address;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("email")
        private String email;
    }
}

