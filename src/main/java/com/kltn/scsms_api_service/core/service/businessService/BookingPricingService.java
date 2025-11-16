package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingItemRequest;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import com.kltn.scsms_api_service.core.entity.PriceBook;
import com.kltn.scsms_api_service.core.service.entityService.PriceBookEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý tính giá cho booking từ bảng giá
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingPricingService {

    private final PricingBusinessService pricingBusinessService;
    private final PriceBookEntityService priceBookEntityService;

    /**
     * Tính tổng giá cho booking từ bảng giá
     * @param bookingItems Danh sách booking items
     * @param priceBookId Optional price book ID (nếu null sẽ dùng active price book)
     * @return Tổng giá của booking
     */
    public BigDecimal calculateBookingTotalPrice(List<CreateBookingItemRequest> bookingItems, UUID priceBookId) {
        if (bookingItems == null || bookingItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        log.info("Calculating booking total price for {} items, priceBook: {}", bookingItems.size(), priceBookId);

        // Lấy price book
        PriceBook priceBook = null;
        if (priceBookId != null) {
            priceBook = priceBookEntityService.require(priceBookId);
        } else {
            // Lấy active price book
            Optional<PriceBook> activePriceBook = pricingBusinessService.resolveActivePriceBook(LocalDateTime.now());
            if (activePriceBook.isPresent()) {
                priceBook = activePriceBook.get();
            }
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CreateBookingItemRequest itemRequest : bookingItems) {
            BigDecimal itemPrice = calculateItemPrice(itemRequest, priceBook);
            totalPrice = totalPrice.add(itemPrice);
        }

        log.info("Calculated booking total price: {}", totalPrice);
        return totalPrice;
    }

    /**
     * Tính giá cho một booking item từ bảng giá
     * @param itemRequest Booking item request
     * @param priceBook Price book để lấy giá
     * @return Giá của item (services are always quantity 1)
     */
    private BigDecimal calculateItemPrice(CreateBookingItemRequest itemRequest, PriceBook priceBook) {
        BigDecimal unitPrice = BigDecimal.ZERO;
        UUID priceBookId = priceBook != null ? priceBook.getId() : null;

        try {
            // Lấy giá service từ bảng giá
            unitPrice = getServicePriceFromPriceBook(itemRequest.getServiceId(), priceBookId);
            
            // Nếu không có trong bảng giá, throw exception
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) == 0) {
                log.error("No price found in price book for service: {}", itemRequest.getServiceId());
                throw new RuntimeException("No price found for service: " + itemRequest.getServiceName());
            }
                
        } catch (Exception e) {
            log.error("Error calculating price for service {}: {}", itemRequest.getServiceId(), e.getMessage());
            throw new RuntimeException("Could not determine price for service: " + itemRequest.getServiceName());
        }

        // Total price = unit price
        log.info("Service {}: unitPrice={}", 
                itemRequest.getServiceName(), unitPrice);
        
        return unitPrice;
    }

    /**
     * Lấy giá service từ bảng giá
     */
    private BigDecimal getServicePriceFromPriceBook(UUID serviceId, UUID priceBookId) {
        try {
            if (priceBookId != null) {
                return pricingBusinessService.resolveServicePrice(serviceId, priceBookId);
            } else {
                // Sử dụng active price book
                return pricingBusinessService.resolveServicePrice(serviceId, null);
            }
        } catch (Exception e) {
            log.warn("Could not get service price from price book for service {}: {}", serviceId, e.getMessage());
            return null;
        }
    }


    /**
     * Tính giá cho booking item đã tồn tại (cho update)
     */
    public BigDecimal calculateBookingItemPrice(BookingItem bookingItem, UUID priceBookId) {
        CreateBookingItemRequest itemRequest = CreateBookingItemRequest.builder()
                .serviceId(bookingItem.getServiceId())
                .serviceName(bookingItem.getServiceName())
                .serviceDescription(bookingItem.getServiceDescription())
                .build();

        return calculateItemPrice(itemRequest, 
                priceBookId != null ? priceBookEntityService.require(priceBookId) : null);
    }
}
