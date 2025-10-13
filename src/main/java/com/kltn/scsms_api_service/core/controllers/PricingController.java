package com.kltn.scsms_api_service.core.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.pricingManagement.PriceBookInfoDto;
import com.kltn.scsms_api_service.core.dto.pricingManagement.PriceBookItemInfoDto;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreatePriceBookItemRequest;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreatePriceBookRequest;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreateServicePriceBookItemRequest;
import com.kltn.scsms_api_service.core.dto.pricingManagement.request.CreateServicePackagePriceBookItemRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.ServicePricingDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackagePricingDto;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.PriceBook;
import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import com.kltn.scsms_api_service.core.entity.ServicePackage;
import com.kltn.scsms_api_service.core.service.businessService.PricingBusinessService;
import com.kltn.scsms_api_service.core.service.businessService.ServicePricingService;
import com.kltn.scsms_api_service.core.service.businessService.ServicePackagePricingService;
import com.kltn.scsms_api_service.core.service.entityService.PriceBookEntityService;
import com.kltn.scsms_api_service.core.service.entityService.PriceBookItemEntityService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.service.entityService.ServicePackageService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.mapper.PriceBookItemMapper;
import com.kltn.scsms_api_service.mapper.PriceBookMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling Pricing operations
 * Manages pricing strategies, discounts, and promotions
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pricing Management", description = "Pricing management endpoints")
public class PricingController {
    
    private final PricingBusinessService pricingBS;
    private final PriceBookEntityService priceBookES;
    private final PriceBookItemEntityService priceBookItemES;
    private final ServicePricingService servicePricingService;
    private final ServicePackagePricingService servicePackagePricingService;
    private final ServiceService serviceService;
    private final ServicePackageService servicePackageService;
    
    private final PriceBookMapper priceBookMapper;
    private final PriceBookItemMapper priceBookItemMapper;
    
    
    @PostMapping("/pricing/preview")
    public ResponseEntity<ApiResponse<PricingPreviewItemResponse>> preview(@RequestBody PricingPreviewItemRequest req) {
        return ResponseBuilder.success("Fetch price preview successfully",
            new PricingPreviewItemResponse(
                req.getProductId(),
                req.getQty(),
                pricingBS.calcLineTotal(req.getProductId(), req.getQty())));
    }
    
    
    @PostMapping("/pricing/books/create")
    public ResponseEntity<ApiResponse<PriceBookInfoDto>> createBook(@RequestBody CreatePriceBookRequest priceBookRequest) {
        PriceBook book = priceBookMapper.toEntityWithItems(priceBookRequest);
        
        PriceBookInfoDto createdBook = priceBookMapper.toPriceBookInfoDto(
            priceBookES.create(book));
        
        return ResponseBuilder.created("Create price book successfully",
            createdBook);
    }
    
    
    @PostMapping("/pricing/books/{bookId}/create-item")
    public ResponseEntity<ApiResponse<PriceBookItemInfoDto>> createItem(@PathVariable UUID bookId, @RequestBody CreatePriceBookItemRequest priceBookItemRequest) {
        PriceBook book = priceBookES.getRefById(bookId);
        
        PriceBookItem item = priceBookItemMapper.toEntity(priceBookItemRequest);
        item.setPriceBook(book);
        
        PriceBookItemInfoDto createdItem = priceBookItemMapper.toPriceBookItemInfoDto(
            priceBookItemES.create(item));
        
        return ResponseBuilder.created("Create price book item successfully",
            createdItem);
    }
    
    @PostMapping("/pricing/books/update/{bookId}")
    public ResponseEntity<ApiResponse<PriceBookInfoDto>> updateBook(@PathVariable UUID bookId, @RequestBody CreatePriceBookRequest priceBookRequest) {
        PriceBook book = priceBookES.require(bookId);
        
        PriceBookInfoDto updatedBook = priceBookMapper.toPriceBookInfoDto(
            priceBookES.update(book, priceBookMapper.toEntityWithItems(priceBookRequest)));
        
        return ResponseBuilder.success("Updated price book successfully",
            updatedBook);
    }
    
    
    @PostMapping("/pricing/books/{bookId}/update-item/{itemId}")
    public ResponseEntity<ApiResponse<PriceBookItemInfoDto>> updateItem(
        @PathVariable UUID bookId,
        @PathVariable UUID itemId,
        @RequestBody CreatePriceBookItemRequest priceBookItemRequest) {
        PriceBook book = priceBookES.getRefById(bookId);
        
        PriceBookItem item = priceBookItemES.require(itemId);
        item.setPriceBook(book);
        
        PriceBookItemInfoDto createdItem = priceBookItemMapper.toPriceBookItemInfoDto(
            priceBookItemES.update(item, priceBookItemMapper.toEntity(priceBookItemRequest)));
        
        return ResponseBuilder.created("Create price book item successfully",
            createdItem);
    }
    
