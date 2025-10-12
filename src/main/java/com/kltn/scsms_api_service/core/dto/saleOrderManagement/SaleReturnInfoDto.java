package com.kltn.scsms_api_service.core.dto.saleOrderManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.dto.warehouseManagement.response.WarehouseInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleReturnInfoDto extends AuditDto {
    private String id;
    
    @JsonProperty("sales_order")
    private SaleOrderInfoDto salesOrder;
    
    private WarehouseInfoDto warehouse;
    
    private List<SaleReturnLineInfoDto> lines;
}
