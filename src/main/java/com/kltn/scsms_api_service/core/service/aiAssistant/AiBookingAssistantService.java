package com.kltn.scsms_api_service.core.service.aiAssistant;

import com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.CreateBookingRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetBranchesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetCustomerVehiclesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.AvailabilityResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.CreateBookingResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetBranchesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetCustomerVehiclesResponse;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleProfileFilterParam;
import com.kltn.scsms_api_service.core.dto.bookingSchedule.AvailableTimeRangesResponse;
import com.kltn.scsms_api_service.core.dto.bookingSchedule.TimeRangeDto;
import com.kltn.scsms_api_service.core.dto.branchServiceFilter.BranchServiceFilterResult;
import com.kltn.scsms_api_service.core.dto.branchServiceFilter.ServiceAvailabilityInfo;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingItemRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingWithScheduleRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingWithScheduleRequest.ScheduleSelectionRequest;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.VehicleProfile;
import com.kltn.scsms_api_service.core.service.businessService.BookingPricingService;
import com.kltn.scsms_api_service.core.service.businessService.BookingTimeRangeService;
import com.kltn.scsms_api_service.core.service.businessService.BranchServiceFilterService;
import com.kltn.scsms_api_service.core.service.businessService.IntegratedBookingService;
import com.kltn.scsms_api_service.core.service.businessService.PricingBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.core.service.entityService.VehicleProfileService;
import com.kltn.scsms_api_service.core.utils.PermissionUtils;
import com.kltn.scsms_api_service.core.dto.token.LoginUserInfo;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true, noRollbackFor = { ClientSideException.class })
public class AiBookingAssistantService {

    private final BookingTimeRangeService bookingTimeRangeService;
    private final BranchServiceFilterService branchServiceFilterService;
    private final ServiceBayService serviceBayService;
    private final ServiceService serviceService;
    private final IntegratedBookingService integratedBookingService;
    private final UserService userService;
    private final VehicleProfileService vehicleProfileService;
    private final BranchService branchService;
    private final PricingBusinessService pricingBusinessService;
    private final BookingPricingService bookingPricingService;


    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("AI Function: checkAvailability() called with request: {}", request);

        try {
            // 1. Parse và validate service
            // Kiểm tra số lượng services trước khi parse để tránh tự động chọn service đầu
            // tiên
            Service service = null;
            if (request.getServiceType() != null && !request.getServiceType().trim().isEmpty()) {
                // Tìm services theo keyword trước
                List<Service> foundServices = serviceService.searchByKeyword(request.getServiceType());

                if (foundServices.isEmpty()) {
                    // Không tìm thấy service nào
                    service = null;
                } else if (foundServices.size() == 1) {
                    // Chỉ có 1 service, dùng luôn
                    service = foundServices.get(0);
                    log.info("Found single service: {}", service.getServiceName());
                } else {
                    // Có nhiều services, kiểm tra exact match
                    Service exactMatch = foundServices.stream()
                            .filter(s -> s.getServiceName().equalsIgnoreCase(request.getServiceType()))
                            .findFirst()
                            .orElse(null);

                    if (exactMatch != null) {
                        // Có exact match, dùng luôn
                        service = exactMatch;
                        log.info("Found exact match service: {}", service.getServiceName());
                    } else {
                        // Không có exact match, trả về danh sách để user chọn
                        // Lấy giá từ price book cho tất cả services
                        List<UUID> serviceIds = foundServices.stream()
                                .map(Service::getServiceId)
                                .collect(Collectors.toList());
                        Map<UUID, BigDecimal> priceMap = pricingBusinessService.getServicePricesBatch(serviceIds, null);
                        
                        List<AvailabilityResponse.SuggestedServiceInfo> suggestedServices = foundServices.stream()
                                .map(s -> AvailabilityResponse.SuggestedServiceInfo.builder()
                                        .serviceId(s.getServiceId())
                                        .serviceName(s.getServiceName())
                                        .description(s.getDescription())
                                        .estimatedDuration(s.getEstimatedDuration())
                                        .price(priceMap.getOrDefault(s.getServiceId(), BigDecimal.ZERO))
                                        .build())
                                .collect(Collectors.toList());

                        return AvailabilityResponse.builder()
                                .status("NEEDS_SERVICE_SELECTION")
                                .message(null)
                                .suggestions(new ArrayList<>())
                                .availableBays(new ArrayList<>())
                                .suggestedServices(suggestedServices)
                                .build();
                    }
                }
            }

            if (service == null) {
                // Nếu serviceType=null hoặc không tìm thấy, tìm danh sách services gợi ý
                if (request.getServiceType() == null || request.getServiceType().trim().isEmpty()) {
                    // Nếu không có serviceType, trả về tất cả services active
                    List<Service> allServices = serviceService.findAll().stream()
                            .filter(s -> s.getIsActive() != null && s.getIsActive())
                            .collect(Collectors.toList());

                    if (allServices.isEmpty()) {
                        return AvailabilityResponse.builder()
                                .status("FULL")
                                .message("Không có dịch vụ nào trong hệ thống. Vui lòng liên hệ admin.")
                                .suggestions(new ArrayList<>())
                                .availableBays(new ArrayList<>())
                                .suggestedServices(new ArrayList<>())
                                .build();
                    }

                    // Trả về danh sách services để user chọn
                    // Lấy giá từ price book cho tất cả services
                    List<UUID> serviceIds = allServices.stream()
                            .map(Service::getServiceId)
                            .collect(Collectors.toList());
                    Map<UUID, BigDecimal> priceMap = pricingBusinessService.getServicePricesBatch(serviceIds, null);
                    
                    List<AvailabilityResponse.SuggestedServiceInfo> suggestedServices = allServices.stream()
                            .map(s -> AvailabilityResponse.SuggestedServiceInfo.builder()
                                    .serviceId(s.getServiceId())
                                    .serviceName(s.getServiceName())
                                    .description(s.getDescription())
                                    .estimatedDuration(s.getEstimatedDuration())
                                    .price(priceMap.getOrDefault(s.getServiceId(), BigDecimal.ZERO))
                                    .build())
                            .collect(Collectors.toList());

                    return AvailabilityResponse.builder()
                            .status("NEEDS_SERVICE_SELECTION")
                            .message(null)
                            .suggestions(new ArrayList<>())
                            .availableBays(new ArrayList<>())
                            .suggestedServices(suggestedServices)
                            .build();
                } else {
                    // Không có serviceType, trả về lỗi
                    return AvailabilityResponse.builder()
                            .status("FULL")
                            .message("Vui lòng chỉ định dịch vụ cụ thể.")
                            .suggestions(new ArrayList<>())
                            .availableBays(new ArrayList<>())
                            .suggestedServices(new ArrayList<>())
                            .build();
                }
            }

            // Tạo final reference để sử dụng trong lambda
            final Service finalService = service;

            Integer serviceDuration = service.getEstimatedDuration();
            if (serviceDuration == null || serviceDuration <= 0) {
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Dịch vụ không có thông tin thời gian thực hiện")
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }

            // 2. Parse dateTime (AI đã parse thành ISO format)
            // Nếu dateTime == null, trả về response yêu cầu thời gian
            if (request.getDateTime() == null || request.getDateTime().trim().isEmpty()) {
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Vui lòng cung cấp thời gian đặt lịch. Bạn muốn đặt lịch khi nào?")
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }

            LocalDate date = parseDate(request.getDateTime());
            if (date == null) {
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Không thể parse ngày: " + request.getDateTime()
                                + ". Vui lòng cung cấp thời gian theo format: YYYY-MM-DD hoặc YYYY-MM-DDTHH:mm:ss")
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }

