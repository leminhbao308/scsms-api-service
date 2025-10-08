package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.pricingManagement.PriceBookItemInfoDto;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreatePriceBookItemRequest;
import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class}
)
public interface PriceBookItemMapper {
    
    PriceBookItem toEntity(CreatePriceBookItemRequest createPriceBookItemRequest);
    
    PriceBookItemInfoDto toPriceBookItemInfoDto(PriceBookItem priceBookItem);
}