    @PostMapping("/pricing/preview-batch")
    public ResponseEntity<ApiResponse<PricingPreviewBatchResponse>> previewBatch(@RequestBody PricingPreviewBatchRequest req) {
        PricingPreviewBatchResponse result = new PricingPreviewBatchResponse();
        
        // calculate each line total price
        result.setItems(req.getItems().stream().map(i -> new PricingPreviewItemResponse(
            i.getProductId(),
            i.getQty(),
            pricingBS.calcLineTotal(i.getProductId(), i.getQty())
        )).toList());
        
        // calculate grand total
        result.setGrandTotal(result.getItems().stream()
            .map(PricingPreviewItemResponse::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        return ResponseBuilder.success("Fetch batch price preview successfully", result);
    }
    
    @GetMapping("/pricing/books/{bookId}")
    public ResponseEntity<ApiResponse<PriceBookInfoDto>> getBook(@PathVariable UUID bookId) {
        PriceBook book = priceBookES.require(bookId);
        return ResponseBuilder.success("Fetch price book successfully",
            priceBookMapper.toPriceBookInfoDto(book));
    }
    
    @GetMapping("/pricing/books/active")
    public ResponseEntity<ApiResponse<List<PriceBookInfoDto>>> getActiveBooks(
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to) {
        List<PriceBook> books = priceBookES.getActivePriceBooksInRange(
            from != null ? java.time.LocalDateTime.parse(from) : null,
            to != null ? java.time.LocalDateTime.parse(to) : null);
        
        List<PriceBookInfoDto> bookDtos = books.stream()
            .map(priceBookMapper::toPriceBookInfoDto).toList();
        
        return ResponseBuilder.success("Fetch active price books successfully",
            bookDtos);
    }
    
    @GetMapping("/pricing/books/get-all")
    public ResponseEntity<ApiResponse<List<PriceBookInfoDto>>> getAllBooks() {
        List<PriceBook> books = priceBookES.getPriceBooksInRange(
            null,
            null);
        
        List<PriceBookInfoDto> bookDtos = books.stream()
            .map(priceBookMapper::toPriceBookInfoDto).toList();
        
        return ResponseBuilder.success("Fetch all price books successfully",
            bookDtos);
    }
    
    // ========== SERVICE PRICING MANAGEMENT ==========
    
    @GetMapping("/pricing/services/{serviceId}")
    public ResponseEntity<ApiResponse<ServicePricingDto>> getServicePricing(
            @PathVariable UUID serviceId,
            @RequestParam(required = false) UUID priceBookId) {
        log.info("Getting service pricing for service: {}, priceBook: {}", serviceId, priceBookId);
        ServicePricingDto pricing = servicePricingService.getServicePricing(serviceId, priceBookId);
        return ResponseBuilder.success("Get service pricing successfully", pricing);
    }
    
    // ========== BRANCH-SPECIFIC PRICING MANAGEMENT ==========
    
    @GetMapping("/pricing/branches/{branchId}/services/{serviceId}")
    public ResponseEntity<ApiResponse<ServicePricingDto>> getServicePricingForBranch(
            @PathVariable UUID branchId,
            @PathVariable UUID serviceId,
            @RequestParam(required = false) UUID priceBookId) {
        log.info("Getting service pricing for branch: {}, service: {}, priceBook: {}", branchId, serviceId, priceBookId);
        ServicePricingDto pricing = servicePricingService.getServicePricingForBranch(serviceId, branchId, priceBookId);
        return ResponseBuilder.success("Get service pricing for branch successfully", pricing);
    }
    
    @PostMapping("/pricing/services/{serviceId}/recalculate")
    public ResponseEntity<ApiResponse<ServicePricingDto>> recalculateServicePricing(
            @PathVariable UUID serviceId,
            @RequestParam(required = false) UUID priceBookId) {
        log.info("Recalculating service pricing for service: {}, priceBook: {}", serviceId, priceBookId);
        ServicePricingDto pricing = servicePricingService.recalculateBasePrice(serviceId, priceBookId);
        return ResponseBuilder.success("Recalculate service pricing successfully", pricing);
    }
    
    // ========== SERVICE PACKAGE PRICING MANAGEMENT ==========
    
    @GetMapping("/pricing/service-packages/{packageId}")
    public ResponseEntity<ApiResponse<ServicePackagePricingDto>> getServicePackagePricing(
            @PathVariable UUID packageId,
            @RequestParam(required = false) UUID priceBookId) {
        log.info("Getting service package pricing for package: {}, priceBook: {}", packageId, priceBookId);
        ServicePackagePricingDto pricing = servicePackagePricingService.getServicePackagePricing(packageId, priceBookId);
        return ResponseBuilder.success("Get service package pricing successfully", pricing);
    }
    
    @GetMapping("/pricing/branches/{branchId}/service-packages/{packageId}")
    public ResponseEntity<ApiResponse<ServicePackagePricingDto>> getServicePackagePricingForBranch(
            @PathVariable UUID branchId,
            @PathVariable UUID packageId,
            @RequestParam(required = false) UUID priceBookId) {
        log.info("Getting service package pricing for branch: {}, package: {}, priceBook: {}", branchId, packageId, priceBookId);
        ServicePackagePricingDto pricing = servicePackagePricingService.getServicePackagePricingForBranch(packageId, branchId, priceBookId);
        return ResponseBuilder.success("Get service package pricing for branch successfully", pricing);
    }
    
    @PostMapping("/pricing/service-packages/{packageId}/recalculate")
    public ResponseEntity<ApiResponse<ServicePackagePricingDto>> recalculateServicePackagePricing(
            @PathVariable UUID packageId,
            @RequestParam(required = false) UUID priceBookId) {
        log.info("Recalculating service package pricing for package: {}, priceBook: {}", packageId, priceBookId);
        ServicePackagePricingDto pricing = servicePackagePricingService.recalculatePackagePrice(packageId, priceBookId);
        return ResponseBuilder.success("Recalculate service package pricing successfully", pricing);
    }
    
    // ========== PRICE BOOK ITEM MANAGEMENT ==========
    
    @PostMapping("/pricing/books/{bookId}/create-service-item")
    public ResponseEntity<ApiResponse<PriceBookItemInfoDto>> createServiceItem(
            @PathVariable UUID bookId,
            @RequestBody CreateServicePriceBookItemRequest request) {
        log.info("Creating service price book item for book: {}, service: {}", bookId, request.getServiceId());
        
        PriceBook book = priceBookES.require(bookId);
        
        // Load existing Service entity from database
        com.kltn.scsms_api_service.core.entity.Service service = serviceService.getById(request.getServiceId());
        
        PriceBookItem item = PriceBookItem.builder()
            .priceBook(book)
            .service(service)
            .policyType(request.getPolicyType())
            .fixedPrice(request.getFixedPrice())
            .markupPercent(request.getMarkupPercent())
            .build();
        
        PriceBookItemInfoDto createdItem = priceBookItemMapper.toPriceBookItemInfoDto(
            priceBookItemES.create(item));
        
        return ResponseBuilder.created("Create service price book item successfully", createdItem);
    }
    
    @PostMapping("/pricing/books/{bookId}/create-service-package-item")
    public ResponseEntity<ApiResponse<PriceBookItemInfoDto>> createServicePackageItem(
            @PathVariable UUID bookId,
            @RequestBody CreateServicePackagePriceBookItemRequest request) {
        log.info("Creating service package price book item for book: {}, package: {}", bookId, request.getServicePackageId());
        
        PriceBook book = priceBookES.require(bookId);
        
        // Load existing ServicePackage entity from database
        ServicePackage servicePackage = servicePackageService.getById(request.getServicePackageId());
        
        PriceBookItem item = PriceBookItem.builder()
            .priceBook(book)
            .servicePackage(servicePackage)
            .policyType(request.getPolicyType())
            .fixedPrice(request.getFixedPrice())
            .markupPercent(request.getMarkupPercent())
            .build();
        
        PriceBookItemInfoDto createdItem = priceBookItemMapper.toPriceBookItemInfoDto(
            priceBookItemES.create(item));
        
        return ResponseBuilder.created("Create service package price book item successfully", createdItem);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingPreviewBatchRequest {
        private List<PricingPreviewItemRequest> items;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingPreviewItemRequest {
        @JsonProperty("product_id")
        private UUID productId;
        
        private Long qty;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingPreviewItemResponse {
        @JsonProperty("product_id")
        private UUID productId;
        
        private Long qty;
        
        @JsonProperty("total_price")
        private BigDecimal totalPrice;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingPreviewBatchResponse {
        private List<PricingPreviewItemResponse> items;
        
        @JsonProperty("grand_total")
        private BigDecimal grandTotal;
    }
}