            // 3. Parse branch (nếu có branch_id hoặc branch_name)
            UUID branchId = null;
            if (request.getBranchId() != null && !request.getBranchId().trim().isEmpty()) {
                try {
                    branchId = UUID.fromString(request.getBranchId());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid branch_id format: '{}'. Will try to find by name.", request.getBranchId());
                }
            }
            if (branchId == null && request.getBranchName() != null && !request.getBranchName().trim().isEmpty()) {
                // Tìm branch theo tên với logic matching linh hoạt hơn
                Branch branch = findBranchByNameOrAddress(request.getBranchName().trim());

                if (branch == null) {
                    return AvailabilityResponse.builder()
                            .status("FULL")
                            .message("Không tìm thấy chi nhánh: " + request.getBranchName()
                                    + ". Vui lòng kiểm tra lại tên chi nhánh.")
                            .suggestions(new ArrayList<>())
                            .availableBays(new ArrayList<>())
                            .suggestedServices(new ArrayList<>())
                            .build();
                }

                branchId = branch.getBranchId();
                log.info("Parsed branch_name '{}' to branch_id: {} ({})", 
                        request.getBranchName(), branchId, branch.getBranchName());
            }

            if (branchId != null) {
                // Check inventory cho service đã chọn - QUAN TRỌNG: Kiểm tra tồn kho sản phẩm
                // Sử dụng batch-check-services để kiểm tra service cụ thể
                long inventoryCheckStart = System.currentTimeMillis();
                log.info("Checking inventory for selected service '{}' at branch {} using batch-check", 
                        finalService.getServiceName(), branchId);
                
                // Gọi batch-check-services với service đã chọn
                List<UUID> serviceIdsToCheck = new ArrayList<>();
                serviceIdsToCheck.add(finalService.getServiceId());
                
                BranchServiceFilterResult filterResult = branchServiceFilterService
                        .checkMultipleServicesAvailability(branchId, serviceIdsToCheck, true);
                
                long inventoryCheckEnd = System.currentTimeMillis();
                log.info("Inventory check completed in {} ms", (inventoryCheckEnd - inventoryCheckStart));

                // Kiểm tra xem service đã chọn có available không
                boolean selectedServiceAvailable = filterResult.getAvailableServices().stream()
                        .anyMatch(info -> info.getId().equals(finalService.getServiceId()));
                
                if (!selectedServiceAvailable) {
                    // Service không hợp lệ - tìm thông tin chi tiết
                    ServiceAvailabilityInfo serviceAvailabilityInfo = filterResult.getUnavailableServices().stream()
                            .filter(info -> info.getId().equals(finalService.getServiceId()))
                            .findFirst()
                            .orElse(null);
                    
                    String errorMessage = "Dịch vụ '" + service.getServiceName() + "' không có đủ hàng tại chi nhánh này";
                    if (serviceAvailabilityInfo != null) {
                        if (serviceAvailabilityInfo.getMissingProducts() != null && !serviceAvailabilityInfo.getMissingProducts().isEmpty()) {
                            errorMessage += ". Thiếu sản phẩm: " + String.join(", ", serviceAvailabilityInfo.getMissingProducts());
                        }
                        if (serviceAvailabilityInfo.getInsufficientProducts() != null && !serviceAvailabilityInfo.getInsufficientProducts().isEmpty()) {
                            errorMessage += ". Không đủ số lượng: " + String.join(", ", serviceAvailabilityInfo.getInsufficientProducts());
                        }
                    }
                    
                    log.warn("Selected service '{}' is not available at branch {} due to inventory issues: {}", 
                            finalService.getServiceName(), branchId, errorMessage);
                    
                    // Lấy danh sách services available tại branch để gợi ý
                    BranchServiceFilterResult allAvailableServices = branchServiceFilterService
                            .filterServicesByBranch(branchId, true);
                    
                    // Lấy danh sách service IDs để lấy giá batch
                    List<UUID> availableServiceIds = allAvailableServices.getAvailableServices().stream()
                            .filter(info -> !info.getId().equals(finalService.getServiceId())) // Loại bỏ service đã chọn
                            .map(info -> info.getId())
                            .collect(Collectors.toList());
                    
                    // Lấy giá từ price book cho tất cả services available
                    Map<UUID, BigDecimal> priceMap = pricingBusinessService.getServicePricesBatch(availableServiceIds, null);
                    
                    List<AvailabilityResponse.SuggestedServiceInfo> availableServiceSuggestions = 
                            allAvailableServices.getAvailableServices().stream()
                                    .filter(info -> !info.getId().equals(finalService.getServiceId())) // Loại bỏ service đã chọn
                                    .map(info -> {
                                        // Lấy thông tin service đầy đủ
                                        Service availableService = serviceService.findById(info.getId()).orElse(null);
                                        if (availableService != null) {
                                            return AvailabilityResponse.SuggestedServiceInfo.builder()
                                                    .serviceId(availableService.getServiceId())
                                                    .serviceName(availableService.getServiceName())
                                                    .description(availableService.getDescription())
                                                    .estimatedDuration(availableService.getEstimatedDuration())
                                                    .price(priceMap.getOrDefault(availableService.getServiceId(), BigDecimal.ZERO))
                                                    .build();
                                        }
                                        return null;
                                    })
                                    .filter(s -> s != null)
                                    .collect(Collectors.toList());
                    
                    // Nếu có services available khác, yêu cầu chọn lại
                    if (!availableServiceSuggestions.isEmpty()) {
                        errorMessage += ". Vui lòng chọn dịch vụ khác từ danh sách sau:";
                        
                        return AvailabilityResponse.builder()
                                .status("NEEDS_SERVICE_SELECTION")
                                .message(errorMessage)
                                .suggestions(new ArrayList<>())
                                .availableBays(new ArrayList<>())
                                .suggestedServices(availableServiceSuggestions)
                                .build();
                    } else {
                        // Không có service nào available
                        return AvailabilityResponse.builder()
                                .status("FULL")
                                .message(errorMessage + ". Hiện tại không có dịch vụ nào khả dụng tại chi nhánh này.")
                                .suggestions(new ArrayList<>())
                                .availableBays(new ArrayList<>())
                                .suggestedServices(new ArrayList<>())
                                .build();
                    }
                }
                
                log.info("Selected service '{}' passed inventory check at branch {}", 
                        finalService.getServiceName(), branchId);
            }

