package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.PriceBook;
import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import com.kltn.scsms_api_service.core.entity.ProductCostStats;
import com.kltn.scsms_api_service.core.service.entityService.PriceBookEntityService;
import com.kltn.scsms_api_service.core.service.entityService.PriceBookItemEntityService;
import com.kltn.scsms_api_service.core.service.entityService.ProductCostStatsService;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.exception.ServerSideException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingBusinessService {
    private final PriceBookEntityService priceBookEntityService;
    private final PriceBookItemEntityService priceBookItemES;
    private final ProductCostStatsService productCostStatsES;
    
    
    public Optional<PriceBook> resolveActivePriceBook(LocalDateTime date) {
        List<PriceBook> books = priceBookEntityService.getActivePriceInRange(
            date, null);
        return books.stream().findFirst(); // customize selection by branch/channel if needed
    }
    
    
    public BigDecimal resolveUnitPrice(UUID productId) {
        return resolveUnitPrice(productId, null);
    }
    
    /**
     * Resolve unit price for a product with optional price book
     * @param productId Product ID
     * @param priceBookId Optional price book ID (if null, uses active price book)
     * @return Unit price
     */
    public BigDecimal resolveUnitPrice(UUID productId, UUID priceBookId) {
        LocalDateTime today = LocalDateTime.now();
        PriceBook book;
        
        if (priceBookId != null) {
            book = priceBookEntityService.require(priceBookId);
        } else {
            book = resolveActivePriceBook(today)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "No active PriceBook for date " + today));
        }
        
        PriceBookItem item = priceBookItemES.findByPriceBookIdAndProductId(book.getId(), productId)
            .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "No price for product=" + productId + " in book=" + book.getCode()));
        
        
        return switch (item.getPolicyType()) {
            case FIXED -> require(item.getFixedPrice());
            case MARKUP_ON_PEAK -> {
                ProductCostStats stats = productCostStatsES.findByProduct(productId)
                    .orElseThrow(() -> new NoSuchElementException("No ProductCostStats for product=" + productId));
                yield pct(stats.getPeakPurchasePrice(), item.getMarkupPercent());
            }
        };
    }
    
    /**
     * Resolve unit price for a service
     * @param serviceId Service ID
     * @param priceBookId Optional price book ID (if null, uses active price book)
     * @return Unit price
     */
    public BigDecimal resolveServicePrice(UUID serviceId, UUID priceBookId) {
        LocalDateTime today = LocalDateTime.now();
        PriceBook book;
        
        if (priceBookId != null) {
            book = priceBookEntityService.require(priceBookId);
        } else {
            book = resolveActivePriceBook(today)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "No active PriceBook for date " + today));
        }
        
        PriceBookItem item = priceBookItemES.findByPriceBookIdAndServiceId(book.getId(), serviceId)
            .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "No price for service=" + serviceId + " in book=" + book.getCode()));
        
        return switch (item.getPolicyType()) {
            case FIXED -> require(item.getFixedPrice());
            case MARKUP_ON_PEAK -> {
                // For services, we might use basePrice + laborCost as the base for markup
                // This would require getting the service entity and calculating its base cost
                throw new UnsupportedOperationException("MARKUP_ON_PEAK not supported for services yet");
            }
        };
    }
    
    /**
     * Resolve unit price for a service package
     * @param packageId Service Package ID
     * @param priceBookId Optional price book ID (if null, uses active price book)
     * @return Unit price
     */
    public BigDecimal resolveServicePackagePrice(UUID packageId, UUID priceBookId) {
        LocalDateTime today = LocalDateTime.now();
        PriceBook book;
        
        if (priceBookId != null) {
            book = priceBookEntityService.require(priceBookId);
        } else {
            book = resolveActivePriceBook(today)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "No active PriceBook for date " + today));
        }
        
        PriceBookItem item = priceBookItemES.findByPriceBookIdAndServicePackageId(book.getId(), packageId)
            .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "No price for service package=" + packageId + " in book=" + book.getCode()));
        
        return switch (item.getPolicyType()) {
            case FIXED -> require(item.getFixedPrice());
            case MARKUP_ON_PEAK -> {
                // For service packages, we might use calculated package price as the base for markup
                throw new UnsupportedOperationException("MARKUP_ON_PEAK not supported for service packages yet");
            }
        };
    }
    
    public BigDecimal calcLineTotal(UUID productId, long quantity) {
        BigDecimal unitPrice = resolveUnitPrice(productId);
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    private BigDecimal require(BigDecimal fixedPrice) {
                if (fixedPrice == null) throw new IllegalStateException("Missing field: fixedPrice");
        return fixedPrice;
    }
    
    private BigDecimal pct(BigDecimal base, BigDecimal percent) {
        if (base == null || percent == null) throw new IllegalStateException("Missing policy data");
        return base.multiply(BigDecimal.ONE.add(percent));
    }
}
