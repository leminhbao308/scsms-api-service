package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.bookingSchedule.AvailableTimeRangesResponse;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.BookingTimeRangeService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Schedule Management", description = "APIs for managing booking schedules and time ranges")
public class BookingScheduleController {
    
    private final BookingTimeRangeService bookingTimeRangeService;
    
    /**
     * Lấy các khoảng thời gian trống cho booking (API mới)
     * Backend chỉ trả về time ranges, frontend tự xử lý hiển thị
     */
    @GetMapping(ApiConstant.GET_AVAILABLE_TIME_RANGES_API)
    @Operation(summary = "Get available time ranges", 
               description = "Retrieve available time ranges for booking. Frontend will handle displaying time slots from these ranges.")
    @SwaggerOperation(summary = "Get available time ranges")
    public ResponseEntity<ApiResponse<AvailableTimeRangesResponse>> getAvailableTimeRanges(
            @Parameter(description = "Bay ID", required = true) @RequestParam UUID bayId,
            @Parameter(description = "Booking date", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting available time ranges for bay: {} on date: {}", bayId, date);
        
        AvailableTimeRangesResponse response = bookingTimeRangeService.getAvailableTimeRanges(bayId, date);
        
        return ResponseBuilder.success(response);
    }
    
}
