package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.BookingScheduleService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.scheduler.DailyScheduleGenerator;
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
@RequestMapping("/api/schedule-management")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule Management", description = "APIs for managing daily schedules")
public class ScheduleManagementController {
    
    private final BookingScheduleService bookingScheduleService;
    private final DailyScheduleGenerator dailyScheduleGenerator;
    
    /**
     * Tạo lịch cho chi nhánh trong ngày cụ thể
     */
    @PostMapping("/generate-daily")
    @Operation(summary = "Generate daily schedule", description = "Generate daily schedule for a branch on specific date")
    @SwaggerOperation(summary = "Generate daily schedule")
    public ResponseEntity<ApiResponse<Void>> generateDailySchedule(
            @Parameter(description = "Branch ID") @RequestParam UUID branchId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Generating daily schedule for branch: {} on date: {}", branchId, date);
        
        bookingScheduleService.generateBranchDailySchedule(branchId, date);
        
        return ResponseBuilder.success("Daily schedule generated successfully");
    }
    
    /**
     * Tạo lịch cho tuần tới
     */
    @PostMapping("/generate-week")
    @Operation(summary = "Generate weekly schedule", description = "Generate schedule for next week")
    @SwaggerOperation(summary = "Generate weekly schedule")
    public ResponseEntity<ApiResponse<Void>> generateWeeklySchedule(
            @Parameter(description = "Branch ID") @RequestParam UUID branchId) {
        
        log.info("Generating weekly schedule for branch: {}", branchId);
        
        dailyScheduleGenerator.generateNextWeekSchedule(branchId);
        
        return ResponseBuilder.success("Weekly schedule generated successfully");
    }
    
    /**
     * Lấy thống kê slot của bay
     */
    @GetMapping("/bay-statistics")
    @Operation(summary = "Get bay statistics", description = "Get slot statistics for a bay on specific date")
    @SwaggerOperation(summary = "Get bay statistics")
    public ResponseEntity<ApiResponse<BookingScheduleService.BaySlotStatistics>> getBayStatistics(
            @Parameter(description = "Bay ID") @RequestParam UUID bayId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting bay statistics for bay: {} on date: {}", bayId, date);
        
        BookingScheduleService.BaySlotStatistics statistics = 
            bookingScheduleService.getBaySlotStatistics(bayId, date);
        
        return ResponseBuilder.success(statistics);
    }
}
