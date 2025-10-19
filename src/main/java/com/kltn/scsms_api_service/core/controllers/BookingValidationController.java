package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.entityService.BayScheduleService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller cho validation booking (giới hạn ngày đặt lịch)
 */
@RestController
@RequestMapping("/api/booking-validation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Validation", description = "APIs for validating booking constraints")
public class BookingValidationController {

    private final BayScheduleService bayScheduleService;
    
    @Value("${app.schedule.mode:MONTHLY}")
    private String scheduleMode;
    
    @Value("${app.schedule.monthly-type:CURRENT_AND_NEXT}")
    private String monthlyType;
    
    @Value("${app.schedule.max-advance-booking-days:30}")
    private int maxAdvanceBookingDays;

    /**
     * Kiểm tra ngày có thể đặt lịch không (giới hạn trong tháng)
     */
    @GetMapping("/check-date")
    @Operation(summary = "Check if date is available for booking", 
               description = "Validates if a specific date is within the allowed booking range (within 1 month)")
    @SwaggerOperation(summary = "Check booking date availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkBookingDate(
            @Parameter(description = "Date to check for booking availability") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Checking booking date availability for: {}", date);
        
        boolean isAvailable = bayScheduleService.isDateAvailableForBooking(date);
        LocalDate today = LocalDate.now();
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("isAvailable", isAvailable);
        result.put("today", today);
        result.put("scheduleMode", scheduleMode);
        result.put("monthlyType", monthlyType);
        
        // Add range information based on mode
        if ("MONTHLY".equalsIgnoreCase(scheduleMode)) {
            addMonthlyRangeInfo(result, today);
        } else {
            LocalDate maxDate = today.plusDays(maxAdvanceBookingDays);
            result.put("maxBookingDate", maxDate);
            result.put("maxAdvanceDays", maxAdvanceBookingDays);
        }
        
        result.put("message", isAvailable ? 
            "Date is available for booking" : 
            "Date is outside allowed booking range");
        
        return ResponseBuilder.success(result);
    }

    /**
     * Lấy thông tin giới hạn đặt lịch
     */
    @GetMapping("/booking-limits")
    @Operation(summary = "Get booking date limits", 
               description = "Returns the current booking date constraints")
    @SwaggerOperation(summary = "Get booking limits")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingLimits() {
        log.info("Getting booking date limits");
        
        LocalDate today = LocalDate.now();
        
        Map<String, Object> limits = new HashMap<>();
        limits.put("today", today);
        limits.put("scheduleMode", scheduleMode);
        limits.put("monthlyType", monthlyType);
        
        // Add range information based on mode
        if ("MONTHLY".equalsIgnoreCase(scheduleMode)) {
            addMonthlyRangeInfo(limits, today);
        } else {
            LocalDate maxDate = today.plusDays(maxAdvanceBookingDays);
            limits.put("maxBookingDate", maxDate);
            limits.put("maxAdvanceDays", maxAdvanceBookingDays);
            limits.put("message", String.format("You can book from %s to %s (max %d days in advance)", today, maxDate, maxAdvanceBookingDays));
        }
        
        return ResponseBuilder.success(limits);
    }

    /**
     * Validate ngày đặt lịch (throw exception nếu không hợp lệ)
     */
    @PostMapping("/validate-date")
    @Operation(summary = "Validate booking date", 
               description = "Validates a booking date and throws error if invalid")
    @SwaggerOperation(summary = "Validate booking date")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateBookingDate(
            @Parameter(description = "Date to validate") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Validating booking date: {}", date);
        
        // Validate date (will throw exception if invalid)
        bayScheduleService.validateBookingDate(date);
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("isValid", true);
        result.put("message", "Date is valid for booking");
        
        return ResponseBuilder.success(result);
    }
    
    /**
     * Thêm thông tin phạm vi monthly vào result
     */
    private void addMonthlyRangeInfo(Map<String, Object> result, LocalDate today) {
        switch (monthlyType.toUpperCase()) {
            case "CURRENT_ONLY":
                LocalDate endOfCurrentMonth = today.withDayOfMonth(today.lengthOfMonth());
                result.put("startDate", today);
                result.put("endDate", endOfCurrentMonth);
                result.put("message", String.format("You can book from %s to end of current month (%s)", today, endOfCurrentMonth));
                break;
                
            case "NEXT_30_DAYS":
                LocalDate startOfMonth = today.withDayOfMonth(1);
                LocalDate endDate = startOfMonth.plusDays(30);
                result.put("startDate", today);
                result.put("endDate", endDate);
                result.put("message", String.format("You can book from %s to %s (30 days from start of month)", today, endDate));
                break;
                
            case "CURRENT_AND_NEXT":
            default:
                LocalDate endOfNextMonth = today.plusMonths(1).withDayOfMonth(
                    today.plusMonths(1).lengthOfMonth()
                );
                result.put("startDate", today);
                result.put("endDate", endOfNextMonth);
                result.put("message", String.format("You can book from %s to end of next month (%s)", today, endOfNextMonth));
                break;
        }
    }
}
