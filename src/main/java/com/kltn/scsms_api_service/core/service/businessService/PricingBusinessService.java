package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.response.ServicePriceDto;
import com.kltn.scsms_api_service.core.entity.PriceBook;
import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import com.kltn.scsms_api_service.core.repository.PriceBookItemRepository;
import com.kltn.scsms_api_service.core.service.entityService.PriceBookEntityService;
import com.kltn.scsms_api_service.core.service.entityService.PriceBookItemEntityService;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.exception.ServerSideException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingBusinessService {
    private final PriceBookEntityService priceBookEntityService;
    private final PriceBookItemEntityService priceBookItemES;
    private final PriceBookItemRepository priceBookItemRepository;

    public Optional<PriceBook> resolveActivePriceBook(LocalDateTime date) {
        List<PriceBook> books = priceBookEntityService.getActivePriceInRange(
                date, null);
        // Lấy PriceBook mới nhất (validFrom gần nhất)
        return books.stream()
                .max(Comparator.comparing(PriceBook::getValidFrom));
    }

    /**
     * Resolve active price book for a specific branch
     *
     * @param branchId Branch ID (null for global price book)
     * @param date     Date to check validity
     * @return Optional PriceBook
     */
    public Optional<PriceBook> resolveActivePriceBook(UUID branchId, LocalDateTime date) {
        List<PriceBook> books;

        if (branchId != null) {
            // Tìm PriceBook của chi nhánh cụ thể
            books = priceBookEntityService.getActivePriceInRangeForBranch(branchId, date, null);

            if (!books.isEmpty()) {
                return books.stream()
                        .max(Comparator.comparing(PriceBook::getValidFrom));
            }
        }

        // Fallback: Tìm global PriceBook (branchId = null)
        return resolveActivePriceBook(date);
    }

    public BigDecimal resolveUnitPrice(UUID productId) {
        return resolveUnitPrice(productId, null);
    }

    /**
     * Resolve unit price for a product with optional price book
     *
     * @param productId   Product ID
     * @param priceBookId Optional price book ID (if null, uses active price book)
     * @return Unit price
     */
    public BigDecimal resolveUnitPrice(UUID productId, UUID priceBookId) {
        return resolveUnitPrice(productId, null, priceBookId);
    }

    /**
     * Resolve unit price for a product with branch and optional price book
     *
     * @param productId   Product ID
     * @param branchId    Branch ID (null for global price book)
     * @param priceBookId Optional price book ID (if null, uses active price book
     *                    for branch)
     * @return Unit price
     */
    public BigDecimal resolveUnitPrice(UUID productId, UUID branchId, UUID priceBookId) {
        LocalDateTime today = LocalDateTime.now();
        PriceBook book;

        if (priceBookId != null) {
            book = priceBookEntityService.require(priceBookId);
        } else {
            book = resolveActivePriceBook(branchId, today)
                    .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND,
                            "No active PriceBook for branch " + branchId + " and date " + today));
        }

        PriceBookItem item = priceBookItemES.findByPriceBookIdAndProductId(book.getId(), productId)
                .orElse(null);

        if (item == null)
            return BigDecimal.ONE;

        return require(item.getFixedPrice());
    }

    /**
     * Resolve unit price for a service
     *
     * @param serviceId   Service ID
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
                    .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND,
                            "No active PriceBook for date " + today));
        }

        PriceBookItem item = priceBookItemES.findByPriceBookIdAndServiceId(book.getId(), serviceId)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND,
                        "No price for service=" + serviceId + " in book=" + book.getCode()));

        return switch (item.getPolicyType()) {
            case FIXED -> require(item.getFixedPrice());
            case MARKUP_ON_PEAK -> {
                // For services, markup is not supported - only fixed pricing
                throw new UnsupportedOperationException(
                        "MARKUP_ON_PEAK not supported for services - only FIXED pricing is allowed");
            }
        };
    }

    public BigDecimal calcLineTotal(UUID productId, long quantity) {
        BigDecimal unitPrice = resolveUnitPrice(productId);
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Batch resolve service prices
     * Prevents N+1 query pattern by fetching all prices in a single query
     *
     * @param serviceIds  List of service IDs to get prices for
     * @param priceBookId Optional price book ID (if null, uses active price book)
     * @return Map of service ID to fixed price
     */
    public Map<UUID, BigDecimal> getServicePricesBatch(List<UUID> serviceIds, UUID priceBookId) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return Collections.emptyMap();
        }

        log.debug("Batch fetching prices for {} services", serviceIds.size());

        LocalDateTime today = LocalDateTime.now();
        PriceBook book;

        if (priceBookId != null) {
            book = priceBookEntityService.require(priceBookId);
        } else {
            book = resolveActivePriceBook(today)
                    .orElseThrow(() -> new ServerSideException(
                            ErrorCode.ENTITY_NOT_FOUND,
                            "No active PriceBook for date " + today));
        }

        // Batch fetch all price book items for these services in ONE query
        List<PriceBookItem> items = priceBookItemRepository.findServicePricesBatch(
                book.getId(),
                serviceIds);

        // Convert to map: serviceId -> price
        Map<UUID, BigDecimal> priceMap = items.stream()
                .filter(item -> item.getFixedPrice() != null)
                .collect(Collectors.toMap(
                        item -> item.getService().getServiceId(),
                        PriceBookItem::getFixedPrice,
                        (existing, replacement) -> existing // Keep first if duplicate
                ));

        log.debug("Found prices for {}/{} services", priceMap.size(), serviceIds.size());

        // Log warning for services not found in price book
        serviceIds.stream()
                .filter(id -> !priceMap.containsKey(id))
                .forEach(id -> log.warn("No price found for service {} in price book {}", id, book.getCode()));

        return priceMap;
    }

    /**
     * Batch resolve service prices with detailed information
     * Returns full ServicePriceDto objects instead of just prices
     *
     * @param serviceIds  List of service IDs to get prices for
     * @param priceBookId Optional price book ID (if null, uses active price book)
     * @return List of ServicePriceDto with price details
     */
    public List<ServicePriceDto> getServicePricesDetailedBatch(List<UUID> serviceIds, UUID priceBookId) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime today = LocalDateTime.now();
        PriceBook book;

        if (priceBookId != null) {
            book = priceBookEntityService.require(priceBookId);
        } else {
            book = resolveActivePriceBook(today)
                    .orElseThrow(() -> new ServerSideException(
                            ErrorCode.ENTITY_NOT_FOUND,
                            "No active PriceBook for date " + today));
        }

        // Batch fetch all price book items
        List<PriceBookItem> items = priceBookItemRepository.findServicePricesBatch(
                book.getId(),
                serviceIds);

        // Convert to DTOs
        return items.stream()
                .map(item -> new ServicePriceDto(
                        item.getService().getServiceId(),
                        item.getService().getServiceName(),
                        item.getFixedPrice(),
                        true))
                .collect(Collectors.toList());
    }

    private BigDecimal require(BigDecimal fixedPrice) {
        if (fixedPrice == null)
            throw new IllegalStateException("Missing field: fixedPrice");
        return fixedPrice;
    }

}
