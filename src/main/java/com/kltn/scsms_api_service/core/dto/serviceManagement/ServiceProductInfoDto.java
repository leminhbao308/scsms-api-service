package com.kltn.scsms_api_service.core.dto.serviceManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceProductInfoDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("service_id")
    private UUID serviceId;

    @JsonProperty("product_id")
    private UUID productId;

    @JsonProperty("product_info")
    private ProductInfoDto productInfo; // Thông tin chi tiết sản phẩm

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("is_required")
    private Boolean isRequired;

    @JsonProperty("sort_order")
    private Integer sortOrder;

    @Builder.Default
    private AuditDto audit = AuditDto.builder().build();
}
