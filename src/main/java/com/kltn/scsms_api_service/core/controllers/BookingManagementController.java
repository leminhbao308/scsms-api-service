package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.param.BookingFilterParam;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.UpdateBookingRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.service.businessService.BookingManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Management", description = "APIs for managing bookings")
public class BookingManagementController {
    
    private final BookingManagementService bookingManagementService;
    
    @GetMapping("/bookings")
    @Operation(summary = "Get all bookings", description = "Retrieve all bookings with optional filtering and pagination")
    @SwaggerOperation(summary = "Get all bookings")
    public ResponseEntity<ApiResponse<Object>> getAllBookings(
            @Parameter(description = "Filter parameters") BookingFilterParam filterParam) {
        log.info("Getting all bookings with filter: {}", filterParam);
        
        if (filterParam != null && filterParam.getPage() >= 0 && filterParam.getSize() > 0) {
            // Return paginated results
            Page<BookingInfoDto> bookingPage = bookingManagementService.getAllBookings(filterParam);
            return ResponseBuilder.success(bookingPage);
        } else {
            // Return all results
            List<BookingInfoDto> bookings = bookingManagementService.getAllBookings();
            return ResponseBuilder.success(bookings);
        }
    }
    
    @GetMapping("/bookings/{bookingId}")
    @Operation(summary = "Get booking by ID", description = "Retrieve a specific booking by its ID")
    @SwaggerOperation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingInfoDto>> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        log.info("Getting booking by ID: {}", bookingId);
        BookingInfoDto booking = bookingManagementService.getBookingById(bookingId);
        return ResponseBuilder.success(booking);
    }
    
    @GetMapping("/bookings/code/{bookingCode}")
    @Operation(summary = "Get booking by code", description = "Retrieve a specific booking by its code")
    @SwaggerOperation(summary = "Get booking by code")
    public ResponseEntity<ApiResponse<BookingInfoDto>> getBookingByCode(
            @Parameter(description = "Booking code") @PathVariable String bookingCode) {
        log.info("Getting booking by code: {}", bookingCode);
        BookingInfoDto booking = bookingManagementService.getBookingByCode(bookingCode);
        return ResponseBuilder.success(booking);
    }
    
    @GetMapping("/customers/{customerId}/bookings")
    @Operation(summary = "Get bookings by customer", description = "Retrieve all bookings for a specific customer")
    @SwaggerOperation(summary = "Get bookings by customer")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsByCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId) {
        log.info("Getting bookings for customer: {}", customerId);
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsByCustomer(customerId);
        return ResponseBuilder.success(bookings);
    }
    
    @GetMapping("/customers/{customerId}/bookings/upcoming")
    @Operation(summary = "Get upcoming bookings by customer", description = "Retrieve upcoming bookings for a specific customer")
    @SwaggerOperation(summary = "Get upcoming bookings by customer")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getUpcomingBookingsByCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId) {
        log.info("Getting upcoming bookings for customer: {}", customerId);
        List<BookingInfoDto> bookings = bookingManagementService.getUpcomingBookingsByCustomer(customerId);
        return ResponseBuilder.success(bookings);
    }
    
    @GetMapping("/customers/{customerId}/bookings/past")
    @Operation(summary = "Get past bookings by customer", description = "Retrieve past bookings for a specific customer")
    @SwaggerOperation(summary = "Get past bookings by customer")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getPastBookingsByCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId) {
        log.info("Getting past bookings for customer: {}", customerId);
        List<BookingInfoDto> bookings = bookingManagementService.getPastBookingsByCustomer(customerId);
        return ResponseBuilder.success(bookings);
    }
    
    @GetMapping("/branches/{branchId}/bookings")
    @Operation(summary = "Get bookings by branch", description = "Retrieve all bookings for a specific branch")
    @SwaggerOperation(summary = "Get bookings by branch")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsByBranch(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId) {
        log.info("Getting bookings for branch: {}", branchId);
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsByBranch(branchId);
        return ResponseBuilder.success(bookings);
    }
    
    @GetMapping("/branches/{branchId}/bookings/date/{bookingDate}")
    @Operation(summary = "Get bookings by branch and date", description = "Retrieve all bookings for a specific branch and date")
    @SwaggerOperation(summary = "Get bookings by branch and date")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsByBranchAndDate(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Booking date") @PathVariable LocalDate bookingDate) {
        log.info("Getting bookings for branch: {} on date: {}", branchId, bookingDate);
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsByBranchAndDate(branchId, bookingDate);
        return ResponseBuilder.success(bookings);
    }
    
    @GetMapping("/bookings/status/{status}")
    @Operation(summary = "Get bookings by status", description = "Retrieve all bookings with a specific status")
    @SwaggerOperation(summary = "Get bookings by status")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsByStatus(
            @Parameter(description = "Booking status") @PathVariable Booking.BookingStatus status) {
        log.info("Getting bookings with status: {}", status);
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsByStatus(status);
        return ResponseBuilder.success(bookings);
    }
    
    @GetMapping("/bookings/search/customer-name")
    @Operation(summary = "Search bookings by customer name", description = "Search bookings by customer name")
    @SwaggerOperation(summary = "Search bookings by customer name")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> searchBookingsByCustomerName(
            @Parameter(description = "Customer name") @RequestParam String customerName) {
        log.info("Searching bookings by customer name: {}", customerName);
        List<BookingInfoDto> bookings = bookingManagementService.searchBookingsByCustomerName(customerName);
        return ResponseBuilder.success(bookings);
    }
    
    @GetMapping("/bookings/search/phone")
    @Operation(summary = "Search bookings by phone number", description = "Search bookings by customer phone number")
    @SwaggerOperation(summary = "Search bookings by phone number")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> searchBookingsByPhone(
            @Parameter(description = "Phone number") @RequestParam String phoneNumber) {
        log.info("Searching bookings by phone: {}", phoneNumber);
        List<BookingInfoDto> bookings = bookingManagementService.searchBookingsByPhone(phoneNumber);
        return ResponseBuilder.success(bookings);
    }
    
    @GetMapping("/bookings/search/license-plate")
    @Operation(summary = "Search bookings by license plate", description = "Search bookings by vehicle license plate")
    @SwaggerOperation(summary = "Search bookings by license plate")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> searchBookingsByLicensePlate(
            @Parameter(description = "License plate") @RequestParam String licensePlate) {
        log.info("Searching bookings by license plate: {}", licensePlate);
        List<BookingInfoDto> bookings = bookingManagementService.searchBookingsByLicensePlate(licensePlate);
        return ResponseBuilder.success(bookings);
    }
    
    @GetMapping("/branches/{branchId}/bookings/statistics/date/{date}")
    @Operation(summary = "Get booking statistics", description = "Get booking statistics for a specific branch and date")
    @SwaggerOperation(summary = "Get booking statistics")
    public ResponseEntity<ApiResponse<BookingManagementService.BookingStatisticsDto>> getBookingStatistics(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Date") @PathVariable LocalDate date) {
        log.info("Getting booking statistics for branch: {} on date: {}", branchId, date);
        BookingManagementService.BookingStatisticsDto statistics = bookingManagementService.getBookingStatistics(branchId, date);
        return ResponseBuilder.success(statistics);
    }
    
    @PostMapping("/bookings/create")
    @Operation(summary = "Create booking", description = "Create a new booking")
    @SwaggerOperation(summary = "Create booking")
    public ResponseEntity<ApiResponse<BookingInfoDto>> createBooking(
            @Parameter(description = "Booking creation request") @Valid @RequestBody CreateBookingRequest request) {
        log.info("Creating booking for customer: {} at branch: {}", request.getCustomerName(), request.getBranchId());
        BookingInfoDto booking = bookingManagementService.createBooking(request);
        return ResponseBuilder.created(booking);
    }
    
    @PostMapping("/bookings/{bookingId}/update")
    @Operation(summary = "Update booking", description = "Update an existing booking")
    @SwaggerOperation(summary = "Update booking")
    public ResponseEntity<ApiResponse<BookingInfoDto>> updateBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId,
            @Parameter(description = "Booking update request") @Valid @RequestBody UpdateBookingRequest request) {
        log.info("Updating booking: {}", bookingId);
        BookingInfoDto booking = bookingManagementService.updateBooking(bookingId, request);
        return ResponseBuilder.success(booking);
    }
    
    @PostMapping("/bookings/{bookingId}/delete")
    @Operation(summary = "Delete booking", description = "Delete a booking (soft delete)")
    @SwaggerOperation(summary = "Delete booking")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        log.info("Deleting booking: {}", bookingId);
        bookingManagementService.deleteBooking(bookingId);
        return ResponseBuilder.success("Booking deleted successfully");
    }
    
    @PostMapping("/bookings/{bookingId}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancel a booking")
    @SwaggerOperation(summary = "Cancel booking")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId,
            @Parameter(description = "Cancellation details") @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        String cancelledBy = request.get("cancelledBy");
        log.info("Cancelling booking: {} with reason: {}", bookingId, reason);
        bookingManagementService.cancelBooking(bookingId, reason, cancelledBy);
        return ResponseBuilder.success("Booking cancelled successfully");
    }
    
    @PostMapping("/bookings/{bookingId}/check-in")
    @Operation(summary = "Check-in booking", description = "Check-in a booking")
    @SwaggerOperation(summary = "Check-in booking")
    public ResponseEntity<ApiResponse<Void>> checkInBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        log.info("Checking in booking: {}", bookingId);
        bookingManagementService.checkInBooking(bookingId);
        return ResponseBuilder.success("Booking checked in successfully");
    }
    
    @PostMapping("/bookings/{bookingId}/start")
    @Operation(summary = "Start service", description = "Start service for a booking")
    @SwaggerOperation(summary = "Start service")
    public ResponseEntity<ApiResponse<Void>> startService(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        log.info("Starting service for booking: {}", bookingId);
        bookingManagementService.startService(bookingId);
        return ResponseBuilder.success("Service started successfully");
    }
    
    @PostMapping("/bookings/{bookingId}/complete")
    @Operation(summary = "Complete service", description = "Complete service for a booking")
    @SwaggerOperation(summary = "Complete service")
    public ResponseEntity<ApiResponse<Void>> completeService(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        log.info("Completing service for booking: {}", bookingId);
        bookingManagementService.completeService(bookingId);
        return ResponseBuilder.success("Service completed successfully");
    }
}
