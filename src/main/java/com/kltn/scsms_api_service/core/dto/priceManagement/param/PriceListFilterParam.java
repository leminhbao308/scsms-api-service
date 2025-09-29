package com.kltn.scsms_api_service.core.dto.priceManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListScope;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceListFilterParam extends BaseFilterParam<PriceListFilterParam> {
    
    @JsonProperty("price_list_code")
    private String priceListCode;
    
    @JsonProperty("price_list_name")
    private String priceListName;
    
    private PriceListScope scope;
    
    @JsonProperty("center_id")
    private UUID centerId;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;
    
    @JsonProperty("expiration_date")
    private LocalDate expirationDate;
    
    private PriceListStatus status;
    
    @JsonProperty("customer_rank")
    private CustomerRank customerRank;
    
    private String currency;
    
    @Override
    protected String getDefaultSortField() {
        return super.getDefaultSortField();
    }
    
    @Override
    protected void standardizeSpecificFields(PriceListFilterParam request) {
        super.standardizeSpecificFields(request);
        
        request.setPriceListCode(trimAndNullify(priceListCode));
        request.setPriceListName(trimAndNullify(priceListName));
        request.setCurrency(trimAndNullify(currency));
        
        if (effectiveDate != null && expirationDate != null && effectiveDate.isAfter(expirationDate)) {
            LocalDate temp = effectiveDate;
            request.setEffectiveDate(expirationDate);
            request.setExpirationDate(temp);
        }
    }
}
