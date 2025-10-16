package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.InventoryLevel;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.service.businessService.PricingBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.InventoryLevelEntityService;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.mapper.ProductMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller handling Catalog operations
 * Returns a paged list of {product, price, inventory}
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Catalog Management", description = "Catalog management endpoints")
public class CatalogController {
    private final ProductService productES;
    private final PricingBusinessService pricingBS;
    private final InventoryLevelEntityService invLevelES;
    
    private final ProductMapper productMapper;
    
    @GetMapping("/catalogs/for-sale")
    public ResponseEntity<ApiResponse<CatalogResponse>> forSale(
        @RequestParam UUID branchId,
        @RequestParam(defaultValue = "1") long qty,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "999999") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "true") boolean onlyAvailable
    ) {
        List<Product> products;
        if (StringUtils.hasText(keyword)) {
            products = productES.searchByKeyword(keyword);
        } else {
            products = productES.findAll();
        }
        
        List<CatalogItem> items = new ArrayList<>();
        for (Product p : products) {
            BigDecimal price = pricingBS.resolveUnitPrice(p.getProductId());
            InventoryLevel level = invLevelES.find(branchId, p.getProductId()).orElse(null);
            long onHand = level != null ? Optional.ofNullable(level.getOnHand()).orElse(0L) : 0L;
            long reserved = level != null ? Optional.ofNullable(level.getReserved()).orElse(0L) : 0L;
            long available = onHand - reserved;
            if (!onlyAvailable || available > 0) {
                ProductInfoDto pDto = productMapper.toProductInfoDto(p);
                
                items.add(new CatalogItem(pDto, price, new InventoryController.InventoryView(onHand, reserved, available)));
            }
        }
        
        return ResponseBuilder.success("Fetch catalog successfully", new CatalogResponse(items));
    }
    
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CatalogItem {
        private ProductInfoDto product;
        private BigDecimal price;
        private InventoryController.InventoryView inventory;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CatalogResponse {
        private List<CatalogItem> items;
    }
}
