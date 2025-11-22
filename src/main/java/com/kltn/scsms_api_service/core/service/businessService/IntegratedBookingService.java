package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingWithScheduleRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingItemRequest;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IntegratedBookingService {
    
    private final BookingService bookingService;
    private final ServiceBayService serviceBayService;
    private final BranchService branchService;
    private final UserService userService;
    private final VehicleProfileService vehicleProfileService;
    private final BookingItemService bookingItemService;
    private final BookingInfoService bookingInfoService;
    private final PricingBusinessService pricingBusinessService;
    private final ServiceService serviceService;
    private final com.kltn.scsms_api_service.core.service.websocket.WebSocketService webSocketService;
    
    /**
     * Tạo booking hoàn chỉnh với scheduling information trong một API call
     * Sử dụng pessimistic locking để ngăn race condition conflict booking
     */
    public BookingInfoDto createBookingWithSlot(CreateBookingWithScheduleRequest request) {
        log.info("Creating integrated scheduled booking for customer: {} at branch: {} with schedule: {}",
            request.getCustomerName(), request.getBranchId(), request.getSelectedSchedule());
        
        try {
            // 1. Validate tất cả thông tin (không bao gồm conflict check)
            validateRequestWithoutConflictCheck(request);
            
            // 2. Tính scheduled times
            LocalDateTime scheduledStartAt = LocalDateTime.of(
                request.getSelectedSchedule().getDate(), 
                request.getSelectedSchedule().getStartTime()
            );
            LocalDateTime scheduledEndAt = scheduledStartAt.plusMinutes(
                request.getSelectedSchedule().getServiceDurationMinutes()
            );
            
            // 3. Check conflict với PESSIMISTIC LOCK (atomic operation - ngăn race condition)
            List<Booking> conflicts = bookingService.findConflictingBookingsWithLock(
                request.getSelectedSchedule().getBayId(),
                scheduledStartAt,
                scheduledEndAt
            );
            
            if (!conflicts.isEmpty()) {
                Booking conflictBooking = conflicts.get(0);
                ServiceBay serviceBay = serviceBayService.getById(request.getSelectedSchedule().getBayId());
                
                // Tạo error data với thông tin chi tiết
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("conflictingBooking", Map.of(
                    "bookingCode", conflictBooking.getBookingCode(),
                    "scheduledStartAt", conflictBooking.getScheduledStartAt().toString(),
                    "scheduledEndAt", conflictBooking.getScheduledEndAt().toString(),
                    "customerName", conflictBooking.getCustomerName() != null ? conflictBooking.getCustomerName() : "N/A",
                    "status", conflictBooking.getStatus().name()
                ));
                errorData.put("requestedTime", Map.of(
                    "startAt", scheduledStartAt.toString(),
                    "endAt", scheduledEndAt.toString()
                ));
                
                throw new ClientSideException(
                    ErrorCode.SERVICE_BAY_NOT_AVAILABLE,
                    String.format(
                        "Service bay '%s' is not available in the specified time range. " +
                        "Conflicts with booking '%s' (%s - %s). Please select a different time slot.",
                        serviceBay.getBayName(),
                        conflictBooking.getBookingCode(),
                        conflictBooking.getScheduledStartAt(),
                        conflictBooking.getScheduledEndAt()
                    ),
                    errorData
                );
            }
            
            // 4. Tạo booking entity (đã được lock, không có race condition)
            Booking booking = createBookingEntity(request);
            
            // 5. Lưu booking
            Booking savedBooking = bookingService.save(booking);
            
            // 6. Tạo booking items
            if (request.getBookingItems() != null && !request.getBookingItems().isEmpty()) {
                createBookingItems(savedBooking, request);
            }
            
            log.info("Successfully created integrated scheduled booking: {} with schedule: {}",
                savedBooking.getBookingId(), request.getSelectedSchedule());
            
            // 7. Gửi WebSocket notification với structured event
            webSocketService.notifyBookingCreated(savedBooking);
            
            return bookingInfoService.toBookingInfoDto(savedBooking);
            
        } catch (DataIntegrityViolationException e) {
            // Database constraint violation (backup protection)
            log.error("Database constraint violation when creating booking: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("idx_booking_bay_time_unique")) {
                throw new ClientSideException(
                    ErrorCode.SERVICE_BAY_NOT_AVAILABLE,
                    "Service bay is not available. Another booking was created for this time slot. Please try again with a different time."
                );
            }
            throw e;
        }
    }
    
    /**
     * Validate request without conflict check (conflict check sẽ được thực hiện với lock)
     */
    private void validateRequestWithoutConflictCheck(CreateBookingWithScheduleRequest request) {
        // Validate required fields
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Customer name is required");
        }
        
        if (request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Customer phone is required");
        }
        
        if (request.getBranchId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Branch ID is required");
        }
        
        if (request.getSelectedSchedule() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Scheduling information is required");
        }
        
        if (request.getSelectedSchedule().getBayId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Bay ID is required");
        }
        
        if (request.getSelectedSchedule().getDate() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Schedule date is required");
        }
        
        if (request.getSelectedSchedule().getStartTime() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Schedule start time is required");
        }
        
        if (request.getSelectedSchedule().getServiceDurationMinutes() == null || 
            request.getSelectedSchedule().getServiceDurationMinutes() <= 0) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Service duration is required and must be positive");
        }
        
        // Validate entities exist
        validateEntities(request);
        
        // Validate bay allows booking (không check conflict ở đây)
        ServiceBay bay = serviceBayService.getById(request.getSelectedSchedule().getBayId());
        if (!bay.isAvailableForBooking()) {
            throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE, 
                "Service bay does not allow booking");
        }
    }
    
    /**
     * Validate entities exist
     */
    private void validateEntities(CreateBookingWithScheduleRequest request) {
        // Validate branch
        branchService.findById(request.getBranchId())
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Branch not found with ID: " + request.getBranchId()));
        
        // Validate customer if provided
        if (request.getCustomerId() != null) {
            userService.findById(request.getCustomerId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                    "Customer not found with ID: " + request.getCustomerId()));
        }
        
        // Validate vehicle if provided
        if (request.getVehicleId() != null) {
            vehicleProfileService.getVehicleProfileById(request.getVehicleId());
        }
        
        // Validate bay
        ServiceBay bay = serviceBayService.getById(request.getSelectedSchedule().getBayId());
        if (!bay.isAvailableForBooking()) {
            throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE, 
                "Service bay does not allow booking");
        }
    }
    
    
    /**
     * Tạo booking entity
     */
    private Booking createBookingEntity(CreateBookingWithScheduleRequest request) {
        // Generate booking code
        String bookingCode = generateBookingCode();
        
        // Get entities
        Branch branch = branchService.findById(request.getBranchId()).orElseThrow();
        User customer = request.getCustomerId() != null ? 
            userService.findById(request.getCustomerId()).orElse(null) : null;
        VehicleProfile vehicle = request.getVehicleId() != null ? 
            vehicleProfileService.getVehicleProfileById(request.getVehicleId()) : null;
        ServiceBay serviceBay = serviceBayService.getById(request.getSelectedSchedule().getBayId());
        
        // Use provided scheduling information or calculate from selected schedule
        LocalDateTime preferredStartAt = request.getPreferredStartAt() != null ? 
            parseDateTime(request.getPreferredStartAt()) : 
            LocalDateTime.of(request.getSelectedSchedule().getDate(), request.getSelectedSchedule().getStartTime());
            
        LocalDateTime scheduledStartAt = request.getScheduledStartAt() != null ? 
            parseDateTime(request.getScheduledStartAt()) : 
            LocalDateTime.of(request.getSelectedSchedule().getDate(), request.getSelectedSchedule().getStartTime());
            
        // Không cộng buffer vào estimated duration
        LocalDateTime scheduledEndAt = request.getScheduledEndAt() != null ? 
            parseDateTime(request.getScheduledEndAt()) : 
            LocalDateTime.of(request.getSelectedSchedule().getDate(), request.getSelectedSchedule().getStartTime())
                .plusMinutes(request.getSelectedSchedule().getServiceDurationMinutes());
        
        // Create booking
        Booking booking = Booking.builder()
            .bookingCode(bookingCode)
            .customer(customer)
            .customerName(request.getCustomerName())
            .customerPhone(request.getCustomerPhone())
            .customerEmail(request.getCustomerEmail())
            .vehicle(vehicle)
            .vehicleLicensePlate(request.getVehicleLicensePlate())
            .vehicleBrandName(request.getVehicleBrandName())
            .vehicleModelName(request.getVehicleModelName())
            .vehicleTypeName(request.getVehicleTypeName())
            .vehicleYear(request.getVehicleYear())
            .vehicleColor(request.getVehicleColor())
            .branch(branch)
            .serviceBay(serviceBay)
            .preferredStartAt(preferredStartAt) // Use provided or calculated time
            .scheduledStartAt(scheduledStartAt)
            .scheduledEndAt(scheduledEndAt)
            .estimatedDurationMinutes(request.getEstimatedDurationMinutes() != null ? 
                request.getEstimatedDurationMinutes() : 
                request.getSelectedSchedule().getServiceDurationMinutes())
            .totalPrice(request.getTotalPrice())
            .currency(request.getCurrency())
            .notes(request.getNotes())
            .status(Booking.BookingStatus.PENDING)
            .paymentStatus(Booking.PaymentStatus.PENDING)
            .bookingType(com.kltn.scsms_api_service.core.entity.enumAttribute.BookingType.SCHEDULED)
            .isActive(true)
            .isDeleted(false)
            .build();
        
        return booking;
    }
    
    
    /**
     * Parse datetime string to LocalDateTime
     * Handles both ISO format with timezone and local format
     */
    private LocalDateTime parseDateTime(String dateTimeString) {
        try {
            // Try parsing as ISO format with timezone first
            if (dateTimeString.contains("T") && (dateTimeString.endsWith("Z") || dateTimeString.contains("+"))) {
                // Convert to LocalDateTime by removing timezone info
                String localDateTimeString = dateTimeString.replace("Z", "").split("\\+")[0];
                return LocalDateTime.parse(localDateTimeString);
            } else {
                // Direct parse for local format
                return LocalDateTime.parse(dateTimeString);
            }
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}, using current time", dateTimeString);
            return LocalDateTime.now();
        }
    }

    /**
     * Tạo booking items với giá từ price book
     */
    private void createBookingItems(Booking booking, CreateBookingWithScheduleRequest request) {
        for (CreateBookingItemRequest itemRequest : request.getBookingItems()) {
            // Lấy giá từ price book
            BigDecimal unitPrice = pricingBusinessService.resolveServicePrice(itemRequest.getServiceId(), null);
            
            // Get duration from Service entity
            Integer durationMinutes = 60; // Default
            try {
                com.kltn.scsms_api_service.core.entity.Service serviceEntity = serviceService.getById(itemRequest.getServiceId());
                if (serviceEntity.getEstimatedDuration() != null) {
                    durationMinutes = serviceEntity.getEstimatedDuration();
                }
            } catch (Exception e) {
                log.warn("Could not get duration from Service entity for serviceId: {}, using default 60 minutes", itemRequest.getServiceId());
            }
            
            // Convert to BookingItem entity
            BookingItem bookingItem = BookingItem.builder()
                .booking(booking)
                .serviceId(itemRequest.getServiceId())
                .serviceName(itemRequest.getServiceName())
                .serviceDescription(itemRequest.getServiceDescription())
                .unitPrice(unitPrice) // From price book
                .durationMinutes(durationMinutes)
                .itemStatus(BookingItem.ItemStatus.PENDING)
                .isActive(true)
                .isDeleted(false)
                .build();
            
            bookingItemService.save(bookingItem);
            log.info("Created booking item for service {} with price from price book: {}", 
                itemRequest.getServiceName(), unitPrice);
        }
    }
    
    /**
     * Generate booking code
     */
    private String generateBookingCode() {
        return "BK-" + System.currentTimeMillis();
    }
}
