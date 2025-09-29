package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.priceManagement.request.PriceListDetailRequest;
import com.kltn.scsms_api_service.core.dto.priceManagement.response.PriceListDetailResponse;
import com.kltn.scsms_api_service.core.entity.PriceListDetail;
import com.kltn.scsms_api_service.core.entity.PriceListHeader;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PriceListDetailMapper {
    
    /**
     * Maps a PriceListDetailRequest to a new PriceListDetail entity
     */
    @Mapping(target = "priceListDetailId", ignore = true)
    @Mapping(target = "priceListHeader", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "servicePackage", ignore = true)
    @Mapping(source = "itemType", target = "itemType")
    @Mapping(source = "itemId", target = "itemId")
    @Mapping(source = "itemName", target = "itemName")
    @Mapping(source = "basePrice", target = "basePrice")
    @Mapping(source = "sellingPrice", target = "sellingPrice")
    @Mapping(source = "minPrice", target = "minPrice")
    @Mapping(source = "maxPrice", target = "maxPrice")
    @Mapping(source = "unitOfMeasure", target = "unitOfMeasure")
    @Mapping(source = "minQuantity", target = "minQuantity", defaultValue = "1")
    @Mapping(source = "maxQuantity", target = "maxQuantity")
    @Mapping(source = "tierPricing", target = "tierPricing")
    @Mapping(source = "conditions", target = "conditions")
    @Mapping(source = "notes", target = "notes")
    PriceListDetail toEntity(PriceListDetailRequest request);

    /**
     * Maps a list of PriceListDetailRequest objects to PriceListDetail entities
     */
    List<PriceListDetail> toEntityList(List<PriceListDetailRequest> requestList);

    /**
     * Creates a PriceListDetail with header reference already set
     */
    default PriceListDetail toEntityWithHeader(PriceListDetailRequest request, PriceListHeader header) {
        PriceListDetail detail = toEntity(request);
        detail.setPriceListHeader(header);
        return detail;
    }

    /**
     * Maps a PriceListDetail entity to a response DTO
     */
    @Mapping(source = "priceListDetailId", target = "priceListDetailId")
    @Mapping(source = "priceListHeader.priceListId", target = "priceListId")
    @Mapping(source = "itemType", target = "itemType")
    @Mapping(source = "itemId", target = "itemId")
    @Mapping(source = "itemName", target = "itemName")
    @Mapping(source = "basePrice", target = "basePrice")
    @Mapping(source = "sellingPrice", target = "sellingPrice")
    @Mapping(source = "minPrice", target = "minPrice")
    @Mapping(source = "maxPrice", target = "maxPrice")
    @Mapping(source = "unitOfMeasure", target = "unitOfMeasure")
    @Mapping(source = "minQuantity", target = "minQuantity")
    @Mapping(source = "maxQuantity", target = "maxQuantity")
    @Mapping(source = "tierPricing", target = "tierPricing")
    @Mapping(source = "conditions", target = "conditions")
    @Mapping(source = "notes", target = "notes")
    PriceListDetailResponse toResponse(PriceListDetail detail);

    /**
     * Maps a list of PriceListDetail entities to response DTOs
     */
    List<PriceListDetailResponse> toResponseList(List<PriceListDetail> detailList);

    /**
     * Updates an existing PriceListDetail entity with data from the request
     */
    @Mapping(target = "priceListDetailId", ignore = true)
    @Mapping(target = "priceListHeader", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "servicePackage", ignore = true)
    void updateEntityFromRequest(PriceListDetailRequest request, @MappingTarget PriceListDetail detail);
}
