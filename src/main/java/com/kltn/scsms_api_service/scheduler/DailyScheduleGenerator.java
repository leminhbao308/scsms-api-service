package com.kltn.scsms_api_service.scheduler;

import com.kltn.scsms_api_service.core.service.businessService.BookingScheduleService;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Scheduler để tự động tạo lịch hàng ngày cho tất cả chi nhánh
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyScheduleGenerator {
    
    private final BookingScheduleService bookingScheduleService;
    private final BranchService branchService;
    
    /**
     * Tự động tạo lịch cho ngày hôm sau vào 23:00 mỗi ngày
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void generateNextDaySchedule() {
        log.info("Starting daily schedule generation for next day");
        
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            
            // Lấy tất cả chi nhánh active
            // Note: Cần implement method findAllActiveBranches trong BranchService
            // List<Branch> activeBranches = branchService.findAllActiveBranches();
            
            // Tạm thời hardcode hoặc comment out cho đến khi có method
            // for (Branch branch : activeBranches) {
            //     log.info("Generating schedule for branch: {} on date: {}", branch.getBranchName(), tomorrow);
            //     bookingScheduleService.generateBranchDailySchedule(branch.getBranchId(), tomorrow);
            // }
            
            log.info("Daily schedule generation completed for date: {}", tomorrow);
            
        } catch (Exception e) {
            log.error("Error generating daily schedule: ", e);
        }
    }
    
    /**
     * Tạo lịch cho ngày cụ thể (manual trigger)
     */
    public void generateScheduleForDate(UUID branchId, LocalDate date) {
        log.info("Manually generating schedule for branch: {} on date: {}", branchId, date);
        
        try {
            bookingScheduleService.generateBranchDailySchedule(branchId, date);
            log.info("Successfully generated schedule for branch: {} on date: {}", branchId, date);
        } catch (Exception e) {
            log.error("Error generating schedule for branch: {} on date: {}: ", branchId, date, e);
            throw e;
        }
    }
    
    /**
     * Tạo lịch cho tuần tới (manual trigger)
     */
    public void generateNextWeekSchedule(UUID branchId) {
        log.info("Generating schedule for next week for branch: {}", branchId);
        
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 7; i++) {
            LocalDate date = today.plusDays(i);
            generateScheduleForDate(branchId, date);
        }
        
        log.info("Successfully generated schedule for next week for branch: {}", branchId);
    }
}
