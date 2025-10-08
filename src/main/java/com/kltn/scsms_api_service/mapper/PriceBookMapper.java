package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.pricingManagement.PriceBookInfoDto;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreatePriceBookRequest;
import com.kltn.scsms_api_service.core.entity.PriceBook;
import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, PriceBookItemMapper.class}
)
public abstract class PriceBookMapper {
    
    @Autowired
    protected PriceBookItemMapper priceBookItemMapper;
    
    @Mapping(target = "items", ignore = true)
    public abstract PriceBook toEntity(CreatePriceBookRequest createPriceBookRequest);
    
    public abstract PriceBookInfoDto toPriceBookInfoDto(PriceBook priceBook);
    
    /**
     * Map CreatePriceBookRequest to PriceBook with proper item relationships
     */
    public PriceBook toEntityWithItems(CreatePriceBookRequest createPriceBookRequest) {
        PriceBook priceBook = toEntity(createPriceBookRequest);
        
        if (createPriceBookRequest.getItems() != null) {
            List<PriceBookItem> items = createPriceBookRequest.getItems().stream()
                .map(itemRequest -> {
                    PriceBookItem item = priceBookItemMapper.toEntity(itemRequest);
                    item.setPriceBook(priceBook); // Set the relationship
                    return item;
                })
                .toList();
            priceBook.setItems(items);
        }
        
        return priceBook;
    }
}
