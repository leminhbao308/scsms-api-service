package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.param.BookingFilterParam;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.ChangeSlotRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.UpdateBookingRequest;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.service.entityService.*;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private final UserService userService;
    private final VehicleProfileService vehicleProfileService;
    private final ServiceBayService serviceBayService;
    private final BayScheduleService bayScheduleService;
    private final BookingMapper bookingMapper;
    private final BookingItemMapper bookingItemMapper;
    private final BookingInfoService bookingInfoService;
    private final BookingPricingService bookingPricingService;
    private final PricingBusinessService pricingBusinessService;

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
     */
    public BookingInfoDto getBookingById(UUID bookingId) {
        log.info("Getting booking by ID: {}", bookingId);
        Booking booking = bookingService.getById(bookingId);
        return bookingInfoService.toBookingInfoDto(booking);
    }

    /**
     * Lấy booking theo mã booking
     */
    public BookingInfoDto getBookingByCode(String bookingCode) {
        log.info("Getting booking by code: {}", bookingCode);
        Booking booking = bookingService.getByBookingCode(bookingCode);
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
     * Tạo booking mới
     */
    @Transactional
    public BookingInfoDto createBooking(CreateBookingRequest request) {
        log.info("Creating booking for customer: {} at branch: {}", request.getCustomerName(), request.getBranchId());

        // Validate branch
        Branch branch = branchService.findById(request.getBranchId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                        "Branch not found with ID: " + request.getBranchId()));

        // Validate customer if provided
        User customer = null;
        if (request.getCustomerId() != null) {
            customer = userService.findById(request.getCustomerId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                            "User not found with ID: " + request.getCustomerId()));
        }

        // Validate vehicle if provided
        VehicleProfile vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleProfileService.getVehicleProfileById(request.getVehicleId());
        }

        // Validate bay if provided
        ServiceBay serviceBay = null;
        if (request.getBayId() != null) {
            serviceBay = serviceBayService.getById(request.getBayId());
            if (!serviceBay.isActive()) {
                throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE, "Service bay is not available");
            }
            if (!serviceBay.isAvailableForBooking()) {
                throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE, "Service bay does not allow booking");
            }

            // Check bay availability for the scheduled time
            if (request.getScheduledStartAt() != null && request.getScheduledEndAt() != null) {
                boolean isBayAvailable = serviceBayService.isBayAvailableInTimeRange(
                        request.getBayId(),
                        request.getScheduledStartAt(),
                        request.getScheduledEndAt());

                if (!isBayAvailable) {
                    // Find conflicting booking for better error message
                    List<Booking> conflictingBookings = bookingService.findConflictingBookings(
                            request.getBayId(),
                            request.getScheduledStartAt(),
                            request.getScheduledEndAt());

                    if (!conflictingBookings.isEmpty()) {
                        Booking conflictBooking = conflictingBookings.get(0);
                        throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE,
                                String.format("Service bay '%s' is not available in the specified time range. " +
                                        "Conflicts with booking '%s' (%s - %s)",
                                        serviceBay.getBayName(),
                                        conflictBooking.getBookingCode(),
                                        conflictBooking.getScheduledStartAt(),
                                        conflictBooking.getScheduledEndAt()));
                    } else {
                        throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE,
                                "Service bay is not available in the specified time range");
                    }
                }
            }
        }

        // Generate booking code
        String bookingCode = generateBookingCode();

        // Create booking entity
        Booking booking = bookingMapper.toEntity(request);
        booking.setBookingCode(bookingCode);
        booking.setBranch(branch);
        booking.setCustomer(customer);
        booking.setVehicle(vehicle);
        booking.setServiceBay(serviceBay);
        booking.setIsActive(true);

        // Calculate total price from price book
        if (request.getBookingItems() != null && !request.getBookingItems().isEmpty()) {
            BigDecimal totalPrice = bookingPricingService.calculateBookingTotalPrice(
                    request.getBookingItems(),
                    null // Sử dụng active price book
            );
            booking.setTotalPrice(totalPrice);
            log.info("Calculated booking total price from price book: {}", totalPrice);
        }

        // Calculate estimated duration from services
        if (request.getBookingItems() != null && !request.getBookingItems().isEmpty()) {
            // For now, use a default duration per service since duration is not in request
            // In a real implementation, you would fetch service duration from Service
            // entity
            Integer totalDuration = request.getBookingItems().size() * 60; // Default 60 minutes per service
            booking.setEstimatedDurationMinutes(totalDuration);
        }

        // Set scheduled times
        if (request.getScheduledStartAt() != null) {
            booking.setScheduledStartAt(request.getScheduledStartAt());
            if (booking.getEstimatedDurationMinutes() != null) {
                Integer bufferMinutes = booking.getBufferMinutes() != null ? booking.getBufferMinutes() : 15;
                booking.setScheduledEndAt(request.getScheduledStartAt()
                        .plusMinutes(booking.getEstimatedDurationMinutes() + bufferMinutes));
            }
        }

        // Save booking
        Booking savedBooking = bookingService.save(booking);

        // Create booking items
        if (request.getBookingItems() != null && !request.getBookingItems().isEmpty()) {
            log.info("Creating {} booking items", request.getBookingItems().size());

            for (CreateBookingItemRequest itemRequest : request.getBookingItems()) {
                // Convert request to entity
                BookingItem bookingItem = bookingItemMapper.toEntity(itemRequest);

                // Set booking reference
                bookingItem.setBooking(savedBooking);

                // Set default values
                bookingItem.setItemStatus(BookingItem.ItemStatus.PENDING);
                bookingItem.setIsActive(true);
                bookingItem.setIsDeleted(false);

                // Always calculate unit price from price book
                BigDecimal unitPrice = calculateItemUnitPrice(itemRequest);
                bookingItem.setUnitPrice(unitPrice);
                log.info("Set unit price from price book for item {}: {}", itemRequest.getItemName(), unitPrice);

                // Calculate total amount (services are always quantity 1)
                BigDecimal subtotal = bookingItem.getUnitPrice(); // Services are always quantity 1
                BigDecimal totalAmount = subtotal
                        .subtract(itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount()
                                : BigDecimal.ZERO)
                        .add(itemRequest.getTaxAmount() != null ? itemRequest.getTaxAmount() : BigDecimal.ZERO);
                bookingItem.setTotalAmount(totalAmount);

                // Save booking item
                bookingItemService.save(bookingItem);
            }
        }

        // Bay assignment is handled automatically through the relationship
        // No need to manually assign bay

        return bookingInfoService.toBookingInfoDto(savedBooking);
    }

    /**
     * Cập nhật booking
     */
    @Transactional
    public BookingInfoDto updateBooking(UUID bookingId, UpdateBookingRequest request) {
        log.info("Updating booking: {}", bookingId);

        Booking existingBooking = bookingService.getById(bookingId);

        // Check if booking can be updated
        if (existingBooking.isCompleted() || existingBooking.isCancelled()) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_UPDATED,
                    "Cannot update completed or cancelled booking");
        }

        // Validate bay availability if time is being updated
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

        // Xử lý thay đổi slot nếu có (chỉ cho slot booking, không cho walk-in booking)
        if ((request.getServiceBayId() != null || request.getSlotDate() != null || request.getSlotStartTime() != null) 
            && !existingBooking.getBookingCode().startsWith("WALK")) {
            handleSlotChange(updatedBooking, request);
        }

        Booking savedBooking = bookingService.update(updatedBooking);

        return bookingInfoService.toBookingInfoDto(savedBooking);
    }

    /**
     * Xóa booking (soft delete)
     */
    @Transactional
    public void deleteBooking(UUID bookingId) {
        log.info("Deleting booking: {}", bookingId);

        Booking booking = bookingService.getById(bookingId);

        // Check if booking can be deleted
        if (booking.isActive()) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_DELETED,
                    "Cannot delete active booking");
        }

        // Bay will be automatically available when booking is deleted
        // No need to manually unassign bay

        bookingService.delete(bookingId);
    }

    /**
     * Hủy booking
     */
    @Transactional
    public void cancelBooking(UUID bookingId, String reason, String cancelledBy) {
        log.info("Cancelling booking: {} with reason: {}", bookingId, reason);

        Booking booking = bookingService.getById(bookingId);

        // Check if booking can be cancelled
        if (booking.isCompleted() || booking.isCancelled()) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_CANCELLED,
                    "Cannot cancel completed or already cancelled booking");
        }

        // Cancel booking
        booking.cancelBooking(reason, cancelledBy);
        bookingService.update(booking);

        // Bay will be automatically available when booking is deleted
        // No need to manually unassign bay
    }

    /**
     * Confirm booking
     */
    @Transactional
    public void confirmBooking(UUID bookingId) {
        log.info("Confirming booking: {}", bookingId);

        Booking booking = bookingService.getById(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_CONFIRMED,
                    "Only pending bookings can be confirmed");
        }

        booking.updateStatus(Booking.BookingStatus.CONFIRMED, "Booking confirmed by staff");
        bookingService.update(booking);
    }

    /**
     * Check-in booking
     */
    @Transactional
    public void checkInBooking(UUID bookingId) {
        log.info("Checking in booking: {}", bookingId);

        Booking booking = bookingService.getById(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_CHECKED_IN,
                    "Only confirmed bookings can be checked in");
        }

        booking.checkIn();
        bookingService.update(booking);
    }

    /**
     * Bắt đầu dịch vụ
     */
    @Transactional
    public void startService(UUID bookingId) {
        log.info("Starting service for booking: {}", bookingId);

        Booking booking = bookingService.getById(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_STARTED,
                    "Only checked-in bookings can be started");
        }

        booking.startService();
        bookingService.update(booking);
    }

    /**
     * Hoàn thành dịch vụ
     */
    @Transactional
    public void completeService(UUID bookingId) {
        log.info("Completing service for booking: {}", bookingId);

        Booking booking = bookingService.getById(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.IN_PROGRESS) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_COMPLETED,
                    "Only in-progress bookings can be completed");
        }

        booking.completeService();
        bookingService.update(booking);

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
     * Tạo mã booking
     */
    private String generateBookingCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        return String.format("BK-%s-%s", dateStr, timeStr);
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
            throw new RuntimeException("Could not determine unit price for service: " + itemRequest.getItemName());
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
        Booking booking = bookingService.getById(bookingId);

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
     * Thay đổi slot của booking
     */
    @Transactional
    public BookingInfoDto changeBookingSlot(UUID bookingId, ChangeSlotRequest request) {
        log.info("Changing slot for booking: {} to bay: {} at {} {}",
                bookingId, request.getNewBayId(), request.getNewSlotDate(), request.getNewSlotStartTime());

        // 1. Lấy booking hiện tại
        Booking existingBooking = bookingService.getById(bookingId);

        // 2. Kiểm tra booking có thể thay đổi slot không
        if (existingBooking.isCompleted() || existingBooking.isCancelled()) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_UPDATED,
                    "Cannot change slot for completed or cancelled booking");
        }

        if (existingBooking.getStatus() == Booking.BookingStatus.IN_PROGRESS) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_UPDATED,
                    "Cannot change slot for booking that is in progress");
        }

        // 3. Validate slot mới
        if (!bayScheduleService.isSlotAvailable(request.getNewBayId(), request.getNewSlotDate(),
                request.getNewSlotStartTime())) {
            throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE,
                    "New slot is not available for booking");
        }

        // 4. Lấy service bay mới
        ServiceBay newServiceBay = serviceBayService.getById(request.getNewBayId());

        // 5. Tính thời gian kết thúc dự kiến
        LocalDateTime newSlotStartDateTime = LocalDateTime.of(request.getNewSlotDate(), request.getNewSlotStartTime());
        LocalDateTime newSlotEndDateTime = newSlotStartDateTime.plusMinutes(
                request.getServiceDurationMinutes() + newServiceBay.getBufferMinutes());

        // 6. Giải phóng slot cũ
        if (existingBooking.getServiceBay() != null && existingBooking.getSlotStartTime() != null) {
            bayScheduleService.releaseAllSlotsForBooking(bookingId);
            log.info("Released old slots for booking: {}", bookingId);
        }

        // 7. Cập nhật thông tin booking
        existingBooking.setServiceBay(newServiceBay);
        existingBooking.setSlotStartTime(request.getNewSlotStartTime());
        existingBooking.setScheduledStartAt(newSlotStartDateTime);
        existingBooking.setScheduledEndAt(newSlotEndDateTime);
        existingBooking.setPreferredStartAt(newSlotStartDateTime);

        // 8. Đặt slot mới
        bayScheduleService.bookSlot(
                request.getNewBayId(),
                request.getNewSlotDate(),
                request.getNewSlotStartTime(),
                bookingId);

        // 9. Block các slot phụ nếu cần
        // Chỉ tính dựa trên service duration, không cộng bufferMinutes
        int slotsNeeded = (int) Math
                .ceil((double) request.getServiceDurationMinutes() / newServiceBay.getSlotDurationMinutes());

        if (slotsNeeded > 1) {
            for (int i = 1; i < slotsNeeded; i++) {
                bayScheduleService.blockSlot(
                        request.getNewBayId(),
                        request.getNewSlotDate(),
                        request.getNewSlotStartTime().plusMinutes(i * newServiceBay.getSlotDurationMinutes()),
                        bookingId);
            }
        }

        // 10. Lưu booking
        Booking savedBooking = bookingService.update(existingBooking);

        log.info("Successfully changed slot for booking: {} to bay: {} at {} {}",
                bookingId, request.getNewBayId(), request.getNewSlotDate(), request.getNewSlotStartTime());

        return bookingInfoService.toBookingInfoDto(savedBooking);
    }

    /**
     * Xử lý thay đổi slot trong update booking
     */
    private void handleSlotChange(Booking booking, UpdateBookingRequest request) {
        log.info("Handling slot change for booking: {}", booking.getBookingId());

        // 1. Validate slot mới
        UUID newBayId = request.getServiceBayId() != null ? request.getServiceBayId()
                : booking.getServiceBay().getBayId();
        LocalDate newSlotDate = request.getSlotDate() != null ? request.getSlotDate()
                : booking.getScheduledStartAt().toLocalDate();
        LocalTime newSlotStartTime = request.getSlotStartTime() != null ? request.getSlotStartTime()
                : booking.getSlotStartTime();

        if (!bayScheduleService.isSlotAvailable(newBayId, newSlotDate, newSlotStartTime)) {
            throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE,
                    "New slot is not available for booking");
        }

        // 2. Lấy service bay mới
        ServiceBay newServiceBay = serviceBayService.getById(newBayId);

        // 3. Tính thời gian kết thúc dự kiến
        LocalDateTime newSlotStartDateTime = LocalDateTime.of(newSlotDate, newSlotStartTime);
        int serviceDurationMinutes = request.getEstimatedDurationMinutes() != null
                ? request.getEstimatedDurationMinutes()
                : booking.getTotalEstimatedDuration();
        LocalDateTime newSlotEndDateTime = newSlotStartDateTime.plusMinutes(
                serviceDurationMinutes + newServiceBay.getBufferMinutes());

        // 4. Giải phóng slot cũ nếu có thay đổi
        boolean slotChanged = !newBayId.equals(booking.getServiceBay().getBayId()) ||
                !newSlotDate.equals(booking.getScheduledStartAt().toLocalDate()) ||
                !newSlotStartTime.equals(booking.getSlotStartTime());

        if (slotChanged) {
            bayScheduleService.releaseAllSlotsForBooking(booking.getBookingId());
            log.info("Released old slots for booking: {}", booking.getBookingId());

            // 5. Cập nhật thông tin slot trong booking
            booking.setServiceBay(newServiceBay);
            booking.setSlotStartTime(newSlotStartTime);
            booking.setScheduledStartAt(newSlotStartDateTime);
            booking.setScheduledEndAt(newSlotEndDateTime);
            booking.setPreferredStartAt(newSlotStartDateTime);

            // 6. Đặt slot mới
            bayScheduleService.bookSlot(newBayId, newSlotDate, newSlotStartTime, booking.getBookingId());

            // 7. Block các slot phụ nếu cần
            // Chỉ tính dựa trên service duration, không cộng bufferMinutes
            int slotsNeeded = (int) Math.ceil((double) serviceDurationMinutes / newServiceBay.getSlotDurationMinutes());

            if (slotsNeeded > 1) {
                for (int i = 1; i < slotsNeeded; i++) {
                    bayScheduleService.blockSlot(
                            newBayId,
                            newSlotDate,
                            newSlotStartTime.plusMinutes(i * newServiceBay.getSlotDurationMinutes()),
                            booking.getBookingId());
                }
            }

            log.info("Successfully changed slot for booking: {} to bay: {} at {} {}",
                    booking.getBookingId(), newBayId, newSlotDate, newSlotStartTime);
        }
    }
}
