package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.walkInBooking.WalkInBookingRequest;
import com.kltn.scsms_api_service.core.dto.walkInBooking.WalkInBookingResponse;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.VehicleProfile;
import com.kltn.scsms_api_service.core.service.entityService.BayQueueService;
import com.kltn.scsms_api_service.core.service.entityService.BookingService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.core.service.entityService.VehicleProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Service xử lý business logic cho walk-in booking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalkInBookingService {

    private final BookingService bookingService;
    private final BayQueueService bayQueueService;
    private final ServiceBayService serviceBayService;
    private final UserService userService;
    private final VehicleProfileService vehicleProfileService;

    /**
     * Tạo walk-in booking
     */
    @Transactional
    public WalkInBookingResponse createWalkInBooking(WalkInBookingRequest request) {
        log.info("Creating walk-in booking: customerName={}, bayId={}",
                request.getCustomerName(), request.getAssignedBayId());

        // 1. Validate request
        validateWalkInRequest(request);

        // 2. Tạo booking entity
        Booking booking = createBookingFromRequest(request);

        // 3. Lưu booking
        booking = bookingService.save(booking);

        log.info("Saved booking to database: bookingId={}, customer={}, vehicle={}, year={}",
                booking.getBookingId(),
                booking.getCustomer() != null ? booking.getCustomer().getFullName() : "null",
                booking.getVehicle() != null ? booking.getVehicle().getLicensePlate() : "null",
                booking.getVehicleYear());

        // 4. Thêm vào hàng chờ bay (sử dụng ngày từ request)
        LocalDate queueDate = request.getBookingDate() != null ? 
            LocalDate.parse(request.getBookingDate()) : LocalDate.now();
        log.info("🔍 DEBUG: Adding to queue with date: {} (from request: {})", queueDate, request.getBookingDate());
        bayQueueService.addToQueue(request.getAssignedBayId(), booking.getBookingId(), queueDate);

        // 5. Lấy thông tin hàng chờ
        var queueEntry = bayQueueService.getBookingQueuePosition(booking.getBookingId());
        int queuePosition = queueEntry.map(q -> q.getQueuePosition()).orElse(0);

        // 6. Tính thời gian chờ ước tính
        int estimatedWaitTime = calculateEstimatedWaitTime(request.getAssignedBayId(), queuePosition);

        log.info("Successfully created walk-in booking: {} with queue position: {}",
                booking.getBookingId(), queuePosition);

        return WalkInBookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .assignedBayId(request.getAssignedBayId())
                .queuePosition(queuePosition)
                .estimatedStartTime(LocalDateTime.now().plusMinutes(estimatedWaitTime))
                .estimatedWaitTime(estimatedWaitTime)
                .status(booking.getStatus().name())
                .message("Walk-in booking created successfully")
                .build();
    }

    /**
     * Validate walk-in request
     */
    private void validateWalkInRequest(WalkInBookingRequest request) {
        if (request.getCustomerType() == null) {
            throw new IllegalArgumentException("Customer type is required");
        }

        if ("EXISTING".equals(request.getCustomerType())) {
            if (request.getCustomerId() == null) {
                throw new IllegalArgumentException("Customer ID is required for existing customer");
            }
            if (request.getVehicleId() == null) {
                throw new IllegalArgumentException("Vehicle ID is required for existing customer");
            }
        } else if ("NEW".equals(request.getCustomerType())) {
            if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
                throw new IllegalArgumentException("Customer name is required for new customer");
            }
            if (request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()) {
                throw new IllegalArgumentException("Customer phone is required for new customer");
            }
            if (request.getVehicleLicensePlate() == null || request.getVehicleLicensePlate().trim().isEmpty()) {
                throw new IllegalArgumentException("Vehicle license plate is required for new customer");
            }
        }

        if (request.getAssignedBayId() == null) {
            throw new IllegalArgumentException("Assigned bay ID is required");
        }

        if (request.getBranchId() == null) {
            throw new IllegalArgumentException("Branch ID is required");
        }
    }

    /**
     * Tạo booking entity từ request
     */
    private Booking createBookingFromRequest(WalkInBookingRequest request) {
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

        // Calculate current time for walk-in booking
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime preferredStartAt = request.getPreferredStartAt() != null
                ? parseDateTime(request.getPreferredStartAt())
                : now;
        LocalDateTime scheduledStartAt = request.getScheduledStartAt() != null
                ? parseDateTime(request.getScheduledStartAt())
                : now;
        LocalDateTime scheduledEndAt = request.getScheduledEndAt() != null ? parseDateTime(request.getScheduledEndAt())
                : now.plusMinutes(
                        request.getEstimatedDurationMinutes() != null ? request.getEstimatedDurationMinutes() : 60);

        Booking.BookingBuilder builder = Booking.builder()
                .bookingCode(bookingCode)
                .branch(serviceBayService.getById(request.getAssignedBayId()).getBranch())
                .serviceBay(serviceBayService.getById(request.getAssignedBayId()))
                .status(Booking.BookingStatus.PENDING)
                .priority(Booking.Priority.NORMAL)
                .totalPrice(request.getTotalPrice())
                .currency(request.getCurrency())
                .depositAmount(request.getDepositAmount())
                .notes(request.getNotes())
                .preferredStartAt(preferredStartAt)
                .scheduledStartAt(scheduledStartAt)
                .scheduledEndAt(scheduledEndAt)
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .slotStartTime(request.getSlotStartTime() != null ? LocalTime.parse(request.getSlotStartTime())
                        : LocalTime.now())
                .slotEndTime(request.getSlotEndTime() != null ? LocalTime.parse(request.getSlotEndTime())
                        : LocalTime.now()
                                .plusMinutes(request.getEstimatedDurationMinutes() != null
                                        ? request.getEstimatedDurationMinutes()
                                        : 60));

        if ("EXISTING".equals(request.getCustomerType())) {
            // Sử dụng customer và vehicle có sẵn
            log.info("Processing EXISTING customer booking: customerId={}, vehicleId={}",
                    request.getCustomerId(), request.getVehicleId());

            if (request.getCustomerId() != null) {
                // Lấy thông tin customer từ database
                User customer = userService.findById(request.getCustomerId())
                        .orElseThrow(() -> new IllegalArgumentException(
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
                                .itemType(BookingItem.ItemType.SERVICE)
                                .itemId(serviceRequest.getServiceId())
                                .itemName(serviceRequest.getServiceName())
                                .itemDescription("Walk-in service")
                                .quantity(1)
                                .unitPrice(serviceRequest.getPrice())
                                .totalAmount(serviceRequest.getPrice())
                                .discountAmount(BigDecimal.ZERO)
                                .taxAmount(serviceRequest.getPrice().multiply(new BigDecimal("0.1"))) // 10% tax
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
     * Tính thời gian chờ ước tính
     */
    private int calculateEstimatedWaitTime(UUID bayId, int queuePosition) {
        // Tính thời gian chờ dựa trên vị trí trong hàng chờ
        // Giả sử mỗi booking mất 60 phút
        return queuePosition * 60;
    }
}
