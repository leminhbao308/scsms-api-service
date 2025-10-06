package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Tìm booking items theo loại item
     */
    List<BookingItem> findByItemTypeOrderByCreatedDateDesc(BookingItem.ItemType itemType);
    
    /**
     * Tìm booking items theo ID item
     */
    List<BookingItem> findByItemIdOrderByCreatedDateDesc(UUID itemId);
    
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
     * Tìm booking items theo loại item và trạng thái
     */
    List<BookingItem> findByItemTypeAndItemStatusOrderByCreatedDateDesc(
        BookingItem.ItemType itemType, BookingItem.ItemStatus itemStatus);
    
    /**
     * Đếm booking items theo booking
     */
    long countByBooking_BookingId(UUID bookingId);
    
    /**
     * Đếm booking items theo trạng thái
     */
    long countByItemStatus(BookingItem.ItemStatus itemStatus);
    
    /**
     * Đếm booking items theo loại item
     */
    long countByItemType(BookingItem.ItemType itemType);
    
    /**
     * Tính tổng số lượng item theo loại
     */
    @Query("SELECT SUM(bi.quantity) FROM BookingItem bi WHERE bi.itemType = :itemType " +
           "AND bi.booking.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS')")
    Long sumQuantityByItemType(@Param("itemType") BookingItem.ItemType itemType);
    
    /**
     * Tính tổng doanh thu theo item
     */
    @Query("SELECT SUM(bi.totalAmount) FROM BookingItem bi WHERE bi.itemId = :itemId " +
           "AND bi.booking.status = 'COMPLETED'")
    java.math.BigDecimal sumTotalAmountByItemId(@Param("itemId") UUID itemId);
    
    /**
     * Tìm booking items phổ biến nhất
     */
    @Query("SELECT bi.itemId, bi.itemName, COUNT(bi) as itemCount " +
           "FROM BookingItem bi " +
           "WHERE bi.booking.status = 'COMPLETED' " +
           "GROUP BY bi.itemId, bi.itemName " +
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
     * Tìm booking items có chiết khấu
     */
    @Query("SELECT bi FROM BookingItem bi WHERE bi.discountAmount > 0 " +
           "ORDER BY bi.discountAmount DESC")
    List<BookingItem> findItemsWithDiscount();
    
    /**
     * Tìm booking items theo booking và loại item
     */
    List<BookingItem> findByBooking_BookingIdAndItemTypeOrderByDisplayOrderAsc(
        UUID bookingId, BookingItem.ItemType itemType);
    
    /**
     * Xóa booking items theo booking
     */
    void deleteByBooking_BookingId(UUID bookingId);
}
