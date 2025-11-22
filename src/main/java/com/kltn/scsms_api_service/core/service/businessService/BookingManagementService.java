package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.param.BookingFilterParam;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.ChangeScheduleRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.UpdateBookingRequest;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.core.service.websocket.WebSocketService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.BookingMapper;
import com.kltn.scsms_api_service.mapper.BookingItemMapper;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingItemRequest;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingManagementService {

    private final BookingService bookingService;
    private final BookingItemService bookingItemService;
    private final BranchService branchService;
    private final ServiceBayService serviceBayService;
    private final BookingMapper bookingMapper;
    private final BookingItemMapper bookingItemMapper;
    private final BookingInfoService bookingInfoService;
    private final PricingBusinessService pricingBusinessService;
    private final ServiceService serviceService;
    private final WebSocketService webSocketService;

    /**
     * Lấy tất cả booking
     */
    public List<BookingInfoDto> getAllBookings() {
        log.info("Getting all bookings");
        List<Booking> bookings = bookingService.findAll();
        return bookingInfoService.toBookingInfoDtoList(bookings);
    }

    /**
     * Lấy booking với phân trang và filter
     */
    public Page<BookingInfoDto> getAllBookings(BookingFilterParam filterParam) {
        log.info("Getting all bookings with filter: {}", filterParam);

        // Standardize filter
        filterParam = filterParam.standardizeFilterRequest(filterParam);

        // Create pageable
        Sort sort = Sort.by(
                filterParam.getDirection().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filterParam.getSort());
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);

        // TODO: Implement custom repository methods for filtering
        // For now, we'll fetch all and filter in memory or use JPA Criteria API
        Page<Booking> bookingPage = bookingService.findAll(pageable);

        return bookingPage.map(bookingInfoService::toBookingInfoDto);
    }

    /**
     * Lấy booking theo ID
     * Uses optimized query to prevent N+1 queries
     */
    public BookingInfoDto getBookingById(UUID bookingId) {
        log.info("Getting booking by ID: {}", bookingId);
        Booking booking = bookingService.getByIdWithDetails(bookingId);
        return bookingInfoService.toBookingInfoDto(booking);
    }

    /**
     * Lấy booking theo mã booking
     * Uses optimized query to prevent N+1 queries
     */
    public BookingInfoDto getBookingByCode(String bookingCode) {
        log.info("Getting booking by code: {}", bookingCode);
        Booking booking = bookingService.getByBookingCodeWithDetails(bookingCode);
        return bookingInfoService.toBookingInfoDto(booking);
    }

    /**
     * Lấy booking theo khách hàng
     */
    public List<BookingInfoDto> getBookingsByCustomer(UUID customerId) {
        log.info("Getting bookings for customer: {}", customerId);
        List<Booking> bookings = bookingService.findByCustomer(customerId);
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy booking theo chi nhánh
     */
    public List<BookingInfoDto> getBookingsByBranch(UUID branchId) {
        log.info("Getting bookings for branch: {}", branchId);
        List<Booking> bookings = bookingService.findByBranch(branchId);
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy booking theo chi nhánh và ngày
     */
    public List<BookingInfoDto> getBookingsByBranchAndDate(UUID branchId, LocalDate bookingDate) {
        log.info("Getting bookings for branch: {} on date: {}", branchId, bookingDate);
        List<Booking> bookings = bookingService.findByBranchAndDate(branchId, bookingDate);
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy booking theo trạng thái
     */
    public List<BookingInfoDto> getBookingsByStatus(Booking.BookingStatus status) {
        log.info("Getting bookings with status: {}", status);
        List<Booking> bookings = bookingService.findByStatus(status);
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy booking theo nhiều trạng thái
     * Dùng để quản lý chăm sóc xe (CHECKED_IN, IN_PROGRESS, CANCELLED, COMPLETED)
     */
    public List<BookingInfoDto> getBookingsByStatuses(List<Booking.BookingStatus> statuses) {
        log.info("Getting bookings with statuses: {}", statuses);
        List<Booking> bookings = bookingService.findByStatusIn(statuses);
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy booking sắp tới của khách hàng
     */
    public List<BookingInfoDto> getUpcomingBookingsByCustomer(UUID customerId) {
        log.info("Getting upcoming bookings for customer: {}", customerId);
        List<Booking> bookings = bookingService.findUpcomingBookingsByCustomer(customerId, LocalDateTime.now());
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy booking quá khứ của khách hàng
     */
    public List<BookingInfoDto> getPastBookingsByCustomer(UUID customerId) {
        log.info("Getting past bookings for customer: {}", customerId);
        List<Booking> bookings = bookingService.findPastBookingsByCustomer(customerId, LocalDateTime.now());
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }


    /**
     * Cập nhật booking
     */
    @Transactional
    public BookingInfoDto updateBooking(UUID bookingId, UpdateBookingRequest request) {
        log.info("Updating booking: {}", bookingId);

        Booking existingBooking = bookingService.getByIdWithDetails(bookingId);

        // Check if booking can be updated
        if (existingBooking.isCompleted() || existingBooking.isCancelled()) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_UPDATED,
                    "Cannot update completed or cancelled booking");
        }

        // Validate bay availability if time is being updated
        // Validate conflict với booking khác nếu có thay đổi scheduledStartAt/scheduledEndAt
        if (request.getScheduledStartAt() != null || request.getScheduledEndAt() != null) {
            UUID bayId = existingBooking.getServiceBay() != null ? existingBooking.getServiceBay().getBayId() : null;
            LocalDateTime startTime = request.getScheduledStartAt() != null ? request.getScheduledStartAt()
                    : existingBooking.getScheduledStartAt();
            LocalDateTime endTime = request.getScheduledEndAt() != null ? request.getScheduledEndAt()
                    : existingBooking.getScheduledEndAt();

            if (bayId != null && startTime != null && endTime != null) {
                boolean isBayAvailable = serviceBayService.isBayAvailableInTimeRange(bayId, startTime, endTime);

                if (!isBayAvailable) {
                    // Find conflicting booking for better error message
                    List<Booking> conflictingBookings = bookingService.findConflictingBookings(bayId, startTime,
                            endTime);

                    if (!conflictingBookings.isEmpty()) {
                        Booking conflictBooking = conflictingBookings.get(0);
                        // Exclude current booking from conflict check
                        if (!conflictBooking.getBookingId().equals(bookingId)) {
                            ServiceBay serviceBay = serviceBayService.getById(bayId);
                            throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE,
                                    String.format("Service bay '%s' is not available in the specified time range. " +
                                            "Conflicts with booking '%s' (%s - %s)",
                                            serviceBay.getBayName(),
                                            conflictBooking.getBookingCode(),
                                            conflictBooking.getScheduledStartAt(),
                                            conflictBooking.getScheduledEndAt()));
                        }
                    }
                }
            }
        }

        // Update booking
        Booking updatedBooking = bookingMapper.updateEntity(existingBooking, request);

        // Handle branch change if branch_id is provided and different from current branch
        if (request.getBranchId() != null) {
            UUID newBranchId = request.getBranchId();
            UUID currentBranchId = existingBooking.getBranch() != null ? existingBooking.getBranch().getBranchId() : null;
            
            // Only update branch if it's different (handle null currentBranchId)
            if (currentBranchId == null || !newBranchId.equals(currentBranchId)) {
                log.info("Updating branch for booking {} from {} to {}", bookingId, currentBranchId, newBranchId);
                Branch newBranch = branchService.findById(newBranchId)
                        .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                                "Branch not found with ID: " + newBranchId));
                updatedBooking.setBranch(newBranch);
            }
        }

        // Xử lý thay đổi schedule nếu có (chỉ cho scheduled booking, không cho walk-in booking)
        if ((request.getServiceBayId() != null || request.getScheduleDate() != null || request.getScheduleStartTime() != null)
                && existingBooking.getBookingType() != null 
                && existingBooking.getBookingType() == com.kltn.scsms_api_service.core.entity.enumAttribute.BookingType.SCHEDULED) {
            handleScheduleChange(updatedBooking, request);
        }

        // Xử lý thay đổi bay cho walk-in booking nếu có
        if (request.getServiceBayId() != null
                && existingBooking.getBookingType() != null
                && existingBooking.getBookingType() == com.kltn.scsms_api_service.core.entity.enumAttribute.BookingType.WALK_IN) {
            handleWalkInBayChange(updatedBooking, request);
        }

        // Xử lý cập nhật booking items (dịch vụ) nếu có
        if (request.getBookingItems() != null) {
            // Lưu duration ban đầu để so sánh
            Integer originalDuration = existingBooking.getEstimatedDurationMinutes();
            updateBookingItems(bookingId, request.getBookingItems(), existingBooking);
            
            // Kiểm tra walk-in booking tăng thời gian và có booking sau
            if (existingBooking.getBookingType() == com.kltn.scsms_api_service.core.entity.enumAttribute.BookingType.WALK_IN
                    && existingBooking.getEstimatedDurationMinutes() != null
                    && originalDuration != null
                    && existingBooking.getEstimatedDurationMinutes() > originalDuration) {
                validateWalkInDurationIncrease(existingBooking, originalDuration);
            }
            
            // Kiểm tra slot booking tăng thời gian và cần slot mới
            if (existingBooking.getBookingType() == com.kltn.scsms_api_service.core.entity.enumAttribute.BookingType.SCHEDULED
                    && existingBooking.getEstimatedDurationMinutes() != null
                    && originalDuration != null
                    && existingBooking.getEstimatedDurationMinutes() > originalDuration
                    && existingBooking.getScheduledStartAt() != null
                    && existingBooking.getServiceBay() != null) {
                validateSlotBookingDurationIncrease(existingBooking, originalDuration);
            }
        }

        Booking savedBooking = bookingService.update(updatedBooking);

        // Gửi WebSocket notification với structured event
        webSocketService.notifyBookingUpdated(savedBooking);

        return bookingInfoService.toBookingInfoDto(savedBooking);
    }

    /**
     * Xóa booking (soft delete)
     */
    @Transactional
    public void deleteBooking(UUID bookingId) {
        log.info("Deleting booking: {}", bookingId);

        Booking booking = bookingService.getByIdWithDetails(bookingId);

        // Check if booking can be deleted
        if (booking.isActive()) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_DELETED,
                    "Cannot delete active booking");
        }

        // Bay will be automatically available when booking is deleted
        // No need to manually unassign bay

        // Lưu booking info trước khi xóa để gửi event
        UUID deletedBookingId = booking.getBookingId();
        String deletedBookingCode = booking.getBookingCode();
        
        bookingService.delete(bookingId);

        // Gửi WebSocket notification với structured event
        // Note: Booking đã bị xóa nên không có bookingData
        webSocketService.notifyBookingDeleted(deletedBookingId, deletedBookingCode);
    }

    /**
     * Hủy booking
     */
    @Transactional
    public void cancelBooking(UUID bookingId, String reason, String cancelledBy) {
        log.info("Cancelling booking: {} with reason: {}", bookingId, reason);

        Booking booking = bookingService.getByIdWithDetails(bookingId);

        // Check if booking can be cancelled
        if (booking.isCompleted() || booking.isCancelled()) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_CANCELLED,
                    "Cannot cancel completed or already cancelled booking");
        }

        // Cancel booking
        booking.cancelBooking(reason, cancelledBy);
        Booking updatedBooking = bookingService.update(booking);

        // Gửi WebSocket notification với structured event
        webSocketService.notifyBookingCancelled(updatedBooking);

        // Bay will be automatically available when booking is deleted
        // No need to manually unassign bay
    }

    /**
     * Confirm booking
     */
    @Transactional
    public void confirmBooking(UUID bookingId) {
        log.info("Confirming booking: {}", bookingId);

        Booking booking = bookingService.getByIdWithDetails(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_CONFIRMED,
                    "Only pending bookings can be confirmed");
        }

        booking.updateStatus(Booking.BookingStatus.CONFIRMED, "Booking confirmed by staff");
        Booking updatedBooking = bookingService.update(booking);

        // Gửi WebSocket notification với structured event
        webSocketService.notifyBookingConfirmed(updatedBooking);
    }

    /**
     * Check-in booking
     */
    @Transactional
    public void checkInBooking(UUID bookingId) {
        log.info("Checking in booking: {}", bookingId);

        Booking booking = bookingService.getByIdWithDetails(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_CHECKED_IN,
                    "Only confirmed bookings can be checked in");
        }

        booking.checkIn();
        Booking updatedBooking = bookingService.update(booking);

        // Gửi WebSocket notification với structured event
        webSocketService.notifyBookingCheckedIn(updatedBooking);
    }

    /**
     * Bắt đầu dịch vụ
     */
    @Transactional
    public void startService(UUID bookingId) {
        log.info("Starting service for booking: {}", bookingId);

        Booking booking = bookingService.getByIdWithDetails(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_STARTED,
                    "Only checked-in bookings can be started");
        }

        booking.startService();
        Booking updatedBooking = bookingService.update(booking);

        // Gửi WebSocket notification với structured event
        webSocketService.notifyBookingStarted(updatedBooking);
    }

    /**
     * Hoàn thành dịch vụ
     */
    @Transactional
    public void completeService(UUID bookingId) {
        log.info("Completing service for booking: {}", bookingId);

        Booking booking = bookingService.getByIdWithDetails(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.IN_PROGRESS) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_COMPLETED,
                    "Only in-progress bookings can be completed");
        }

        booking.completeService();
        Booking updatedBooking = bookingService.update(booking);

        // Gửi WebSocket notification với structured event
        webSocketService.notifyBookingCompleted(updatedBooking);

        // Bay will be automatically available when booking is completed
        // No need to manually unassign bay
    }

    /**
     * Tìm kiếm booking theo tên khách hàng
     */
    public List<BookingInfoDto> searchBookingsByCustomerName(String customerName) {
        log.info("Searching bookings by customer name: {}", customerName);
        List<Booking> bookings = bookingService.findByCustomerNameContaining(customerName);
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm booking theo số điện thoại
     */
    public List<BookingInfoDto> searchBookingsByPhone(String phoneNumber) {
        log.info("Searching bookings by phone: {}", phoneNumber);
        List<Booking> bookings = bookingService.findByCustomerPhoneContaining(phoneNumber);
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm booking theo biển số xe
     */
    public List<BookingInfoDto> searchBookingsByLicensePlate(String licensePlate) {
        log.info("Searching bookings by license plate: {}", licensePlate);
        List<Booking> bookings = bookingService.findByVehicleLicensePlateContaining(licensePlate);
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thống kê booking
     */
    public BookingStatisticsDto getBookingStatistics(UUID branchId, LocalDate date) {
        log.info("Getting booking statistics for branch: {} on date: {}", branchId, date);

        long totalBookings = bookingService.countByBranchAndDate(branchId, date);
        long pendingBookings = bookingService.countByBranchAndStatus(branchId, Booking.BookingStatus.PENDING);
        long confirmedBookings = bookingService.countByBranchAndStatus(branchId, Booking.BookingStatus.CONFIRMED);
        long completedBookings = bookingService.countByBranchAndStatus(branchId, Booking.BookingStatus.COMPLETED);
        long cancelledBookings = bookingService.countByBranchAndStatus(branchId, Booking.BookingStatus.CANCELLED);

        return BookingStatisticsDto.builder()
                .totalBookings(totalBookings)
                .pendingBookings(pendingBookings)
                .confirmedBookings(confirmedBookings)
                .completedBookings(completedBookings)
                .cancelledBookings(cancelledBookings)
                .build();
    }


    /**
     * Tính unit price cho booking item từ bảng giá
     */
    private BigDecimal calculateItemUnitPrice(CreateBookingItemRequest itemRequest) {
        try {
            // Lấy giá service từ bảng giá
            return pricingBusinessService.resolveServicePrice(itemRequest.getServiceId(), null);
        } catch (Exception e) {
            log.error("Error calculating unit price for service {}: {}", itemRequest.getServiceId(), e.getMessage());
            throw new RuntimeException("Could not determine unit price for service: " + itemRequest.getServiceName());
        }
    }

    public void revertPaymentStatusToPending(Set<UUID> bookingIds) {
        log.info("Reverting payment status to PENDING for bookings: {}", bookingIds);
        List<Booking> bookings = bookingService.findAllByIds(bookingIds);
        for (Booking booking : bookings) {
            if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
                booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
                bookingService.update(booking);
                log.info("Reverted payment status to PENDING for booking: {}", booking.getBookingId());
            } else {
                log.info("Booking {} is not in PAID status, skipping", booking.getBookingId());
            }
        }
    }

    /**
     * DTO cho thống kê booking
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BookingStatisticsDto {
        private long totalBookings;
        private long pendingBookings;
        private long confirmedBookings;
        private long completedBookings;
        private long cancelledBookings;
    }

    /**
     * Lấy tất cả booking có trạng thái thanh toán pending và trạng thái booking
     * completed
     */
    public List<BookingInfoDto> getBookingsWithPendingPayment() {
        log.info("Getting bookings with pending payment status and completed status");

        List<Booking> bookings = bookingService.findByPaymentStatusAndStatus(
                Booking.PaymentStatus.PENDING,
                Booking.BookingStatus.COMPLETED);

        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Đánh dấu booking đã thanh toán
     */
    @Transactional
    public BookingInfoDto markBookingAsPaid(UUID bookingId, Map<String, String> paymentDetails) {
        log.info("Marking booking as paid: {}", bookingId);

        // Lấy booking từ database
        Booking booking = bookingService.getByIdWithDetails(bookingId);

        // Kiểm tra trạng thái booking
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Cannot mark cancelled booking as paid");
        }

        // Kiểm tra trạng thái thanh toán hiện tại
        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Booking is already marked as paid");
        }

        // Cập nhật trạng thái thanh toán
        booking.setPaymentStatus(Booking.PaymentStatus.PAID);

        // Cập nhật thông tin thanh toán nếu có
        if (paymentDetails != null) {
            String paymentMethod = paymentDetails.get("paymentMethod");
            String transactionId = paymentDetails.get("transactionId");
            String notes = paymentDetails.get("notes");

            if (paymentMethod != null) {
                log.info("Payment method: {}", paymentMethod);
            }
            if (transactionId != null) {
                log.info("Transaction ID: {}", transactionId);
            }
            if (notes != null) {
                log.info("Payment notes: {}", notes);
            }
        }

        // Lưu booking
        Booking savedBooking = bookingService.save(booking);

        log.info("Successfully marked booking {} as paid", bookingId);
        return bookingInfoService.toBookingInfoDto(savedBooking);
    }

    /**
     * Thay đổi schedule (bay, date, time) của booking
     */
    @Transactional
    public BookingInfoDto changeBookingSlot(UUID bookingId, ChangeScheduleRequest request) {
        log.info("Changing schedule for booking: {} to bay: {} at {} {}",
                bookingId, request.getNewBayId(), request.getNewScheduleDate(), request.getNewScheduleStartTime());

        // 1. Lấy booking hiện tại
        Booking existingBooking = bookingService.getByIdWithDetails(bookingId);

        // 2. Kiểm tra booking có thể thay đổi schedule không
        if (existingBooking.isCompleted() || existingBooking.isCancelled()) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_UPDATED,
                    "Cannot change schedule for completed or cancelled booking");
        }

        if (existingBooking.getStatus() == Booking.BookingStatus.IN_PROGRESS) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_UPDATED,
                    "Cannot change schedule for booking that is in progress");
        }

        // 3. Validate bay mới và conflict với booking khác
        ServiceBay newServiceBay = serviceBayService.getById(request.getNewBayId());
        
        // 3.1. Ensure branch matches the bay's branch
        if (newServiceBay.getBranch() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Service bay must have an associated branch");
        }
        UUID newBayBranchId = newServiceBay.getBranch().getBranchId();
        if (existingBooking.getBranch() == null || !newBayBranchId.equals(existingBooking.getBranch().getBranchId())) {
            log.info("Bay belongs to different branch, updating branch to match bay's branch");
            Branch bayBranch = branchService.findById(newBayBranchId)
                    .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                            "Branch not found for service bay: " + newBayBranchId));
            existingBooking.setBranch(bayBranch);
        }

        // 4. Tính thời gian kết thúc dự kiến
        LocalDateTime newScheduleStartDateTime = LocalDateTime.of(request.getNewScheduleDate(), request.getNewScheduleStartTime());
        // Không cộng buffer vào estimated duration
        LocalDateTime newScheduleEndDateTime = newScheduleStartDateTime.plusMinutes(
                request.getServiceDurationMinutes());

        // 5. Validate không có conflict với booking khác
        boolean isBayAvailable = serviceBayService.isBayAvailableInTimeRange(
                request.getNewBayId(),
                newScheduleStartDateTime,
                newScheduleEndDateTime);
        
        if (!isBayAvailable) {
            List<Booking> conflictingBookings = bookingService.findConflictingBookings(
                    request.getNewBayId(),
                    newScheduleStartDateTime,
                    newScheduleEndDateTime);
            
            if (!conflictingBookings.isEmpty()) {
                Booking conflictBooking = conflictingBookings.get(0);
                // Exclude current booking from conflict check
                if (!conflictBooking.getBookingId().equals(bookingId)) {
                    throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE,
                            String.format("Service bay '%s' is not available in the specified time range. " +
                                    "Conflicts with booking '%s' (%s - %s)",
                                    newServiceBay.getBayName(),
                                    conflictBooking.getBookingCode(),
                                    conflictBooking.getScheduledStartAt(),
                                    conflictBooking.getScheduledEndAt()));
                }
            }
        }

        // 6. Cập nhật thông tin booking
        existingBooking.setServiceBay(newServiceBay);
        existingBooking.setScheduledStartAt(newScheduleStartDateTime);
        existingBooking.setScheduledEndAt(newScheduleEndDateTime);
        existingBooking.setPreferredStartAt(newScheduleStartDateTime);

        // Lưu booking
        Booking savedBooking = bookingService.update(existingBooking);

        // Gửi WebSocket notification với structured event
        webSocketService.notifyBookingUpdated(savedBooking, 
            String.format("Lịch hẹn cho booking #%s đã được thay đổi", savedBooking.getBookingCode()));

        log.info("Successfully changed schedule for booking: {} to bay: {} at {} {}",
                bookingId, request.getNewBayId(), request.getNewScheduleDate(), request.getNewScheduleStartTime());

        return bookingInfoService.toBookingInfoDto(savedBooking);
    }

    /**
     * Xử lý thay đổi schedule (bay, date, time) trong update booking
     */
    private void handleScheduleChange(Booking booking, UpdateBookingRequest request) {
        log.info("Handling schedule change for booking: {}", booking.getBookingId());

        // 1. Determine new bay and scheduled time values
        UUID newBayId = request.getServiceBayId() != null ? request.getServiceBayId()
                : booking.getServiceBay() != null ? booking.getServiceBay().getBayId() : null;
        
        if (newBayId == null) {
            log.warn("No bay ID provided, skipping");
            return;
        }
        
        // Lấy scheduled time từ request hoặc booking hiện tại
        LocalDateTime newScheduledStartAt = null;
        if (request.getScheduledStartAt() != null) {
            newScheduledStartAt = request.getScheduledStartAt();
        } else if (request.getScheduleDate() != null && request.getScheduleStartTime() != null) {
            // Calculate from schedule_date + schedule_start_time
            newScheduledStartAt = LocalDateTime.of(request.getScheduleDate(), request.getScheduleStartTime());
        } else {
            newScheduledStartAt = booking.getScheduledStartAt();
        }
        
        if (newScheduledStartAt == null) {
            log.warn("No scheduled start time provided, skipping");
            return;
        }

        // 2. Check if bay or time has actually changed
        boolean hasChanged = booking.getServiceBay() == null || 
                !newBayId.equals(booking.getServiceBay().getBayId()) ||
                (booking.getScheduledStartAt() != null && !newScheduledStartAt.equals(booking.getScheduledStartAt()));

        if (!hasChanged) {
            log.info("No changes detected, skipping");
            return;
        }

        // 3. Lấy service bay mới
        ServiceBay newServiceBay = serviceBayService.getById(newBayId);
        
        // 4. Ensure branch matches the bay's branch
        if (newServiceBay.getBranch() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Service bay must have an associated branch");
        }
        UUID newBayBranchId = newServiceBay.getBranch().getBranchId();
        if (booking.getBranch() == null || !newBayBranchId.equals(booking.getBranch().getBranchId())) {
            log.info("Bay belongs to different branch, updating branch to match bay's branch");
            Branch bayBranch = branchService.findById(newBayBranchId)
                    .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                            "Branch not found for service bay: " + newBayBranchId));
            booking.setBranch(bayBranch);
        }

        // 5. Tính thời gian kết thúc dự kiến
        int serviceDurationMinutes = request.getEstimatedDurationMinutes() != null
                ? request.getEstimatedDurationMinutes()
                : (booking.getEstimatedDurationMinutes() != null ? booking.getEstimatedDurationMinutes() : 60);
        LocalDateTime newScheduledEndAt = newScheduledStartAt.plusMinutes(serviceDurationMinutes);

        // 6. Validate không có conflict với booking khác
        boolean isBayAvailable = serviceBayService.isBayAvailableInTimeRange(
                newBayId,
                newScheduledStartAt,
                newScheduledEndAt);
        
        if (!isBayAvailable) {
            List<Booking> conflictingBookings = bookingService.findConflictingBookings(
                    newBayId,
                    newScheduledStartAt,
                    newScheduledEndAt);
            
            if (!conflictingBookings.isEmpty()) {
                Booking conflictBooking = conflictingBookings.get(0);
                // Exclude current booking from conflict check
                if (!conflictBooking.getBookingId().equals(booking.getBookingId())) {
                    throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE,
                            String.format("Service bay '%s' is not available in the specified time range. " +
                                    "Conflicts with booking '%s' (%s - %s)",
                                    newServiceBay.getBayName(),
                                    conflictBooking.getBookingCode(),
                                    conflictBooking.getScheduledStartAt(),
                                    conflictBooking.getScheduledEndAt()));
                }
            }
        }

        // 7. Update booking information
        booking.setServiceBay(newServiceBay);
        booking.setScheduledStartAt(newScheduledStartAt);
        booking.setScheduledEndAt(newScheduledEndAt);
        booking.setPreferredStartAt(newScheduledStartAt);

        log.info("Successfully updated schedule for booking: {} to bay: {} at {}",
                booking.getBookingId(), newBayId, newScheduledStartAt);
    }

    /**
     * Xử lý thay đổi bay cho walk-in booking
     * Walk-in booking không có scheduled time cố định, chỉ cần validate bay thuộc branch
     */
    private void handleWalkInBayChange(Booking booking, UpdateBookingRequest request) {
        log.info("Handling bay change for walk-in booking: {}", booking.getBookingId());

        UUID newBayId = request.getServiceBayId();
        if (newBayId == null) {
            log.warn("No bay ID provided for walk-in booking, skipping");
            return;
        }

        // Check if bay has actually changed
        UUID currentBayId = booking.getServiceBay() != null ? booking.getServiceBay().getBayId() : null;
        if (currentBayId != null && newBayId.equals(currentBayId)) {
            log.info("Bay has not changed, skipping update");
            return;
        }

        // Get new service bay
        ServiceBay newServiceBay = serviceBayService.getById(newBayId);

        // Validate bay belongs to booking's branch
        if (newServiceBay.getBranch() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Service bay must have an associated branch");
        }

        UUID newBayBranchId = newServiceBay.getBranch().getBranchId();
        UUID bookingBranchId = booking.getBranch() != null ? booking.getBranch().getBranchId() : null;

        if (bookingBranchId == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Booking must have an associated branch");
        }

        if (!newBayBranchId.equals(bookingBranchId)) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    String.format("Service bay '%s' does not belong to booking's branch. " +
                            "Bay belongs to branch '%s', but booking belongs to branch '%s'",
                            newServiceBay.getBayName(),
                            newServiceBay.getBranch().getBranchName(),
                            booking.getBranch().getBranchName()));
        }

        // Validate bay is active
        if (!newServiceBay.getIsActive()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Service bay is not active: " + newServiceBay.getBayName());
        }

        // Only validate allowBooking if bay is actually changing
        // If keeping the same bay, we don't need to check allowBooking as it's already in use
        if (!newServiceBay.getAllowBooking()) {
            log.warn("Service bay '{}' does not allow booking, but proceeding as it may be the current bay or a valid exception",
                    newServiceBay.getBayName());
            // For walk-in bookings, we allow updating to a bay even if allowBooking is false
            // as the bay might be temporarily disabled but still usable for existing bookings
            // or it might be a special case for walk-in bookings
        }

        // Update bay for walk-in booking
        booking.setServiceBay(newServiceBay);

        log.info("Successfully updated bay for walk-in booking: {} to bay: {}",
                booking.getBookingId(), newBayId);
    }

    /**
     * Xử lý cập nhật booking items (dịch vụ)
     * Hỗ trợ operation DELETE để xóa items một cách explicit
     */
    @Transactional
    private void updateBookingItems(UUID bookingId, List<CreateBookingItemRequest> newBookingItems, Booking existingBooking) {
        log.info("Updating booking items for booking: {}", bookingId);

        // 1. Lấy danh sách booking items hiện tại
        List<BookingItem> existingItems = bookingItemService.findByBooking(bookingId);
        
        // 2. Tạo map để tìm nhanh: bookingItemId -> BookingItem và serviceId -> BookingItem
        Map<UUID, BookingItem> existingItemsByIdMap = existingItems.stream()
                .collect(Collectors.toMap(BookingItem::getBookingItemId, item -> item));
        Map<UUID, BookingItem> existingItemsByServiceIdMap = existingItems.stream()
                .collect(Collectors.toMap(BookingItem::getServiceId, item -> item));

        // 3. Xử lý các items có operation DELETE trước
        for (CreateBookingItemRequest itemRequest : newBookingItems) {
            if (itemRequest.getOperation() == CreateBookingItemRequest.ItemOperation.DELETE) {
                handleDeleteItem(itemRequest, bookingId, existingItemsByIdMap, existingItemsByServiceIdMap);
            }
        }

        // 4. Xử lý các items cần thêm hoặc cập nhật (không có operation DELETE)
        for (CreateBookingItemRequest itemRequest : newBookingItems) {
            if (itemRequest.getOperation() != CreateBookingItemRequest.ItemOperation.DELETE) {
                handleAddOrUpdateItem(itemRequest, existingBooking, existingItemsByServiceIdMap);
            }
        }

        // 5. Tính lại tổng giá và thời gian ước tính
        recalculateBookingTotals(existingBooking);
    }

    /**
     * Xử lý xóa item theo operation DELETE
     */
    private void handleDeleteItem(CreateBookingItemRequest itemRequest, UUID bookingId,
                                   Map<UUID, BookingItem> existingItemsByIdMap,
                                   Map<UUID, BookingItem> existingItemsByServiceIdMap) {
        BookingItem itemToDelete = null;

        // Tìm item theo bookingItemId (ưu tiên)
        if (itemRequest.getBookingItemId() != null) {
            itemToDelete = existingItemsByIdMap.get(itemRequest.getBookingItemId());
            if (itemToDelete == null) {
                throw new ClientSideException(ErrorCode.NOT_FOUND,
                        "Booking item not found with ID: " + itemRequest.getBookingItemId());
            }
        }
        // Nếu không có bookingItemId, tìm theo serviceId
        else if (itemRequest.getServiceId() != null) {
            itemToDelete = existingItemsByServiceIdMap.get(itemRequest.getServiceId());
            if (itemToDelete == null) {
                throw new ClientSideException(ErrorCode.NOT_FOUND,
                        "Booking item not found with serviceId: " + itemRequest.getServiceId());
            }
        }
        else {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Cannot delete booking item: must provide either booking_item_id or service_id");
        }

        // Xóa hoàn toàn item
        log.info("Hard deleting booking item: {} (serviceId: {}) due to DELETE operation", 
                itemToDelete.getBookingItemId(), itemToDelete.getServiceId());
        bookingItemService.hardDeleteWithoutStatusCheck(itemToDelete.getBookingItemId());

        // Xóa khỏi map để tránh xử lý lại
        existingItemsByIdMap.remove(itemToDelete.getBookingItemId());
        existingItemsByServiceIdMap.remove(itemToDelete.getServiceId());
    }

    /**
     * Xử lý thêm mới hoặc cập nhật item
     */
    private void handleAddOrUpdateItem(CreateBookingItemRequest itemRequest, Booking existingBooking,
                                       Map<UUID, BookingItem> existingItemsByServiceIdMap) {
        UUID serviceId = itemRequest.getServiceId();
        if (serviceId == null) {
            log.warn("Skipping booking item with null serviceId: {}", itemRequest.getServiceName());
            return;
        }

        BookingItem existingItem = existingItemsByServiceIdMap.get(serviceId);

        if (existingItem != null) {
            // Item đã tồn tại - CẬP NHẬT
            updateExistingBookingItem(existingItem, itemRequest);
        } else {
            // Item mới - THÊM MỚI
            createNewBookingItem(existingBooking, itemRequest);
        }
    }

    /**
     * Cập nhật booking item đã tồn tại
     */
    private void updateExistingBookingItem(BookingItem existingItem, CreateBookingItemRequest newItemRequest) {
        log.info("Updating existing booking item: {} (serviceId: {})", 
                existingItem.getBookingItemId(), existingItem.getServiceId());

        // Cập nhật thông tin service nếu có
        if (newItemRequest.getServiceName() != null) {
            existingItem.setServiceName(newItemRequest.getServiceName());
        }
        if (newItemRequest.getServiceDescription() != null) {
            existingItem.setServiceDescription(newItemRequest.getServiceDescription());
        }

        bookingItemService.update(existingItem);

        log.info("Updated booking item: {}", existingItem.getBookingItemId());
    }

    /**
     * Tạo booking item mới
     */
    private void createNewBookingItem(Booking booking, CreateBookingItemRequest itemRequest) {
        log.info("Creating new booking item for service: {} (serviceId: {})", 
                itemRequest.getServiceName(), itemRequest.getServiceId());

        // Validate service tồn tại
        com.kltn.scsms_api_service.core.entity.Service serviceEntity = serviceService.getById(itemRequest.getServiceId());
        if (!serviceEntity.getIsActive()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Service is not active: " + itemRequest.getServiceName());
        }

        // Convert request to entity
        BookingItem bookingItem = bookingItemMapper.toEntity(itemRequest);
        bookingItem.setBooking(booking);

        // Validate serviceId
        if (itemRequest.getServiceId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "serviceId is required for booking item: " + itemRequest.getServiceName());
        }

        // Set default values
        bookingItem.setItemStatus(BookingItem.ItemStatus.PENDING);
        bookingItem.setIsActive(true);
        bookingItem.setIsDeleted(false);

        // Lấy duration từ Service entity
        if (serviceEntity.getEstimatedDuration() != null) {
            bookingItem.setDurationMinutes(serviceEntity.getEstimatedDuration());
        } else {
            bookingItem.setDurationMinutes(60); // Default 60 minutes
        }

        // Tính giá từ price book hiện tại
        BigDecimal unitPrice = calculateItemUnitPrice(itemRequest);
        bookingItem.setUnitPrice(unitPrice);
        log.info("Set unit price from price book for service {}: {}", itemRequest.getServiceName(), unitPrice);

        // Save booking item
        bookingItemService.save(bookingItem);
        log.info("Created new booking item: {}", bookingItem.getBookingItemId());
    }

    /**
     * Tính lại tổng giá và thời gian ước tính cho booking
     */
    private void recalculateBookingTotals(Booking booking) {
        log.info("Recalculating totals for booking: {}", booking.getBookingId());

        // Lấy danh sách items hiện tại (không bao gồm items đã bị xóa)
        List<BookingItem> activeItems = bookingItemService.findByBooking(booking.getBookingId());

        // Tính tổng giá (tổng unitPrice của tất cả items)
        BigDecimal totalPrice = activeItems.stream()
                .map(BookingItem::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        booking.setTotalPrice(totalPrice);

        // Tính tổng thời gian ước tính
        Integer totalDuration = activeItems.stream()
                .filter(item -> item.getDurationMinutes() != null)
                .mapToInt(BookingItem::getDurationMinutes)
                .sum();
        booking.setEstimatedDurationMinutes(totalDuration > 0 ? totalDuration : null);

        // Cập nhật scheduledEndAt nếu có scheduledStartAt
        if (booking.getScheduledStartAt() != null && totalDuration != null && totalDuration > 0) {
            booking.setScheduledEndAt(booking.getScheduledStartAt().plusMinutes(totalDuration));
            log.info("Updated scheduledEndAt to: {}", booking.getScheduledEndAt());
        }

        log.info("Recalculated booking totals - totalPrice: {}, estimatedDurationMinutes: {}", 
                totalPrice, totalDuration);
    }
    
    /**
     * Kiểm tra walk-in booking tăng thời gian có booking sau trong hàng đợi không
     * Nếu có booking sau, từ chối và yêu cầu user chọn bay khác
     */
    private void validateWalkInDurationIncrease(Booking booking, Integer originalDuration) {
        log.info("Validating walk-in booking duration increase: bookingId={}, originalDuration={}, newDuration={}", 
                booking.getBookingId(), originalDuration, booking.getEstimatedDurationMinutes());
        
        // Chỉ kiểm tra nếu là walk-in booking và có bay
        if (booking.getServiceBay() == null || booking.getScheduledStartAt() == null) {
            log.warn("Cannot validate: booking has no bay or scheduledStartAt");
            return;
        }
        
        UUID bayId = booking.getServiceBay().getBayId();
        LocalDate bookingDate = booking.getScheduledStartAt().toLocalDate();
        
        // Tính thời gian kết thúc mới
        LocalDateTime newScheduledEndAt = booking.getScheduledStartAt()
                .plusMinutes(booking.getEstimatedDurationMinutes());
        
        // Kiểm tra conflict với booking khác trong cùng bay
        boolean isBayAvailable = serviceBayService.isBayAvailableInTimeRange(
                bayId, booking.getScheduledStartAt(), newScheduledEndAt);
        
        if (!isBayAvailable) {
            List<Booking> conflictingBookings = bookingService.findConflictingBookings(
                    bayId, booking.getScheduledStartAt(), newScheduledEndAt);
            
            // Exclude current booking from conflict check
            conflictingBookings = conflictingBookings.stream()
                    .filter(b -> !b.getBookingId().equals(booking.getBookingId()))
                    .collect(Collectors.toList());
            
            if (!conflictingBookings.isEmpty()) {
                log.warn("Cannot increase duration: found {} conflicting bookings", conflictingBookings.size());
                throw new ClientSideException(
                        ErrorCode.WALK_IN_DURATION_INCREASE_REQUIRES_BAY_CHANGE,
                        "Tổng thời gian dịch vụ bạn chọn vượt quá thời gian có sẵn trong bay hiện tại do đã có booking khác. " +
                                "Vui lòng chọn bay khác hoặc giảm số lượng/bớt dịch vụ.");
            }
        }
        
        log.info("Duration increase validated: no conflicts found");
    }
    
    /**
     * Kiểm tra slot booking tăng thời gian có conflict với booking khác không
     * Nếu có conflict, từ chối và yêu cầu user chọn slot khác
     */
    private void validateSlotBookingDurationIncrease(Booking booking, Integer originalDuration) {
        log.info("Validating slot booking duration increase: bookingId={}, originalDuration={}, newDuration={}", 
                booking.getBookingId(), originalDuration, booking.getEstimatedDurationMinutes());
        
        // Chỉ kiểm tra nếu là slot booking và có bay, scheduledStartAt
        if (booking.getServiceBay() == null || booking.getScheduledStartAt() == null) {
            log.warn("Cannot validate: booking has no bay or scheduledStartAt");
            return;
        }
        
        UUID bayId = booking.getServiceBay().getBayId();
        LocalDateTime scheduledStartAt = booking.getScheduledStartAt();
        LocalDateTime newScheduledEndAt = scheduledStartAt.plusMinutes(booking.getEstimatedDurationMinutes());
        
        // Tính thời gian kết thúc ban đầu
        LocalDateTime originalScheduledEndAt = scheduledStartAt.plusMinutes(originalDuration);
        
        // Nếu thời gian mới vẫn nằm trong slot ban đầu, không cần kiểm tra
        if (newScheduledEndAt.isBefore(originalScheduledEndAt) || newScheduledEndAt.equals(originalScheduledEndAt)) {
            log.info("New duration still fits within original slot, no validation needed");
            return;
        }
        
        // Kiểm tra conflict với booking khác
        boolean isBayAvailable = serviceBayService.isBayAvailableInTimeRange(
                bayId, scheduledStartAt, newScheduledEndAt);
        
        if (!isBayAvailable) {
            List<Booking> conflictingBookings = bookingService.findConflictingBookings(
                    bayId, scheduledStartAt, newScheduledEndAt);
            
            // Exclude current booking from conflict check
            conflictingBookings = conflictingBookings.stream()
                    .filter(b -> !b.getBookingId().equals(booking.getBookingId()))
                    .collect(Collectors.toList());
            
            if (!conflictingBookings.isEmpty()) {
                log.warn("Cannot increase duration: found {} conflicting bookings", conflictingBookings.size());
                throw new ClientSideException(
                        ErrorCode.SLOT_BOOKING_DURATION_INCREASE_REQUIRES_SLOT_CHANGE,
                        "Tổng thời gian dịch vụ bạn chọn vượt quá thời gian slot hiện tại do đã có booking khác. " +
                                "Vui lòng chọn thời gian slot khác hoặc giảm số lượng/bớt dịch vụ.");
            }
        }
        
        log.info("Duration increase validated: no conflicts found");
    }
}