            // 4. Lấy service bays
            List<ServiceBay> serviceBays;
            if (branchId != null) {
                // Lấy bays của branch cụ thể có allow_booking = true
                serviceBays = serviceBayService.findBookingAllowedBaysByBranch(branchId);
            } else {
                // Lấy tất cả bays có allow_booking = true
                serviceBays = serviceBayService.findAll().stream()
                        .filter(ServiceBay::isAvailableForBooking)
                        .collect(Collectors.toList());
            }

            if (serviceBays.isEmpty()) {
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Không có service bay nào cho phép đặt lịch")
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }

            // 5. Lấy available time ranges cho mỗi bay
            // Tối ưu: Sử dụng parallel stream để gọi getAvailableTimeRanges() song song cho nhiều bay
            long bayCheckStart = System.currentTimeMillis();
            log.info("Checking {} bays for available time ranges (using parallel processing)", serviceBays.size());
            
            final LocalDate finalDate = date;
            final Integer finalServiceDuration = serviceDuration;
            
            List<AvailabilityResponse.AvailableBayInfo> availableBays = serviceBays.parallelStream()
                    .map(bay -> {
                        try {
                            AvailableTimeRangesResponse timeRangesResponse = bookingTimeRangeService
                                    .getAvailableTimeRanges(bay.getBayId(), finalDate);

                            // Filter ranges đủ lớn cho service duration
                            List<TimeRangeDto> suitableRanges = filterRangesByDuration(
                                    timeRangesResponse.getAvailableTimeRanges(),
                                    finalServiceDuration);

                            if (!suitableRanges.isEmpty()) {
                                // Convert time ranges thành slots
                                List<String> slots = convertTimeRangesToSlots(
                                        suitableRanges,
                                        timeRangesResponse.getWorkingHours(),
                                        finalServiceDuration);

                                if (!slots.isEmpty()) {
                                    // Lấy thông tin branch từ bay
                                    Branch bayBranch = bay.getBranch();

                                    return AvailabilityResponse.AvailableBayInfo.builder()
                                            .bayId(bay.getBayId())
                                            .bayName(bay.getBayName())
                                            .bayCode(bay.getBayCode())
                                            .branchId(bayBranch != null ? bayBranch.getBranchId() : null)
                                            .branchName(bayBranch != null ? bayBranch.getBranchName() : null)
                                            .description(bay.getDescription())
                                            .status(bay.getStatus() != null ? bay.getStatus().name() : null)
                                            .availableSlots(slots)
                                            .build();
                                }
                            }
                        } catch (ClientSideException e) {
                            // Xử lý exception khi branch đóng cửa
                            if (e.getCode() == ErrorCode.BRANCH_CLOSED) {
                                String dayOfWeek = finalDate.getDayOfWeek().name();
                                log.warn("Branch is closed on {}: {}", dayOfWeek, e.getMessage());
                            } else {
                                log.warn("Error getting time ranges for bay {}: {}", bay.getBayId(), e.getMessage());
                            }
                        } catch (Exception e) {
                            log.warn("Error getting time ranges for bay {}: {}", bay.getBayId(), e.getMessage());
                        }
                        return null;
                    })
                    .filter(bayInfo -> bayInfo != null)
                    .collect(Collectors.toList());

            // Collect all suggestions from available bays
            List<String> allSuggestions = availableBays.stream()
                    .flatMap(bay -> bay.getAvailableSlots().stream())
                    .collect(Collectors.toList());
            
            long bayCheckEnd = System.currentTimeMillis();
            log.info("Bay availability check completed in {} ms. Found {} available bays", 
                    (bayCheckEnd - bayCheckStart), availableBays.size());
            
            // Check if all bays failed due to branch closed
            String branchClosedMessage = null;
            if (availableBays.isEmpty() && !serviceBays.isEmpty()) {
                // Try to get a sample error message by checking one bay synchronously
                try {
                    bookingTimeRangeService.getAvailableTimeRanges(serviceBays.get(0).getBayId(), date);
                } catch (ClientSideException e) {
                    if (e.getCode() == ErrorCode.BRANCH_CLOSED) {
                        String dayOfWeek = date.getDayOfWeek().name();
                        branchClosedMessage = "Chi nhánh đóng cửa vào " + dayOfWeek + " ("
                                + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                + "). Vui lòng chọn ngày khác.";
                    }
                }
            }

