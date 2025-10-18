package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.bookingManagement.TimeSlotDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.AvailableSlotsRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.BookSlotRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CompleteEarlyRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.BookingScheduleService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking-schedule")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Schedule Management", description = "APIs for managing booking schedules and time slots")
public class BookingScheduleController {
    
    private final BookingScheduleService bookingScheduleService;
    
    /**
     * Lấy các slot trống cho booking
     */
    @GetMapping("/available-slots")
    @Operation(summary = "Get available time slots", description = "Retrieve available time slots for booking")
    @SwaggerOperation(summary = "Get available time slots")
    public ResponseEntity<ApiResponse<List<TimeSlotDto>>> getAvailableSlots(
            @Parameter(description = "Branch ID") @RequestParam UUID branchId,
            @Parameter(description = "Booking date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Service duration in minutes") @RequestParam Integer serviceDurationMinutes,
            @Parameter(description = "Specific bay ID (optional)") @RequestParam(required = false) UUID bayId,
            @Parameter(description = "From hour (optional)") @RequestParam(required = false) Integer fromHour,
            @Parameter(description = "To hour (optional)") @RequestParam(required = false) Integer toHour) {
        
        log.info("Getting available slots for branch: {} on date: {} with duration: {} minutes", 
            branchId, date, serviceDurationMinutes);
        
        AvailableSlotsRequest request = AvailableSlotsRequest.builder()
            .branchId(branchId)
            .date(date)
            .serviceDurationMinutes(serviceDurationMinutes)
            .bayId(bayId)
            .fromHour(fromHour)
            .toHour(toHour)
            .build();
        
        List<TimeSlotDto> slots = bookingScheduleService.findAvailableSlots(request);
        
        return ResponseBuilder.success(slots);
    }
    
    /**
     * Đặt slot cho booking
     */
    @PostMapping("/book-slot")
    @Operation(summary = "Book a time slot", description = "Book a specific time slot for a booking")
    @SwaggerOperation(summary = "Book a time slot")
    public ResponseEntity<ApiResponse<Void>> bookSlot(
            @Parameter(description = "Book slot request") @Valid @RequestBody BookSlotRequest request) {
        
        log.info("Booking slot for bay: {} at {} {} for booking: {}", 
            request.getBayId(), request.getDate(), request.getStartTime(), request.getBookingId());
        
        bookingScheduleService.bookSlot(request);
        
        return ResponseBuilder.success("Slot booked successfully");
    }
    
    /**
     * Hoàn thành dịch vụ sớm - mở slot trống
     */
    @PostMapping("/complete-early")
    @Operation(summary = "Complete service early", description = "Complete service early and release time slots")
    @SwaggerOperation(summary = "Complete service early")
    public ResponseEntity<ApiResponse<Void>> completeEarly(
            @Parameter(description = "Complete early request") @Valid @RequestBody CompleteEarlyRequest request) {
        
        log.info("Completing service early for booking: {} at {}", 
            request.getBookingId(), request.getActualCompletionTime());
        
        bookingScheduleService.completeEarlyAndReleaseSlots(request);
        
        return ResponseBuilder.success("Service completed early, slots released");
    }
    
    /**
     * Tạo lịch cho tất cả bay trong chi nhánh trong ngày
     */
    @PostMapping("/generate-daily-schedule")
    @Operation(summary = "Generate daily schedule", description = "Generate daily schedule for all bays in a branch")
    @SwaggerOperation(summary = "Generate daily schedule")
    public ResponseEntity<ApiResponse<Void>> generateDailySchedule(
            @Parameter(description = "Branch ID") @RequestParam UUID branchId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Generating daily schedule for branch: {} on date: {}", branchId, date);
        
        bookingScheduleService.generateBranchDailySchedule(branchId, date);
        
        return ResponseBuilder.success("Daily schedule generated successfully");
    }
    
    /**
     * Lấy thống kê slot của bay trong ngày
     */
    @GetMapping("/bay-statistics")
    @Operation(summary = "Get bay slot statistics", description = "Get slot statistics for a specific bay on a date")
    @SwaggerOperation(summary = "Get bay slot statistics")
    public ResponseEntity<ApiResponse<BookingScheduleService.BaySlotStatistics>> getBaySlotStatistics(
            @Parameter(description = "Bay ID") @RequestParam UUID bayId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting slot statistics for bay: {} on date: {}", bayId, date);
        
        BookingScheduleService.BaySlotStatistics statistics = 
            bookingScheduleService.getBaySlotStatistics(bayId, date);
        
        return ResponseBuilder.success(statistics);
    }
    
    /**
     * Lấy các slot có thể mở rộng (hoàn thành sớm)
     */
    @GetMapping("/expandable-slots")
    @Operation(summary = "Get expandable slots", description = "Get slots that can be expanded due to early completion")
    @SwaggerOperation(summary = "Get expandable slots")
    public ResponseEntity<ApiResponse<List<TimeSlotDto>>> getExpandableSlots(
            @Parameter(description = "Branch ID") @RequestParam UUID branchId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "From time (optional)") @RequestParam(required = false) String fromTime) {
        
        log.info("Getting expandable slots for branch: {} on date: {}", branchId, date);
        
        List<TimeSlotDto> expandableSlots = bookingScheduleService.getExpandableSlots(
            branchId, date, fromTime != null ? java.time.LocalTime.parse(fromTime) : null);
        
        return ResponseBuilder.success(expandableSlots);
    }
}
