package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.BayQueue;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.repository.BayQueueRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý business logic cho BayQueue
 * Quản lý hàng chờ của từng bay
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BayQueueService {
    
    private final BayQueueRepository bayQueueRepository;
    private final BookingService bookingService;
    private final ServiceBayService serviceBayService;
    
    /**
     * Thêm booking vào hàng chờ của một bay
     */
    @Transactional
    public BayQueue addToQueue(UUID bayId, UUID bookingId) {
        return addToQueue(bayId, bookingId, LocalDate.now());
    }
    
    /**
     * Thêm booking vào hàng chờ của một bay trong ngày cụ thể
     */
    @Transactional
    public BayQueue addToQueue(UUID bayId, UUID bookingId, LocalDate queueDate) {
        log.info("Adding booking {} to bay {} queue", bookingId, bayId);
        
        // Validate bay và booking
        serviceBayService.getById(bayId);
        bookingService.getById(bookingId);
        
        // Kiểm tra booking đã có trong hàng chờ chưa
        Optional<BayQueue> existingQueue = bayQueueRepository.findActiveByBookingId(bookingId);
        if (existingQueue.isPresent()) {
            throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE, 
                "Booking đã có trong hàng chờ bay khác");
        }
        
        // Lấy vị trí cuối cùng trong hàng chờ của ngày cụ thể
        Integer lastPosition = bayQueueRepository.findLastQueuePositionByDate(bayId, queueDate);
        Integer newPosition = lastPosition + 1;
        
        // Tính thời gian dự kiến
        LocalDateTime estimatedStartTime = calculateEstimatedStartTime(bayId, newPosition);
        Booking booking = bookingService.getById(bookingId);
        LocalDateTime estimatedCompletionTime = calculateEstimatedCompletionTime(booking, estimatedStartTime);
        
        // Tạo queue entry
        BayQueue queueEntry = BayQueue.builder()
            .bayId(bayId)
            .bookingId(bookingId)
            .queuePosition(newPosition)
            .queueDate(queueDate)
            .estimatedStartTime(estimatedStartTime)
            .estimatedCompletionTime(estimatedCompletionTime)
            .isActive(true)
            .notes("Added to queue at " + LocalDateTime.now())
            .build();
        
        // Lưu vào database
        BayQueue savedQueue = bayQueueRepository.save(queueEntry);
        
        log.info("Successfully added booking {} to bay {} queue at position {}", 
            bookingId, bayId, newPosition);
        
        return savedQueue;
    }
    
    /**
     * Xóa booking khỏi hàng chờ
     */
    @Transactional
    public void removeFromQueue(UUID bayId, UUID bookingId) {
        log.info("Removing booking {} from bay {} queue", bookingId, bayId);
        
        // Tìm queue entry
        Optional<BayQueue> queueEntry = bayQueueRepository.findByBayIdAndBookingId(bayId, bookingId);
        if (queueEntry.isEmpty()) {
            throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE, 
                "Booking không có trong hàng chờ bay này");
        }
        
        BayQueue queue = queueEntry.get();
        Integer removedPosition = queue.getQueuePosition();
        
        // Deactivate queue entry
        queue.deactivate();
        bayQueueRepository.save(queue);
        
        // Cập nhật vị trí các booking khác
        updateQueuePositionsAfterRemoval(bayId, removedPosition);
        
        log.info("Successfully removed booking {} from bay {} queue", bookingId, bayId);
    }
    
    /**
     * Chuyển booking từ bay này sang bay khác
     */
    @Transactional
    public BayQueue transferBooking(UUID fromBayId, UUID toBayId, UUID bookingId) {
        log.info("Transferring booking {} from bay {} to bay {}", bookingId, fromBayId, toBayId);
        
        // Xóa khỏi bay cũ
        removeFromQueue(fromBayId, bookingId);
        
        // Thêm vào bay mới
        BayQueue newQueue = addToQueue(toBayId, bookingId);
        
        // Cập nhật booking
        Booking booking = bookingService.getById(bookingId);
        booking.setServiceBay(serviceBayService.getById(toBayId));
        bookingService.save(booking);
        
        log.info("Successfully transferred booking {} from bay {} to bay {}", 
            bookingId, fromBayId, toBayId);
        
        return newQueue;
    }
    
    /**
     * Lấy hàng chờ của một bay
     */
    public List<BayQueue> getBayQueue(UUID bayId) {
        log.info("Getting queue for bay {}", bayId);
        return bayQueueRepository.findActiveByBayId(bayId);
    }
    
    /**
     * Lấy hàng chờ của một bay trong ngày cụ thể
     */
    public List<BayQueue> getBayQueue(UUID bayId, LocalDate queueDate) {
        log.info("Getting queue for bay {} on date {}", bayId, queueDate);
        return bayQueueRepository.findActiveByBayIdAndDate(bayId, queueDate);
    }
    
    /**
     * Lấy vị trí của booking trong hàng chờ
     */
    public Optional<BayQueue> getBookingQueuePosition(UUID bookingId) {
        log.info("Getting queue position for booking {}", bookingId);
        return bayQueueRepository.findActiveByBookingId(bookingId);
    }
    
    /**
     * Đếm số lượng booking trong hàng chờ của một bay
     */
    public Long getQueueLength(UUID bayId) {
        log.info("Getting queue length for bay {}", bayId);
        return bayQueueRepository.countActiveByBayId(bayId);
    }
    
    /**
     * Lấy booking sắp được phục vụ (vị trí 1-3)
     */
    public List<BayQueue> getUpcomingBookings(UUID bayId) {
        log.info("Getting upcoming bookings for bay {}", bayId);
        return bayQueueRepository.findUpcomingByBayId(bayId);
    }
    
    /**
     * Cập nhật vị trí trong hàng chờ sau khi xóa một booking
     */
    @Transactional
    public void updateQueuePositionsAfterRemoval(UUID bayId, Integer removedPosition) {
        log.info("Updating queue positions for bay {} after removing position {}", bayId, removedPosition);
        
        // Lấy tất cả queue entries có vị trí lớn hơn vị trí bị xóa
        List<BayQueue> queuesToUpdate = bayQueueRepository.findByBayIdAndPositionGreaterThanEqual(bayId, removedPosition + 1);
        
        // Cập nhật vị trí (giảm đi 1)
        for (BayQueue queue : queuesToUpdate) {
            queue.updateQueuePosition(queue.getQueuePosition() - 1);
            bayQueueRepository.save(queue);
        }
        
        log.info("Updated {} queue positions for bay {}", queuesToUpdate.size(), bayId);
    }
    
    /**
     * Tính thời gian bắt đầu dự kiến
     */
    private LocalDateTime calculateEstimatedStartTime(UUID bayId, Integer queuePosition) {
        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();
        
        // Nếu là vị trí đầu tiên, bắt đầu ngay
        if (queuePosition == 1) {
            return now;
        }
        
        // Lấy hàng chờ hiện tại của bay
        List<BayQueue> currentQueue = bayQueueRepository.findActiveByBayId(bayId);
        
        // Tính thời gian bắt đầu dự kiến dựa trên thời gian hoàn thành của booking trước đó
        LocalDateTime estimatedStartTime = now;
        
        for (BayQueue queueItem : currentQueue) {
            if (queueItem.getQueuePosition() < queuePosition) {
                // Lấy thời gian hoàn thành của booking trước đó
                if (queueItem.getEstimatedCompletionTime() != null) {
                    estimatedStartTime = queueItem.getEstimatedCompletionTime();
                } else {
                    // Nếu không có thời gian hoàn thành, tính dựa trên thời gian dịch vụ
                    Booking booking = bookingService.getById(queueItem.getBookingId());
                    Integer duration = booking.getEstimatedDurationMinutes();
                    if (duration == null) {
                        duration = 60; // Default 60 phút
                    }
                    estimatedStartTime = estimatedStartTime.plusMinutes(duration);
                }
            }
        }
        
        // Cho phép đặt qua thời gian kết thúc của bay (không giới hạn thời gian)
        return estimatedStartTime;
    }
    
    /**
     * Tính thời gian hoàn thành dự kiến
     */
    private LocalDateTime calculateEstimatedCompletionTime(Booking booking, LocalDateTime startTime) {
        // Lấy thời gian ước tính của booking
        Integer estimatedDuration = booking.getEstimatedDurationMinutes();
        
        // Nếu không có thời gian ước tính, tính từ số lượng booking items
        if (estimatedDuration == null || estimatedDuration == 0) {
            if (booking.getBookingItems() != null && !booking.getBookingItems().isEmpty()) {
                // Mỗi booking item mất khoảng 60 phút
                estimatedDuration = booking.getBookingItems().size() * 60;
            } else {
                estimatedDuration = 60; // Default 60 phút
            }
        }
        
        // Nếu vẫn không có thời gian ước tính, sử dụng default
        if (estimatedDuration == 0) {
            estimatedDuration = 60; // Default 60 phút
        }
        
        return startTime.plusMinutes(estimatedDuration);
    }
    
    /**
     * Cập nhật thời gian dự kiến cho tất cả booking trong hàng chờ
     */
    @Transactional
    public void updateEstimatedTimesForBay(UUID bayId) {
        log.info("Updating estimated times for bay {}", bayId);
        
        List<BayQueue> queues = bayQueueRepository.findActiveByBayId(bayId);
        
        for (int i = 0; i < queues.size(); i++) {
            BayQueue queue = queues.get(i);
            Integer position = i + 1;
            
            // Cập nhật vị trí
            queue.updateQueuePosition(position);
            
            // Tính thời gian mới
            LocalDateTime estimatedStartTime = calculateEstimatedStartTime(bayId, position);
            LocalDateTime estimatedCompletionTime = calculateEstimatedCompletionTime(
                bookingService.getById(queue.getBookingId()), estimatedStartTime);
            
            queue.updateEstimatedTimes(estimatedStartTime, estimatedCompletionTime);
            bayQueueRepository.save(queue);
        }
        
        log.info("Updated estimated times for {} bookings in bay {}", queues.size(), bayId);
    }
    
    /**
     * Xóa booking khỏi hàng chờ (không cần biết bayId)
     * Sử dụng cho việc cancel booking
     */
    @Transactional
    public void removeBookingFromQueue(UUID bookingId) {
        log.info("Removing booking {} from queue (auto-detect bay)", bookingId);
        
        // Tìm queue entry của booking
        Optional<BayQueue> queueEntry = bayQueueRepository.findActiveByBookingId(bookingId);
        if (queueEntry.isEmpty()) {
            log.info("Booking {} not found in any queue", bookingId);
            return; // Không throw exception vì có thể booking không có trong queue
        }
        
        BayQueue queue = queueEntry.get();
        UUID bayId = queue.getBayId();
        Integer removedPosition = queue.getQueuePosition();
        
        log.info("Found booking {} in bay {} at position {}", bookingId, bayId, removedPosition);
        
        // Deactivate queue entry
        queue.deactivate();
        bayQueueRepository.save(queue);
        
        // Cập nhật vị trí các booking khác
        updateQueuePositionsAfterRemoval(bayId, removedPosition);
        
        log.info("Successfully removed booking {} from bay {} queue", bookingId, bayId);
    }
}
