package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.pricingManagement.PriceBookItemInfoDto;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreatePriceBookItemRequest;
import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class}
)
public abstract class PriceBookItemMapper {
    
    @Autowired
    protected ProductService productService;
    
    @Mapping(target = "product", source = "productId", qualifiedByName = "mapProductIdToProduct")
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "servicePackage", ignore = true)
    @Mapping(target = "priceBook", ignore = true)
    public abstract PriceBookItem toEntity(CreatePriceBookItemRequest createPriceBookItemRequest);
    
    public abstract PriceBookItemInfoDto toPriceBookItemInfoDto(PriceBookItem priceBookItem);
    
    @Named("mapProductIdToProduct")
    protected Product mapProductIdToProduct(UUID productId) {
        if (productId == null) {
            return null;
        }
        return productService.getRefByProductId(productId);
    }
}
