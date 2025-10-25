package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.BayQueue;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.repository.BayQueueRepository;
import com.kltn.scsms_api_service.core.service.entityService.BayQueueService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service đề xuất bay tốt nhất cho walk-in booking
 * Sử dụng thuật toán tính điểm để chọn bay phù hợp nhất
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BayRecommendationService {
    
    private final ServiceBayService serviceBayService;
    private final BayQueueService bayQueueService;
    private final BayQueueRepository bayQueueRepository;
    
    /**
     * Đề xuất bay tốt nhất cho walk-in booking
     */
    public BayRecommendation recommendBay(BayRecommendationRequest request) {
        return recommendBay(request, LocalDate.now());
    }
    
    /**
     * Đề xuất bay tốt nhất cho walk-in booking trong ngày cụ thể
     */
    public BayRecommendation recommendBay(BayRecommendationRequest request, LocalDate queueDate) {
        log.info("Recommending bay for walk-in booking: branchId={}, serviceDuration={} minutes", 
            request.getBranchId(), request.getServiceDurationMinutes());
        
        // 1. Lấy danh sách bay khả dụng
        List<ServiceBay> availableBays = getAvailableBays(request.getBranchId());
        
        if (availableBays.isEmpty()) {
            throw new RuntimeException("Không có bay nào khả dụng trong chi nhánh này");
        }
        
        // 2. Tính điểm cho từng bay
        List<BayScore> bayScores = calculateBayScores(availableBays, request, queueDate);
        
        // 3. Sắp xếp theo điểm số (cao nhất trước)
        bayScores.sort(Comparator.comparing(BayScore::getScore).reversed());
        
        // 4. Lấy bay tốt nhất
        BayScore bestBayScore = bayScores.get(0);
        ServiceBay recommendedBay = bestBayScore.getBay();
        
        // 5. Lấy thông tin hàng chờ của bay được đề xuất trong ngày cụ thể
        List<BookingQueueItem> queue = getBayQueue(recommendedBay.getBayId(), queueDate);
        
        // 6. Tính thời gian chờ ước tính
        int estimatedWaitTime = calculateEstimatedWaitTime(queue, request.getServiceDurationMinutes());
        
        // 7. Lấy các bay thay thế (top 3)
        List<ServiceBay> alternativeBays = bayScores.subList(1, Math.min(4, bayScores.size()))
            .stream()
            .map(BayScore::getBay)
            .collect(Collectors.toList());
        
        log.info("Recommended bay: {} with score: {}", recommendedBay.getBayName(), bestBayScore.getScore());
        
        return BayRecommendation.builder()
            .recommendedBay(recommendedBay)
            .queue(queue)
            .estimatedWaitTime(estimatedWaitTime)
            .reason(bestBayScore.getReason())
            .alternativeBays(alternativeBays)
            .build();
    }
    
    /**
     * Lấy danh sách bay khả dụng trong chi nhánh
     */
    private List<ServiceBay> getAvailableBays(UUID branchId) {
        // Lấy tất cả bay trong chi nhánh
        List<ServiceBay> allBays = serviceBayService.findActiveBaysByBranch(branchId);
        
        // Lọc chỉ bay cho phép walk-in (allowBooking = false)
        return allBays.stream()
            .filter(bay -> !bay.isBookingAllowed()) // Bay cho walk-in
            .filter(ServiceBay::isActive) // Bay đang hoạt động
            .collect(Collectors.toList());
    }
    
    /**
     * Tính điểm cho từng bay
     */
    private List<BayScore> calculateBayScores(List<ServiceBay> bays, BayRecommendationRequest request) {
        return calculateBayScores(bays, request, LocalDate.now());
    }
    
    /**
     * Tính điểm cho từng bay trong ngày cụ thể
     */
    private List<BayScore> calculateBayScores(List<ServiceBay> bays, BayRecommendationRequest request, LocalDate queueDate) {
        return bays.stream()
            .map(bay -> {
                int score = 0;
                StringBuilder reason = new StringBuilder();
                
                // 1. Ưu tiên bay trống (100 điểm)
                if (isBayEmpty(bay.getBayId(), queueDate)) {
                    score += 100;
                    reason.append("Bay trống; ");
                }
                
                // 2. Ưu tiên bay có ít người chờ (10 điểm mỗi vị trí trống)
                int queueLength = getBayQueueLength(bay.getBayId(), queueDate);
                score += (10 - queueLength) * 10;
                reason.append("Hàng chờ: ").append(queueLength).append(" người; ");
                
                // 3. Ưu tiên bay phù hợp với loại dịch vụ (50 điểm)
                if (isServiceCompatible(bay, request.getServiceType())) {
                    score += 50;
                    reason.append("Phù hợp dịch vụ; ");
                }
                
                // 4. Ưu tiên bay có thời gian hoàn thành sớm (30 điểm)
                LocalDateTime estimatedCompletion = getEstimatedCompletionTime(bay.getBayId(), queueDate);
                if (estimatedCompletion != null) {
                    long minutesToComplete = java.time.Duration.between(LocalDateTime.now(), estimatedCompletion).toMinutes();
                    score += Math.max(0, 30 - (int) minutesToComplete);
                    reason.append("Hoàn thành sớm; ");
                }
                
                // 5. Ưu tiên bay có capacity cao (20 điểm)
                // if (bay.getCapacity() != null && bay.getCapacity() > 1) {
                //     score += 20;
                //     reason.append("Capacity cao; ");
                // }
                
                // 6. Ưu tiên bay VIP (30 điểm)
                // if (bay.getBayType() != null && bay.getBayType().equals("VIP")) {
                //     score += 30;
                //     reason.append("Bay VIP; ");
                // }
                
                return new BayScore(bay, score, reason.toString());
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Kiểm tra bay có trống không
     */
    private boolean isBayEmpty(UUID bayId) {
        return isBayEmpty(bayId, LocalDate.now());
    }
    
    /**
     * Kiểm tra bay có trống không trong ngày cụ thể
     */
    private boolean isBayEmpty(UUID bayId, LocalDate queueDate) {
        // Kiểm tra cả hàng chờ và booking đang được xử lý
        Long queueLength = bayQueueRepository.countActiveByBayIdAndDate(bayId, queueDate);
        
        // Kiểm tra booking đang được xử lý trong bay
        Long activeBookings = countActiveBookingsInBay(bayId, queueDate);
        
        return queueLength == 0 && activeBookings == 0;
    }
    
    /**
     * Lấy độ dài hàng chờ của bay
     */
    private int getBayQueueLength(UUID bayId) {
        return getBayQueueLength(bayId, LocalDate.now());
    }
    
    /**
     * Lấy độ dài hàng chờ của bay trong ngày cụ thể
     */
    private int getBayQueueLength(UUID bayId, LocalDate queueDate) {
        Long queueLength = bayQueueRepository.countActiveByBayIdAndDate(bayId, queueDate);
        return queueLength.intValue();
    }
    
    /**
     * Kiểm tra bay có phù hợp với loại dịch vụ không
     */
    private boolean isServiceCompatible(ServiceBay bay, String serviceType) {
        // Logic kiểm tra compatibility
        // Có thể dựa trên bay type, capacity, equipment, etc.
        if (serviceType == null) return true;
        
        // Ví dụ: Bay VIP phù hợp với dịch vụ cao cấp
        // if ("PREMIUM".equals(serviceType) && "VIP".equals(bay.getBayType())) {
        //     return true;
        // }
        
        // Bay thường phù hợp với dịch vụ cơ bản
        // if ("BASIC".equals(serviceType) && !"VIP".equals(bay.getBayType())) {
        //     return true;
        // }
        
        return true; // Default: tất cả bay đều phù hợp
    }
    
    /**
     * Lấy thời gian hoàn thành dự kiến của bay
     */
    private LocalDateTime getEstimatedCompletionTime(UUID bayId) {
        return getEstimatedCompletionTime(bayId, LocalDate.now());
    }
    
    /**
     * Lấy thời gian hoàn thành dự kiến của bay trong ngày cụ thể
     */
    private LocalDateTime getEstimatedCompletionTime(UUID bayId, LocalDate queueDate) {
        List<BayQueue> queues = bayQueueRepository.findActiveByBayIdAndDate(bayId, queueDate);
        if (queues.isEmpty()) {
            return null;
        }
        
        // Lấy thời gian hoàn thành của booking cuối cùng
        return queues.stream()
            .map(BayQueue::getEstimatedCompletionTime)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }
    
    /**
     * Lấy thông tin hàng chờ của bay
     */
    private List<BookingQueueItem> getBayQueue(UUID bayId) {
        return getBayQueue(bayId, LocalDate.now());
    }
    
    /**
     * Lấy thông tin hàng chờ của bay trong ngày cụ thể
     */
    private List<BookingQueueItem> getBayQueue(UUID bayId, LocalDate queueDate) {
        List<BayQueue> queues = bayQueueRepository.findActiveByBayIdAndDate(bayId, queueDate);
        
        return queues.stream()
            .map(this::convertToQueueItem)
            .collect(Collectors.toList());
    }
    
    /**
     * Chuyển đổi BayQueue thành BookingQueueItem
     */
    private BookingQueueItem convertToQueueItem(BayQueue queue) {
        // Lấy thông tin booking từ BayQueue relationship
        Booking booking = queue.getBooking();
        
        if (booking == null) {
            return BookingQueueItem.builder()
                .bookingId(queue.getBookingId())
                .bookingCode("N/A")
                .customerName("N/A")
                .customerPhone("N/A")
                .vehicleLicensePlate("N/A")
                .serviceType("N/A")
                .queuePosition(queue.getQueuePosition())
                .estimatedStartTime(queue.getEstimatedStartTime())
                .estimatedCompletionTime(queue.getEstimatedCompletionTime())
                .status("WAITING")
                .build();
        }
        
        return BookingQueueItem.builder()
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .customerName(booking.getCustomerName())
            .customerPhone(booking.getCustomerPhone())
            .vehicleLicensePlate(booking.getVehicleLicensePlate())
            .serviceType(getServiceType(booking))
            .queuePosition(queue.getQueuePosition())
            .estimatedStartTime(queue.getEstimatedStartTime())
            .estimatedCompletionTime(queue.getEstimatedCompletionTime())
            .status(booking.getStatus().name())
            .bookingServiceNames(getBookingServiceNames(booking))
            .bookingTotalPrice(booking.getTotalPrice())
            .bookingCustomerName(booking.getCustomerName())
            .bookingVehicleLicensePlate(booking.getVehicleLicensePlate())
            .build();
    }
    
    /**
     * Lấy loại dịch vụ từ booking
     */
    private String getServiceType(Booking booking) {
        if (booking.getBookingItems() != null && !booking.getBookingItems().isEmpty()) {
            // Lấy loại dịch vụ từ booking item đầu tiên
            // return booking.getBookingItems().get(0).getService() != null ? 
            //     booking.getBookingItems().get(0).getService().getServiceName() : "GENERAL";
            return "GENERAL";
        }
        return "GENERAL";
    }
    
    /**
     * Lấy danh sách tên dịch vụ từ booking
     */
    private java.util.List<String> getBookingServiceNames(Booking booking) {
        if (booking.getBookingItems() != null && !booking.getBookingItems().isEmpty()) {
            return booking.getBookingItems().stream()
                .map(item -> item.getItemName() != null ? item.getItemName() : "Dịch vụ")
                .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Arrays.asList("Dịch vụ");
    }
    
    /**
     * Tính thời gian chờ ước tính
     */
    private int calculateEstimatedWaitTime(List<BookingQueueItem> queue, int serviceDurationMinutes) {
        if (queue.isEmpty()) {
            return 0; // Không có hàng chờ
        }
        
        // Tính thời gian chờ dựa trên thời gian làm việc của bay
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime estimatedStartTime = currentTime;
        
        // Tính thời gian bắt đầu dự kiến dựa trên hàng chờ hiện tại
        for (BookingQueueItem item : queue) {
            if (item.getEstimatedCompletionTime() != null) {
                estimatedStartTime = item.getEstimatedCompletionTime();
            } else {
                // Nếu không có thời gian hoàn thành, tính dựa trên thời gian dịch vụ trung bình
                estimatedStartTime = estimatedStartTime.plusMinutes(60); // 60 phút mỗi booking
            }
        }
        
        // Tính thời gian chờ (phút)
        long waitMinutes = java.time.Duration.between(currentTime, estimatedStartTime).toMinutes();
        
        // Cho phép đặt qua thời gian kết thúc của bay (không giới hạn thời gian)
        return Math.max(0, (int) waitMinutes);
    }
    
    /**
     * DTO cho request
     */
    public static class BayRecommendationRequest {
        private UUID branchId;
        private String serviceType;
        private Integer serviceDurationMinutes;
        private String priority;
        
        // Getters and setters
        public UUID getBranchId() { return branchId; }
        public void setBranchId(UUID branchId) { this.branchId = branchId; }
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        public Integer getServiceDurationMinutes() { return serviceDurationMinutes; }
        public void setServiceDurationMinutes(Integer serviceDurationMinutes) { this.serviceDurationMinutes = serviceDurationMinutes; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
    
    /**
     * DTO cho response
     */
    public static class BayRecommendation {
        private ServiceBay recommendedBay;
        private List<BookingQueueItem> queue;
        private Integer estimatedWaitTime;
        private String reason;
        private List<ServiceBay> alternativeBays;
        
        // Builder pattern
        public static BayRecommendationBuilder builder() {
            return new BayRecommendationBuilder();
        }
        
        public static class BayRecommendationBuilder {
            private ServiceBay recommendedBay;
            private List<BookingQueueItem> queue;
            private Integer estimatedWaitTime;
            private String reason;
            private List<ServiceBay> alternativeBays;
            
            public BayRecommendationBuilder recommendedBay(ServiceBay recommendedBay) {
                this.recommendedBay = recommendedBay;
                return this;
            }
            
            public BayRecommendationBuilder queue(List<BookingQueueItem> queue) {
                this.queue = queue;
                return this;
            }
            
            public BayRecommendationBuilder estimatedWaitTime(Integer estimatedWaitTime) {
                this.estimatedWaitTime = estimatedWaitTime;
                return this;
            }
            
            public BayRecommendationBuilder reason(String reason) {
                this.reason = reason;
                return this;
            }
            
            public BayRecommendationBuilder alternativeBays(List<ServiceBay> alternativeBays) {
                this.alternativeBays = alternativeBays;
                return this;
            }
            
            public BayRecommendation build() {
                BayRecommendation recommendation = new BayRecommendation();
                recommendation.recommendedBay = this.recommendedBay;
                recommendation.queue = this.queue;
                recommendation.estimatedWaitTime = this.estimatedWaitTime;
                recommendation.reason = this.reason;
                recommendation.alternativeBays = this.alternativeBays;
                return recommendation;
            }
        }
        
        // Getters
        public ServiceBay getRecommendedBay() { return recommendedBay; }
        public List<BookingQueueItem> getQueue() { return queue; }
        public Integer getEstimatedWaitTime() { return estimatedWaitTime; }
        public String getReason() { return reason; }
        public List<ServiceBay> getAlternativeBays() { return alternativeBays; }
    }
    
    /**
     * DTO cho queue item
     */
    public static class BookingQueueItem {
        private UUID bookingId;
        private String bookingCode;
        private String customerName;
        private String customerPhone;
        private String vehicleLicensePlate;
        private String serviceType;
        private Integer queuePosition;
        private LocalDateTime estimatedStartTime;
        private LocalDateTime estimatedCompletionTime;
        private String status;
        private java.util.List<String> bookingServiceNames;
        private java.math.BigDecimal bookingTotalPrice;
        private String bookingCustomerName;
        private String bookingVehicleLicensePlate;
        
        // Builder pattern
        public static BookingQueueItemBuilder builder() {
            return new BookingQueueItemBuilder();
        }
        
        public static class BookingQueueItemBuilder {
            private UUID bookingId;
            private String bookingCode;
            private String customerName;
            private String customerPhone;
            private String vehicleLicensePlate;
            private String serviceType;
            private Integer queuePosition;
            private LocalDateTime estimatedStartTime;
            private LocalDateTime estimatedCompletionTime;
            private String status;
            private java.util.List<String> bookingServiceNames;
            private java.math.BigDecimal bookingTotalPrice;
            private String bookingCustomerName;
            private String bookingVehicleLicensePlate;
            
            public BookingQueueItemBuilder bookingId(UUID bookingId) {
                this.bookingId = bookingId;
                return this;
            }
            
            public BookingQueueItemBuilder bookingCode(String bookingCode) {
                this.bookingCode = bookingCode;
                return this;
            }
            
            public BookingQueueItemBuilder customerName(String customerName) {
                this.customerName = customerName;
                return this;
            }
            
            public BookingQueueItemBuilder customerPhone(String customerPhone) {
                this.customerPhone = customerPhone;
                return this;
            }
            
            public BookingQueueItemBuilder vehicleLicensePlate(String vehicleLicensePlate) {
                this.vehicleLicensePlate = vehicleLicensePlate;
                return this;
            }
            
            public BookingQueueItemBuilder serviceType(String serviceType) {
                this.serviceType = serviceType;
                return this;
            }
            
            public BookingQueueItemBuilder queuePosition(Integer queuePosition) {
                this.queuePosition = queuePosition;
                return this;
            }
            
            public BookingQueueItemBuilder estimatedStartTime(LocalDateTime estimatedStartTime) {
                this.estimatedStartTime = estimatedStartTime;
                return this;
            }
            
            public BookingQueueItemBuilder estimatedCompletionTime(LocalDateTime estimatedCompletionTime) {
                this.estimatedCompletionTime = estimatedCompletionTime;
                return this;
            }
            
            public BookingQueueItemBuilder status(String status) {
                this.status = status;
                return this;
            }
            
            public BookingQueueItemBuilder bookingServiceNames(java.util.List<String> bookingServiceNames) {
                this.bookingServiceNames = bookingServiceNames;
                return this;
            }
            
            public BookingQueueItemBuilder bookingTotalPrice(java.math.BigDecimal bookingTotalPrice) {
                this.bookingTotalPrice = bookingTotalPrice;
                return this;
            }
            
            public BookingQueueItemBuilder bookingCustomerName(String bookingCustomerName) {
                this.bookingCustomerName = bookingCustomerName;
                return this;
            }
            
            public BookingQueueItemBuilder bookingVehicleLicensePlate(String bookingVehicleLicensePlate) {
                this.bookingVehicleLicensePlate = bookingVehicleLicensePlate;
                return this;
            }
            
            public BookingQueueItem build() {
                BookingQueueItem item = new BookingQueueItem();
                item.bookingId = this.bookingId;
                item.bookingCode = this.bookingCode;
                item.customerName = this.customerName;
                item.customerPhone = this.customerPhone;
                item.vehicleLicensePlate = this.vehicleLicensePlate;
                item.serviceType = this.serviceType;
                item.queuePosition = this.queuePosition;
                item.estimatedStartTime = this.estimatedStartTime;
                item.estimatedCompletionTime = this.estimatedCompletionTime;
                item.status = this.status;
                item.bookingServiceNames = this.bookingServiceNames;
                item.bookingTotalPrice = this.bookingTotalPrice;
                item.bookingCustomerName = this.bookingCustomerName;
                item.bookingVehicleLicensePlate = this.bookingVehicleLicensePlate;
                return item;
            }
        }
        
        // Getters
        public UUID getBookingId() { return bookingId; }
        public String getBookingCode() { return bookingCode; }
        public String getCustomerName() { return customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public String getVehicleLicensePlate() { return vehicleLicensePlate; }
        public String getServiceType() { return serviceType; }
        public Integer getQueuePosition() { return queuePosition; }
        public LocalDateTime getEstimatedStartTime() { return estimatedStartTime; }
        public LocalDateTime getEstimatedCompletionTime() { return estimatedCompletionTime; }
        public String getStatus() { return status; }
        public java.util.List<String> getBookingServiceNames() { return bookingServiceNames; }
        public java.math.BigDecimal getBookingTotalPrice() { return bookingTotalPrice; }
        public String getBookingCustomerName() { return bookingCustomerName; }
        public String getBookingVehicleLicensePlate() { return bookingVehicleLicensePlate; }
    }
    
    /**
     * DTO cho bay score
     */
    private static class BayScore {
        private final ServiceBay bay;
        private final int score;
        private final String reason;
        
        public BayScore(ServiceBay bay, int score, String reason) {
            this.bay = bay;
            this.score = score;
            this.reason = reason;
        }
        
        public ServiceBay getBay() { return bay; }
        public int getScore() { return score; }
        public String getReason() { return reason; }
    }
    
    /**
     * Đếm số booking đang được xử lý trong bay
     */
    private Long countActiveBookingsInBay(UUID bayId, LocalDate queueDate) {
        // Kiểm tra booking có status đang được xử lý trong bay
        // Tạm thời return 0 để tránh lỗi compilation
        // TODO: Implement proper logic to count active bookings in bay
        log.debug("Checking active bookings in bay: {} for date: {}", bayId, queueDate);
        return 0L;
    }
}
