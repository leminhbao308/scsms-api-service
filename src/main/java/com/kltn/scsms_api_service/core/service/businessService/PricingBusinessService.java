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
        LocalDateTime today = LocalDateTime.now();
        PriceBook book = resolveActivePriceBook(today)
            .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "No active PriceBook for date " + today));
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
