package com.kltn.scsms_api_service.core.dto.priceManagement.request;

import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListScope;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceListHeaderRequest {
    private String priceListCode;
    private String priceListName;
    private String description;
    private PriceListScope scope;
    private List<UUID> centerIds;
    private List<UUID> branchIds;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private PriceListStatus status;
    private List<CustomerRank> customerRanks;
    private Integer priority;
    private String currency;
    private String internalNotes;
    private List<PriceListDetailRequest> details;
}
