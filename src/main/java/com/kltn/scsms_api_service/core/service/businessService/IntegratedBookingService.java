package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingWithSlotRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingItemRequest;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service tích hợp để tạo booking với slot trong một API call
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IntegratedBookingService {
    
    private final BookingService bookingService;
    private final BayScheduleService bayScheduleService;
    private final ServiceBayService serviceBayService;
    private final BranchService branchService;
    private final UserService userService;
    private final VehicleProfileService vehicleProfileService;
    private final BookingItemService bookingItemService;
    private final BookingPricingService bookingPricingService;
    private final BookingInfoService bookingInfoService;
    
    /**
     * Tạo booking hoàn chỉnh với slot trong một API call
     */
    public BookingInfoDto createBookingWithSlot(CreateBookingWithSlotRequest request) {
        log.info("Creating integrated booking for customer: {} at branch: {} with slot: {}",
            request.getCustomerName(), request.getBranchId(), request.getSelectedSlot());
        
        // 1. Validate tất cả thông tin
        validateRequest(request);
        
        // 2. Tạo booking entity
        Booking booking = createBookingEntity(request);
        
        // 3. Lưu booking trước để có bookingId
        Booking savedBooking = bookingService.save(booking);
        
        // 4. Đặt slot tự động (sau khi đã có bookingId)
        bookSlotForBooking(savedBooking, request.getSelectedSlot());
        
        // 5. Tạo booking items
        if (request.getBookingItems() != null && !request.getBookingItems().isEmpty()) {
            createBookingItems(savedBooking, request);
        }
        
        log.info("Successfully created integrated booking: {} with slot: {}",
            savedBooking.getBookingId(), request.getSelectedSlot());
        
        return bookingInfoService.toBookingInfoDto(savedBooking);
    }
    
    /**
     * Validate request
     */
    private void validateRequest(CreateBookingWithSlotRequest request) {
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
        
        if (request.getSelectedSlot() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Slot selection is required");
        }
        
        if (request.getSelectedSlot().getBayId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Bay ID is required");
        }
        
        if (request.getSelectedSlot().getDate() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Slot date is required");
        }
        
        if (request.getSelectedSlot().getStartTime() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Slot start time is required");
        }
        
        if (request.getSelectedSlot().getServiceDurationMinutes() == null || 
            request.getSelectedSlot().getServiceDurationMinutes() <= 0) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Service duration is required and must be positive");
        }
        
        // Validate entities exist
        validateEntities(request);
        
        // Validate slot availability
        validateSlotAvailability(request.getSelectedSlot());
    }
    
    /**
     * Validate entities exist
     */
    private void validateEntities(CreateBookingWithSlotRequest request) {
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
        ServiceBay bay = serviceBayService.getById(request.getSelectedSlot().getBayId());
        if (!bay.isAvailableForBooking()) {
            throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE, 
                "Service bay does not allow booking");
        }
    }
    
    /**
     * Validate slot availability
     */
    private void validateSlotAvailability(CreateBookingWithSlotRequest.SlotSelectionRequest slot) {
        // Validate booking date (giới hạn trong tháng)
        bayScheduleService.validateBookingDate(slot.getDate());
        
        if (!bayScheduleService.isSlotAvailable(slot.getBayId(), slot.getDate(), slot.getStartTime())) {
            throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE, 
                "Slot is not available for booking");
        }
        
        // Validate slot has enough time for service
        BaySchedule baySchedule = bayScheduleService.getSlot(slot.getBayId(), slot.getDate(), slot.getStartTime());
        if (baySchedule == null) {
            throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE, 
                "Slot not found");
        }
        
        // Check if we have enough consecutive slots for the service
        ServiceBay bay = serviceBayService.getById(slot.getBayId());
        int slotsNeeded = (int) Math.ceil((double) slot.getServiceDurationMinutes() / bay.getSlotDurationMinutes());
        
        // Check if all required slots are available
        for (int i = 0; i < slotsNeeded; i++) {
            LocalTime slotStartTime = slot.getStartTime().plusMinutes(i * bay.getSlotDurationMinutes());
            if (!bayScheduleService.isSlotAvailable(slot.getBayId(), slot.getDate(), slotStartTime)) {
                throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE, 
                    "Required slot at " + slotStartTime + " is not available for service duration " + 
                    slot.getServiceDurationMinutes() + " minutes (needs " + slotsNeeded + " slots)");
            }
        }
    }
    
    /**
     * Tạo booking entity
     */
    private Booking createBookingEntity(CreateBookingWithSlotRequest request) {
        // Generate booking code
        String bookingCode = generateBookingCode();
        
        // Get entities
        Branch branch = branchService.findById(request.getBranchId()).orElseThrow();
        User customer = request.getCustomerId() != null ? 
            userService.findById(request.getCustomerId()).orElse(null) : null;
        VehicleProfile vehicle = request.getVehicleId() != null ? 
            vehicleProfileService.getVehicleProfileById(request.getVehicleId()) : null;
        ServiceBay serviceBay = serviceBayService.getById(request.getSelectedSlot().getBayId());
        
        // Use provided scheduling information or calculate from slot
        LocalDateTime preferredStartAt = request.getPreferredStartAt() != null ? 
            parseDateTime(request.getPreferredStartAt()) : 
            LocalDateTime.of(request.getSelectedSlot().getDate(), request.getSelectedSlot().getStartTime());
            
        LocalDateTime scheduledStartAt = request.getScheduledStartAt() != null ? 
            parseDateTime(request.getScheduledStartAt()) : 
            LocalDateTime.of(request.getSelectedSlot().getDate(), request.getSelectedSlot().getStartTime());
            
        // Không cộng buffer vào estimated duration
        LocalDateTime scheduledEndAt = request.getScheduledEndAt() != null ? 
            parseDateTime(request.getScheduledEndAt()) : 
            LocalDateTime.of(request.getSelectedSlot().getDate(), request.getSelectedSlot().getStartTime())
                .plusMinutes(request.getSelectedSlot().getServiceDurationMinutes());
        
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
                request.getSelectedSlot().getServiceDurationMinutes())
            .bufferMinutes(serviceBay.getBufferMinutes())
            .slotStartTime(request.getSlotStartTime() != null ? 
                LocalTime.parse(request.getSlotStartTime()) : 
                request.getSelectedSlot().getStartTime())
            .slotEndTime(request.getSlotEndTime() != null ? 
                LocalTime.parse(request.getSlotEndTime()) : 
                request.getSelectedSlot().getStartTime().plusMinutes(serviceBay.getSlotDurationMinutes()))
            .totalPrice(request.getTotalPrice())
            .currency(request.getCurrency())
            .depositAmount(request.getDepositAmount())
            .couponCode(request.getCouponCode())
            .notes(request.getNotes())
            .specialRequests(request.getSpecialRequests())
            .status(Booking.BookingStatus.PENDING)
            .paymentStatus(Booking.PaymentStatus.PENDING)
            .priority(Booking.Priority.NORMAL)
            .isActive(true)
            .isDeleted(false)
            .build();
        
        return booking;
    }
    
    /**
     * Đặt slot cho booking
     */
    private void bookSlotForBooking(Booking booking, CreateBookingWithSlotRequest.SlotSelectionRequest slot) {
        // Book the slot
        bayScheduleService.bookSlot(
            slot.getBayId(), 
            slot.getDate(), 
            slot.getStartTime(), 
            booking.getBookingId()
        );
        
        // Block additional slots if needed
        ServiceBay bay = serviceBayService.getById(slot.getBayId());
        // Chỉ tính dựa trên service duration, không cộng bufferMinutes
        int slotsNeeded = (int) Math.ceil((double) slot.getServiceDurationMinutes() / bay.getSlotDurationMinutes());
        
        if (slotsNeeded > 1) {
            // Block additional slots with bookingId
            for (int i = 1; i < slotsNeeded; i++) {
                bayScheduleService.blockSlot(
                    slot.getBayId(),
                    slot.getDate(),
                    slot.getStartTime().plusMinutes(i * bay.getSlotDurationMinutes()),
                    booking.getBookingId()
                );
            }
        }
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
    private void createBookingItems(Booking booking, CreateBookingWithSlotRequest request) {
        for (CreateBookingItemRequest itemRequest : request.getBookingItems()) {
            // Lấy giá từ price book bằng cách tạo temporary BookingItem
            BookingItem tempBookingItem = BookingItem.builder()
                .itemType(BookingItem.ItemType.SERVICE)
                .itemId(itemRequest.getServiceId())
                .itemName(itemRequest.getItemName())
                .itemDescription(itemRequest.getItemDescription())
                .quantity(1)
                .discountAmount(itemRequest.getDiscountAmount())
                .taxAmount(itemRequest.getTaxAmount())
                .build();
            
            BigDecimal unitPrice = bookingPricingService.calculateBookingItemPrice(tempBookingItem, null);
            
            // Calculate total amount (KHÔNG cộng tax, chỉ lấy giá dịch vụ trừ discount)
            BigDecimal subtotal = unitPrice; // Services are always quantity 1
            BigDecimal totalAmount = subtotal
                .subtract(itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount() : BigDecimal.ZERO);
            // KHÔNG cộng tax vào total amount - chỉ lấy giá dịch vụ
            
            // Convert to BookingItem entity
            BookingItem bookingItem = BookingItem.builder()
                .booking(booking)
                .itemType(BookingItem.ItemType.SERVICE)
                .itemId(itemRequest.getServiceId())
                .itemName(itemRequest.getItemName())
                .itemDescription(itemRequest.getItemDescription())
                .quantity(1) // Services are always quantity 1
                .unitPrice(unitPrice) // From price book
                .discountAmount(itemRequest.getDiscountAmount())
                .taxAmount(itemRequest.getTaxAmount())
                .totalAmount(totalAmount)
                .itemStatus(BookingItem.ItemStatus.PENDING)
                .isActive(true)
                .isDeleted(false)
                .build();
            
            bookingItemService.save(bookingItem);
            log.info("Created booking item for service {} with price from price book: {}", 
                itemRequest.getItemName(), unitPrice);
        }
    }
    
    /**
     * Generate booking code
     */
    private String generateBookingCode() {
        return "BK-" + System.currentTimeMillis();
    }
}
