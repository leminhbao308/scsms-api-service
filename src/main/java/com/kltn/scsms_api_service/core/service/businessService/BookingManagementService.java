package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.param.BookingFilterParam;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private final BookingMapper bookingMapper;
    private final BookingItemMapper bookingItemMapper;
    
    /**
     * Lấy tất cả booking
     */
    public List<BookingInfoDto> getAllBookings() {
        log.info("Getting all bookings");
        List<Booking> bookings = bookingService.findAll();
        return bookings.stream()
                .map(bookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
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
                filterParam.getDirection().equalsIgnoreCase("DESC") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filterParam.getSort()
        );
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);
        
        // TODO: Implement custom repository methods for filtering
        // For now, we'll fetch all and filter in memory or use JPA Criteria API
        Page<Booking> bookingPage = bookingService.findAll(pageable);
        
        return bookingPage.map(bookingMapper::toBookingInfoDto);
    }
    
    /**
     * Lấy booking theo ID
     */
    public BookingInfoDto getBookingById(UUID bookingId) {
        log.info("Getting booking by ID: {}", bookingId);
        Booking booking = bookingService.getById(bookingId);
        return bookingMapper.toBookingInfoDto(booking);
    }
    
    /**
     * Lấy booking theo mã booking
     */
    public BookingInfoDto getBookingByCode(String bookingCode) {
        log.info("Getting booking by code: {}", bookingCode);
        Booking booking = bookingService.getByBookingCode(bookingCode);
        return bookingMapper.toBookingInfoDto(booking);
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
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found with ID: " + request.getBranchId()));
        
        // Validate customer if provided
        User customer = null;
        if (request.getCustomerId() != null) {
            customer = userService.findById(request.getCustomerId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "User not found with ID: " + request.getCustomerId()));
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
        
        // Calculate total price from items
        if (request.getBookingItems() != null && !request.getBookingItems().isEmpty()) {
            BigDecimal totalPrice = request.getBookingItems().stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            booking.setTotalPrice(totalPrice);
        }
        
        // Calculate estimated duration
        if (request.getBookingItems() != null && !request.getBookingItems().isEmpty()) {
            Integer totalDuration = request.getBookingItems().stream()
                    .mapToInt(item -> item.getDurationMinutes())
                    .sum();
            booking.setEstimatedDurationMinutes(totalDuration);
        }
        
        // Set scheduled times
        if (request.getScheduledStartAt() != null) {
            booking.setScheduledStartAt(request.getScheduledStartAt());
            if (booking.getEstimatedDurationMinutes() != null) {
                booking.setScheduledEndAt(request.getScheduledStartAt()
                        .plusMinutes(booking.getEstimatedDurationMinutes() + booking.getBufferMinutes()));
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
                
                // Calculate total amount
                BigDecimal subtotal = itemRequest.getUnitPrice()
                        .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                BigDecimal totalAmount = subtotal
                        .subtract(itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount() : BigDecimal.ZERO)
                        .add(itemRequest.getTaxAmount() != null ? itemRequest.getTaxAmount() : BigDecimal.ZERO);
                bookingItem.setTotalAmount(totalAmount);
                
                // Save booking item
                bookingItemService.save(bookingItem);
            }
        }
        
        // Bay assignment is handled automatically through the relationship
        // No need to manually assign bay
        
        return bookingMapper.toBookingInfoDto(savedBooking);
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
        
        // Update booking
        Booking updatedBooking = bookingMapper.updateEntity(existingBooking, request);
        Booking savedBooking = bookingService.update(updatedBooking);
        
        return bookingMapper.toBookingInfoDto(savedBooking);
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
}
