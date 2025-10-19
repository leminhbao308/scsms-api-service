package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.BaySchedule;
import com.kltn.scsms_api_service.core.service.businessService.BookingScheduleService;
import com.kltn.scsms_api_service.core.service.entityService.BayScheduleService;
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
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedule-management")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule Management", description = "APIs for managing monthly schedules and slot generation")
public class ScheduleManagementController {
    
    private final BookingScheduleService bookingScheduleService;
    private final DailyScheduleGenerator dailyScheduleGenerator;
    private final BayScheduleService bayScheduleService;
    
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
        
        return ResponseBuilder.success("Daily schedule generated successfully for branch: " + branchId + " on date: " + date);
    }
    
    /**
     * Tạo lịch monthly cho tất cả chi nhánh (manual trigger)
     */
    @PostMapping("/generate-monthly")
    @Operation(summary = "Generate monthly schedule", description = "Generate monthly schedule for all branches")
    @SwaggerOperation(summary = "Generate monthly schedule")
    public ResponseEntity<ApiResponse<Void>> generateMonthlySchedule() {
        
        log.info("Manually triggering monthly schedule generation");
        
        dailyScheduleGenerator.generateSchedule();
        
        return ResponseBuilder.success("Monthly schedule generated successfully for all branches");
    }
    
    /**
     * Kiểm tra slot có tồn tại cho bay và ngày
     */
    @GetMapping("/check-slots")
    @Operation(summary = "Check slots existence", description = "Check if slots exist for a bay on specific date")
    @SwaggerOperation(summary = "Check slots existence")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSlotsExistence(
            @Parameter(description = "Bay ID") @RequestParam UUID bayId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Checking slots existence for bay: {} on date: {}", bayId, date);
        
        boolean hasSlots = bayScheduleService.hasSlotsForDate(bayId, date);
        
        Map<String, Object> result = new HashMap<>();
        result.put("bayId", bayId);
        result.put("date", date);
        result.put("hasSlots", hasSlots);
        result.put("message", hasSlots ? "Slots exist for this date" : "No slots found for this date");
        
        return ResponseBuilder.success(result);
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
    
    /**
     * Debug slot availability - kiểm tra chi tiết trạng thái slot
     */
    @GetMapping("/debug-slot")
    @Operation(summary = "Debug slot availability", description = "Debug detailed slot status for troubleshooting")
    @SwaggerOperation(summary = "Debug slot availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugSlotAvailability(
            @Parameter(description = "Bay ID") @RequestParam UUID bayId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Start Time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime) {
        
        log.info("Debugging slot availability for bay: {} on date: {} at time: {}", bayId, date, startTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("bayId", bayId);
        result.put("date", date);
        result.put("startTime", startTime);
        
        try {
            // Check if slot exists
            boolean slotExists = bayScheduleService.getSlot(bayId, date, startTime) != null;
            result.put("slotExists", slotExists);
            
            // Check if slot is available
            boolean isAvailable = bayScheduleService.isSlotAvailable(bayId, date, startTime);
            result.put("isAvailable", isAvailable);
            
            // Get all slots for this bay and date
            List<BaySchedule> allSlots = bayScheduleService.getBaySchedules(bayId, date);
            result.put("totalSlots", allSlots.size());
            
            // Get available slots
            List<BaySchedule> availableSlots = bayScheduleService.getAvailableSlots(bayId, date);
            result.put("availableSlots", availableSlots.size());
            
            // Get slot details if exists
            if (slotExists) {
                BaySchedule slot = bayScheduleService.getSlot(bayId, date, startTime);
                result.put("slotStatus", slot.getStatus().toString());
                result.put("slotId", slot.getScheduleId());
                result.put("isActive", slot.getIsActive());
                result.put("isDeleted", slot.getIsDeleted());
                if (slot.getBooking() != null) {
                    result.put("bookingId", slot.getBooking().getBookingId());
                    result.put("bookingCode", slot.getBooking().getBookingCode());
                }
            }
            
            result.put("message", "Debug information retrieved successfully");
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("message", "Error occurred while debugging slot");
        }
        
        return ResponseBuilder.success(result);
    }
    
    /**
     * Reset slot status to AVAILABLE (for debugging)
     */
    @PostMapping("/reset-slot")
    @Operation(summary = "Reset slot status", description = "Reset slot status to AVAILABLE for debugging purposes")
    @SwaggerOperation(summary = "Reset slot status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetSlotStatus(
            @Parameter(description = "Bay ID") @RequestParam UUID bayId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Start Time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime) {
        
        log.info("Resetting slot status for bay: {} on date: {} at time: {}", bayId, date, startTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("bayId", bayId);
        result.put("date", date);
        result.put("startTime", startTime);
        
        try {
            BaySchedule slot = bayScheduleService.getSlot(bayId, date, startTime);
            String oldStatus = slot.getStatus().toString();
            
            // Reset to AVAILABLE
            slot.setStatus(BaySchedule.ScheduleStatus.AVAILABLE);
            slot.setBooking(null);
            bayScheduleService.save(slot);
            
            result.put("oldStatus", oldStatus);
            result.put("newStatus", "AVAILABLE");
            result.put("message", "Slot status reset successfully");
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("message", "Error occurred while resetting slot status");
        }
        
        return ResponseBuilder.success(result);
    }
    
    /**
     * Lấy thông tin cấu hình scheduling hiện tại
     */
    @GetMapping("/config")
    @Operation(summary = "Get scheduling configuration", description = "Get current scheduling configuration")
    @SwaggerOperation(summary = "Get scheduling configuration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSchedulingConfig() {
        
        log.info("Getting current scheduling configuration");
        
        Map<String, Object> config = new HashMap<>();
        config.put("scheduleMode", dailyScheduleGenerator.getScheduleMode());
        config.put("monthlyType", dailyScheduleGenerator.getMonthlyType());
        config.put("archiveDays", dailyScheduleGenerator.getArchiveDays());
        config.put("message", "Current scheduling configuration retrieved successfully");
        
        return ResponseBuilder.success(config);
    }
}
