package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import com.kltn.scsms_api_service.core.entity.StockTransaction;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockTxnType;
import com.kltn.scsms_api_service.core.repository.StockTransactionRepository;
import com.kltn.scsms_api_service.core.service.entityService.BookingItemService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service xử lý logic inventory cho booking
 * - Reserve stock khi booking được tạo (PENDING)
 * - Fulfill stock khi booking chuyển sang IN_PROGRESS
 * - Return/release stock khi booking bị cancel
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingInventoryService {
    
    private final InventoryBusinessService inventoryBusinessService;
    private final BookingItemService bookingItemService;
    private final ServiceProductService serviceProductService;
    private final StockTransactionRepository stockTransactionRepository;
    
    /**
     * Reserve stock cho booking khi booking được tạo với status PENDING
     * Lấy tất cả products từ services trong booking và reserve chúng
     */
    @Transactional
    public void reserveStockForBooking(Booking booking) {
        if (booking == null || booking.getBookingId() == null) {
            log.warn("Cannot reserve stock: booking is null or bookingId is null");
            return;
        }
        
        if (booking.getBranch() == null || booking.getBranch().getBranchId() == null) {
            log.warn("Cannot reserve stock for booking {}: branch is null", booking.getBookingId());
            return;
        }
        
        UUID branchId = booking.getBranch().getBranchId();
        UUID bookingId = booking.getBookingId();
        
        log.info("Reserving stock for booking: {} in branch: {}", bookingId, branchId);
        
        try {
            // Lấy tất cả booking items
            List<BookingItem> bookingItems = bookingItemService.findByBooking(bookingId);
            
            if (bookingItems.isEmpty()) {
                log.info("No booking items found for booking: {}, skipping stock reservation", bookingId);
                return;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            // Với mỗi booking item (service)
            for (BookingItem bookingItem : bookingItems) {
                try {
                    reserveStockForBookingItem(bookingItem, branchId, bookingId);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to reserve stock for booking item {} (serviceId: {}): {}", 
                        bookingItem.getBookingItemId(), bookingItem.getServiceId(), e.getMessage(), e);
                    failCount++;
                    // Continue với item tiếp theo thay vì throw exception ngay
                    // Để đảm bảo các items khác vẫn được reserve
                }
            }
            
            // Log chi tiết số lượng products đã reserve
            int totalProductsReserved = bookingItems.stream()
                .mapToInt(item -> {
                    List<ServiceProduct> sps = serviceProductService.findByServiceIdWithProduct(item.getServiceId());
                    return (int) sps.stream()
                        .filter(sp -> sp.getProduct() != null && sp.getProduct().getProductId() != null)
                        .filter(sp -> sp.getQuantity() != null && sp.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                        .map(sp -> sp.getProduct().getProductId())
                        .distinct()
                        .count();
                })
                .sum();
            
            log.info("Completed reserving stock for booking: {} - success: {} booking items ({} unique products), failed: {}", 
                bookingId, successCount, totalProductsReserved, failCount);
            
            if (failCount > 0 && successCount == 0) {
                // Nếu tất cả đều fail, throw exception để rollback
                throw new RuntimeException("Failed to reserve stock for all booking items in booking: " + bookingId);
            }
            
        } catch (Exception e) {
            log.error("Error reserving stock for booking {}: {}", bookingId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Reserve stock cho một booking item (service)
     * Tổng hợp quantity từ tất cả ServiceProduct records cho cùng 1 product để tránh duplicate
     */
    private void reserveStockForBookingItem(BookingItem bookingItem, UUID branchId, UUID bookingId) {
        UUID serviceId = bookingItem.getServiceId();
        
        if (serviceId == null) {
            log.warn("Cannot reserve stock for booking item {}: serviceId is null", bookingItem.getBookingItemId());
            return;
        }
        
        log.debug("Reserving stock for booking item {} (serviceId: {})", bookingItem.getBookingItemId(), serviceId);
        
        // Lấy tất cả products của service này
        List<ServiceProduct> serviceProducts = serviceProductService.findByServiceIdWithProduct(serviceId);
        
        if (serviceProducts.isEmpty()) {
            log.debug("Service {} has no products, skipping stock reservation", serviceId);
            return;
        }
        
        // Tổng hợp quantity theo productId để tránh duplicate nếu có nhiều ServiceProduct records cho cùng product
        Map<UUID, Long> productQuantityMap = serviceProducts.stream()
            .filter(sp -> sp.getProduct() != null && sp.getProduct().getProductId() != null)
            .filter(sp -> sp.getQuantity() != null && sp.getQuantity().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.groupingBy(
                sp -> sp.getProduct().getProductId(),
                Collectors.summingLong(sp -> sp.getQuantity().longValue())
            ));
        
        if (productQuantityMap.isEmpty()) {
            log.debug("Service {} has no valid products with quantity > 0, skipping stock reservation", serviceId);
            return;
        }
        
        log.info("Reserving stock for booking {}: {} unique products from service {}", 
            bookingId, productQuantityMap.size(), serviceId);
        
        // Reserve stock cho từng product (đã tổng hợp quantity)
        for (Map.Entry<UUID, Long> entry : productQuantityMap.entrySet()) {
            UUID productId = entry.getKey();
            long totalQty = entry.getValue();
            
            // Kiểm tra xem đã reserve chưa để tránh duplicate
            Long alreadyReserved = stockTransactionRepository.sumReservedQuantity(
                bookingId, StockRefType.BOOKING, StockTxnType.RESERVATION, productId, branchId);
            
            if (alreadyReserved != null && alreadyReserved >= totalQty) {
                log.warn("Product {} already has {} units reserved for booking {}, skipping duplicate reservation", 
                    productId, alreadyReserved, bookingId);
                continue;
            }
            
            // Reserve số lượng còn thiếu (nếu có)
            long qtyToReserve = totalQty - (alreadyReserved != null ? alreadyReserved : 0L);
            if (qtyToReserve <= 0) {
                log.debug("Product {} already has sufficient reservation for booking {}, skipping", 
                    productId, bookingId);
                continue;
            }
            
            try {
                log.info("Reserving {} units of product {} for booking {} (total needed: {}, already reserved: {})", 
                    qtyToReserve, productId, bookingId, totalQty, alreadyReserved);
                inventoryBusinessService.reserveStock(
                    branchId,
                    productId,
                    qtyToReserve,
                    bookingId,
                    StockRefType.BOOKING
                );
                log.info("Successfully reserved {} units of product {} for booking {}", 
                    qtyToReserve, productId, bookingId);
            } catch (Exception e) {
                log.error("Failed to reserve {} units of product {} for booking {}: {}", 
                    qtyToReserve, productId, bookingId, e.getMessage());
                throw e; // Re-throw để caller có thể xử lý
            }
        }
    }
    
    /**
     * Fulfill stock cho booking khi booking chuyển sang IN_PROGRESS
     * Chuyển từ reserved sang fulfilled (giảm onHand và reserved)
     */
    @Transactional
    public void fulfillStockForBooking(Booking booking) {
        if (booking == null || booking.getBookingId() == null) {
            log.warn("Cannot fulfill stock: booking is null or bookingId is null");
            return;
        }
        
        if (booking.getBranch() == null || booking.getBranch().getBranchId() == null) {
            log.warn("Cannot fulfill stock for booking {}: branch is null", booking.getBookingId());
            return;
        }
        
        UUID branchId = booking.getBranch().getBranchId();
        UUID bookingId = booking.getBookingId();
        
        log.info("Fulfilling stock for booking: {} in branch: {}", bookingId, branchId);
        
        try {
            // Lấy tất cả booking items
            List<BookingItem> bookingItems = bookingItemService.findByBooking(bookingId);
            
            if (bookingItems.isEmpty()) {
                log.info("No booking items found for booking: {}, skipping stock fulfillment", bookingId);
                return;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            // Với mỗi booking item (service)
            for (BookingItem bookingItem : bookingItems) {
                try {
                    fulfillStockForBookingItem(bookingItem, branchId, bookingId);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to fulfill stock for booking item {} (serviceId: {}): {}", 
                        bookingItem.getBookingItemId(), bookingItem.getServiceId(), e.getMessage(), e);
                    failCount++;
                    // Continue với item tiếp theo
                }
            }
            
            // Log chi tiết số lượng products đã fulfill
            int totalProductsFulfilled = bookingItems.stream()
                .mapToInt(item -> {
                    List<ServiceProduct> sps = serviceProductService.findByServiceIdWithProduct(item.getServiceId());
                    return (int) sps.stream()
                        .filter(sp -> sp.getProduct() != null && sp.getProduct().getProductId() != null)
                        .filter(sp -> sp.getQuantity() != null && sp.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                        .map(sp -> sp.getProduct().getProductId())
                        .distinct()
                        .count();
                })
                .sum();
            
            log.info("Completed fulfilling stock for booking: {} - success: {} booking items ({} unique products), failed: {}", 
                bookingId, successCount, totalProductsFulfilled, failCount);
            
            if (failCount > 0 && successCount == 0) {
                // Nếu tất cả đều fail, throw exception để rollback
                throw new RuntimeException("Failed to fulfill stock for all booking items in booking: " + bookingId);
            }
            
        } catch (Exception e) {
            log.error("Error fulfilling stock for booking {}: {}", bookingId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Fulfill stock cho một booking item (service)
     * Tổng hợp quantity từ tất cả ServiceProduct records cho cùng 1 product để tránh duplicate
     */
    private void fulfillStockForBookingItem(BookingItem bookingItem, UUID branchId, UUID bookingId) {
        UUID serviceId = bookingItem.getServiceId();
        
        if (serviceId == null) {
            log.warn("Cannot fulfill stock for booking item {}: serviceId is null", bookingItem.getBookingItemId());
            return;
        }
        
        log.debug("Fulfilling stock for booking item {} (serviceId: {})", bookingItem.getBookingItemId(), serviceId);
        
        // Lấy tất cả products của service này
        List<ServiceProduct> serviceProducts = serviceProductService.findByServiceIdWithProduct(serviceId);
        
        if (serviceProducts.isEmpty()) {
            log.debug("Service {} has no products, skipping stock fulfillment", serviceId);
            return;
        }
        
        // Tổng hợp quantity theo productId để tránh duplicate nếu có nhiều ServiceProduct records cho cùng product
        Map<UUID, Long> productQuantityMap = serviceProducts.stream()
            .filter(sp -> sp.getProduct() != null && sp.getProduct().getProductId() != null)
            .filter(sp -> sp.getQuantity() != null && sp.getQuantity().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.groupingBy(
                sp -> sp.getProduct().getProductId(),
                Collectors.summingLong(sp -> sp.getQuantity().longValue())
            ));
        
        if (productQuantityMap.isEmpty()) {
            log.debug("Service {} has no valid products with quantity > 0, skipping stock fulfillment", serviceId);
            return;
        }
        
        log.info("Fulfilling stock for booking {}: {} unique products from service {}", 
            bookingId, productQuantityMap.size(), serviceId);
        
        // Fulfill stock cho từng product (đã tổng hợp quantity)
        for (Map.Entry<UUID, Long> entry : productQuantityMap.entrySet()) {
            UUID productId = entry.getKey();
            long totalQty = entry.getValue();
            
            // QUAN TRỌNG: Kiểm tra xem đã fulfill chưa (cả BOOKING và SALE_ORDER)
            // Nếu sales order từ booking đã fulfill, thì booking KHÔNG được fulfill lại
            List<StockTransaction> existingBookingFulfills = stockTransactionRepository.findByRefIdAndRefTypeAndType(
                bookingId, StockRefType.BOOKING, StockTxnType.SALE);
            
            long alreadyFulfilledFromBooking = existingBookingFulfills.stream()
                .filter(txn -> txn.getProduct().getProductId().equals(productId) 
                    && txn.getBranch().getBranchId().equals(branchId))
                .mapToLong(txn -> Math.abs(txn.getQuantity()))
                .sum();
            
            // NGHIỆP VỤ QUAN TRỌNG: CHỈ BOOKING xử lý tồn kho, KHÔNG có SALE_ORDER
            // - Booking reserve stock khi đặt chỗ (PENDING) → BOOKING RESERVE
            // - Booking fulfill stock khi bắt đầu thực hiện dịch vụ (IN_PROGRESS) → BOOKING SALE
            // - Sales order từ booking CHỈ thanh toán tiền, KHÔNG động chạm đến tồn kho
            // - Sales order từ booking đã được skip fulfill trong SalesBusinessService.fulfill()
            // - Booking LUÔN được fulfill (chỉ tạo BOOKING SALE transactions)
            
            // Kiểm tra xem đã fulfill BOOKING chưa (chỉ kiểm tra BOOKING, không kiểm tra SALE_ORDER)
            if (alreadyFulfilledFromBooking >= totalQty) {
                log.warn("Product {} already has {} units fulfilled for booking {} (BOOKING only). " +
                    "Skipping duplicate fulfillment.", 
                    productId, alreadyFulfilledFromBooking, bookingId);
                continue;
            }
            
            // Fulfill số lượng còn thiếu (chỉ BOOKING)
            long qtyToFulfill = totalQty - alreadyFulfilledFromBooking;
            if (qtyToFulfill <= 0) {
                log.debug("Product {} already has sufficient fulfillment for booking {} (BOOKING only), skipping", 
                    productId, bookingId);
                continue;
            }
            
            try {
                log.info("Fulfilling {} units of product {} for booking {} (total needed: {}, " +
                    "already fulfilled from BOOKING: {}). " +
                    "NGHIỆP VỤ: CHỈ BOOKING xử lý tồn kho - Booking fulfill stock khi bắt đầu thực hiện dịch vụ (IN_PROGRESS). " +
                    "Sales order từ booking CHỈ thanh toán tiền, KHÔNG động chạm đến tồn kho.", 
                    qtyToFulfill, productId, bookingId, totalQty, alreadyFulfilledFromBooking);
                // Fulfill sẽ tự động giảm reserved và onHand
                inventoryBusinessService.fulfillStockFIFO(
                    branchId,
                    productId,
                    qtyToFulfill,
                    bookingId,
                    StockRefType.BOOKING
                );
                log.info("Successfully fulfilled {} units of product {} for booking {}", 
                    qtyToFulfill, productId, bookingId);
            } catch (Exception e) {
                log.error("Failed to fulfill {} units of product {} for booking {}: {}", 
                    qtyToFulfill, productId, bookingId, e.getMessage());
                throw e; // Re-throw để caller có thể xử lý
            }
        }
    }
    
    /**
     * Return/release stock khi booking bị cancel
     * 
     * NGHIỆP VỤ QUAN TRỌNG:
     * - Chỉ xử lý tồn kho của booking, KHÔNG đụng chạm đến sales order
     * - Nếu booking chưa fulfill (PENDING/CONFIRMED/CHECKED_IN): Release reservation
     * - Nếu booking đã fulfill (IN_PROGRESS/PAUSED): Return stock về kho
     * - Sales order (nếu có) sẽ được xử lý riêng, không ảnh hưởng đến logic này
     */
    @Transactional
    public void returnStockForCancelledBooking(Booking booking) {
        if (booking == null || booking.getBookingId() == null) {
            log.warn("Cannot return stock: booking is null or bookingId is null");
            return;
        }
        
        if (booking.getBranch() == null || booking.getBranch().getBranchId() == null) {
            log.warn("Cannot return stock for booking {}: branch is null", booking.getBookingId());
            return;
        }
        
        UUID branchId = booking.getBranch().getBranchId();
        UUID bookingId = booking.getBookingId();
        
        log.info("Returning stock for cancelled booking: {} in branch: {}", bookingId, branchId);
        
        try {
            // Lấy tất cả booking items
            List<BookingItem> bookingItems = bookingItemService.findByBooking(bookingId);
            
            if (bookingItems.isEmpty()) {
                log.info("No booking items found for booking: {}, skipping stock return", bookingId);
                return;
            }
            
            // Xác định booking đã fulfill chưa
            boolean isFulfilled = booking.getStatus() == Booking.BookingStatus.IN_PROGRESS 
                || booking.getStatus() == Booking.BookingStatus.PAUSED;
            
            int successCount = 0;
            int failCount = 0;
            
            // Với mỗi booking item (service)
            for (BookingItem bookingItem : bookingItems) {
                try {
                    if (isFulfilled) {
                        returnStockForBookingItem(bookingItem, branchId, bookingId);
                    } else {
                        releaseReservationForBookingItem(bookingItem, branchId, bookingId);
                    }
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to return stock for booking item {} (serviceId: {}): {}", 
                        bookingItem.getBookingItemId(), bookingItem.getServiceId(), e.getMessage(), e);
                    failCount++;
                    // Continue với item tiếp theo
                }
            }
            
            log.info("Completed returning stock for cancelled booking: {} - success: {}, failed: {} (fulfilled: {})", 
                bookingId, successCount, failCount, isFulfilled);
            
        } catch (Exception e) {
            log.error("Error returning stock for cancelled booking {}: {}", bookingId, e.getMessage(), e);
            // Không throw exception ở đây để đảm bảo booking vẫn có thể được cancel
            // Chỉ log lỗi để admin có thể xử lý sau
        }
    }
    
    /**
     * Release reservation cho một booking item (khi booking chưa fulfill)
     * Tổng hợp quantity từ tất cả ServiceProduct records cho cùng 1 product để tránh duplicate
     */
    private void releaseReservationForBookingItem(BookingItem bookingItem, UUID branchId, UUID bookingId) {
        UUID serviceId = bookingItem.getServiceId();
        
        if (serviceId == null) {
            log.warn("Cannot release reservation for booking item {}: serviceId is null", bookingItem.getBookingItemId());
            return;
        }
        
        log.debug("Releasing reservation for booking item {} (serviceId: {})", bookingItem.getBookingItemId(), serviceId);
        
        // Lấy tất cả products của service này
        List<ServiceProduct> serviceProducts = serviceProductService.findByServiceIdWithProduct(serviceId);
        
        if (serviceProducts.isEmpty()) {
            log.debug("Service {} has no products, skipping reservation release", serviceId);
            return;
        }
        
        // Tổng hợp quantity theo productId để tránh duplicate
        Map<UUID, Long> productQuantityMap = serviceProducts.stream()
            .filter(sp -> sp.getProduct() != null && sp.getProduct().getProductId() != null)
            .filter(sp -> sp.getQuantity() != null && sp.getQuantity().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.groupingBy(
                sp -> sp.getProduct().getProductId(),
                Collectors.summingLong(sp -> sp.getQuantity().longValue())
            ));
        
        if (productQuantityMap.isEmpty()) {
            log.debug("Service {} has no valid products with quantity > 0, skipping reservation release", serviceId);
            return;
        }
        
        log.info("Releasing reservation for booking {}: {} unique products from service {}", 
            bookingId, productQuantityMap.size(), serviceId);
        
        // Release reservation cho từng product (đã tổng hợp quantity)
        for (Map.Entry<UUID, Long> entry : productQuantityMap.entrySet()) {
            UUID productId = entry.getKey();
            long totalQty = entry.getValue();
            
            // QUAN TRỌNG: Chỉ release BOOKING, KHÔNG release SALE_ORDER
            // Kiểm tra xem đã release chưa (chỉ BOOKING, không kiểm tra SALE_ORDER)
            List<StockTransaction> existingBookingReleases = stockTransactionRepository.findByRefIdAndRefTypeAndType(
                bookingId, StockRefType.BOOKING, StockTxnType.RELEASE);
            
            long alreadyReleasedFromBooking = existingBookingReleases.stream()
                .filter(txn -> txn.getProduct().getProductId().equals(productId) 
                    && txn.getBranch().getBranchId().equals(branchId))
                .mapToLong(StockTransaction::getQuantity)
                .sum();
            
            if (alreadyReleasedFromBooking >= totalQty) {
                log.warn("Product {} already has {} units released for booking {} (BOOKING only). " +
                    "Skipping duplicate release. Note: SALE_ORDER releases are handled separately.", 
                    productId, alreadyReleasedFromBooking, bookingId);
                continue;
            }
            
            // Release số lượng còn thiếu (nếu có) - CHỈ cho BOOKING
            long qtyToRelease = totalQty - alreadyReleasedFromBooking;
            if (qtyToRelease <= 0) {
                log.debug("Product {} already has sufficient release for booking {}, skipping", 
                    productId, bookingId);
                continue;
            }
            
            try {
                log.info("Releasing reservation for {} units of product {} for booking {} " +
                    "(total needed: {}, already released from BOOKING: {}). " +
                    "Note: SALE_ORDER releases are handled separately and do not affect booking inventory.", 
                    qtyToRelease, productId, bookingId, totalQty, alreadyReleasedFromBooking);
                inventoryBusinessService.releaseReservation(
                    branchId,
                    productId,
                    qtyToRelease,
                    bookingId,
                    StockRefType.BOOKING
                );
                log.info("Successfully released reservation for {} units of product {} for booking {}", 
                    qtyToRelease, productId, bookingId);
            } catch (Exception e) {
                log.error("Failed to release reservation for {} units of product {} for booking {}: {}", 
                    qtyToRelease, productId, bookingId, e.getMessage());
                throw e;
            }
        }
    }
    
    /**
     * Return stock về kho cho một booking item (khi booking đã fulfill)
     * Tổng hợp quantity từ tất cả ServiceProduct records cho cùng 1 product để tránh duplicate
     */
    private void returnStockForBookingItem(BookingItem bookingItem, UUID branchId, UUID bookingId) {
        UUID serviceId = bookingItem.getServiceId();
        
        if (serviceId == null) {
            log.warn("Cannot return stock for booking item {}: serviceId is null", bookingItem.getBookingItemId());
            return;
        }
        
        log.debug("Returning stock for booking item {} (serviceId: {})", bookingItem.getBookingItemId(), serviceId);
        
        // Lấy tất cả products của service này
        List<ServiceProduct> serviceProducts = serviceProductService.findByServiceIdWithProduct(serviceId);
        
        if (serviceProducts.isEmpty()) {
            log.debug("Service {} has no products, skipping stock return", serviceId);
            return;
        }
        
        // Tổng hợp quantity theo productId để tránh duplicate
        Map<UUID, Long> productQuantityMap = serviceProducts.stream()
            .filter(sp -> sp.getProduct() != null && sp.getProduct().getProductId() != null)
            .filter(sp -> sp.getQuantity() != null && sp.getQuantity().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.groupingBy(
                sp -> sp.getProduct().getProductId(),
                Collectors.summingLong(sp -> sp.getQuantity().longValue())
            ));
        
        if (productQuantityMap.isEmpty()) {
            log.debug("Service {} has no valid products with quantity > 0, skipping stock return", serviceId);
            return;
        }
        
        log.info("Returning stock for booking {}: {} unique products from service {}", 
            bookingId, productQuantityMap.size(), serviceId);
        
        // Return stock cho từng product (đã tổng hợp quantity)
        for (Map.Entry<UUID, Long> entry : productQuantityMap.entrySet()) {
            UUID productId = entry.getKey();
            long totalQty = entry.getValue();
            
            // QUAN TRỌNG: Chỉ return BOOKING, KHÔNG return SALE_ORDER
            // Kiểm tra xem đã return chưa (chỉ BOOKING, không kiểm tra SALE_ORDER)
            List<StockTransaction> existingBookingReturns = stockTransactionRepository.findByRefIdAndRefTypeAndType(
                bookingId, StockRefType.BOOKING, StockTxnType.RETURN);
            
            long alreadyReturnedFromBooking = existingBookingReturns.stream()
                .filter(txn -> txn.getProduct().getProductId().equals(productId) 
                    && txn.getBranch().getBranchId().equals(branchId))
                .mapToLong(StockTransaction::getQuantity)
                .sum();
            
            if (alreadyReturnedFromBooking >= totalQty) {
                log.warn("Product {} already has {} units returned for booking {} (BOOKING only). " +
                    "Skipping duplicate return. Note: SALE_ORDER returns are handled separately.", 
                    productId, alreadyReturnedFromBooking, bookingId);
                continue;
            }
            
            // Return số lượng còn thiếu (nếu có) - CHỈ cho BOOKING
            long qtyToReturn = totalQty - alreadyReturnedFromBooking;
            if (qtyToReturn <= 0) {
                log.debug("Product {} already has sufficient return for booking {}, skipping", 
                    productId, bookingId);
                continue;
            }
            
            try {
                log.info("Returning {} units of product {} to stock for booking {} " +
                    "(total needed: {}, already returned from BOOKING: {}). " +
                    "Note: SALE_ORDER returns are handled separately and do not affect booking inventory.", 
                    qtyToReturn, productId, bookingId, totalQty, alreadyReturnedFromBooking);
                // Sử dụng unitCost = 0 vì đây là return từ booking, không cần track cost
                // Hoặc có thể lấy từ lot nếu cần, nhưng đơn giản hóa thì dùng 0
                inventoryBusinessService.returnToStock(
                    branchId,
                    productId,
                    qtyToReturn,
                    java.math.BigDecimal.ZERO, // unitCost = 0 cho return từ booking
                    bookingId,
                    StockRefType.BOOKING
                );
                log.info("Successfully returned {} units of product {} to stock for booking {}", 
                    qtyToReturn, productId, bookingId);
            } catch (Exception e) {
                log.error("Failed to return {} units of product {} to stock for booking {}: {}", 
                    qtyToReturn, productId, bookingId, e.getMessage());
                throw e;
            }
        }
    }
}

