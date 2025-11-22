
package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.param.BookingFilterParam;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.ChangeScheduleRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingWithScheduleRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.UpdateBookingRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.service.businessService.BookingManagementService;
import com.kltn.scsms_api_service.core.service.businessService.BookingWorkflowService;
import com.kltn.scsms_api_service.core.service.businessService.IntegratedBookingService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
    private final BookingWorkflowService bookingWorkflowService;
    private final IntegratedBookingService integratedBookingService;

    @GetMapping(ApiConstant.GET_ALL_BOOKINGS_API)
    @Operation(summary = "Get all bookings", description = "Retrieve all bookings with optional filtering and pagination")
    @SwaggerOperation(summary = "Get all bookings")
    public ResponseEntity<ApiResponse<Object>> getAllBookings(
            @Parameter(description = "Filter parameters") BookingFilterParam filterParam) {

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

    @GetMapping(ApiConstant.GET_BOOKING_BY_ID_API)
    @Operation(summary = "Get booking by ID", description = "Retrieve a specific booking by its ID")
    @SwaggerOperation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingInfoDto>> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        BookingInfoDto booking = bookingManagementService.getBookingById(bookingId);
        return ResponseBuilder.success(booking);
    }

    @GetMapping(ApiConstant.GET_BOOKINGS_BY_CUSTOMER_API)
    @Operation(summary = "Get bookings by customer", description = "Retrieve all bookings for a specific customer")
    @SwaggerOperation(summary = "Get bookings by customer")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsByCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId) {
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsByCustomer(customerId);
        return ResponseBuilder.success(bookings);
    }

    @GetMapping(ApiConstant.GET_BOOKINGS_BY_BRANCH_API)
    @Operation(summary = "Get bookings by branch", description = "Retrieve all bookings for a specific branch")
    @SwaggerOperation(summary = "Get bookings by branch")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsByBranch(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId) {
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsByBranch(branchId);
        return ResponseBuilder.success(bookings);
    }

    @GetMapping(ApiConstant.GET_BOOKINGS_BY_BRANCH_AND_DATE_API)
    @Operation(summary = "Get bookings by branch and date", description = "Retrieve all bookings for a specific branch and date")
    @SwaggerOperation(summary = "Get bookings by branch and date")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsByBranchAndDate(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Booking date") @PathVariable LocalDate bookingDate) {
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsByBranchAndDate(branchId, bookingDate);
        return ResponseBuilder.success(bookings);
    }

    @GetMapping(ApiConstant.GET_BOOKINGS_BY_STATUS_API)
    @Operation(summary = "Get bookings by status", description = "Retrieve all bookings with a specific status")
    @SwaggerOperation(summary = "Get bookings by status")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsByStatus(
            @Parameter(description = "Booking status") @PathVariable Booking.BookingStatus status) {
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsByStatus(status);
        return ResponseBuilder.success(bookings);
    }

    @GetMapping(ApiConstant.GET_BOOKINGS_FOR_MANAGEMENT_API)
    @Operation(summary = "Get bookings for management", description = "Retrieve all bookings with statuses: CHECKED_IN, IN_PROGRESS, CANCELLED, COMPLETED for vehicle care management. Sorted by scheduledStartAt DESC")
    @SwaggerOperation(summary = "Get bookings for management")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsForManagement() {
        List<Booking.BookingStatus> statuses = List.of(
                Booking.BookingStatus.CHECKED_IN,
                Booking.BookingStatus.IN_PROGRESS,
                Booking.BookingStatus.CANCELLED,
                Booking.BookingStatus.COMPLETED);
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsByStatuses(statuses);
        return ResponseBuilder.success(bookings);
    }

    @GetMapping(ApiConstant.GET_BOOKING_STATISTICS_API)
    @Operation(summary = "Get booking statistics", description = "Get booking statistics for a specific branch and date")
    @SwaggerOperation(summary = "Get booking statistics")
    public ResponseEntity<ApiResponse<BookingManagementService.BookingStatisticsDto>> getBookingStatistics(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Date") @PathVariable LocalDate date) {
        BookingManagementService.BookingStatisticsDto statistics = bookingManagementService
                .getBookingStatistics(branchId, date);
        return ResponseBuilder.success(statistics);
    }

    /**
     * Tạo scheduled booking hoàn chỉnh với scheduling information trong một API
     * call
     * Tự động set bookingType = SCHEDULED
     */
    @PostMapping(ApiConstant.CREATE_BOOKING_WITH_SCHEDULE_API)
    @Operation(summary = "Create scheduled booking with schedule", description = "Create a complete scheduled booking with customer info, vehicle info, services, and scheduling information (bay, date, time) in one API call. Automatically sets bookingType = SCHEDULED")
    @SwaggerOperation(summary = "Create scheduled booking with schedule")
    public ResponseEntity<ApiResponse<BookingInfoDto>> createBookingWithSchedule(
            @Parameter(description = "Complete booking creation request with scheduling information") @Valid @RequestBody CreateBookingWithScheduleRequest request) {

        log.info("Creating scheduled booking for customer: {} at branch: {} with schedule: {}",
                request.getCustomerName(), request.getBranchId(), request.getSelectedSchedule());

        BookingInfoDto booking = integratedBookingService.createBookingWithSlot(request);

        return ResponseBuilder.created(booking);
    }

    @PostMapping(ApiConstant.UPDATE_BOOKING_API)
    @Operation(summary = "Update booking", description = "Update an existing booking")
    @SwaggerOperation(summary = "Update booking")
    public ResponseEntity<ApiResponse<BookingInfoDto>> updateBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId,
            @Parameter(description = "Booking update request") @RequestBody UpdateBookingRequest request) {
        BookingInfoDto booking = bookingManagementService.updateBooking(bookingId, request);
        return ResponseBuilder.success(booking);
    }

    @PostMapping(ApiConstant.CHANGE_BOOKING_SCHEDULE_API)
    @Operation(summary = "Change booking schedule", description = "Change the schedule (bay, date, time) of an existing booking")
    @SwaggerOperation(summary = "Change booking schedule")
    public ResponseEntity<ApiResponse<BookingInfoDto>> changeBookingSlot(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId,
            @Parameter(description = "Change schedule request") @RequestBody ChangeScheduleRequest request) {
        BookingInfoDto booking = bookingManagementService.changeBookingSlot(bookingId, request);
        return ResponseBuilder.success(booking);
    }

    @PostMapping(ApiConstant.DELETE_BOOKING_API)
    @Operation(summary = "Delete booking", description = "Delete a booking (soft delete)")
    @SwaggerOperation(summary = "Delete booking")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        bookingManagementService.deleteBooking(bookingId);
        return ResponseBuilder.success("Booking deleted successfully");
    }

    @PostMapping(ApiConstant.CANCEL_BOOKING_API)
    @Operation(summary = "Cancel booking", description = "Cancel a booking")
    @SwaggerOperation(summary = "Cancel booking")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId,
            @Parameter(description = "Cancellation details") @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        String cancelledBy = request.get("cancelledBy");
        bookingWorkflowService.cancelBooking(bookingId, reason, cancelledBy);
        return ResponseBuilder.success("Booking cancelled successfully");
    }

    @PostMapping(ApiConstant.CONFIRM_BOOKING_API)
    @Operation(summary = "Confirm booking", description = "Confirm a pending booking")
    @SwaggerOperation(summary = "Confirm booking")
    public ResponseEntity<ApiResponse<Void>> confirmBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        bookingWorkflowService.confirmBooking(bookingId);
        return ResponseBuilder.success("Booking confirmed successfully");
    }

    @PostMapping(ApiConstant.CHECK_IN_BOOKING_API)
    @Operation(summary = "Check-in booking", description = "Check-in a booking")
    @SwaggerOperation(summary = "Check-in booking")
    public ResponseEntity<ApiResponse<Void>> checkInBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        bookingWorkflowService.checkInBooking(bookingId);
        return ResponseBuilder.success("Booking checked in successfully");
    }

    @PostMapping(ApiConstant.START_BOOKING_SERVICE_API)
    @Operation(summary = "Start service", description = "Start service for a booking")
    @SwaggerOperation(summary = "Start service")
    public ResponseEntity<ApiResponse<Void>> startService(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        bookingWorkflowService.startService(bookingId);
        return ResponseBuilder.success("Service started successfully");
    }

    @PostMapping(ApiConstant.COMPLETE_BOOKING_SERVICE_API)
    @Operation(summary = "Complete service", description = "Complete service for a booking")
    @SwaggerOperation(summary = "Complete service")
    public ResponseEntity<ApiResponse<Void>> completeService(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        bookingWorkflowService.completeService(bookingId);
        return ResponseBuilder.success("Service completed successfully");
    }

    @GetMapping(ApiConstant.GET_BOOKINGS_PENDING_PAYMENT_API)
    @Operation(summary = "Get bookings with pending payment", description = "Retrieve all bookings with pending payment status and completed status")
    @SwaggerOperation(summary = "Get bookings with pending payment")
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBookingsWithPendingPayment() {
        List<BookingInfoDto> bookings = bookingManagementService.getBookingsWithPendingPayment();
        return ResponseBuilder.success(bookings);
    }

    @PostMapping(ApiConstant.MARK_BOOKING_AS_PAID_API)
    @Operation(summary = "Mark booking as paid", description = "Update booking payment status to PAID")
    @SwaggerOperation(summary = "Mark booking as paid")
    public ResponseEntity<ApiResponse<BookingInfoDto>> markBookingAsPaid(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId,
            @Parameter(description = "Payment details") @RequestBody(required = false) Map<String, String> paymentDetails) {
        BookingInfoDto booking = bookingManagementService.markBookingAsPaid(bookingId, paymentDetails);
        return ResponseBuilder.success("Booking marked as paid successfully", booking);
    }
}
