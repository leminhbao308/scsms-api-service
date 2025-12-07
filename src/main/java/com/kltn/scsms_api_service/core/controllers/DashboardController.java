package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.dashboard.*;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.DashboardService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for dashboard statistics and analytics
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "APIs for dashboard statistics and analytics")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping(ApiConstant.GET_DASHBOARD_STATS_API)
  @Operation(summary = "Get dashboard statistics", description = "Retrieve overall dashboard statistics including totals and growth")
  @SwaggerOperation(summary = "Get dashboard statistics")
  public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
    log.info("Getting dashboard statistics");
    DashboardStatsDto stats = dashboardService.getDashboardStats();
    return ResponseBuilder.success(stats);
  }

  @GetMapping(ApiConstant.GET_BOOKING_STATUS_STATS_API)
  @Operation(summary = "Get booking status statistics", description = "Retrieve booking counts by status")
  @SwaggerOperation(summary = "Get booking status statistics")
  public ResponseEntity<ApiResponse<BookingStatusStatsDto>> getBookingStatusStats() {
    log.info("Getting booking status statistics");
    BookingStatusStatsDto stats = dashboardService.getBookingStatusStats();
    return ResponseBuilder.success(stats);
  }

  @GetMapping(ApiConstant.GET_RECENT_ACTIVITIES_API)
  @Operation(summary = "Get recent activities", description = "Retrieve recent activities in the system")
  @SwaggerOperation(summary = "Get recent activities")
  public ResponseEntity<ApiResponse<List<RecentActivityDto>>> getRecentActivities(
      @Parameter(description = "Maximum number of activities to return") @RequestParam(defaultValue = "10") int limit) {
    log.info("Getting recent activities, limit: {}", limit);
    List<RecentActivityDto> activities = dashboardService.getRecentActivities(limit);
    return ResponseBuilder.success(activities);
  }

  @GetMapping(ApiConstant.GET_UPCOMING_BOOKINGS_API)
  @Operation(summary = "Get upcoming bookings", description = "Retrieve upcoming bookings for the next 7 days")
  @SwaggerOperation(summary = "Get upcoming bookings")
  public ResponseEntity<ApiResponse<List<UpcomingBookingDto>>> getUpcomingBookings(
      @Parameter(description = "Maximum number of bookings to return") @RequestParam(defaultValue = "10") int limit) {
    log.info("Getting upcoming bookings, limit: {}", limit);
    List<UpcomingBookingDto> bookings = dashboardService.getUpcomingBookings(limit);
    return ResponseBuilder.success(bookings);
  }

  @GetMapping(ApiConstant.GET_REVENUE_STATS_API)
  @Operation(summary = "Get revenue statistics", description = "Retrieve revenue statistics including daily, weekly, monthly and historical data")
  @SwaggerOperation(summary = "Get revenue statistics")
  public ResponseEntity<ApiResponse<RevenueStatsDto>> getRevenueStats() {
    log.info("Getting revenue statistics");
    RevenueStatsDto stats = dashboardService.getRevenueStats();
    return ResponseBuilder.success(stats);
  }
}
