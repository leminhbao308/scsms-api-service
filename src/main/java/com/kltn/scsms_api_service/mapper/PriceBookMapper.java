package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.pricingManagement.PriceBookInfoDto;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreatePriceBookRequest;
import com.kltn.scsms_api_service.core.entity.PriceBook;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, PriceBookItemMapper.class}
)
public interface PriceBookMapper {
    
    PriceBook toEntity(CreatePriceBookRequest createPriceBookRequest);
    
    PriceBookInfoDto toPriceBookInfoDto(PriceBook priceBook);
}