            // Nếu tất cả bays đều bị lỗi do branch đóng cửa, trả về message phù hợp
            if (availableBays.isEmpty() && branchClosedMessage != null) {
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message(branchClosedMessage)
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }

            // 6. Build response
            if (availableBays.isEmpty()) {
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Không có slot trống phù hợp cho dịch vụ '" + service.getServiceName() +
                                "' (thời lượng: " + serviceDuration + " phút) vào ngày " + date)
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }

            // Remove duplicates và sort suggestions
            allSuggestions = allSuggestions.stream()
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Check nếu user hỏi giờ cụ thể
            String specificSlot = null;
            if (request.getDateTime() != null && request.getDateTime().contains("T")) {
                // Parse time từ ISO format
                try {
                    LocalTime requestedTime = LocalTime.parse(
                            request.getDateTime().split("T")[1].substring(0, 5),
                            DateTimeFormatter.ofPattern("HH:mm"));
                    String requestedSlot = requestedTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                    if (allSuggestions.contains(requestedSlot)) {
                        specificSlot = requestedSlot;
                    }
                } catch (Exception e) {
                    // Ignore parse error
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("checkAvailability() completed in {} ms. Found {} available bays with {} total slots", 
                    (endTime - startTime), availableBays.size(), allSuggestions.size());
            
            return AvailabilityResponse.builder()
                    .status("AVAILABLE")
                    .slot(specificSlot)
                    .suggestions(allSuggestions)
                    .availableBays(availableBays)
                    .message("Tìm thấy " + allSuggestions.size() + " khung giờ trống phù hợp")
                    .suggestedServices(new ArrayList<>())
                    .build();

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Error in checkAvailability() after {} ms: {}", (endTime - startTime), e.getMessage(), e);
            return AvailabilityResponse.builder()
                    .status("FULL")
                    .message("Có lỗi xảy ra khi kiểm tra slot: " + e.getMessage())
                    .suggestions(new ArrayList<>())
                    .availableBays(new ArrayList<>())
                    .suggestedServices(new ArrayList<>())
                    .build();
        }
    }

    /**
     * Tìm branch theo tên hoặc địa chỉ với logic matching linh hoạt
     * Hỗ trợ các trường hợp:
     * - Exact match trên branch name
     * - Substring match trên branch name
     * - Substring match trên address
     * - Keyword matching (ví dụ: "Premium" từ "Premium - 416 Thảo Điền")
     */
    public Branch findBranchByNameOrAddress(String searchString) {
        if (searchString == null || searchString.trim().isEmpty()) {
            return null;
        }

        String searchLower = searchString.toLowerCase().trim();
        List<Branch> allBranches = branchService.findAllActiveBranches();

        // 1. Thử exact match trên branch name
        Optional<Branch> exactMatch = allBranches.stream()
                .filter(b -> b.getBranchName() != null 
                        && b.getBranchName().equalsIgnoreCase(searchString))
                .findFirst();
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }

        // 2. Thử substring match trên branch name
        Optional<Branch> nameContains = allBranches.stream()
                .filter(b -> b.getBranchName() != null 
                        && b.getBranchName().toLowerCase().contains(searchLower))
                .findFirst();
        if (nameContains.isPresent()) {
            return nameContains.get();
        }

        // 3. Thử substring match trên address
        Optional<Branch> addressContains = allBranches.stream()
                .filter(b -> b.getAddress() != null 
                        && b.getAddress().toLowerCase().contains(searchLower))
                .findFirst();
        if (addressContains.isPresent()) {
            return addressContains.get();
        }

        // 4. Thử keyword matching: tách searchString thành các từ và tìm branch có tên hoặc địa chỉ chứa từ đó
        String[] keywords = searchLower.split("[\\s-]+");
        for (String keyword : keywords) {
            if (keyword.length() < 2) { // Bỏ qua từ quá ngắn
                continue;
            }
            
            // Tìm branch có tên chứa keyword
            Optional<Branch> keywordMatch = allBranches.stream()
                    .filter(b -> {
                        boolean nameMatch = b.getBranchName() != null 
                                && b.getBranchName().toLowerCase().contains(keyword);
                        boolean addressMatch = b.getAddress() != null 
                                && b.getAddress().toLowerCase().contains(keyword);
                        return nameMatch || addressMatch;
                    })
                    .findFirst();
            if (keywordMatch.isPresent()) {
                return keywordMatch.get();
            }
        }

        // 5. Thử reverse matching: kiểm tra xem branch name có chứa trong searchString không
        Optional<Branch> reverseMatch = allBranches.stream()
                .filter(b -> {
                    if (b.getBranchName() == null) {
                        return false;
                    }
                    String branchNameLower = b.getBranchName().toLowerCase();
                    // Loại bỏ "Chi nhánh" prefix nếu có
                    String branchNameWithoutPrefix = branchNameLower
                            .replaceFirst("^chi nhánh\\s+", "")
                            .trim();
                    return searchLower.contains(branchNameWithoutPrefix) 
                            || searchLower.contains(branchNameLower);
                })
                .findFirst();
        if (reverseMatch.isPresent()) {
            return reverseMatch.get();
        }

        return null;
    }

    /**
     * Parse service từ serviceType (tên dịch vụ hoặc serviceId)
     */
    private Service parseService(String serviceType) {
        if (serviceType == null || serviceType.trim().isEmpty()) {
            return null;
        }

        try {
            // Thử parse như UUID
            UUID serviceId = UUID.fromString(serviceType);
            return serviceService.findById(serviceId).orElse(null);
        } catch (IllegalArgumentException e) {
            // Không phải UUID, tìm theo tên bằng searchByKeyword
            List<Service> services = serviceService.searchByKeyword(serviceType);
            // Tìm exact match trước
            for (Service s : services) {
                if (s.getServiceName().equalsIgnoreCase(serviceType)) {
                    return s;
                }
            }
            // Nếu không có exact match, lấy service đầu tiên
            return services.isEmpty() ? null : services.get(0);
        }
    }

    /**
     * Parse date từ dateTime string (ISO format: "2025-11-21" hoặc
     * "2025-11-21T08:00:00")
     */
    private LocalDate parseDate(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return null;
        }

        try {
            // Nếu có "T", lấy phần date
            if (dateTime.contains("T")) {
                dateTime = dateTime.split("T")[0];
            }
            return LocalDate.parse(dateTime);
        } catch (Exception e) {
            log.warn("Error parsing date: {}", dateTime, e);
            return null;
        }
    }

    /**
     * Filter time ranges đủ lớn cho service duration
     */
    private List<TimeRangeDto> filterRangesByDuration(
            List<TimeRangeDto> ranges,
            Integer durationMinutes) {
        return ranges.stream()
                .filter(range -> {
                    LocalTime start = range.getStartTime();
                    LocalTime end = range.getEndTime();
                    long rangeDurationMinutes = java.time.Duration.between(start, end).toMinutes();
                    return rangeDurationMinutes >= durationMinutes;
                })
                .collect(Collectors.toList());
    }

    /**
     * Convert time ranges thành slots (8:00, 8:30, 9:00, ...)
     * Tương tự như frontend BookingScheduleService.convertTimeRangesToSlots()
     */
    private List<String> convertTimeRangesToSlots(
            List<TimeRangeDto> timeRanges,
            com.kltn.scsms_api_service.core.dto.bookingSchedule.WorkingHoursDto workingHours,
            Integer serviceDurationMinutes) {
        List<String> slots = new ArrayList<>();
        int slotIntervalMinutes = 30; // 30 phút interval

        for (TimeRangeDto range : timeRanges) {
            LocalTime rangeStart = range.getStartTime();
            LocalTime rangeEnd = range.getEndTime();

            LocalTime current = rangeStart;
            while (current.isBefore(rangeEnd)) {
                LocalTime slotEnd = current.plusMinutes(serviceDurationMinutes);

                // Check nếu slot + duration fit trong range
                if (!slotEnd.isAfter(rangeEnd)) {
                    String slotTime = current.format(DateTimeFormatter.ofPattern("HH:mm"));
                    if (!slots.contains(slotTime)) {
                        slots.add(slotTime);
                    }
                }

                current = current.plusMinutes(slotIntervalMinutes);
            }
        }

        return slots.stream().sorted().collect(Collectors.toList());
    }

    @Transactional
    public CreateBookingResponse createBooking(CreateBookingRequest request) {
        log.info("AI Function: createBooking() called with request: {}", request);

        try {
            // 1. Validate customer - Lấy từ SecurityContext (token authentication)
            // Ưu tiên lấy từ token, không cần phone number trong request
            User customer = null;
            LoginUserInfo currentUser = PermissionUtils.getCurrentUser();
            
            if (currentUser != null && currentUser.getSub() != null) {
                // Đã đăng nhập - lấy customer từ userId trong token
                try {
                    UUID customerId = UUID.fromString(currentUser.getSub());
                    customer = userService.findById(customerId)
                            .orElse(null);
                    if (customer == null) {
                        log.warn("Customer not found with userId from token: {}", customerId);
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Không tìm thấy thông tin khách hàng. Vui lòng đăng nhập lại.")
                                .build();
                    }
                    log.info("Customer found from token: {} ({})", customer.getFullName(), customer.getPhoneNumber());
                } catch (IllegalArgumentException e) {
                    log.error("Invalid userId format in token: {}", currentUser.getSub());
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message("Token không hợp lệ. Vui lòng đăng nhập lại.")
                            .build();
                }
            } else {
                // Fallback: Nếu không có token, thử dùng phone number từ request (cho trường hợp đặc biệt)
                String customerPhone = request.getCustomerPhone();
                if (customerPhone != null && !customerPhone.trim().isEmpty()) {
                    customer = userService.findByPhoneNumber(customerPhone)
                            .orElse(null);
                    if (customer == null) {
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Không tìm thấy khách hàng với số điện thoại: " + customerPhone +
                                        ". Vui lòng đăng ký tài khoản trước.")
                                .build();
                    }
                    log.info("Customer found from phone number: {} ({})", customer.getFullName(), customer.getPhoneNumber());
                } else {
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message("Vui lòng đăng nhập để đặt lịch")
                            .build();
                }
            }

            // 2. Validate vehicle
            VehicleProfile vehicle = null;
            if (request.getVehicleId() != null && !request.getVehicleId().trim().isEmpty()) {
                try {
                    UUID vehicleId = UUID.fromString(request.getVehicleId());
                    vehicle = vehicleProfileService.getVehicleProfileById(vehicleId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid vehicle_id format: '{}'. Will try to find by license plate.", request.getVehicleId());
                }
            } else if (request.getVehicleLicensePlate() != null && !request.getVehicleLicensePlate().trim().isEmpty()) {
                // Tìm vehicle theo license plate và owner
                // Note: Cần implement method này hoặc dùng filter
                // Tạm thời yêu cầu vehicleId
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Vui lòng cung cấp vehicle_id. Tìm kiếm theo biển số sẽ được hỗ trợ sau.")
                        .build();
            } else {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Vui lòng cung cấp thông tin xe (vehicle_id hoặc vehicle_license_plate)")
                        .build();
            }

            // 3. Parse branch (nếu có branch_id hoặc branch_name)
            UUID parsedBranchId = null;
            if (request.getBranchId() != null && !request.getBranchId().trim().isEmpty()) {
                try {
                    parsedBranchId = UUID.fromString(request.getBranchId());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid branch_id format: '{}'. Will try to find by name.", request.getBranchId());
                }
            }
            if (parsedBranchId == null && request.getBranchName() != null
                    && !request.getBranchName().trim().isEmpty()) {
                // Tìm branch theo tên với logic matching linh hoạt hơn
                Branch foundBranch = findBranchByNameOrAddress(request.getBranchName().trim());

                if (foundBranch == null) {
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message("Không tìm thấy chi nhánh: " + request.getBranchName()
                                    + ". Vui lòng kiểm tra lại tên chi nhánh.")
                            .build();
                }

                parsedBranchId = foundBranch.getBranchId();
            }

            if (parsedBranchId == null) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Vui lòng cung cấp branch_id hoặc branch_name")
                        .build();
            }

            final UUID branchId = parsedBranchId;

            // Validate branch
            Branch branch = branchService.findById(branchId)
                    .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                            "Branch not found with ID: " + branchId));

            // Parse bay (nếu có bay_id hoặc bay_name)
            UUID parsedBayId = null;
            if (request.getBayId() != null && !request.getBayId().trim().isEmpty()) {
                try {
                    parsedBayId = UUID.fromString(request.getBayId());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid bay_id format: '{}'. Will try to find by name.", request.getBayId());
                }
            }
            if (parsedBayId == null && request.getBayName() != null && !request.getBayName().trim().isEmpty()) {
                // Tìm bay theo tên, ưu tiên tìm trong branch đã chọn
                ServiceBay foundBay = serviceBayService.getByBayName(request.getBayName())
                        .orElseGet(() -> {
                            // Nếu không tìm thấy exact match, thử search by keyword trong branch
                            List<ServiceBay> bays = serviceBayService.searchByKeywordInBranch(branchId,
                                    request.getBayName());
                            return bays.isEmpty() ? null : bays.get(0);
                        });

                if (foundBay == null) {
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message("Không tìm thấy bay: " + request.getBayName() +
                                    (branch != null ? " ở chi nhánh " + branch.getBranchName() : "") +
                                    ". Vui lòng kiểm tra lại tên bay.")
                            .build();
                }

                // Validate bay thuộc branch đã chọn
                if (foundBay.getBranch() != null && !foundBay.getBranch().getBranchId().equals(branchId)) {
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message("Bay '" + request.getBayName() + "' không thuộc chi nhánh "
                                    + branch.getBranchName())
                            .build();
                }

                parsedBayId = foundBay.getBayId();
                log.info("Parsed bay_name '{}' to bay_id: {}", request.getBayName(), parsedBayId);
            }

            if (parsedBayId == null) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Vui lòng cung cấp bay_id hoặc bay_name")
                        .build();
            }

            final UUID bayId = parsedBayId;

            // Validate bay
            ServiceBay bay = serviceBayService.getById(bayId);
            if (!bay.isAvailableForBooking()) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Service bay '" + bay.getBayName() + "' không cho phép đặt lịch")
                        .build();
            }

            // Validate bay thuộc branch
            if (bay.getBranch() != null && !bay.getBranch().getBranchId().equals(branchId)) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Bay '" + bay.getBayName() + "' không thuộc chi nhánh " + branch.getBranchName())
                        .build();
            }

            // 4. Parse services
            List<Service> services = parseServices(request.getServiceType());
            if (services.isEmpty()) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Không tìm thấy dịch vụ: " + request.getServiceType())
                        .build();
            }

            // 5. Check inventory cho services đã chọn - sử dụng batch-check-services
            List<UUID> serviceIdsToCheck = services.stream()
                    .map(Service::getServiceId)
                    .collect(Collectors.toList());
            
            BranchServiceFilterResult filterResult = branchServiceFilterService
                    .checkMultipleServicesAvailability(branchId, serviceIdsToCheck, true);

            List<Service> availableServices = new ArrayList<>();
            for (Service service : services) {
                boolean isAvailable = filterResult.getAvailableServices().stream()
                        .anyMatch(s -> s.getId().equals(service.getServiceId()));

                if (isAvailable) {
                    availableServices.add(service);
                } else {
                    // Log thông tin chi tiết về service không available
                    ServiceAvailabilityInfo unavailableInfo = filterResult.getUnavailableServices().stream()
                            .filter(info -> info.getId().equals(service.getServiceId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (unavailableInfo != null) {
                        log.warn("Service '{}' is not available at branch {}: missing={}, insufficient={}", 
                                service.getServiceName(), branchId,
                                unavailableInfo.getMissingProducts(),
                                unavailableInfo.getInsufficientProducts());
                    }
                }
            }

            if (availableServices.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Các dịch vụ đã chọn không có đủ hàng tại chi nhánh này");
                
                // Thêm thông tin chi tiết về sản phẩm thiếu
                List<String> allMissingProducts = new ArrayList<>();
                List<String> allInsufficientProducts = new ArrayList<>();
                
                for (ServiceAvailabilityInfo info : filterResult.getUnavailableServices()) {
                    if (info.getMissingProducts() != null) {
                        allMissingProducts.addAll(info.getMissingProducts());
                    }
                    if (info.getInsufficientProducts() != null) {
                        allInsufficientProducts.addAll(info.getInsufficientProducts());
                    }
                }
                
                if (!allMissingProducts.isEmpty()) {
                    errorMessage.append(". Thiếu sản phẩm: ").append(String.join(", ", allMissingProducts));
                }
                if (!allInsufficientProducts.isEmpty()) {
                    errorMessage.append(". Không đủ số lượng: ").append(String.join(", ", allInsufficientProducts));
                }
                
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message(errorMessage.toString())
                        .build();
            }

            // 6. Parse dateTime và check availability
            LocalDate date = parseDate(request.getDateTime());
            if (date == null) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Không thể parse ngày: " + request.getDateTime())
                        .build();
            }

            // Parse start time từ dateTime
            LocalTime startTime = parseTime(request.getDateTime());
            if (startTime == null) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Không thể parse giờ: " + request.getDateTime())
                        .build();
            }

            // Tính total duration
            int totalDuration = availableServices.stream()
                    .mapToInt(s -> s.getEstimatedDuration() != null ? s.getEstimatedDuration() : 0)
                    .sum();

            // Check availability trước khi tạo
            AvailabilityRequest availabilityRequest = AvailabilityRequest.builder()
                    .serviceType(services.get(0).getServiceName()) // Dùng service đầu tiên để check
                    .dateTime(request.getDateTime())
                    .branchId(branchId != null ? branchId.toString() : null)
                    .build();

            AvailabilityResponse availabilityResponse = checkAvailability(availabilityRequest);

            if (!"AVAILABLE".equals(availabilityResponse.getStatus())) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Khung giờ đã chọn không còn trống. " +
                                (availabilityResponse.getSuggestions() != null
                                        && !availabilityResponse.getSuggestions().isEmpty()
                                                ? "Các khung giờ khả dụng: "
                                                        + String.join(", ", availabilityResponse.getSuggestions())
                                                : "Vui lòng chọn khung giờ khác."))
                        .build();
            }

            // 8. Build booking items
            List<CreateBookingItemRequest> bookingItems = availableServices.stream()
                    .map(service -> CreateBookingItemRequest.builder()
                            .serviceId(service.getServiceId())
                            .serviceName(service.getServiceName())
                            .serviceDescription(service.getDescription())
                            .build())
                    .collect(Collectors.toList());

            // 9. Tính tổng giá từ bảng giá
            BigDecimal totalPrice;
            try {
                totalPrice = bookingPricingService.calculateBookingTotalPrice(bookingItems, null);
                log.info("Calculated total price for booking: {} VND ({} services)", totalPrice, bookingItems.size());
            } catch (Exception e) {
                log.error("Error calculating booking total price: {}", e.getMessage(), e);
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Không thể tính giá dịch vụ. Vui lòng thử lại sau.")
                        .build();
            }

            // 10. Build CreateBookingWithScheduleRequest
            LocalDateTime scheduledStartAt = LocalDateTime.of(date, startTime);
            LocalDateTime scheduledEndAt = scheduledStartAt.plusMinutes(totalDuration);

            // Validate vehicle không null (đã được validate ở trên, nhưng check lại để an toàn)
            if (vehicle == null) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Vui lòng cung cấp thông tin xe (vehicle_id hoặc vehicle_license_plate)")
                        .build();
            }

            CreateBookingWithScheduleRequest createRequest = CreateBookingWithScheduleRequest.builder()
                    .customerId(customer.getUserId())
                    .customerName(customer.getFullName())
                    .customerPhone(customer.getPhoneNumber())
                    .customerEmail(customer.getEmail())
                    .vehicleId(vehicle.getVehicleId())
                    .vehicleLicensePlate(vehicle.getLicensePlate())
                    .vehicleBrandName(null) // Backend sẽ lấy từ vehicle profile nếu cần
                    .vehicleModelName(null) // Backend sẽ lấy từ vehicle profile nếu cần
                    .vehicleTypeName(null) // Backend sẽ lấy từ vehicle profile nếu cần
                    .vehicleYear(vehicle.getVehicleYear())
                    .vehicleColor(null) // VehicleProfile không có color field
                    .branchId(branchId)
                    .selectedSchedule(ScheduleSelectionRequest.builder()
                            .bayId(bayId)
                            .date(date)
                            .startTime(startTime)
                            .serviceDurationMinutes(totalDuration)
                            .build())
                    .bookingItems(bookingItems)
                    .totalPrice(totalPrice)
                    .currency("VND")
                    .estimatedDurationMinutes(totalDuration)
                    .preferredStartAt(scheduledStartAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .scheduledStartAt(scheduledStartAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .scheduledEndAt(scheduledEndAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .notes(request.getNotes())
                    .build();

            // 10. Gọi IntegratedBookingService
            BookingInfoDto booking = integratedBookingService.createBookingWithSlot(createRequest);

            // 11. Build response
            return CreateBookingResponse.builder()
                    .status("SUCCESS")
                    .bookingCode(booking.getBookingCode())
                    .message("Đặt lịch thành công!")
                    .bookingDetails(CreateBookingResponse.BookingDetails.builder()
                            .dateTime(scheduledStartAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .serviceName(availableServices.stream()
                                    .map(Service::getServiceName)
                                    .collect(Collectors.joining(", ")))
                            .duration(totalDuration)
                            .branchName(branch.getBranchName())
                            .bayName(bay.getBayName())
                            .totalPrice(booking.getTotalPrice() != null ? booking.getTotalPrice().longValue() : 0L)
                            .build())
                    .build();

        } catch (ClientSideException e) {
            log.error("Client error in createBooking(): {}", e.getMessage());
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error in createBooking(): {}", e.getMessage(), e);
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message("Có lỗi xảy ra khi tạo booking: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Parse services từ serviceType (có thể là string hoặc list)
     */
    private List<Service> parseServices(Object serviceType) {
        List<Service> services = new ArrayList<>();

        if (serviceType == null) {
            return services;
        }

        if (serviceType instanceof String) {
            Service service = parseService((String) serviceType);
            if (service != null) {
                services.add(service);
            }
        } else if (serviceType instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> serviceNames = (List<String>) serviceType;
            for (String serviceName : serviceNames) {
                Service service = parseService(serviceName);
                if (service != null) {
                    services.add(service);
                }
            }
        }

        return services;
    }

    /**
     * Parse time từ dateTime string
     */
    private LocalTime parseTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return null;
        }

        try {
            // Nếu có "T", lấy phần time
            if (dateTime.contains("T")) {
                String timePart = dateTime.split("T")[1];
                // Remove timezone nếu có
                if (timePart.contains("+") || timePart.contains("Z")) {
                    timePart = timePart.split("\\+")[0].split("Z")[0];
                }
                // Parse HH:mm hoặc HH:mm:ss
                if (timePart.length() >= 5) {
                    return LocalTime.parse(timePart.substring(0, 5), DateTimeFormatter.ofPattern("HH:mm"));
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Error parsing time: {}", dateTime, e);
            return null;
        }
    }

    /**
     * Lấy danh sách xe của khách hàng dựa trên customer_id hoặc customer_phone.
     * Tự động lấy từ SecurityContext nếu không có trong request (user đã đăng
     * nhập).
     * Dùng để hỏi khách hàng muốn đặt lịch cho xe nào.
     */
    public GetCustomerVehiclesResponse getCustomerVehicles(GetCustomerVehiclesRequest request) {
        log.info("getCustomerVehicles called: customerId={}, customerPhone={}",
                request.getCustomerId(), request.getCustomerPhone());

        try {
            UUID customerId = request.getCustomerId();

            // Ưu tiên lấy từ SecurityContext (token authentication) - đây là cách đúng
            if (customerId == null) {
                LoginUserInfo currentUser = PermissionUtils.getCurrentUser();
                if (currentUser != null && currentUser.getSub() != null) {
                    try {
                        customerId = UUID.fromString(currentUser.getSub());
                        log.info("Auto-detected customerId from SecurityContext (token): {}", customerId);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid user ID format in SecurityContext: {}", currentUser.getSub());
                    }
                }
            }

            // Fallback: Nếu không có token, thử tìm từ customerPhone (cho trường hợp đặc biệt)
            if (customerId == null && request.getCustomerPhone() != null
                    && !request.getCustomerPhone().trim().isEmpty()) {
                Optional<User> userOpt = userService.findByPhoneNumber(request.getCustomerPhone().trim());
                if (userOpt.isPresent()) {
                    customerId = userOpt.get().getUserId();
                    log.info("CustomerId found from phone number: {}", customerId);
                } else {
                    return GetCustomerVehiclesResponse.builder()
                            .status("CUSTOMER_NOT_FOUND")
                            .message("Không tìm thấy khách hàng với số điện thoại: " + request.getCustomerPhone())
                            .vehicles(List.of())
                            .build();
                }
            }

            if (customerId == null) {
                return GetCustomerVehiclesResponse.builder()
                        .status("CUSTOMER_NOT_FOUND")
                        .message("Vui lòng đăng nhập để xem danh sách xe của bạn")
                        .vehicles(List.of())
                        .build();
            }

            // Lấy danh sách xe của khách hàng
            // Set size lớn để lấy tất cả xe (tương tự như frontend request với size=1000)
            VehicleProfileFilterParam filterParam = new VehicleProfileFilterParam();
            filterParam.setOwnerId(customerId);
            filterParam.setPage(1); // 1-based page (standardize sẽ convert sang 0-based)
            filterParam.setDeleted(false); // Chỉ lấy xe chưa bị xóa
            filterParam.setActive(true); // Chỉ lấy xe đang active
            filterParam.setSort("createdDate");
            filterParam.setDirection("DESC");

            // Standardize filter param (sẽ convert page từ 1-based sang 0-based và limit
            // size)
            filterParam = VehicleProfileFilterParam.standardize(filterParam);

            // Sau khi standardize, set lại size lớn để lấy tất cả xe
            // (standardize sẽ limit size xuống 100, nhưng ta cần lấy tất cả)
            filterParam.setSize(1000);

            Page<VehicleProfile> vehiclePage = vehicleProfileService.getAllVehicleProfilesByOwnerIdWithFilters(
                    customerId, filterParam);

            List<VehicleProfile> vehicles = vehiclePage.getContent();

            log.info("Found {} vehicles for customerId: {}", vehicles.size(), customerId);

            if (vehicles.isEmpty()) {
                return GetCustomerVehiclesResponse.builder()
                        .status("NO_VEHICLES")
                        .message("Bạn chưa có xe nào trong hệ thống. Vui lòng tạo xe mới trước khi đặt lịch.")
                        .vehicles(List.of())
                        .build();
            }

            // Map VehicleProfile sang VehicleInfo
            List<GetCustomerVehiclesResponse.VehicleInfo> vehicleInfos = vehicles.stream()
                    .map((VehicleProfile vehicle) -> {
                        return GetCustomerVehiclesResponse.VehicleInfo.builder()
                                .vehicleId(vehicle.getVehicleId())
                                .licensePlate(vehicle.getLicensePlate())
                                .description(vehicle.getDescription())
                                .vehicleBrandId(vehicle.getVehicleBrandId())
                                .vehicleTypeId(vehicle.getVehicleTypeId())
                                .vehicleModelId(vehicle.getVehicleModelId())
                                .vehicleYear(vehicle.getVehicleYear())
                                .build();
                    })
                    .collect(Collectors.toList());

            return GetCustomerVehiclesResponse.builder()
                    .status("SUCCESS")
                    .message("Tìm thấy " + vehicleInfos.size() + " xe")
                    .vehicles(vehicleInfos)
                    .build();

        } catch (Exception e) {
            log.error("Error in getCustomerVehicles: {}", e.getMessage(), e);
            return GetCustomerVehiclesResponse.builder()
                    .status("FAILED")
                    .message("Có lỗi xảy ra khi lấy danh sách xe: " + e.getMessage())
                    .vehicles(List.of())
                    .build();
        }
    }
    
    public GetBranchesResponse getBranches(GetBranchesRequest request) {
        log.info("AI Function: getBranches() called");
        
        try {
            // Lấy danh sách tất cả chi nhánh đang hoạt động
            List<Branch> branches = branchService.findAllActiveBranches();
            
            log.info("Found {} active branches", branches.size());
            
            if (branches.isEmpty()) {
                return GetBranchesResponse.builder()
                        .status("NO_BRANCHES")
                        .message("Hiện tại không có chi nhánh nào đang hoạt động.")
                        .branches(List.of())
                        .build();
            }
            
            // Map Branch sang BranchInfo
            List<GetBranchesResponse.BranchInfo> branchInfos = branches.stream()
                    .map(branch -> {
                        GetBranchesResponse.BranchInfo info = GetBranchesResponse.BranchInfo.builder()
                                .branchId(branch.getBranchId())
                                .branchName(branch.getBranchName())
                                .branchCode(branch.getBranchCode())
                                .address(branch.getAddress())
                                .phone(branch.getPhone())
                                .email(branch.getEmail())
                                .build();
                        // Log từng chi nhánh để debug
                        log.info("Branch: branch_id={}, branch_name='{}', address='{}'", 
                                info.getBranchId(), info.getBranchName(), info.getAddress());
                        return info;
                    })
                    .collect(Collectors.toList());
            
            log.info("Returning {} branches to AI: {}", branchInfos.size(), 
                    branchInfos.stream()
                            .map(b -> String.format("%s (%s)", b.getBranchName(), b.getAddress()))
                            .collect(Collectors.joining(", ")));
            
            return GetBranchesResponse.builder()
                    .status("SUCCESS")
                    .message("Tìm thấy " + branchInfos.size() + " chi nhánh")
                    .branches(branchInfos)
                    .build();
            
        } catch (Exception e) {
            log.error("Error in getBranches: {}", e.getMessage(), e);
            return GetBranchesResponse.builder()
                    .status("FAILED")
                    .message("Có lỗi xảy ra khi lấy danh sách chi nhánh: " + e.getMessage())
                    .branches(List.of())
                    .build();
        }
    }
}
