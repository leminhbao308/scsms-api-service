package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, UUID> {
    
    /**
     * Tìm booking items theo booking
     */
    List<BookingItem> findByBooking_BookingIdOrderByDisplayOrderAsc(UUID bookingId);
    
    /**
     * Tìm booking items theo service ID
     */
    List<BookingItem> findByServiceIdOrderByCreatedDateDesc(UUID serviceId);
    
    /**
     * Tìm booking items theo trạng thái
     */
    List<BookingItem> findByItemStatusOrderByCreatedDateDesc(BookingItem.ItemStatus itemStatus);
    
    /**
     * Tìm booking items theo booking và trạng thái
     */
    List<BookingItem> findByBooking_BookingIdAndItemStatusOrderByDisplayOrderAsc(
        UUID bookingId, BookingItem.ItemStatus itemStatus);
    
    /**
     * Đếm booking items theo booking
     */
    long countByBooking_BookingId(UUID bookingId);
    
    /**
     * Đếm booking items theo trạng thái
     */
    long countByItemStatus(BookingItem.ItemStatus itemStatus);
    
    /**
     * Tính tổng doanh thu theo service
     */
    @Query("SELECT SUM(bi.unitPrice) FROM BookingItem bi WHERE bi.serviceId = :serviceId " +
           "AND bi.booking.status = 'COMPLETED'")
    java.math.BigDecimal sumTotalAmountByServiceId(@Param("serviceId") UUID serviceId);
    
    /**
     * Tìm booking items phổ biến nhất
     */
    @Query("SELECT bi.serviceId, bi.serviceName, COUNT(bi) as itemCount " +
           "FROM BookingItem bi " +
           "WHERE bi.booking.status = 'COMPLETED' " +
           "GROUP BY bi.serviceId, bi.serviceName " +
           "ORDER BY itemCount DESC")
    List<Object[]> findMostPopularItems();
    
    /**
     * Tìm booking items theo khoảng giá
     */
    @Query("SELECT bi FROM BookingItem bi WHERE bi.unitPrice BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY bi.unitPrice ASC")
    List<BookingItem> findByPriceRange(
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    /**
     * Tìm booking items theo thời gian thực hiện
     */
    @Query("SELECT bi FROM BookingItem bi WHERE bi.durationMinutes BETWEEN :minDuration AND :maxDuration " +
           "ORDER BY bi.durationMinutes ASC")
    List<BookingItem> findByDurationRange(
        @Param("minDuration") Integer minDuration,
        @Param("maxDuration") Integer maxDuration);
    
    /**
     * Xóa booking items theo booking
     */
    void deleteByBooking_BookingId(UUID bookingId);
    
    /**
     * Xóa booking item bằng native query (xóa trực tiếp trong DB, tránh Hibernate cache)
     */
    @Modifying
    @Query(value = "DELETE FROM dev.booking_items WHERE booking_item_id = :bookingItemId", nativeQuery = true)
    int deleteByIdNative(@Param("bookingItemId") UUID bookingItemId);
}
