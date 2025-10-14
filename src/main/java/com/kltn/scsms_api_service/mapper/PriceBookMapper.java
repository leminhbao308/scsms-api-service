package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.pricingManagement.PriceBookInfoDto;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreatePriceBookItemRequest;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreatePriceBookRequest;
import com.kltn.scsms_api_service.core.entity.PriceBook;
import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.service.entityService.ServicePackageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, PriceBookItemMapper.class}
)
public abstract class PriceBookMapper {
    
    @Autowired
    protected ProductService productService;
    
    @Autowired
    protected ServiceService serviceService;
    
    @Autowired
    protected ServicePackageService servicePackageService;
    
    /**
     * Map CreatePriceBookRequest to PriceBook entity (basic fields only)
     */
    @Mapping(target = "items", ignore = true)
    public abstract PriceBook toEntity(CreatePriceBookRequest request);
    
    /**
     * Map CreatePriceBookRequest to PriceBook entity WITH items
     * This method manually handles the items mapping to properly link Product/Service/ServicePackage
     */
    public PriceBook toEntityWithItems(CreatePriceBookRequest request) {
        // Map basic fields
        PriceBook priceBook = toEntity(request);
        
        // Manually map items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<PriceBookItem> items = request.getItems().stream()
                .map(itemReq -> mapRequestToItem(itemReq, priceBook))
                .collect(Collectors.toList());
            
            priceBook.setItems(items);
        } else {
            priceBook.setItems(new ArrayList<>());
        }
        
        return priceBook;
    }
    
    /**
     * Map individual CreatePriceBookItemRequest to PriceBookItem
     * Determines which entity (Product/Service/ServicePackage) to load based on provided ID
     */
    private PriceBookItem mapRequestToItem(CreatePriceBookItemRequest itemReq, PriceBook priceBook) {
        PriceBookItem item = PriceBookItem.builder()
            .priceBook(priceBook)
            .policyType(itemReq.getPolicyType())
            .fixedPrice(itemReq.getFixedPrice())
            .markupPercent(itemReq.getMarkupPercent())
            .build();
        
        // Set the appropriate reference based on which ID is provided
        if (itemReq.getProductId() != null) {
            Product product = productService.getRefByProductId(itemReq.getProductId());
            item.setProduct(product);
        } else if (itemReq.getServiceId() != null) {
            Service service = serviceService.getRefById(itemReq.getServiceId());
            item.setService(service);
        } else if (itemReq.getServicePackageId() != null) {
            ServicePackage servicePackage = servicePackageService.getRefById(itemReq.getServicePackageId());
            item.setServicePackage(servicePackage);
        } else {
            throw new IllegalArgumentException(
                "PriceBookItem must have one of: product_id, service_id, or service_package_id"
            );
        }
        
        return item;
    }
    
    /**
     * Map PriceBook entity to DTO
     */
    public abstract PriceBookInfoDto toPriceBookInfoDto(PriceBook priceBook);
}
