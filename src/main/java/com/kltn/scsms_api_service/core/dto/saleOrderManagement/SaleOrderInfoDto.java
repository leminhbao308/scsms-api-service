package com.kltn.scsms_api_service.core.dto.saleOrderManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.branchManagement.BranchInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.dto.userManagement.UserInfoDto;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleOrderInfoDto extends AuditDto {
    private UUID id;

    private UserInfoDto customer;

    private BranchInfoDto branch;

    private SalesStatus status;

    private List<SaleOrderLineInfoDto> lines;

    // ===== DISCOUNT TRACKING FIELDS =====

    @JsonProperty("original_amount")
    private BigDecimal originalAmount;

    @JsonProperty("total_discount_amount")
    private BigDecimal totalDiscountAmount;

    @JsonProperty("final_amount")
    private BigDecimal finalAmount;

    @JsonProperty("discount_percentage")
    private BigDecimal discountPercentage;

    @JsonProperty("promotion_snapshot")
    private String promotionSnapshot; // JSON array of applied promotions
}
