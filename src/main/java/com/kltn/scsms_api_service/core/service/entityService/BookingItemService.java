package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.BookingItem;
import com.kltn.scsms_api_service.core.repository.BookingItemRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingItemService {
    
    private final BookingItemRepository bookingItemRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public BookingItem getById(UUID bookingItemId) {
        return bookingItemRepository.findById(bookingItemId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Booking item not found with ID: " + bookingItemId));
    }
    
    public List<BookingItem> findAll() {
        return bookingItemRepository.findAll();
    }
    
    @Transactional
    public BookingItem save(BookingItem bookingItem) {
        return bookingItemRepository.save(bookingItem);
    }
    
    @Transactional
    public BookingItem update(BookingItem bookingItem) {
        return bookingItemRepository.save(bookingItem);
    }
    
    @Transactional
    public void delete(UUID bookingItemId) {
        BookingItem bookingItem = getById(bookingItemId);
        bookingItem.setIsDeleted(true);
        bookingItemRepository.save(bookingItem);
    }
    
    /**
     * Xóa hoàn toàn booking item (hard delete)
     * Chỉ xóa vật lý nếu item status là PENDING
     */
    @Transactional
    public void hardDelete(UUID bookingItemId) {
        BookingItem bookingItem = getById(bookingItemId);
        
        // Chỉ cho phép xóa hoàn toàn nếu item chưa được thực hiện
        if (bookingItem.getItemStatus() != BookingItem.ItemStatus.PENDING) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Cannot hard delete booking item with status: " + bookingItem.getItemStatus() + 
                    ". Only PENDING items can be hard deleted.");
        }
        
        bookingItemRepository.delete(bookingItem);
        log.info("Hard deleted booking item: {}", bookingItemId);
    }
    
    /**
     * Xóa hoàn toàn booking item mà không kiểm tra status
     * Sử dụng khi cần xóa trong update booking
     * Dùng native query để xóa trực tiếp trong DB, tránh Hibernate cache
     * KHÔNG kiểm tra existsById vì có thể gây auto-flush và conflict với orphanRemoval
     */
    @Transactional
    public void hardDeleteWithoutStatusCheck(UUID bookingItemId) {
        // Dùng native query để xóa trực tiếp trong DB
        // KHÔNG kiểm tra existsById vì có thể trigger auto-flush và gây lỗi với orphanRemoval
        int deletedCount = bookingItemRepository.deleteByIdNative(bookingItemId);
        
        if (deletedCount > 0) {
            log.info("Hard deleted booking item without status check: {} (deleted {} row(s))", bookingItemId, deletedCount);
        } else {
            log.debug("No row deleted for booking item: {} (may not exist)", bookingItemId);
        }
    }
    
    public List<BookingItem> findByBooking(UUID bookingId) {
        return bookingItemRepository.findByBooking_BookingIdOrderByDisplayOrderAsc(bookingId);
    }
    
    /**
     * Tìm booking items và clear persistence context trước khi query
     * Sử dụng khi cần đảm bảo lấy dữ liệu mới nhất sau khi có thay đổi (delete/update)
     * KHÔNG flush ở đây vì có thể gây lỗi nếu có entities đã bị xóa trong persistence context
     */
    @Transactional
    public List<BookingItem> findByBookingWithClear(UUID bookingId) {
        // Clear persistence context để tránh cache và stale references
        // KHÔNG flush vì có thể gây lỗi nếu có entities đã bị xóa (như khi xóa bằng native query)
        entityManager.clear();
        // Query lại từ database
        return bookingItemRepository.findByBooking_BookingIdOrderByDisplayOrderAsc(bookingId);
    }
    
    /**
     * Clear persistence context để tránh stale references
     * Sử dụng sau khi xóa items bằng native query để đảm bảo entities được reload từ DB
     */
    @Transactional
    public void clearPersistenceContext() {
        entityManager.clear();
        log.debug("Cleared persistence context");
    }
    
    /**
     * Flush persistence context để đảm bảo các thay đổi được lưu vào DB
     * Sử dụng trước khi reload entity để đảm bảo các items mới được query từ DB
     */
    @Transactional
    public void flush() {
        entityManager.flush();
        log.debug("Flushed persistence context to database");
    }
    
    public List<BookingItem> findByServiceId(UUID serviceId) {
        return bookingItemRepository.findByServiceIdOrderByCreatedDateDesc(serviceId);
    }
    
    public List<BookingItem> findByItemStatus(BookingItem.ItemStatus itemStatus) {
        return bookingItemRepository.findByItemStatusOrderByCreatedDateDesc(itemStatus);
    }
    
    public List<BookingItem> findByBookingAndItemStatus(UUID bookingId, BookingItem.ItemStatus itemStatus) {
        return bookingItemRepository.findByBooking_BookingIdAndItemStatusOrderByDisplayOrderAsc(bookingId, itemStatus);
    }
    
    public long countByBooking(UUID bookingId) {
        return bookingItemRepository.countByBooking_BookingId(bookingId);
    }
    
    public long countByItemStatus(BookingItem.ItemStatus itemStatus) {
        return bookingItemRepository.countByItemStatus(itemStatus);
    }
    
    public BigDecimal sumTotalAmountByServiceId(UUID serviceId) {
        return bookingItemRepository.sumTotalAmountByServiceId(serviceId);
    }
    
    public List<Object[]> findMostPopularItems() {
        return bookingItemRepository.findMostPopularItems();
    }
    
    public List<BookingItem> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return bookingItemRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    public List<BookingItem> findByDurationRange(Integer minDuration, Integer maxDuration) {
        return bookingItemRepository.findByDurationRange(minDuration, maxDuration);
    }
    
    @Transactional
    public void deleteByBooking(UUID bookingId) {
        bookingItemRepository.deleteByBooking_BookingId(bookingId);
    }
    
    @Transactional
    public void saveAll(List<BookingItem> bookingItems) {
        bookingItemRepository.saveAll(bookingItems);
    }
}
