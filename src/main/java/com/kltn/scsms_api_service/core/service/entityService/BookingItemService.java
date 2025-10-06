package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.BookingItem;
import com.kltn.scsms_api_service.core.repository.BookingItemRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
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
    
    public List<BookingItem> findByBooking(UUID bookingId) {
        return bookingItemRepository.findByBooking_BookingIdOrderByDisplayOrderAsc(bookingId);
    }
    
    public List<BookingItem> findByItemType(BookingItem.ItemType itemType) {
        return bookingItemRepository.findByItemTypeOrderByCreatedDateDesc(itemType);
    }
    
    public List<BookingItem> findByItemId(UUID itemId) {
        return bookingItemRepository.findByItemIdOrderByCreatedDateDesc(itemId);
    }
    
    public List<BookingItem> findByItemStatus(BookingItem.ItemStatus itemStatus) {
        return bookingItemRepository.findByItemStatusOrderByCreatedDateDesc(itemStatus);
    }
    
    public List<BookingItem> findByBookingAndItemStatus(UUID bookingId, BookingItem.ItemStatus itemStatus) {
        return bookingItemRepository.findByBooking_BookingIdAndItemStatusOrderByDisplayOrderAsc(bookingId, itemStatus);
    }
    
    public List<BookingItem> findByItemTypeAndItemStatus(BookingItem.ItemType itemType, BookingItem.ItemStatus itemStatus) {
        return bookingItemRepository.findByItemTypeAndItemStatusOrderByCreatedDateDesc(itemType, itemStatus);
    }
    
    public long countByBooking(UUID bookingId) {
        return bookingItemRepository.countByBooking_BookingId(bookingId);
    }
    
    public long countByItemStatus(BookingItem.ItemStatus itemStatus) {
        return bookingItemRepository.countByItemStatus(itemStatus);
    }
    
    public long countByItemType(BookingItem.ItemType itemType) {
        return bookingItemRepository.countByItemType(itemType);
    }
    
    public Long sumQuantityByItemType(BookingItem.ItemType itemType) {
        return bookingItemRepository.sumQuantityByItemType(itemType);
    }
    
    public BigDecimal sumTotalAmountByItemId(UUID itemId) {
        return bookingItemRepository.sumTotalAmountByItemId(itemId);
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
    
    public List<BookingItem> findItemsWithDiscount() {
        return bookingItemRepository.findItemsWithDiscount();
    }
    
    public List<BookingItem> findByBookingAndItemType(UUID bookingId, BookingItem.ItemType itemType) {
        return bookingItemRepository.findByBooking_BookingIdAndItemTypeOrderByDisplayOrderAsc(bookingId, itemType);
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
