package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.dashboard.*;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.entity.VehicleProfile;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import com.kltn.scsms_api_service.core.repository.BookingRepository;
import com.kltn.scsms_api_service.core.repository.SalesOrderRepository;
import com.kltn.scsms_api_service.core.repository.UserRepository;
import com.kltn.scsms_api_service.core.repository.VehicleProfileRepository;
import com.kltn.scsms_api_service.core.repository.VehicleBrandRepository;
import com.kltn.scsms_api_service.core.repository.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for dashboard statistics and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

  private final BookingRepository bookingRepository;
  private final SalesOrderRepository salesOrderRepository;
  private final UserRepository userRepository;
  private final VehicleProfileRepository vehicleProfileRepository;
  private final VehicleBrandRepository vehicleBrandRepository;
  private final VehicleModelRepository vehicleModelRepository;

  /**
   * Get overall dashboard statistics
   */
  public DashboardStatsDto getDashboardStats() {
    log.info("Getting dashboard statistics");

    // Get current counts
    Long totalCustomers = userRepository.count();
    Long totalVehicles = vehicleProfileRepository.count();
    Long totalBookings = bookingRepository.count();

    // Calculate total revenue from completed bookings
    BigDecimal totalRevenue = calculateTotalRevenue();

    // Calculate monthly growth
    MonthlyGrowthDto monthlyGrowth = calculateMonthlyGrowth();

    return DashboardStatsDto.builder()
        .totalCustomers(totalCustomers)
        .totalVehicles(totalVehicles)
        .totalBookings(totalBookings)
        .totalRevenue(totalRevenue)
        .monthlyGrowth(monthlyGrowth)
        .build();
  }

  /**
   * Get booking status statistics
   */
  public BookingStatusStatsDto getBookingStatusStats() {
    log.info("Getting booking status statistics");

    return BookingStatusStatsDto.builder()
        .pending(bookingRepository.countByStatus(Booking.BookingStatus.PENDING))
        .confirmed(bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED))
        .checkedIn(bookingRepository.countByStatus(Booking.BookingStatus.CHECKED_IN))
        .inProgress(bookingRepository.countByStatus(Booking.BookingStatus.IN_PROGRESS))
        .completed(bookingRepository.countByStatus(Booking.BookingStatus.COMPLETED))
        .cancelled(bookingRepository.countByStatus(Booking.BookingStatus.CANCELLED))
        .noShow(bookingRepository.countByStatus(Booking.BookingStatus.NO_SHOW))
        .build();
  }

  /**
   * Get recent activities
   */
  public List<RecentActivityDto> getRecentActivities(int limit) {
    log.info("Getting recent activities, limit: {}", limit);

    List<RecentActivityDto> activities = new ArrayList<>();

    // Get recent bookings
    List<Booking> recentBookings = bookingRepository.findTop10ByOrderByCreatedDateDesc();

    for (Booking booking : recentBookings) {
      if (activities.size() >= limit)
        break;

      String description = String.format("Booking %s - %s",
          booking.getBookingCode(),
          booking.getCustomer().getFullName());

      activities.add(RecentActivityDto.builder()
          .type("BOOKING")
          .description(description)
          .status(booking.getStatus().name())
          .timestamp(booking.getCreatedDate())
          .build());
    }

    return activities;
  }

  /**
   * Get upcoming bookings
   */
  public List<UpcomingBookingDto> getUpcomingBookings(int limit) {
    log.info("Getting upcoming bookings, limit: {}", limit);

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endOfDay = now.plusDays(7); // Get bookings for next 7 days

    List<Booking> upcomingBookings = bookingRepository
        .findUpcomingBookings(now, endOfDay, limit);

    return upcomingBookings.stream()
        .map(booking -> {
          String vehicleInfo = getVehicleInfo(booking);

          String serviceNames = booking.getBookingItems().stream()
              .map(BookingItem::getServiceName)
              .collect(Collectors.joining(", "));

          return UpcomingBookingDto.builder()
              .bookingId(booking.getBookingId())
              .bookingCode(booking.getBookingCode())
              .customerName(booking.getCustomer().getFullName())
              .vehicleInfo(vehicleInfo)
              .service(serviceNames)
              .scheduledTime(booking.getScheduledStartAt())
              .status(booking.getStatus().name())
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * Helper method to get vehicle info from booking
   */
  private String getVehicleInfo(Booking booking) {
    // First try to get from snapshot fields
    if (booking.getVehicleBrandName() != null && booking.getVehicleModelName() != null) {
      return String.format("%s %s (%s)",
          booking.getVehicleBrandName(),
          booking.getVehicleModelName(),
          booking.getVehicleLicensePlate());
    }

    // If snapshot is null, get from vehicle profile relationships
    VehicleProfile vehicle = booking.getVehicle();
    if (vehicle != null) {
      String brandName = vehicleBrandRepository.findById(vehicle.getVehicleBrandId())
          .map(brand -> brand.getBrandName())
          .orElse("Unknown");

      String modelName = vehicleModelRepository.findById(vehicle.getVehicleModelId())
          .map(model -> model.getModelName())
          .orElse("Unknown");

      return String.format("%s %s (%s)",
          brandName,
          modelName,
          vehicle.getLicensePlate());
    }

    // Fallback if no vehicle info available
    return "N/A (" + booking.getVehicleLicensePlate() + ")";
  }

  /**
   * Get revenue statistics
   */
  public RevenueStatsDto getRevenueStats() {
    log.info("Getting revenue statistics");

    LocalDate now = LocalDate.now();
    LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
    LocalDateTime startOfWeek = now.minusDays(7).atStartOfDay();
    LocalDateTime startOfDay = now.atStartOfDay();

    BigDecimal totalRevenue = calculateTotalRevenue();
    BigDecimal monthlyRevenue = calculateRevenueInPeriod(startOfMonth, LocalDateTime.now());
    BigDecimal weeklyRevenue = calculateRevenueInPeriod(startOfWeek, LocalDateTime.now());
    BigDecimal dailyRevenue = calculateRevenueInPeriod(startOfDay, LocalDateTime.now());

    // Get monthly revenue data for chart (last 12 months)
    List<MonthlyRevenueDto> monthlyRevenueData = calculateMonthlyRevenueData(12);

    return RevenueStatsDto.builder()
        .totalRevenue(totalRevenue)
        .monthlyRevenue(monthlyRevenue)
        .weeklyRevenue(weeklyRevenue)
        .dailyRevenue(dailyRevenue)
        .monthlyRevenueData(monthlyRevenueData)
        .build();
  }

  /**
   * Calculate total revenue from all completed bookings and fulfilled sales
   * orders
   */
  private BigDecimal calculateTotalRevenue() {
    // Revenue from completed bookings
    List<Booking> completedBookings = bookingRepository
        .findByStatus(Booking.BookingStatus.COMPLETED);

    BigDecimal bookingRevenue = completedBookings.stream()
        .map(Booking::getTotalPrice)
        .filter(java.util.Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Revenue from fulfilled and confirmed sales orders
    List<SalesOrder> salesOrders = salesOrderRepository.findAll();
    BigDecimal salesRevenue = salesOrders.stream()
        .filter(so -> so.getStatus() == SalesStatus.FULFILLED || so.getStatus() == SalesStatus.CONFIRMED)
        .map(SalesOrder::getFinalAmount)
        .filter(java.util.Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return bookingRevenue.add(salesRevenue);
  }

  /**
   * Calculate revenue in a specific period from both bookings and sales orders
   */
  private BigDecimal calculateRevenueInPeriod(LocalDateTime start, LocalDateTime end) {
    // Revenue from completed bookings
    List<Booking> bookings = bookingRepository
        .findCompletedBookingsBetween(start, end);

    BigDecimal bookingRevenue = bookings.stream()
        .map(Booking::getTotalPrice)
        .filter(java.util.Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Revenue from sales orders in period
    List<SalesOrder> salesOrders = salesOrderRepository.findByCreatedDateBetween(start, end);
    BigDecimal salesRevenue = salesOrders.stream()
        .filter(so -> so.getStatus() == SalesStatus.FULFILLED || so.getStatus() == SalesStatus.CONFIRMED)
        .map(SalesOrder::getFinalAmount)
        .filter(java.util.Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return bookingRevenue.add(salesRevenue);
  }

  /**
   * Calculate monthly growth percentages
   */
  private MonthlyGrowthDto calculateMonthlyGrowth() {
    LocalDate now = LocalDate.now();
    LocalDate startOfCurrentMonth = now.withDayOfMonth(1);
    LocalDate startOfLastMonth = startOfCurrentMonth.minusMonths(1);

    // Get counts for current and last month
    long currentMonthCustomers = userRepository.countCreatedAfter(startOfCurrentMonth.atStartOfDay());
    long lastMonthCustomers = userRepository.countCreatedBetween(
        startOfLastMonth.atStartOfDay(),
        startOfCurrentMonth.atStartOfDay());

    long currentMonthVehicles = vehicleProfileRepository.countCreatedAfter(startOfCurrentMonth.atStartOfDay());
    long lastMonthVehicles = vehicleProfileRepository.countCreatedBetween(
        startOfLastMonth.atStartOfDay(),
        startOfCurrentMonth.atStartOfDay());

    long currentMonthBookings = bookingRepository.countCreatedAfter(startOfCurrentMonth.atStartOfDay());
    long lastMonthBookings = bookingRepository.countCreatedBetween(
        startOfLastMonth.atStartOfDay(),
        startOfCurrentMonth.atStartOfDay());

    BigDecimal currentMonthRevenue = calculateRevenueInPeriod(
        startOfCurrentMonth.atStartOfDay(),
        LocalDateTime.now());
    BigDecimal lastMonthRevenue = calculateRevenueInPeriod(
        startOfLastMonth.atStartOfDay(),
        startOfCurrentMonth.atStartOfDay());

    return MonthlyGrowthDto.builder()
        .customers(calculateGrowthPercentage(lastMonthCustomers, currentMonthCustomers))
        .vehicles(calculateGrowthPercentage(lastMonthVehicles, currentMonthVehicles))
        .bookings(calculateGrowthPercentage(lastMonthBookings, currentMonthBookings))
        .revenue(calculateGrowthPercentage(
            lastMonthRevenue.doubleValue(),
            currentMonthRevenue.doubleValue()))
        .build();
  }

  /**
   * Calculate growth percentage
   */
  private Double calculateGrowthPercentage(long oldValue, long newValue) {
    if (oldValue == 0) {
      return newValue > 0 ? 100.0 : 0.0;
    }
    return ((double) (newValue - oldValue) / oldValue) * 100;
  }

  private Double calculateGrowthPercentage(double oldValue, double newValue) {
    if (oldValue == 0) {
      return newValue > 0 ? 100.0 : 0.0;
    }
    return ((newValue - oldValue) / oldValue) * 100;
  }

  /**
   * Calculate monthly revenue data for the last N months
   */
  private List<MonthlyRevenueDto> calculateMonthlyRevenueData(int months) {
    List<MonthlyRevenueDto> data = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

    for (int i = months - 1; i >= 0; i--) {
      YearMonth yearMonth = YearMonth.now().minusMonths(i);
      LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
      LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

      BigDecimal revenue = calculateRevenueInPeriod(startOfMonth, endOfMonth);
      Long orderCount = bookingRepository.countCompletedBookingsBetween(startOfMonth, endOfMonth);

      data.add(MonthlyRevenueDto.builder()
          .month(yearMonth.format(formatter))
          .revenue(revenue)
          .orderCount(orderCount)
          .build());
    }

    return data;
  }
}
