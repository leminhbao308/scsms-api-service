package com.kltn.scsms_api_service.core.dto.saleOrderManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleReturnLineInfoDto extends AuditDto {
    
    private UUID id;
    
    private ProductInfoDto product;
    
    private Long quantity; // nhập lại kho khi nhận hàng trả
}
