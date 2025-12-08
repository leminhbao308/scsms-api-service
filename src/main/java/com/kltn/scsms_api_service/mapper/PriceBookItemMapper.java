package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.pricingManagement.PriceBookItemInfoDto;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreatePriceBookItemRequest;
import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, ProductMapper.class, ServiceMapper.class}
)
public abstract class PriceBookItemMapper {
    
    @Autowired
    protected ProductService productService;
    
    @Autowired
    protected ServiceService serviceService;
    
    /**
     * Map CreatePriceBookItemRequest to PriceBookItem entity
     * Handles product_id or service_id based on which is provided
     */
    @Mapping(target = "product", source = "productId", qualifiedByName = "mapProductIdToProduct")
    @Mapping(target = "service", source = "serviceId", qualifiedByName = "mapServiceIdToService")
    @Mapping(target = "priceBook", ignore = true)
    public abstract PriceBookItem toEntity(CreatePriceBookItemRequest createPriceBookItemRequest);
    
    @Mapping(target = "itemType", source = ".", qualifiedByName = "mapItemType")
    @Mapping(target = "itemId", source = ".", qualifiedByName = "mapItemId")
    @Mapping(target = "itemName", source = ".", qualifiedByName = "mapItemName")
    public abstract PriceBookItemInfoDto toPriceBookItemInfoDto(PriceBookItem priceBookItem);
    
    @Named("mapProductIdToProduct")
    protected Product mapProductIdToProduct(UUID productId) {
        if (productId == null) {
            return null;
        }
        return productService.getRefByProductId(productId);
    }
    
    @Named("mapServiceIdToService")
    protected Service mapServiceIdToService(UUID serviceId) {
        if (serviceId == null) {
            return null;
        }
        return serviceService.getRefById(serviceId);
    }
    
    
    @Named("mapItemType")
    protected String mapItemType(PriceBookItem priceBookItem) {
        return priceBookItem.getItemType().name();
    }
    
    @Named("mapItemId")
    protected String mapItemId(PriceBookItem priceBookItem) {
        return priceBookItem.getReferencedItemId().toString();
    }
    
    @Named("mapItemName")
    protected String mapItemName(PriceBookItem priceBookItem) {
        return priceBookItem.getReferencedItemName();
    }
}
