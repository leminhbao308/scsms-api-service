package com.kltn.scsms_api_service.scheduler;

import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.service.businessService.BookingScheduleService;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.BayScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Scheduler để tự động tạo lịch hàng ngày cho tất cả chi nhánh
 * Sử dụng Rolling Window approach để luôn duy trì 30 ngày slot available
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyScheduleGenerator {

    private final BookingScheduleService bookingScheduleService;
    private final BranchService branchService;
    private final BayScheduleService bayScheduleService;
    
    @Value("${app.schedule.mode:MONTHLY}")
    private String scheduleMode;
    
    @Value("${app.schedule.monthly-type:CURRENT_AND_NEXT}")
    private String monthlyType;
    
    @Value("${app.schedule.archive-days:30}")
    private int archiveDays;
    
    // Getter methods for configuration access
    public String getScheduleMode() {
        return scheduleMode;
    }
    
    public String getMonthlyType() {
        return monthlyType;
    }
    
    public int getArchiveDays() {
        return archiveDays;
    }
    
    /**
     * Tạo lịch khi application start
     */
    @EventListener(ApplicationReadyEvent.class)
    public void generateScheduleOnStartup() {
        log.info("Application started - generating schedule with mode: {}", scheduleMode);
        generateSchedule();
    }
    
    /**
     * Tự động tạo lịch vào 23:00 mỗi ngày
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void generateScheduleScheduled() {
        log.info("Scheduled task: generating schedule with mode: {}", scheduleMode);
        generateSchedule();
    }
    
    /**
     * Archive slot cũ vào 2:00 sáng hàng ngày
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveOldSlots() {
        log.info("Scheduled task: archiving old slots");
        try {
            bayScheduleService.archiveOldBookedSlots(archiveDays);
            log.info("Completed archiving old slots older than {} days", archiveDays);
        } catch (Exception e) {
            log.error("Failed to archive old slots", e);
        }
    }
    
    /**
     * Tạo lịch theo mode được cấu hình (public method cho manual trigger)
     */
    public void generateSchedule() {
        if ("MONTHLY".equalsIgnoreCase(scheduleMode)) {
            generateMonthlySchedule();
        } else {
            generateRollingWindowSchedule();
        }
    }
    
    /**
     * Tạo lịch theo tháng
     */
    private void generateMonthlySchedule() {
        log.info("Generating monthly schedule with type: {}", monthlyType);
        
        try {
            List<Branch> activeBranches = branchService.findAllActiveBranches();
            LocalDate today = LocalDate.now();
            
            for (Branch branch : activeBranches) {
                try {
                    List<LocalDate> datesToGenerate = getMonthlyDates(today);
                    
                    for (LocalDate date : datesToGenerate) {
                        bookingScheduleService.generateBranchDailySchedule(branch.getBranchId(), date);
                        log.debug("Generated schedule for branch: {} on date: {}", 
                            branch.getBranchName(), date);
                    }
                    
                    log.info("Generated monthly schedule for branch: {} for {} days", 
                        branch.getBranchName(), datesToGenerate.size());
                } catch (Exception e) {
                    log.error("Failed to generate monthly schedule for branch: {}", 
                        branch.getBranchName(), e);
                }
            }
            
            log.info("Completed generating monthly schedule with type: {}", monthlyType);
        } catch (Exception e) {
            log.error("Failed to generate monthly schedule", e);
        }
    }
    
    /**
     * Lấy danh sách ngày cần tạo slot theo monthly type
     */
    private List<LocalDate> getMonthlyDates(LocalDate today) {
        List<LocalDate> dates = new ArrayList<>();
        
        switch (monthlyType.toUpperCase()) {
            case "CURRENT_ONLY":
                // Chỉ tháng hiện tại
                LocalDate startOfCurrentMonth = today.withDayOfMonth(1);
                LocalDate endOfCurrentMonth = today.withDayOfMonth(today.lengthOfMonth());
                
                for (LocalDate date = startOfCurrentMonth; !date.isAfter(endOfCurrentMonth); date = date.plusDays(1)) {
                    if (!date.isBefore(today)) { // Chỉ tạo từ hôm nay trở đi
                        dates.add(date);
                    }
                }
                break;
                
            case "NEXT_30_DAYS":
                // 30 ngày từ đầu tháng hiện tại
                LocalDate startOfMonth = today.withDayOfMonth(1);
                LocalDate endDate = startOfMonth.plusDays(30);
                
                for (LocalDate date = startOfMonth; !date.isAfter(endDate); date = date.plusDays(1)) {
                    if (!date.isBefore(today)) { // Chỉ tạo từ hôm nay trở đi
                        dates.add(date);
                    }
                }
                break;
                
            case "CURRENT_AND_NEXT":
            default:
                // Tháng hiện tại + tháng sau
                LocalDate startOfCurrent = today.withDayOfMonth(1);
                LocalDate endOfNextMonth = today.plusMonths(1).withDayOfMonth(
                    today.plusMonths(1).lengthOfMonth()
                );
                
                for (LocalDate date = startOfCurrent; !date.isAfter(endOfNextMonth); date = date.plusDays(1)) {
                    if (!date.isBefore(today)) { // Chỉ tạo từ hôm nay trở đi
                        dates.add(date);
                    }
                }
                break;
        }
        
        return dates;
    }
    
    /**
     * Tạo lịch cho rolling window (từ hôm nay đến +30 ngày) - fallback
     */
    private void generateRollingWindowSchedule() {
        log.info("Generating rolling window schedule (fallback mode)");
        
        try {
            List<Branch> activeBranches = branchService.findAllActiveBranches();
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(30);
            
            for (Branch branch : activeBranches) {
                try {
                    for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
                        bookingScheduleService.generateBranchDailySchedule(branch.getBranchId(), date);
                        log.debug("Generated schedule for branch: {} on date: {}", 
                            branch.getBranchName(), date);
                    }
                    log.info("Generated rolling window schedule for branch: {} from {} to {}", 
                        branch.getBranchName(), today, endDate);
                } catch (Exception e) {
                    log.error("Failed to generate rolling window schedule for branch: {}", 
                        branch.getBranchName(), e);
                }
            }
            
            log.info("Completed generating rolling window schedule");
        } catch (Exception e) {
            log.error("Failed to generate rolling window schedule", e);
        }
    }
    
    /**
     * Tạo lịch cho một ngày cụ thể cho tất cả chi nhánh (manual trigger)
     */
    public void generateScheduleForDate(LocalDate date) {
        log.info("Manually generating schedule for date: {}", date);
        
        try {
            List<Branch> activeBranches = branchService.findAllActiveBranches();
            
            for (Branch branch : activeBranches) {
                try {
                    bookingScheduleService.generateBranchDailySchedule(branch.getBranchId(), date);
                    log.info("Generated schedule for branch: {} on date: {}", 
                        branch.getBranchName(), date);
                } catch (Exception e) {
                    log.error("Failed to generate schedule for branch: {} on date: {}", 
                        branch.getBranchName(), date, e);
                }
            }
            
            log.info("Completed generating schedule for date: {}", date);
        } catch (Exception e) {
            log.error("Failed to generate schedule for date: {}", date, e);
        }
    }
    
    /**
     * Tạo lịch cho một chi nhánh cụ thể cho một ngày (manual trigger)
     */
    public void generateScheduleForBranchAndDate(UUID branchId, LocalDate date) {
        log.info("Manually generating schedule for branch: {} on date: {}", branchId, date);
        
        try {
            bookingScheduleService.generateBranchDailySchedule(branchId, date);
            log.info("Successfully generated schedule for branch: {} on date: {}", branchId, date);
        } catch (Exception e) {
            log.error("Error generating schedule for branch: {} on date: {}: ", branchId, date, e);
            throw e;
        }
    }
}