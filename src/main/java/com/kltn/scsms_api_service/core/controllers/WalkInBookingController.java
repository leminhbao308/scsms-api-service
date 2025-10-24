package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.service.businessService.BayRecommendationService;
import com.kltn.scsms_api_service.core.service.entityService.BayQueueService;
import com.kltn.scsms_api_service.core.service.businessService.WalkInBookingService;
import com.kltn.scsms_api_service.core.dto.walkInBooking.*;
import com.kltn.scsms_api_service.core.entity.BayQueue;
import com.kltn.scsms_api_service.core.entity.Booking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import java.util.UUID;

/**
 * Controller cho Walk-in Booking
 * Cung cấp API endpoints cho việc đặt lịch walk-in
 */
@Slf4j
@RestController
@RequestMapping("/walk-in")
@RequiredArgsConstructor
@Tag(name = "Walk-in Booking", description = "API cho walk-in booking")
public class WalkInBookingController {
    
    private final BayRecommendationService bayRecommendationService;
    private final BayQueueService bayQueueService;
    private final WalkInBookingService walkInBookingService;
    
    /**
     * Đề xuất bay tốt nhất cho walk-in booking
     */
    @PostMapping("/recommend-bay")
    @Operation(summary = "Đề xuất bay", description = "Đề xuất bay tốt nhất cho walk-in booking")
    public ResponseEntity<BayRecommendationResponse> recommendBay(
            @RequestBody BayRecommendationRequest request) {
        try {
            log.info("Recommending bay for walk-in booking: branchId={}, serviceDuration={} minutes", 
                request.getBranchId(), request.getServiceDurationMinutes());
            
            // Gọi service để đề xuất bay
            BayRecommendationService.BayRecommendation recommendation = bayRecommendationService.recommendBay(convertToServiceRequest(request));
            
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
    @PostMapping("/create-booking")
    @Operation(summary = "Tạo walk-in booking", description = "Tạo booking cho khách hàng walk-in")
    public ResponseEntity<String> createWalkInBooking(
            @RequestBody WalkInBookingRequest request) {
        try {
            log.info("Creating walk-in booking: customerName={}, bayId={}", 
                request.getCustomerName(), request.getAssignedBayId());
            
            // Gọi service để tạo booking
            walkInBookingService.createWalkInBooking(request);
            
            return ResponseEntity.ok("Walk-in booking created successfully");
                
        } catch (Exception e) {
            log.error("Error creating walk-in booking: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to create walk-in booking: " + e.getMessage());
        }
    }
    
    /**
     * Lấy thông tin hàng chờ của một bay
     */
    @GetMapping("/bay-queue/{bayId}")
    @Operation(summary = "Lấy hàng chờ bay", description = "Lấy thông tin hàng chờ của một bay")
    public ResponseEntity<List<BookingQueueItemResponse>> getBayQueue(@PathVariable UUID bayId) {
        try {
            log.info("Getting queue for bay: {}", bayId);

            // Lấy thông tin hàng chờ
            List<BayQueue> bayQueues = bayQueueService.getBayQueue(bayId);
            
            // Convert to response DTOs
            List<BookingQueueItemResponse> queueItems = bayQueues.stream()
                .map(this::convertBayQueueToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(queueItems);

        } catch (Exception e) {
            log.error("Error getting bay queue: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Chuyển booking từ bay này sang bay khác
     */
    @PostMapping("/transfer-booking")
    @Operation(summary = "Chuyển booking", description = "Chuyển booking từ bay này sang bay khác")
    public ResponseEntity<String> transferBooking(
            @RequestBody TransferBookingRequest request) {
        try {
            log.info("Transferring booking {} from bay {} to bay {}", 
                request.getBookingId(), request.getFromBayId(), request.getToBayId());
            
            // Gọi service để chuyển booking
            bayQueueService.transferBooking(request.getFromBayId(), request.getToBayId(), request.getBookingId());
            
            return ResponseEntity.ok("Booking transferred successfully");
                
        } catch (Exception e) {
            log.error("Error transferring booking: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to transfer booking: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private BayRecommendationService.BayRecommendationRequest convertToServiceRequest(BayRecommendationRequest request) {
        BayRecommendationService.BayRecommendationRequest serviceRequest = new BayRecommendationService.BayRecommendationRequest();
        serviceRequest.setBranchId(request.getBranchId());
        serviceRequest.setServiceType(request.getServiceType());
        serviceRequest.setServiceDurationMinutes(request.getServiceDurationMinutes());
        serviceRequest.setPriority(request.getPriority());
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

    private BookingQueueItemResponse convertBayQueueToResponse(BayQueue bayQueue) {
        // Lấy thông tin booking từ bayQueue
        Booking booking = bayQueue.getBooking();
        
        return BookingQueueItemResponse.builder()
            .bookingId(bayQueue.getBookingId())
            .bookingCode(booking != null ? booking.getBookingCode() : "N/A")
            .customerName(booking != null ? booking.getCustomerName() : "N/A")
            .customerPhone(booking != null ? booking.getCustomerPhone() : "N/A")
            .vehicleLicensePlate(booking != null ? booking.getVehicleLicensePlate() : "N/A")
            .serviceType("GENERAL") // Default service type
            .queuePosition(bayQueue.getQueuePosition())
            .estimatedStartTime(bayQueue.getEstimatedStartTime())
            .estimatedCompletionTime(bayQueue.getEstimatedCompletionTime())
            .status(booking != null ? booking.getStatus().name() : "UNKNOWN")
            .bookingServiceNames(booking != null ? getBookingServiceNames(booking) : java.util.Arrays.asList("Dịch vụ"))
            .bookingTotalPrice(booking != null ? booking.getTotalPrice() : java.math.BigDecimal.ZERO)
            .bookingCustomerName(booking != null ? booking.getCustomerName() : "N/A")
            .bookingVehicleLicensePlate(booking != null ? booking.getVehicleLicensePlate() : "N/A")
            .build();
    }
    
    /**
     * Lấy danh sách tên dịch vụ từ booking
     */
    private java.util.List<String> getBookingServiceNames(Booking booking) {
        if (booking.getBookingItems() != null && !booking.getBookingItems().isEmpty()) {
            return booking.getBookingItems().stream()
                .map(item -> item.getItemName() != null ? item.getItemName() : "Dịch vụ")
                .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Arrays.asList("Dịch vụ");
    }
    
}