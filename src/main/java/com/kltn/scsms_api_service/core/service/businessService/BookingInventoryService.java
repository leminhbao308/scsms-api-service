package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.service.entityService.BookingItemService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
            
            log.info("Completed reserving stock for booking: {} - success: {}, failed: {}", 
                bookingId, successCount, failCount);
            
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
        
        // Reserve stock cho từng product
        for (ServiceProduct serviceProduct : serviceProducts) {
            if (serviceProduct.getProduct() == null || serviceProduct.getProduct().getProductId() == null) {
                log.warn("ServiceProduct {} has null product, skipping", serviceProduct.getId());
                continue;
            }
            
            UUID productId = serviceProduct.getProduct().getProductId();
            BigDecimal quantity = serviceProduct.getQuantity();
            
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("ServiceProduct {} has invalid quantity: {}, skipping", serviceProduct.getId(), quantity);
                continue;
            }
            
            long qty = quantity.longValue();
            
            try {
                log.debug("Reserving {} units of product {} for booking {}", qty, productId, bookingId);
                inventoryBusinessService.reserveStock(
                    branchId,
                    productId,
                    qty,
                    bookingId,
                    StockRefType.BOOKING
                );
                log.debug("Successfully reserved {} units of product {} for booking {}", qty, productId, bookingId);
            } catch (Exception e) {
                log.error("Failed to reserve {} units of product {} for booking {}: {}", 
                    qty, productId, bookingId, e.getMessage());
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
            
            log.info("Completed fulfilling stock for booking: {} - success: {}, failed: {}", 
                bookingId, successCount, failCount);
            
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
        
        // Fulfill stock cho từng product
        for (ServiceProduct serviceProduct : serviceProducts) {
            if (serviceProduct.getProduct() == null || serviceProduct.getProduct().getProductId() == null) {
                log.warn("ServiceProduct {} has null product, skipping", serviceProduct.getId());
                continue;
            }
            
            UUID productId = serviceProduct.getProduct().getProductId();
            BigDecimal quantity = serviceProduct.getQuantity();
            
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("ServiceProduct {} has invalid quantity: {}, skipping", serviceProduct.getId(), quantity);
                continue;
            }
            
            long qty = quantity.longValue();
            
            try {
                log.debug("Fulfilling {} units of product {} for booking {}", qty, productId, bookingId);
                // Fulfill sẽ tự động giảm reserved và onHand
                inventoryBusinessService.fulfillStockFIFO(
                    branchId,
                    productId,
                    qty,
                    bookingId,
                    StockRefType.BOOKING
                );
                log.debug("Successfully fulfilled {} units of product {} for booking {}", qty, productId, bookingId);
            } catch (Exception e) {
                log.error("Failed to fulfill {} units of product {} for booking {}: {}", 
                    qty, productId, bookingId, e.getMessage());
                throw e; // Re-throw để caller có thể xử lý
            }
        }
    }
    
    /**
     * Return/release stock khi booking bị cancel
     * - Nếu booking chưa fulfill (PENDING/CONFIRMED/CHECKED_IN): Release reservation
     * - Nếu booking đã fulfill (IN_PROGRESS/PAUSED): Return stock về kho
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
        
        // Release reservation cho từng product
        for (ServiceProduct serviceProduct : serviceProducts) {
            if (serviceProduct.getProduct() == null || serviceProduct.getProduct().getProductId() == null) {
                log.warn("ServiceProduct {} has null product, skipping", serviceProduct.getId());
                continue;
            }
            
            UUID productId = serviceProduct.getProduct().getProductId();
            BigDecimal quantity = serviceProduct.getQuantity();
            
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("ServiceProduct {} has invalid quantity: {}, skipping", serviceProduct.getId(), quantity);
                continue;
            }
            
            long qty = quantity.longValue();
            
            try {
                log.debug("Releasing reservation for {} units of product {} for booking {}", qty, productId, bookingId);
                inventoryBusinessService.releaseReservation(
                    branchId,
                    productId,
                    qty,
                    bookingId,
                    StockRefType.BOOKING
                );
                log.debug("Successfully released reservation for {} units of product {} for booking {}", qty, productId, bookingId);
            } catch (Exception e) {
                log.error("Failed to release reservation for {} units of product {} for booking {}: {}", 
                    qty, productId, bookingId, e.getMessage());
                throw e;
            }
        }
    }
    
    /**
     * Return stock về kho cho một booking item (khi booking đã fulfill)
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
        
        // Return stock cho từng product
        for (ServiceProduct serviceProduct : serviceProducts) {
            if (serviceProduct.getProduct() == null || serviceProduct.getProduct().getProductId() == null) {
                log.warn("ServiceProduct {} has null product, skipping", serviceProduct.getId());
                continue;
            }
            
            UUID productId = serviceProduct.getProduct().getProductId();
            BigDecimal quantity = serviceProduct.getQuantity();
            
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("ServiceProduct {} has invalid quantity: {}, skipping", serviceProduct.getId(), quantity);
                continue;
            }
            
            long qty = quantity.longValue();
            
            try {
                log.debug("Returning {} units of product {} to stock for booking {}", qty, productId, bookingId);
                // Sử dụng unitCost = 0 vì đây là return từ booking, không cần track cost
                // Hoặc có thể lấy từ lot nếu cần, nhưng đơn giản hóa thì dùng 0
                inventoryBusinessService.returnToStock(
                    branchId,
                    productId,
                    qty,
                    java.math.BigDecimal.ZERO, // unitCost = 0 cho return từ booking
                    bookingId,
                    StockRefType.BOOKING
                );
                log.debug("Successfully returned {} units of product {} to stock for booking {}", qty, productId, bookingId);
            } catch (Exception e) {
                log.error("Failed to return {} units of product {} to stock for booking {}: {}", 
                    qty, productId, bookingId, e.getMessage());
                throw e;
            }
        }
    }
}

