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
    private final ServiceService serviceService;

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
                throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE,
                        "Service bay does not allow booking");
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
                // Không cộng buffer vào estimated duration
                booking.setScheduledEndAt(request.getScheduledStartAt()
                        .plusMinutes(booking.getEstimatedDurationMinutes()));
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

                // Calculate total amount (KHÔNG cộng tax, chỉ lấy giá dịch vụ trừ discount)
                BigDecimal subtotal = bookingItem.getUnitPrice(); // Services are always quantity 1
                BigDecimal totalAmount = subtotal
                        .subtract(itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount()
                                : BigDecimal.ZERO);
                // KHÔNG cộng tax vào total amount - chỉ lấy giá dịch vụ
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

        Booking existingBooking = bookingService.getByIdWithDetails(bookingId);

        // Check if booking can be updated
        if (existingBooking.isCompleted() || existingBooking.isCancelled()) {
            throw new ClientSideException(ErrorCode.BOOKING_CANNOT_BE_UPDATED,
                    "Cannot update completed or cancelled booking");
        }

        // Validate bay availability if time is being updated
        // Skip this validation if we have slot_date and slot_start_time, because handleSlotChange will validate slot availability
        boolean hasSlotInfo = request.getSlotDate() != null && request.getSlotStartTime() != null;
        if (!hasSlotInfo && (request.getScheduledStartAt() != null || request.getScheduledEndAt() != null)) {
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

        // Xử lý thay đổi slot nếu có (chỉ cho slot booking, không cho walk-in booking)
        if ((request.getServiceBayId() != null || request.getSlotDate() != null || request.getSlotStartTime() != null)
                && !existingBooking.getBookingCode().startsWith("WALK")) {
            handleSlotChange(updatedBooking, request);
        }

        // Xử lý cập nhật booking items (dịch vụ) nếu có
        if (request.getBookingItems() != null) {
            boolean itemsWereDeleted = updateBookingItems(bookingId, request.getBookingItems(), existingBooking);
            
            // QUAN TRỌNG: Nếu đã xóa items, cần reload lại updatedBooking
            // để tránh stale references đến items đã xóa trong collection
            if (itemsWereDeleted) {
                // Clear persistence context để đảm bảo không còn stale references
                bookingItemService.clearPersistenceContext();
                // Reload updatedBooking với collection mới nhất (không còn items đã xóa)
                updatedBooking = bookingService.getByIdWithDetails(bookingId);
                // Áp dụng lại tất cả các thay đổi từ request vào booking đã reload
                updatedBooking = bookingMapper.updateEntity(updatedBooking, request);
                
                // Cập nhật branch nếu có thay đổi
                if (request.getBranchId() != null) {
                    UUID newBranchId = request.getBranchId();
                    UUID currentBranchId = updatedBooking.getBranch() != null ? updatedBooking.getBranch().getBranchId() : null;
                    if (currentBranchId == null || !newBranchId.equals(currentBranchId)) {
                        Branch newBranch = branchService.findById(newBranchId)
                                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                                        "Branch not found with ID: " + newBranchId));
                        updatedBooking.setBranch(newBranch);
                    }
                }
                
                // Xử lý lại slot change nếu có (vì booking đã reload)
                if ((request.getServiceBayId() != null || request.getSlotDate() != null || request.getSlotStartTime() != null)
                        && !updatedBooking.getBookingCode().startsWith("WALK")) {
                    handleSlotChange(updatedBooking, request);
                }
                
                log.info("Reloaded updatedBooking after deleting items to avoid stale references");
            }
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

        Booking booking = bookingService.getByIdWithDetails(bookingId);

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

        Booking booking = bookingService.getByIdWithDetails(bookingId);

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

        Booking booking = bookingService.getByIdWithDetails(bookingId);

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

        Booking booking = bookingService.getByIdWithDetails(bookingId);

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

        Booking booking = bookingService.getByIdWithDetails(bookingId);

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

        Booking booking = bookingService.getByIdWithDetails(bookingId);

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
     * Thay đổi slot của booking
     */
    @Transactional
    public BookingInfoDto changeBookingSlot(UUID bookingId, ChangeSlotRequest request) {
        log.info("Changing slot for booking: {} to bay: {} at {} {}",
                bookingId, request.getNewBayId(), request.getNewSlotDate(), request.getNewSlotStartTime());

        // 1. Lấy booking hiện tại
        Booking existingBooking = bookingService.getByIdWithDetails(bookingId);

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
        
        // 4.1. Ensure branch matches the bay's branch
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

        // 5. Tính thời gian kết thúc dự kiến
        LocalDateTime newSlotStartDateTime = LocalDateTime.of(request.getNewSlotDate(), request.getNewSlotStartTime());
        // Không cộng buffer vào estimated duration
        LocalDateTime newSlotEndDateTime = newSlotStartDateTime.plusMinutes(
                request.getServiceDurationMinutes());

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

        // 1. Determine new slot values
        UUID newBayId = request.getServiceBayId() != null ? request.getServiceBayId()
                : booking.getServiceBay() != null ? booking.getServiceBay().getBayId() : null;
        
        if (newBayId == null) {
            log.warn("No bay ID provided for slot change, skipping");
            return;
        }
        
        LocalDate newSlotDate = request.getSlotDate() != null ? request.getSlotDate()
                : booking.getScheduledStartAt() != null ? booking.getScheduledStartAt().toLocalDate() : null;
        LocalTime newSlotStartTime = request.getSlotStartTime() != null ? request.getSlotStartTime()
                : booking.getSlotStartTime();

        if (newSlotDate == null || newSlotStartTime == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Slot date and start time are required for slot change");
        }

        // 2. Check if slot has actually changed
        boolean slotChanged = booking.getServiceBay() == null || 
                !newBayId.equals(booking.getServiceBay().getBayId()) ||
                (booking.getScheduledStartAt() != null && !newSlotDate.equals(booking.getScheduledStartAt().toLocalDate())) ||
                !newSlotStartTime.equals(booking.getSlotStartTime());

        // 3. Only validate availability if slot has changed
        // If slot hasn't changed, skip availability check since it's already booked by this booking
        if (slotChanged && !bayScheduleService.isSlotAvailable(newBayId, newSlotDate, newSlotStartTime)) {
            throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE,
                    "New slot is not available for booking");
        }

        // 2. Lấy service bay mới
        ServiceBay newServiceBay = serviceBayService.getById(newBayId);
        
        // 3. Ensure branch matches the bay's branch
        // If branch was changed in request, it should already be set
        // But we need to make sure the bay belongs to the current branch
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

        // 4. Tính thời gian kết thúc dự kiến
        LocalDateTime newSlotStartDateTime = LocalDateTime.of(newSlotDate, newSlotStartTime);
        int serviceDurationMinutes = request.getEstimatedDurationMinutes() != null
                ? request.getEstimatedDurationMinutes()
                : (booking.getEstimatedDurationMinutes() != null ? booking.getEstimatedDurationMinutes() : 60);
        // Không cộng buffer vào estimated duration
        LocalDateTime newSlotEndDateTime = newSlotStartDateTime.plusMinutes(serviceDurationMinutes);

        // 4. Update booking information
        if (slotChanged) {
            // Release old slots and book new ones
            bayScheduleService.releaseAllSlotsForBooking(booking.getBookingId());
            log.info("Released old slots for booking: {}", booking.getBookingId());

            // Update slot information in booking
            booking.setServiceBay(newServiceBay);
            booking.setSlotStartTime(newSlotStartTime);
            booking.setScheduledStartAt(newSlotStartDateTime);
            booking.setScheduledEndAt(newSlotEndDateTime);
            booking.setPreferredStartAt(newSlotStartDateTime);
            
            // Ensure branch matches the bay's branch (reuse the branch check from above)
            // Branch should already be set from step 3, but double-check here
            if (booking.getBranch() == null || !newBayBranchId.equals(booking.getBranch().getBranchId())) {
                Branch bayBranch = branchService.findById(newBayBranchId)
                        .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                                "Branch not found for service bay: " + newBayBranchId));
                booking.setBranch(bayBranch);
            }

            // Book new slot
            bayScheduleService.bookSlot(newBayId, newSlotDate, newSlotStartTime, booking.getBookingId());

            // Block additional slots if needed
            // Only calculate based on service duration, don't add bufferMinutes
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
        } else {
            // Slot hasn't changed, but we may need to update scheduled_end_at if service duration changed
            booking.setScheduledEndAt(newSlotEndDateTime);
            
            // If service duration changed, we may need to update blocked slots
            // Calculate how many slots are needed with new duration
            if (booking.getServiceBay() != null && booking.getSlotStartTime() != null) {
                int slotsNeeded = (int) Math.ceil((double) serviceDurationMinutes / newServiceBay.getSlotDurationMinutes());
                
                // Get currently blocked slots for this booking (excluding the main slot which is booked)
                // We need to release old blocked slots and add new ones if duration increased
                // For now, we'll release all slots and re-book them to ensure consistency
                // This is safe because the slot hasn't changed, so we know it's still available
                bayScheduleService.releaseAllSlotsForBooking(booking.getBookingId());
                
                // Re-book the main slot (slot hasn't changed, so we know it's available)
                bayScheduleService.bookSlot(newBayId, newSlotDate, newSlotStartTime, booking.getBookingId());
                
                // Block additional slots if needed based on new service duration
                if (slotsNeeded > 1) {
                    for (int i = 1; i < slotsNeeded; i++) {
                        bayScheduleService.blockSlot(
                                newBayId,
                                newSlotDate,
                                newSlotStartTime.plusMinutes(i * newServiceBay.getSlotDurationMinutes()),
                                booking.getBookingId());
                    }
                }
            }
            
            log.info("Updated service duration for booking: {} without changing slot", booking.getBookingId());
        }
    }

    /**
     * Xử lý cập nhật booking items (dịch vụ)
     * Hỗ trợ operation DELETE để xóa items một cách explicit
     * @return true nếu đã xóa items (cần reload Booking entity)
     */
    @Transactional
    private boolean updateBookingItems(UUID bookingId, List<CreateBookingItemRequest> newBookingItems, Booking existingBooking) {
        log.info("Updating booking items for booking: {}", bookingId);

        // 1. Lấy danh sách booking items hiện tại
        List<BookingItem> existingItems = bookingItemService.findByBooking(bookingId);
        log.info("Current booking has {} items before update", existingItems.size());
        
        // Kiểm tra duplicate service_id (không nên xảy ra)
        Map<UUID, Long> serviceIdCounts = existingItems.stream()
                .collect(Collectors.groupingBy(BookingItem::getItemId, Collectors.counting()));
        serviceIdCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> log.warn("WARNING: Multiple items with same serviceId {} found: {} items", 
                        entry.getKey(), entry.getValue()));
        
        // 2. Tạo map để tìm nhanh: bookingItemId -> BookingItem và serviceId -> BookingItem
        // Lưu ý: Nếu có nhiều items cùng serviceId, chỉ giữ item đầu tiên (không nên xảy ra trong thực tế)
        Map<UUID, BookingItem> existingItemsByIdMap = existingItems.stream()
                .collect(Collectors.toMap(
                        BookingItem::getBookingItemId, 
                        item -> item,
                        (existing, replacement) -> existing // Giữ item đầu tiên nếu trùng
                ));
        Map<UUID, BookingItem> existingItemsByServiceIdMap = existingItems.stream()
                .collect(Collectors.toMap(
                        BookingItem::getItemId, 
                        item -> item,
                        (existing, replacement) -> {
                            log.warn("Duplicate serviceId found, keeping first item: {}", existing.getBookingItemId());
                            return existing; // Giữ item đầu tiên nếu trùng
                        }
                ));

        // 3. Xử lý các items có operation DELETE trước
        // QUAN TRỌNG: Xóa items khỏi Booking collection TRƯỚC khi xóa bằng native query
        // để tránh lỗi detached entity khi flush
        int deletedCount = 0;
        List<UUID> deletedItemIds = new java.util.ArrayList<>();
        for (CreateBookingItemRequest itemRequest : newBookingItems) {
            if (itemRequest.getOperation() == CreateBookingItemRequest.ItemOperation.DELETE) {
                UUID deletedId = findItemIdToDelete(itemRequest, existingItemsByIdMap, existingItemsByServiceIdMap);
                if (deletedId != null) {
                    deletedItemIds.add(deletedId);
                    deletedCount++;
                }
            }
        }
        
        // Xóa items bằng native query TRƯỚC
        boolean itemsWereDeleted = false;
        if (deletedCount > 0) {
            log.info("Deleting {} items from database using native query", deletedCount);
            for (UUID deletedId : deletedItemIds) {
                bookingItemService.hardDeleteWithoutStatusCheck(deletedId);
            }
            itemsWereDeleted = true;
            log.info("Deleted {} items from database", deletedCount);
        }

        // 4. Xử lý các items cần thêm hoặc cập nhật (không có operation DELETE)
        for (CreateBookingItemRequest itemRequest : newBookingItems) {
            if (itemRequest.getOperation() != CreateBookingItemRequest.ItemOperation.DELETE) {
                BookingItem processedItem = handleAddOrUpdateItem(itemRequest, existingBooking, existingItemsByServiceIdMap);
                // Cập nhật map sau khi thêm item mới để tránh duplicate
                if (processedItem != null && processedItem.getBookingItemId() != null) {
                    existingItemsByServiceIdMap.put(processedItem.getItemId(), processedItem);
                }
            }
        }

        // 5. Tính lại tổng giá và thời gian ước tính
        // Sử dụng findByBookingWithClear để đảm bảo lấy dữ liệu mới nhất từ DB
        recalculateBookingTotals(existingBooking);
        
        // 6. Log số lượng items sau khi xử lý để debug
        List<BookingItem> finalItems = bookingItemService.findByBookingWithClear(bookingId);
        log.info("Booking has {} items after update", finalItems.size());
        finalItems.forEach(item -> log.debug("Item: {} - serviceId: {}", 
                item.getBookingItemId(), item.getItemId()));
        
        // Trả về true nếu đã xóa items (cần reload Booking entity)
        return itemsWereDeleted;
    }

    /**
     * Tìm ID của item cần xóa (không thực hiện xóa ở đây)
     * @return UUID của item cần xóa (null nếu không tìm thấy)
     */
    private UUID findItemIdToDelete(CreateBookingItemRequest itemRequest,
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

        UUID deletedItemId = itemToDelete.getBookingItemId();
        log.info("Found booking item to delete: {} (serviceId: {})", 
                deletedItemId, itemToDelete.getItemId());

        // Xóa khỏi map để tránh xử lý lại
        existingItemsByIdMap.remove(deletedItemId);
        existingItemsByServiceIdMap.remove(itemToDelete.getItemId());

        return deletedItemId;
    }

    /**
     * Xử lý thêm mới hoặc cập nhật item
     * @return BookingItem đã được xử lý (null nếu bỏ qua)
     */
    private BookingItem handleAddOrUpdateItem(CreateBookingItemRequest itemRequest, Booking existingBooking,
                                       Map<UUID, BookingItem> existingItemsByServiceIdMap) {
        UUID serviceId = itemRequest.getServiceId();
        if (serviceId == null) {
            log.warn("Skipping booking item with null serviceId: {}", itemRequest.getItemName());
            return null;
        }

        BookingItem existingItem = existingItemsByServiceIdMap.get(serviceId);

        if (existingItem != null) {
            // Item đã tồn tại - CẬP NHẬT
            updateExistingBookingItem(existingItem, itemRequest);
            return existingItem;
        } else {
            // Item mới - THÊM MỚI
            return createNewBookingItem(existingBooking, itemRequest);
        }
    }

    /**
     * Cập nhật booking item đã tồn tại
     */
    private void updateExistingBookingItem(BookingItem existingItem, CreateBookingItemRequest newItemRequest) {
        log.info("Updating existing booking item: {} (serviceId: {})", 
                existingItem.getBookingItemId(), existingItem.getItemId());

        // Chỉ cập nhật discount và tax, giữ nguyên unitPrice (snapshot)
        if (newItemRequest.getDiscountAmount() != null) {
            existingItem.setDiscountAmount(newItemRequest.getDiscountAmount());
        }
        if (newItemRequest.getTaxAmount() != null) {
            existingItem.setTaxAmount(newItemRequest.getTaxAmount());
        }

        // Cập nhật thông tin mô tả nếu có
        if (newItemRequest.getItemName() != null) {
            existingItem.setItemName(newItemRequest.getItemName());
        }
        if (newItemRequest.getItemDescription() != null) {
            existingItem.setItemDescription(newItemRequest.getItemDescription());
        }

        // Tính lại total amount (KHÔNG cộng tax, chỉ lấy giá dịch vụ trừ discount)
        BigDecimal subtotal = existingItem.getUnitPrice();
        BigDecimal newTotalAmount = subtotal
                .subtract(existingItem.getDiscountAmount() != null ? existingItem.getDiscountAmount() : BigDecimal.ZERO);
        // KHÔNG cộng tax vào total amount - chỉ lấy giá dịch vụ
        existingItem.setTotalAmount(newTotalAmount);
        bookingItemService.update(existingItem);

        log.info("Updated booking item: {}, new totalAmount: {}", 
                existingItem.getBookingItemId(), existingItem.getTotalAmount());
    }

    /**
     * Tạo booking item mới
     * @return BookingItem đã được tạo
     */
    private BookingItem createNewBookingItem(Booking booking, CreateBookingItemRequest itemRequest) {
        log.info("Creating new booking item for service: {} (serviceId: {})", 
                itemRequest.getItemName(), itemRequest.getServiceId());

        // Validate service tồn tại
        com.kltn.scsms_api_service.core.entity.Service serviceEntity = serviceService.getById(itemRequest.getServiceId());
        if (!serviceEntity.getIsActive()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Service is not active: " + itemRequest.getItemName());
        }

        // Convert request to entity
        BookingItem bookingItem = bookingItemMapper.toEntity(itemRequest);
        bookingItem.setBooking(booking);

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
        log.info("Set unit price from price book for item {}: {}", itemRequest.getItemName(), unitPrice);

        // Tính total amount (KHÔNG cộng tax, chỉ lấy giá dịch vụ trừ discount)
        BigDecimal subtotal = bookingItem.getUnitPrice(); // Services are always quantity 1
        BigDecimal totalAmount = subtotal
                .subtract(itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount() : BigDecimal.ZERO);
        // KHÔNG cộng tax vào total amount - chỉ lấy giá dịch vụ
        bookingItem.setTotalAmount(totalAmount);

        // Save booking item
        BookingItem savedItem = bookingItemService.save(bookingItem);
        log.info("Created new booking item: {} with totalAmount: {}", 
                savedItem.getBookingItemId(), savedItem.getTotalAmount());
        return savedItem;
    }

    /**
     * Tính lại tổng giá và thời gian ước tính cho booking
     */
    private void recalculateBookingTotals(Booking booking) {
        log.info("Recalculating totals for booking: {}", booking.getBookingId());
        
        // Lấy danh sách items hiện tại (không bao gồm items đã bị xóa)
        // Sử dụng findByBookingWithClear để clear persistence context và lấy dữ liệu mới nhất từ DB
        List<BookingItem> activeItems = bookingItemService.findByBookingWithClear(booking.getBookingId());

        // Tính tổng giá
        BigDecimal totalPrice = activeItems.stream()
                .map(BookingItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        booking.setTotalPrice(totalPrice);

        // Tính tổng thời gian ước tính
        Integer totalDuration = activeItems.stream()
                .filter(item -> item.getDurationMinutes() != null)
                .mapToInt(BookingItem::getDurationMinutes)
                .sum();
        booking.setEstimatedDurationMinutes(totalDuration > 0 ? totalDuration : null);

        // Cập nhật scheduledEndAt nếu là booking đặt trước và có scheduledStartAt
        if (!booking.getBookingCode().startsWith("WALK") 
                && booking.getScheduledStartAt() != null 
                && totalDuration != null && totalDuration > 0) {
            booking.setScheduledEndAt(booking.getScheduledStartAt().plusMinutes(totalDuration));
            log.info("Updated scheduledEndAt to: {}", booking.getScheduledEndAt());
        }

        log.info("Recalculated booking totals - totalPrice: {}, estimatedDurationMinutes: {}", 
                totalPrice, totalDuration);
    }
}
