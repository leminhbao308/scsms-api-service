package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.service.businessService.BayRecommendationService;
import com.kltn.scsms_api_service.core.service.businessService.WalkInBookingService;
import com.kltn.scsms_api_service.core.service.entityService.BookingService;
import com.kltn.scsms_api_service.core.dto.walkInBooking.*;
import com.kltn.scsms_api_service.core.entity.Booking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Walk-in Booking", description = "API cho walk-in booking")
public class WalkInBookingController {
    
    private final BayRecommendationService bayRecommendationService;
    private final WalkInBookingService walkInBookingService;
    private final BookingService bookingService;
    
        /**
         * Đề xuất bay tốt nhất cho walk-in booking
         */
        @PostMapping(ApiConstant.RECOMMEND_BAY_API)
        @Operation(summary = "Đề xuất bay", description = "Đề xuất bay tốt nhất cho walk-in booking")
        public ResponseEntity<BayRecommendationResponse> recommendBay(
                @RequestBody BayRecommendationRequest request,
                @RequestParam(required = false) String queueDate) {
        try {
            log.info("DEBUG: Recommending bay for walk-in booking: branchId={}, serviceDuration={} minutes, queueDate={}",
                request.getBranchId(), request.getServiceDurationMinutes(), queueDate);
            
            // Parse ngày nếu có
            LocalDate parsedQueueDate = queueDate != null ? LocalDate.parse(queueDate) : LocalDate.now();
            log.info("DEBUG: Parsed queue date: {}", parsedQueueDate);
            
            // Gọi service để đề xuất bay
            BayRecommendationService.BayRecommendation recommendation = bayRecommendationService.recommendBay(convertToServiceRequest(request), parsedQueueDate);
            
            // Convert to response DTO
            BayRecommendationResponse response = convertToResponse(recommendation);
            
            return ResponseEntity.ok(response);
                
        } catch (Exception e) {
            log.error("Error recommending bay: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Tạo walk-in booking
     */
    @PostMapping(ApiConstant.CREATE_WALK_IN_BOOKING_API)
    @Operation(summary = "Tạo walk-in booking", description = "Tạo booking cho khách hàng walk-in")
    public ResponseEntity<WalkInBookingResponse> createWalkInBooking(
            @RequestBody WalkInBookingRequest request) {
        try {
            log.info("Creating walk-in booking: customerName={}, bayId={}", 
                request.getCustomerName(), request.getAssignedBayId());
            
            // Gọi service để tạo booking
            WalkInBookingResponse response = walkInBookingService.createWalkInBooking(request);
            
            return ResponseEntity.ok(response);
                
        } catch (Exception e) {
            log.error("Error creating walk-in booking: {}", e.getMessage(), e);
            throw e; // Let global exception handler handle this
        }
    }
    
        /**
         * Lấy thông tin hàng chờ của một bay
         * @param bayId ID của bay
         * @param queueDate Ngày cần lấy queue (optional, mặc định là hôm nay)
         * @param includeBookingId ID của booking cần include vào queue (optional, dùng khi update booking để luôn hiển thị booking hiện tại)
         */
        @GetMapping(ApiConstant.GET_BAY_QUEUE_API)
        @Operation(summary = "Lấy hàng chờ bay", description = "Lấy thông tin hàng chờ của một bay")
        public ResponseEntity<List<BookingQueueItemResponse>> getBayQueue(
                @PathVariable UUID bayId,
                @RequestParam(required = false) String queueDate,
                @RequestParam(required = false) UUID includeBookingId) {
        try {

            // Parse ngày nếu có
            LocalDate parsedQueueDate = queueDate != null ? LocalDate.parse(queueDate) : LocalDate.now();
            
            // Lấy thông tin WALK_IN bookings của bay
            List<Booking> walkInBookings = bookingService.findWalkInBookingsByBayAndDate(bayId, parsedQueueDate);
            
            // Nếu có includeBookingId, thêm booking đó vào queue nếu chưa có
            if (includeBookingId != null) {
                // Kiểm tra xem booking đã có trong queue chưa
                boolean alreadyInQueue = walkInBookings.stream()
                    .anyMatch(b -> b.getBookingId().equals(includeBookingId));
                
                if (!alreadyInQueue) {
                    try {
                        // Lấy booking từ database với đầy đủ thông tin (bookingItems, serviceBay, etc.)
                        // Sử dụng getByIdWithDetails() để load related entities
                        Booking bookingToInclude = bookingService.getByIdWithDetails(includeBookingId);
                        if (bookingToInclude != null 
                                && bookingToInclude.getBookingType() == com.kltn.scsms_api_service.core.entity.enumAttribute.BookingType.WALK_IN
                                && bookingToInclude.getServiceBay() != null
                                && bookingToInclude.getServiceBay().getBayId().equals(bayId)
                                && bookingToInclude.getScheduledStartAt() != null
                                && bookingToInclude.getScheduledStartAt().toLocalDate().equals(parsedQueueDate)) {
                            walkInBookings.add(bookingToInclude);
                            // Sort lại theo scheduledStartAt
                            walkInBookings.sort((b1, b2) -> {
                                if (b1.getScheduledStartAt() == null) return 1;
                                if (b2.getScheduledStartAt() == null) return -1;
                                return b1.getScheduledStartAt().compareTo(b2.getScheduledStartAt());
                            });
                        } else {
                            log.warn("DEBUG: Cannot include booking {} - validation failed: bookingType={}, hasServiceBay={}, bayIdMatch={}, hasScheduledStartAt={}, dateMatch={}",
                                includeBookingId,
                                bookingToInclude != null ? bookingToInclude.getBookingType() : "null",
                                bookingToInclude != null && bookingToInclude.getServiceBay() != null,
                                bookingToInclude != null && bookingToInclude.getServiceBay() != null && bookingToInclude.getServiceBay().getBayId().equals(bayId),
                                bookingToInclude != null && bookingToInclude.getScheduledStartAt() != null,
                                bookingToInclude != null && bookingToInclude.getScheduledStartAt() != null && bookingToInclude.getScheduledStartAt().toLocalDate().equals(parsedQueueDate));
                        }
                    } catch (Exception e) {
                        log.warn("DEBUG: Cannot include booking {} - error loading booking: {}", includeBookingId, e.getMessage());
                        // Continue without including the booking if it doesn't exist or has errors
                    }
                }
            }
            
            // Convert to response DTOs
            List<BookingQueueItemResponse> queueItems = walkInBookings.stream()
                .map(this::convertBookingToResponse)
                .collect(Collectors.toList());

            log.info("DEBUG: Converted to {} response items", queueItems.size());
            return ResponseEntity.ok(queueItems);

        } catch (Exception e) {
            log.error("Error getting bay queue: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Helper methods
    
    private BayRecommendationService.BayRecommendationRequest convertToServiceRequest(BayRecommendationRequest request) {
        BayRecommendationService.BayRecommendationRequest serviceRequest = new BayRecommendationService.BayRecommendationRequest();
        serviceRequest.setBranchId(request.getBranchId());
        serviceRequest.setServiceType(request.getServiceType());
        serviceRequest.setServiceDurationMinutes(request.getServiceDurationMinutes());
        return serviceRequest;
    }
    
    private BayRecommendationResponse convertToResponse(BayRecommendationService.BayRecommendation recommendation) {
        BayRecommendationResponse response = new BayRecommendationResponse();
        
        // Convert recommended bay
        if (recommendation.getRecommendedBay() != null) {
            BayResponse bayResponse = BayResponse.builder()
                .bayId(recommendation.getRecommendedBay().getBayId())
                .bayName(recommendation.getRecommendedBay().getBayName())
                .bayCode(recommendation.getRecommendedBay().getBayCode())
                .status(recommendation.getRecommendedBay().getStatus().name())
                .allowBooking(recommendation.getRecommendedBay().isBookingAllowed())
                .build();
            response.setRecommendedBay(bayResponse);
        }
        
        // Convert queue
        if (recommendation.getQueue() != null) {
            List<BookingQueueItemResponse> queueItems = recommendation.getQueue().stream()
                .map(this::convertBookingQueueItemToResponse)
                .collect(Collectors.toList());
            response.setQueue(queueItems);
        }
        
        // Set other fields
        response.setEstimatedWaitTime(recommendation.getEstimatedWaitTime());
        response.setReason(recommendation.getReason());
        
        // Convert alternative bays
        if (recommendation.getAlternativeBays() != null) {
            List<BayResponse> alternativeBays = recommendation.getAlternativeBays().stream()
                .map(bay -> BayResponse.builder()
                    .bayId(bay.getBayId())
                    .bayName(bay.getBayName())
                    .bayCode(bay.getBayCode())
                    .status(bay.getStatus().name())
                    .allowBooking(bay.isBookingAllowed())
                    .build())
                .collect(Collectors.toList());
            response.setAlternativeBays(alternativeBays);
        }
        
        return response;
    }
    
    private BookingQueueItemResponse convertBookingQueueItemToResponse(BayRecommendationService.BookingQueueItem item) {
        return BookingQueueItemResponse.builder()
            .bookingId(item.getBookingId())
            .bookingCode("WALK-IN-" + item.getBookingId().toString().substring(0, 8))
            .customerName(item.getCustomerName())
            .customerPhone(item.getCustomerPhone())
            .vehicleLicensePlate(item.getVehicleLicensePlate())
            .serviceType(item.getServiceType())
            .queuePosition(item.getQueuePosition())
            .estimatedStartTime(item.getEstimatedStartTime())
            .estimatedCompletionTime(item.getEstimatedCompletionTime())
            .status(item.getStatus())
            .build();
    }

    private BookingQueueItemResponse convertBookingToResponse(Booking booking) {
        // Tính queue position dựa trên số booking trước scheduledStartAt
        int queuePosition = calculateQueuePosition(booking);
        
        return BookingQueueItemResponse.builder()
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .customerName(booking.getCustomerName())
            .customerPhone(booking.getCustomerPhone())
            .vehicleLicensePlate(booking.getVehicleLicensePlate())
            .serviceType("GENERAL") // Default service type
            .queuePosition(queuePosition)
            .estimatedStartTime(booking.getScheduledStartAt())
            .estimatedCompletionTime(booking.getScheduledEndAt())
            .status(booking.getStatus().name())
            .bookingServiceNames(getBookingServiceNames(booking))
            .bookingTotalPrice(booking.getTotalPrice())
            .bookingCustomerName(booking.getCustomerName())
            .bookingVehicleLicensePlate(booking.getVehicleLicensePlate())
            .build();
    }
    
    /**
     * Tính queue position dựa trên số booking trước scheduledStartAt
     */
    private int calculateQueuePosition(Booking booking) {
        if (booking.getServiceBay() == null || booking.getScheduledStartAt() == null) {
            return 1;
        }
        
        LocalDate date = booking.getScheduledStartAt().toLocalDate();
        List<Booking> previousBookings = bookingService.findWalkInBookingsByBayAndDate(
            booking.getServiceBay().getBayId(), date);
        
        long count = previousBookings.stream()
            .filter(b -> b.getScheduledStartAt() != null && 
                        b.getScheduledStartAt().isBefore(booking.getScheduledStartAt()))
            .count();
        
        return (int) count + 1;
    }
    
    /**
     * Lấy danh sách tên dịch vụ từ booking
     */
    private java.util.List<String> getBookingServiceNames(Booking booking) {
        if (booking.getBookingItems() != null && !booking.getBookingItems().isEmpty()) {
            return booking.getBookingItems().stream()
                .map(item -> item.getServiceName() != null ? item.getServiceName() : "Dịch vụ")
                .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Arrays.asList("Dịch vụ");
    }
    
}