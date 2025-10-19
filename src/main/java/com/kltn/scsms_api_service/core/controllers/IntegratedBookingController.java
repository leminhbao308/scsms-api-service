package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingWithSlotRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.IntegratedBookingService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller cho API tích hợp booking với slot
 */
@RestController
@RequestMapping("/integrated-booking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Integrated Booking Management", description = "APIs for creating bookings with slot selection in one call")
public class IntegratedBookingController {
    
    private final IntegratedBookingService integratedBookingService;
    
    /**
     * Tạo booking hoàn chỉnh với slot trong một API call
     */
    @PostMapping("/create-with-slot")
    @Operation(summary = "Create booking with slot", 
               description = "Create a complete booking with customer info, vehicle info, services, and slot selection in one API call")
    @SwaggerOperation(summary = "Create booking with slot")
    public ResponseEntity<ApiResponse<BookingInfoDto>> createBookingWithSlot(
            @Parameter(description = "Complete booking creation request with slot selection") 
            @Valid @RequestBody CreateBookingWithSlotRequest request) {
        
        log.info("Creating integrated booking for customer: {} at branch: {} with slot: {}",
            request.getCustomerName(), request.getBranchId(), request.getSelectedSlot());
        
        BookingInfoDto booking = integratedBookingService.createBookingWithSlot(request);
        
        return ResponseBuilder.created(booking);
    }
    
    /**
     * Tạo booking với slot và tự động confirm
     */
    @PostMapping("/create-and-confirm")
    @Operation(summary = "Create and confirm booking with slot", 
               description = "Create a booking with slot and automatically confirm it")
    @SwaggerOperation(summary = "Create and confirm booking with slot")
    public ResponseEntity<ApiResponse<BookingInfoDto>> createAndConfirmBookingWithSlot(
            @Parameter(description = "Complete booking creation request with slot selection") 
            @Valid @RequestBody CreateBookingWithSlotRequest request) {
        
        log.info("Creating and confirming integrated booking for customer: {} at branch: {} with slot: {}",
            request.getCustomerName(), request.getBranchId(), request.getSelectedSlot());
        
        // Create booking
        BookingInfoDto booking = integratedBookingService.createBookingWithSlot(request);
        
        // Auto confirm
        // TODO: Implement auto confirmation logic
        
        return ResponseBuilder.created(booking);
    }
}
