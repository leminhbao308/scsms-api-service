package com.kltn.scsms_api_service.core.dto.priceManagement.response;

import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListScope;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceListHeaderResponse {
    private UUID priceListId;
    private String priceListCode;
    private String priceListName;
    private String description;
    private PriceListScope scope;
    private List<CenterItemResponse> centers;
    private List<BranchItemResponse> branches;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private PriceListStatus status;
    private List<CustomerRank> customerRanks;
    private Integer priority;
    private String currency;
    private String internalNotes;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<PriceListDetailResponse> details;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CenterItemResponse {
        private UUID centerId;
        private String centerName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchItemResponse {
        private UUID branchId;
        private String branchName;
    }
}
