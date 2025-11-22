package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.walkInBooking.WalkInBookingRequest;
import com.kltn.scsms_api_service.core.dto.walkInBooking.WalkInBookingResponse;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.VehicleProfile;
import com.kltn.scsms_api_service.core.service.entityService.BookingService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.core.service.entityService.VehicleProfileService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service xử lý business logic cho walk-in booking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalkInBookingService {

    private final BookingService bookingService;
    private final ServiceBayService serviceBayService;
    private final UserService userService;
    private final VehicleProfileService vehicleProfileService;

    /**
     * Tạo walk-in booking
     * Sử dụng pessimistic locking để ngăn race condition conflict booking
     */
    @Transactional
    public WalkInBookingResponse createWalkInBooking(WalkInBookingRequest request) {
        log.info("Creating walk-in booking: customerName={}, bayId={}",
                request.getCustomerName(), request.getAssignedBayId());

        try {
            // 1. Validate request
            validateWalkInRequest(request);

            // 2. Validate bay
            ServiceBay serviceBay = serviceBayService.getById(request.getAssignedBayId());
            if (!serviceBay.isActive()) {
                throw new ClientSideException(ErrorCode.SERVICE_BAY_NOT_AVAILABLE, 
                    "Service bay is not active");
            }

            // 3. Tính scheduledStartAt và scheduledEndAt từ booking trước đó
            LocalDate bookingDate = request.getBookingDate() != null ? 
                LocalDate.parse(request.getBookingDate()) : LocalDate.now();
            
            LocalDateTime scheduledStartAt = calculateScheduledStartAt(
                request.getAssignedBayId(), 
                bookingDate
            );
            
            LocalDateTime scheduledEndAt = scheduledStartAt.plusMinutes(
                request.getEstimatedDurationMinutes() != null ? request.getEstimatedDurationMinutes() : 60
            );

            // 4. Check conflict với PESSIMISTIC LOCK (atomic operation - ngăn race condition)
            List<Booking> conflictingBookings = bookingService.findConflictingBookingsWithLock(
                request.getAssignedBayId(),
                scheduledStartAt,
                scheduledEndAt
            );
            
            if (!conflictingBookings.isEmpty()) {
                Booking conflictBooking = conflictingBookings.get(0);
                
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
                        "Bay is not available at the calculated time. " +
                        "Conflicts with booking '%s' (%s - %s). Please try again.",
                        conflictBooking.getBookingCode(),
                        conflictBooking.getScheduledStartAt(),
                        conflictBooking.getScheduledEndAt()
                    ),
                    errorData
                );
            }

            // 5. Tạo booking entity với scheduledStartAt và scheduledEndAt đã tính (đã được lock)
            Booking booking = createBookingFromRequest(request, scheduledStartAt, scheduledEndAt);

            // 6. Lưu booking
            booking = bookingService.save(booking);

            log.info("Saved walk-in booking to database: bookingId={}, scheduledStartAt={}, scheduledEndAt={}",
                    booking.getBookingId(), scheduledStartAt, scheduledEndAt);

            // 7. Tính queue position (dựa trên số booking trước đó)
            int queuePosition = calculateQueuePosition(request.getAssignedBayId(), bookingDate, scheduledStartAt);

            // 8. Tính thời gian chờ ước tính
            LocalDateTime now = LocalDateTime.now();
            long estimatedWaitMinutes = java.time.Duration.between(now, scheduledStartAt).toMinutes();
            int estimatedWaitTime = Math.max(0, (int) estimatedWaitMinutes);

            log.info("Successfully created walk-in booking: {} with scheduledStartAt: {}, queue position: {}",
                    booking.getBookingId(), scheduledStartAt, queuePosition);

            return WalkInBookingResponse.builder()
                    .bookingId(booking.getBookingId())
                    .bookingCode(booking.getBookingCode())
                    .assignedBayId(request.getAssignedBayId())
                    .queuePosition(queuePosition)
                    .estimatedStartTime(scheduledStartAt)
                    .estimatedWaitTime(estimatedWaitTime)
                    .status(booking.getStatus().name())
                    .message("Walk-in booking created successfully")
                    .build();
                    
        } catch (DataIntegrityViolationException e) {
            // Database constraint violation (backup protection)
            log.error("Database constraint violation when creating walk-in booking: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("idx_booking_bay_time_unique")) {
                throw new ClientSideException(
                    ErrorCode.SERVICE_BAY_NOT_AVAILABLE,
                    "Bay is not available. Another booking was created for this time slot. Please try again."
                );
            }
            throw e;
        }
    }
    
    /**
     * Tính scheduledStartAt cho WALK_IN booking
     * Logic: max(scheduledEndAt của các booking trước) (hoặc NOW() nếu không có)
     */
    private LocalDateTime calculateScheduledStartAt(UUID bayId, LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        
        // Query các WALK_IN bookings chưa kết thúc của bay trong ngày
        LocalDateTime maxScheduledEndAt = bookingService.findMaxScheduledEndAtForWalkInBookings(bayId, date);
        
        if (maxScheduledEndAt == null) {
            // Không có booking nào → scheduledStartAt = NOW()
            log.info("No existing walk-in bookings found, using current time: {}", now);
            return now;
        }
        
        // Có booking trước đó → scheduledStartAt = max(scheduledEndAt)
        LocalDateTime scheduledStartAt = maxScheduledEndAt;
        
        // Đảm bảo scheduledStartAt >= NOW()
        if (scheduledStartAt.isBefore(now)) {
            log.info("Calculated scheduledStartAt {} is before now {}, using now", scheduledStartAt, now);
            scheduledStartAt = now;
        }
        
        log.info("Calculated scheduledStartAt: {} (from maxScheduledEndAt: {})", 
                scheduledStartAt, maxScheduledEndAt);
        
        return scheduledStartAt;
    }
    
    /**
     * Tính queue position dựa trên số booking trước scheduledStartAt
     */
    private int calculateQueuePosition(UUID bayId, LocalDate date, LocalDateTime scheduledStartAt) {
        List<Booking> previousBookings = bookingService.findWalkInBookingsByBayAndDate(bayId, date);
        
        // Đếm số booking có scheduledStartAt < scheduledStartAt của booking hiện tại
        long count = previousBookings.stream()
            .filter(b -> b.getScheduledStartAt() != null && 
                        b.getScheduledStartAt().isBefore(scheduledStartAt))
            .count();
        
        return (int) count + 1; // Position bắt đầu từ 1
    }

    /**
     * Validate walk-in request
     */
    private void validateWalkInRequest(WalkInBookingRequest request) {
        if (request.getCustomerType() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Customer type is required");
        }

        if ("EXISTING".equals(request.getCustomerType())) {
            if (request.getCustomerId() == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Customer ID is required for existing customer");
            }
            if (request.getVehicleId() == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Vehicle ID is required for existing customer");
            }
        } else if ("NEW".equals(request.getCustomerType())) {
            if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Customer name is required for new customer");
            }
            if (request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Customer phone is required for new customer");
            }
            if (request.getVehicleLicensePlate() == null || request.getVehicleLicensePlate().trim().isEmpty()) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Vehicle license plate is required for new customer");
            }
        }

        if (request.getAssignedBayId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Assigned bay ID is required");
        }

        if (request.getBranchId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Branch ID is required");
        }
    }

    /**
     * Tạo booking entity từ request
     */
    private Booking createBookingFromRequest(WalkInBookingRequest request, 
            LocalDateTime scheduledStartAt, LocalDateTime scheduledEndAt) {
        // Debug log để kiểm tra dữ liệu request
        log.info("🔍 DEBUG: WalkInBookingRequest data:");
        log.info("  - customerType: {}", request.getCustomerType());
        log.info("  - customerId: {}", request.getCustomerId());
        log.info("  - vehicleId: {}", request.getVehicleId());
        log.info("  - vehicleLicensePlate: {}", request.getVehicleLicensePlate());
        log.info("  - vehicleBrand: {}", request.getVehicleBrand());
        log.info("  - vehicleModel: {}", request.getVehicleModel());
        log.info("  - vehicleType: {}", request.getVehicleType());
        log.info("  - vehicleColor: {}", request.getVehicleColor());
        log.info("  - vehicleYear: {}", request.getVehicleYear());
        log.info("  - bookingDate: {}", request.getBookingDate());
        
        // Tạo booking code
        String bookingCode = generateBookingCode();

        // Sử dụng scheduledStartAt và scheduledEndAt đã được tính toán
        LocalDateTime preferredStartAt = scheduledStartAt;

        var builder = Booking.builder()
                .bookingCode(bookingCode)
                .branch(serviceBayService.getById(request.getAssignedBayId()).getBranch())
                .serviceBay(serviceBayService.getById(request.getAssignedBayId()))
                .bookingType(com.kltn.scsms_api_service.core.entity.enumAttribute.BookingType.WALK_IN)
                .status(Booking.BookingStatus.PENDING)
                .totalPrice(request.getTotalPrice())
                .currency(request.getCurrency())
                .notes(request.getNotes())
                .preferredStartAt(preferredStartAt)
                .scheduledStartAt(scheduledStartAt)
                .scheduledEndAt(scheduledEndAt)
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes());

        if ("EXISTING".equals(request.getCustomerType())) {
            // Sử dụng customer và vehicle có sẵn
            log.info("Processing EXISTING customer booking: customerId={}, vehicleId={}",
                    request.getCustomerId(), request.getVehicleId());

            if (request.getCustomerId() != null) {
                // Lấy thông tin customer từ database
                User customer = userService.findById(request.getCustomerId())
                        .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                                "Customer not found with ID: " + request.getCustomerId()));
                builder.customer(customer)
                        .customerName(customer.getFullName())
                        .customerPhone(customer.getPhoneNumber())
                        .customerEmail(customer.getEmail());
                log.info("Customer found: {} ({})", customer.getFullName(), customer.getEmail());
            }
            if (request.getVehicleId() != null) {
                // Lấy thông tin vehicle từ database
                VehicleProfile vehicleProfile = vehicleProfileService.getVehicleProfileById(request.getVehicleId());
                builder.vehicle(vehicleProfile);
                
                // Sử dụng trực tiếp từ request (giống IntegratedBookingService)
                builder.vehicleLicensePlate(request.getVehicleLicensePlate())
                       .vehicleBrandName(request.getVehicleBrand())
                       .vehicleModelName(request.getVehicleModel())
                       .vehicleTypeName(request.getVehicleType())
                       .vehicleColor(request.getVehicleColor())
                       .vehicleYear(request.getVehicleYear());
                
                log.info("Vehicle found: {} (ID: {}, Year: {})", 
                    vehicleProfile.getLicensePlate(), 
                    vehicleProfile.getVehicleId(),
                    vehicleProfile.getVehicleYear());
                log.info("Vehicle details from request: {} - {} {} ({})",
                    request.getVehicleLicensePlate(),
                    request.getVehicleBrand(),
                    request.getVehicleModel(),
                    request.getVehicleType());
                log.info("Final vehicle info saved: {} - {} {} ({})",
                    request.getVehicleLicensePlate(),
                    request.getVehicleBrand(),
                    request.getVehicleModel(),
                    request.getVehicleType());
            }
        } else {
            // Sử dụng thông tin walk-in (NEW customer + NEW vehicle)
            log.info("Processing NEW customer booking with new vehicle info");
            builder.customerName(request.getCustomerName())
                    .customerPhone(request.getCustomerPhone())
                    .customerEmail(request.getCustomerEmail())
                    .vehicleLicensePlate(request.getVehicleLicensePlate())
                    .vehicleBrandName(request.getVehicleBrand())
                    .vehicleModelName(request.getVehicleModel())
                    .vehicleTypeName(request.getVehicleType())
                    .vehicleColor(request.getVehicleColor())
                    .vehicleYear(request.getVehicleYear());
            log.info("New customer info: {} ({})", request.getCustomerName(), request.getCustomerPhone());
            log.info("New vehicle info: {} - {} {}",
                    request.getVehicleLicensePlate(),
                    request.getVehicleBrand(),
                    request.getVehicleModel());
        }

        Booking booking = builder.build();

        log.info("Created booking entity: bookingId={}, customer={}, vehicle={}, year={}",
                booking.getBookingId(),
                booking.getCustomer() != null ? booking.getCustomer().getFullName() : "null",
                booking.getVehicle() != null ? booking.getVehicle().getLicensePlate() : "null",
                booking.getVehicleYear());

        // Tạo booking items từ request
        if (request.getServices() != null && !request.getServices().isEmpty()) {
            booking.setBookingItems(request.getServices().stream()
                    .map(serviceRequest -> {
                        BookingItem item = BookingItem.builder()
                                .booking(booking)
                                .serviceId(serviceRequest.getServiceId())
                                .serviceName(serviceRequest.getServiceName())
                                .serviceDescription("Walk-in service")
                                .unitPrice(serviceRequest.getPrice())
                                .durationMinutes(serviceRequest.getDurationMinutes() != null ? serviceRequest.getDurationMinutes() : 60)
                                .itemStatus(BookingItem.ItemStatus.PENDING)
                                .build();
                        return item;
                    })
                    .collect(java.util.stream.Collectors.toList()));
        }

        return booking;
    }

    /**
     * Tạo booking code duy nhất
     */
    private String generateBookingCode() {
        // Format: WALK-IN-YYYYMMDD-HHMMSS-XXXX
        LocalDateTime now = LocalDateTime.now();
        String dateTime = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String randomSuffix = String.format("%04d", (int) (Math.random() * 10000));
        return "WALK-IN-" + dateTime + "-" + randomSuffix;
    }

}
