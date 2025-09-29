package com.kltn.scsms_api_service.core.dto.inventoryManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.enumAttribute.InventoryStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.TransactionType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryFilterParam extends BaseFilterParam<InventoryFilterParam> {
    @JsonProperty("inventory_code")
    private String inventoryCode;
    
    @JsonProperty("transaction_type")
    private TransactionType transactionType;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("supplier_id")
    private UUID supplierId;
    
    private InventoryStatus status;
    
    @JsonProperty("from_date")
    private LocalDateTime fromDate;
    
    @JsonProperty("to_date")
    private LocalDateTime toDate;
    
    @JsonProperty("product_name")
    private String productName;
    
    @JsonProperty("product_sku")
    private String productSku;
    
    @Override
    protected void standardizeSpecificFields(InventoryFilterParam request) {
        request.setInventoryCode(trimAndNullify(inventoryCode));
        request.setProductName(trimAndNullify(productName));
        request.setProductSku(trimAndNullify(productSku));
        
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            LocalDateTime temp = fromDate;
            request.setFromDate(toDate);
            request.setToDate(temp);
        }
        
        super.standardizeSpecificFields(request);
    }
    
    @Override
    protected String getDefaultSortField() {
        return "createdAt";
    }
}
