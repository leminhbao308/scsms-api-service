package com.kltn.scsms_api_service.core.dto.saleOrderManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleOrderLineInfoDto extends AuditDto {
    private String id;

    private ProductInfoDto product;

    private Long quantity;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("is_free_item")
    private Boolean isFreeItem;

    // Service item fields
    @JsonProperty("service_id")
    private UUID serviceId;

    @JsonProperty("is_service_item")
    private Boolean isServiceItem;

    // Booking context fields
    @JsonProperty("original_booking_id")
    private UUID originalBookingId;

    @JsonProperty("original_booking_code")
    private String originalBookingCode;
}
