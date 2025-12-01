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
import com.kltn.scsms_api_service.core.dto.bookingSchedule.WorkingHoursDto;
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

    // Giờ làm việc mặc định của hệ thống: 8:00 sáng đến 18:00 chiều
    private static final LocalTime DEFAULT_WORKING_HOURS_START = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_WORKING_HOURS_END = LocalTime.of(18, 0);

    private final BookingTimeRangeService bookingTimeRangeService;
    
    /**
     * Tạo WorkingHoursDto mặc định với giờ làm việc 8:00 - 18:00
     */
    private WorkingHoursDto getDefaultWorkingHours() {
        return WorkingHoursDto.builder()
                .start(DEFAULT_WORKING_HOURS_START)
                .end(DEFAULT_WORKING_HOURS_END)
                .build();
    }
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
        log.info("=== CHECK AVAILABILITY START ===");
        log.info("AI Function: checkAvailability() called with request: {}", request);
        log.info("Request details - branch_id: '{}', branch_name: '{}', service_type: '{}', date_time: '{}'",
                request.getBranchId(), request.getBranchName(), request.getServiceType(), request.getDateTime());

        try {
            // STEP-BY-STEP VALIDATION: Kiểm tra dữ liệu theo từng bước
            AvailabilityResponse validationError = validateCheckAvailabilitySteps(request);
            if (validationError != null) {
                log.warn("=== VALIDATION FAILED ===");
                log.warn("Step: {}, Missing data: {}, Message: {}", 
                        validationError.getState() != null ? validationError.getState().getCurrentStep() : "unknown",
                        validationError.getState() != null ? validationError.getState().getMissingData() : "unknown",
                        validationError.getMessage());
                return validationError;
            }
            
            // Log state tracking
            AvailabilityResponse.BookingState state = buildAvailabilityState(request);
            log.info("=== STATE TRACKING ===");
            log.info("Current step: {}, Has vehicle: {}, Has date: {}, Has branch: {}, Has service: {}, Has bay: {}, Has time: {}",
                    state.getCurrentStep(), state.getHasVehicleId(), state.getHasDateTime(), 
                    state.getHasBranchId(), state.getHasServiceType(), state.getHasBayId(), state.getHasTimeSlot());
            log.info("Missing data: {}, Next action: {}", state.getMissingData(), state.getNextAction());
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
                                .state(buildAvailabilityState(request))
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
                            .state(buildAvailabilityState(request))
                            .build();
                } else {
                    // Không có serviceType, trả về lỗi
                    return AvailabilityResponse.builder()
                            .status("FULL")
                            .message("Vui lòng chỉ định dịch vụ cụ thể.")
                            .suggestions(new ArrayList<>())
                            .availableBays(new ArrayList<>())
                            .suggestedServices(new ArrayList<>())
                            .state(buildAvailabilityState(request))
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
            
            // CRITICAL: Validate date không được trong quá khứ
            LocalDate today = LocalDate.now();
            if (date.isBefore(today)) {
                log.warn("User attempted to check availability in the past: {} (today: {})", date, today);
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Không thể kiểm tra lịch trong quá khứ. Vui lòng chọn ngày từ hôm nay trở đi.")
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }
            
            log.info("Date validation passed in checkAvailability: {} (today: {})", date, today);

            // 3. Parse branch (nếu có branch_id hoặc branch_name)
            UUID parsedBranchId = null;
            log.info("Parsing branch - branch_id from request: '{}', branch_name from request: '{}'", 
                    request.getBranchId(), request.getBranchName());
            
            if (request.getBranchId() != null && !request.getBranchId().trim().isEmpty()) {
                try {
                    parsedBranchId = UUID.fromString(request.getBranchId());
                    log.info("Successfully parsed branch_id to UUID: {}", parsedBranchId);
                    
                    // CRITICAL: Validate branch tồn tại ngay sau khi parse
                    Branch testBranch = branchService.findById(parsedBranchId).orElse(null);
                    if (testBranch == null) {
                        log.error("CRITICAL: Parsed branch_id '{}' does not exist in database! This UUID is invalid. Attempting fallback...", parsedBranchId);
                        
                        // Fallback: Try to find by name if provided
                        Branch branchByName = null;
                        if (request.getBranchName() != null && !request.getBranchName().trim().isEmpty()) {
                            log.info("Attempting to find branch by name as fallback: '{}'", request.getBranchName());
                            branchByName = findBranchByNameOrAddress(request.getBranchName().trim());
                        }
                        
                        // Nếu vẫn không tìm thấy, thử tìm tất cả branches active để gợi ý
                        if (branchByName == null) {
                            log.warn("Branch not found by ID '{}' or name '{}'. Returning error with branch suggestions.", 
                                    parsedBranchId, request.getBranchName());
                            
                            // Lấy danh sách tất cả branches để gợi ý
                            List<Branch> allBranches = branchService.findAllActiveBranches();
                            String branchList = allBranches.stream()
                                    .map(b -> String.format("- %s (ID: %s)", b.getBranchName(), b.getBranchId()))
                                    .collect(Collectors.joining("\n"));
                            
                            return AvailabilityResponse.builder()
                                    .status("FULL")
                                    .message(String.format(
                                            "Không tìm thấy chi nhánh với ID: %s. UUID này không tồn tại trong hệ thống.\n\n" +
                                            "Vui lòng chọn lại chi nhánh từ danh sách sau:\n%s\n\n" +
                                            "LƯU Ý: Khi gọi checkAvailability(), bạn PHẢI dùng branch_id (UUID) từ lần chọn chi nhánh gần nhất, " +
                                            "KHÔNG được dùng branch_id từ response cũ hoặc tự bịa ra.",
                                            parsedBranchId, branchList))
                                    .suggestions(new ArrayList<>())
                                    .availableBays(new ArrayList<>())
                                    .suggestedServices(new ArrayList<>())
                                    .build();
                        } else {
                            // Tìm thấy branch theo tên, dùng UUID đúng
                            parsedBranchId = branchByName.getBranchId();
                            log.warn("Found branch by name '{}' with correct UUID: {}. Using this UUID instead of invalid UUID '{}'.", 
                                    branchByName.getBranchName(), parsedBranchId, request.getBranchId());
                        }
                    } else {
                        log.info("Branch validation passed: branch_id={}, branch_name='{}'", 
                                parsedBranchId, testBranch.getBranchName());
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid branch_id format: '{}'. Will try to find by name.", request.getBranchId());
                }
            }
            if (parsedBranchId == null && request.getBranchName() != null && !request.getBranchName().trim().isEmpty()) {
                // Tìm branch theo tên với logic matching linh hoạt hơn
                log.info("Finding branch by name: '{}'", request.getBranchName());
                Branch branch = findBranchByNameOrAddress(request.getBranchName().trim());

                if (branch == null) {
                    log.error("Branch not found by name: '{}'", request.getBranchName());
                    return AvailabilityResponse.builder()
                            .status("FULL")
                            .message("Không tìm thấy chi nhánh: " + request.getBranchName()
                                    + ". Vui lòng kiểm tra lại tên chi nhánh hoặc chọn từ danh sách.")
                            .suggestions(new ArrayList<>())
                            .availableBays(new ArrayList<>())
                            .suggestedServices(new ArrayList<>())
                            .build();
                }

                parsedBranchId = branch.getBranchId();
                log.info("Found branch by name '{}' -> branch_id: {}, branch_name: '{}'", 
                        request.getBranchName(), parsedBranchId, branch.getBranchName());
            }
            
            if (parsedBranchId == null) {
                log.error("CRITICAL: Cannot parse branch - both branch_id and branch_name are null or invalid");
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Vui lòng cung cấp branch_id hoặc branch_name. Bạn muốn đặt lịch ở chi nhánh nào?")
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }
            
            // Final reference để sử dụng trong lambda
            final UUID branchId = parsedBranchId;
            
            // Variables để lưu inventory warning (nếu có) - khai báo ở ngoài scope để dùng sau
            String inventoryWarning = null;
            List<AvailabilityResponse.SuggestedServiceInfo> suggestedServicesForInventory = new ArrayList<>();
            
            // CRITICAL: Validate branch tồn tại ngay từ đầu (nếu có branchId)
            Branch validatedBranch = null;
            if (branchId != null) {
                validatedBranch = branchService.findById(branchId).orElse(null);
                if (validatedBranch == null) {
                    log.error("CRITICAL: Branch not found with branch_id: {} (from request: branch_id='{}', branch_name='{}')", 
                            branchId, request.getBranchId(), request.getBranchName());
                    return AvailabilityResponse.builder()
                            .status("FULL")
                            .message("Không tìm thấy chi nhánh với ID: " + branchId + 
                                    ". Có thể UUID không đúng hoặc chi nhánh đã bị xóa. " +
                                    "Vui lòng chọn lại chi nhánh từ danh sách hoặc liên hệ admin.")
                            .suggestions(new ArrayList<>())
                            .availableBays(new ArrayList<>())
                            .suggestedServices(new ArrayList<>())
                            .build();
                }
                
                log.info("Branch validation passed: branch_id={}, branch_name='{}'", 
                        branchId, validatedBranch.getBranchName());
            }

            if (branchId != null && validatedBranch != null) {
                
                // Check inventory cho service đã chọn - QUAN TRỌNG: Kiểm tra tồn kho sản phẩm
                // Sử dụng batch-check-services để kiểm tra service cụ thể
                long inventoryCheckStart = System.currentTimeMillis();
                log.info("Checking inventory for selected service '{}' at branch {} (name: '{}') using batch-check", 
                        finalService.getServiceName(), branchId, validatedBranch.getBranchName());
                
                // Gọi batch-check-services với service đã chọn
                List<UUID> serviceIdsToCheck = new ArrayList<>();
                serviceIdsToCheck.add(finalService.getServiceId());
                
                BranchServiceFilterResult filterResult;
                try {
                    filterResult = branchServiceFilterService
                            .checkMultipleServicesAvailability(branchId, serviceIdsToCheck, true);
                } catch (ClientSideException e) {
                    if (e.getCode() == ErrorCode.NOT_FOUND && e.getMessage().contains("Branch not found")) {
                        log.error("CRITICAL: Branch not found in checkMultipleServicesAvailability: branch_id={}", branchId);
                        return AvailabilityResponse.builder()
                                .status("FULL")
                                .message("Không tìm thấy chi nhánh. Vui lòng chọn lại chi nhánh hoặc liên hệ admin.")
                                .suggestions(new ArrayList<>())
                                .availableBays(new ArrayList<>())
                                .suggestedServices(new ArrayList<>())
                                .build();
                    }
                    throw e; // Re-throw other exceptions
                }
                
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
                    
                    log.warn("⚠️ Selected service '{}' is not available at branch {} due to inventory issues: {}", 
                            finalService.getServiceName(), branchId, errorMessage);
                    log.warn("⚠️ BUT: Will continue to check bay availability anyway, so user can see available bays and choose another service");
                    
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
                    
                    // LƯU Ý: KHÔNG return ngay, mà tiếp tục check bay
                    // Sau đó sẽ return với availableBays + suggestedServices + warning message
                    // Store inventory warning để dùng sau
                    inventoryWarning = errorMessage;
                    suggestedServicesForInventory = availableServiceSuggestions;
                    
                    // Continue to check bays, will handle inventory warning later
                    // (We'll add this to the response after checking bays)
                } else {
                    log.info("✅ Selected service '{}' passed inventory check at branch {}", 
                            finalService.getServiceName(), branchId);
                }
            }

            // 4. Lấy service bays - BẮT BUỘC phải có branchId (theo quy trình, khách đã chọn chi nhánh ở STEP 5-6)
            if (branchId == null) {
                log.error("branchId is null when getting service bays. This should not happen if user has selected a branch.");
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Vui lòng chọn chi nhánh trước khi xem danh sách bay. Bạn muốn đặt lịch ở chi nhánh nào?")
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }
            
            // CRITICAL: Validate branch tồn tại trước khi lấy bays (reuse validatedBranch nếu đã có)
            Branch branchForBays = validatedBranch;
            if (branchForBays == null && branchId != null) {
                branchForBays = branchService.findById(branchId).orElse(null);
                if (branchForBays == null) {
                    log.error("CRITICAL: Branch not found when getting service bays: branch_id={} (from request: branch_id='{}', branch_name='{}')", 
                            branchId, request.getBranchId(), request.getBranchName());
                    return AvailabilityResponse.builder()
                            .status("FULL")
                            .message("Không tìm thấy chi nhánh với ID: " + branchId + 
                                    ". Có thể UUID không đúng hoặc chi nhánh đã bị xóa. " +
                                    "Vui lòng chọn lại chi nhánh từ danh sách hoặc liên hệ admin.")
                            .suggestions(new ArrayList<>())
                            .availableBays(new ArrayList<>())
                            .suggestedServices(new ArrayList<>())
                            .build();
                }
            }
            
            // QUAN TRỌNG: Chỉ lấy bays của branch đã chọn, KHÔNG lấy toàn bộ hệ thống
            log.info("Getting service bays for branch: '{}' (branch_id: {})", 
                branchForBays != null ? branchForBays.getBranchName() : "Unknown", branchId);
            
            List<ServiceBay> serviceBays = serviceBayService.findBookingAllowedBaysByBranch(branchId);
            
            log.info("Found {} booking-allowed bays for branch: {}", serviceBays.size(), branchId);
            
            if (serviceBays.isEmpty()) {
                String branchName = branchForBays != null ? branchForBays.getBranchName() : "chi nhánh này";
                log.warn("No booking-allowed bays found for branch: '{}' (branch_id: {})", branchName, branchId);
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Không có service bay nào cho phép đặt lịch tại " + branchName)
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .build();
            }
            
            // Validate tất cả bays đều thuộc branch đã chọn (double check)
            List<ServiceBay> invalidBays = serviceBays.stream()
                    .filter(bay -> bay.getBranch() == null || !bay.getBranch().getBranchId().equals(branchId))
                    .collect(Collectors.toList());
            
            if (!invalidBays.isEmpty()) {
                log.error("CRITICAL: Found {} bays that do not belong to branch {}: {}", 
                    invalidBays.size(), branchId, 
                    invalidBays.stream().map(ServiceBay::getBayName).collect(Collectors.joining(", ")));
                // Filter out invalid bays
                serviceBays = serviceBays.stream()
                        .filter(bay -> bay.getBranch() != null && bay.getBranch().getBranchId().equals(branchId))
                        .collect(Collectors.toList());
                log.info("Filtered out invalid bays. Remaining {} valid bays for branch: {}", serviceBays.size(), branchId);
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
                            log.debug("Checking bay: {} (ID: {}) for date: {}, serviceDuration: {} minutes", 
                                    bay.getBayName(), bay.getBayId(), finalDate, finalServiceDuration);
                            
                            AvailableTimeRangesResponse timeRangesResponse = bookingTimeRangeService
                                    .getAvailableTimeRanges(bay.getBayId(), finalDate);

                            log.info("Bay {} - Raw time ranges from API: {}", bay.getBayName(), 
                                    timeRangesResponse.getAvailableTimeRanges().stream()
                                            .map(r -> {
                                                long duration = java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes();
                                                return String.format("%s - %s (duration: %d min)", 
                                                        r.getStartTime(), r.getEndTime(), duration);
                                            })
                                            .collect(Collectors.joining(", ")));

                            // Filter ranges đủ lớn cho service duration
                            List<TimeRangeDto> suitableRanges = filterRangesByDuration(
                                    timeRangesResponse.getAvailableTimeRanges(),
                                    finalServiceDuration);

                            log.info("Bay {} - Suitable ranges (duration >= {} min): {} ranges", 
                                    bay.getBayName(), finalServiceDuration, suitableRanges.size());
                            if (!suitableRanges.isEmpty()) {
                                log.info("Bay {} - Suitable range details: {}", 
                                        bay.getBayName(),
                                        suitableRanges.stream()
                                                .map(r -> {
                                                    long duration = java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes();
                                                    return String.format("%s - %s (duration: %d min)", 
                                                            r.getStartTime(), r.getEndTime(), duration);
                                                })
                                                .collect(Collectors.joining(", ")));
                            } else {
                                log.warn("Bay {} - NO suitable ranges found! All ranges are too small for {} minutes service", 
                                        bay.getBayName(), finalServiceDuration);
                            }

                            if (!suitableRanges.isEmpty()) {
                                // Convert time ranges thành slots
                                List<String> slots = convertTimeRangesToSlots(
                                        suitableRanges,
                                        getDefaultWorkingHours(),
                                        finalServiceDuration);

                                log.info("Bay {} - Converted {} suitable ranges to {} slots: {}", 
                                        bay.getBayName(), suitableRanges.size(), slots.size(), slots);
                                
                                if (slots.isEmpty()) {
                                    log.error("CRITICAL: Bay {} has {} suitable ranges but 0 slots generated! This should not happen!", 
                                            bay.getBayName(), suitableRanges.size());
                                }

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
                            } else {
                                log.warn("⚠️ Bay {} - No slots generated from {} suitable ranges! This is a bug!", 
                                        bay.getBayName(), suitableRanges.size());
                                log.warn("  - Suitable ranges: {}", suitableRanges);
                                log.warn("  - Service duration: {} minutes", finalServiceDuration);
                            }
                        } else {
                            log.warn("⚠️ Bay {} - No suitable ranges found (all ranges too small for {} minutes duration)", 
                                    bay.getBayName(), finalServiceDuration);
                            log.warn("  - Raw time ranges count: {}", timeRangesResponse.getAvailableTimeRanges().size());
                            if (!timeRangesResponse.getAvailableTimeRanges().isEmpty()) {
                                log.warn("  - Largest range duration: {} minutes", 
                                        timeRangesResponse.getAvailableTimeRanges().stream()
                                                .mapToLong(r -> java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes())
                                                .max()
                                                .orElse(0));
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
                            log.error("Error getting time ranges for bay {}: {}", bay.getBayId(), e.getMessage(), e);
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
            
            // Log chi tiết khi không có bay trống
            if (availableBays.isEmpty() && !serviceBays.isEmpty()) {
                log.warn("⚠️ NO AVAILABLE BAYS FOUND - Debugging info:");
                log.warn("  - Total service bays checked: {}", serviceBays.size());
                log.warn("  - Service: {} (duration: {} minutes)", service.getServiceName(), serviceDuration);
                log.warn("  - Branch: {} (branch_id: {})", 
                        branchForBays != null ? branchForBays.getBranchName() : "Unknown", branchId);
                log.warn("  - Date: {}", date);
                log.warn("  - Possible reasons:");
                log.warn("    1. All bays have no suitable time ranges (duration < {} minutes)", serviceDuration);
                log.warn("    2. All bays are fully booked");
                log.warn("    3. Branch is closed on this date");
                log.warn("    4. Time ranges are filtered out by working hours");
            }
            
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
                AvailabilityResponse.BookingState errorState = buildAvailabilityState(request);
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message(branchClosedMessage)
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .state(errorState)
                        .build();
            }

            // 6. Build response
            if (availableBays.isEmpty()) {
                AvailabilityResponse.BookingState errorState = buildAvailabilityState(request);
                
                // Log chi tiết lý do tại sao không có bay trống
                log.error("❌ RETURNING FULL STATUS - No available bays found");
                log.error("  - Service: {} (ID: {}, duration: {} minutes)", 
                        service.getServiceName(), service.getServiceId(), serviceDuration);
                log.error("  - Branch: {} (ID: {})", 
                        branchForBays != null ? branchForBays.getBranchName() : "Unknown", branchId);
                log.error("  - Date: {}", date);
                log.error("  - Total service bays in branch: {}", serviceBays.size());
                log.error("  - This should NOT happen if there are available time slots in the database");
                
                return AvailabilityResponse.builder()
                        .status("FULL")
                        .message("Không có slot trống phù hợp cho dịch vụ '" + service.getServiceName() +
                                "' (thời lượng: " + serviceDuration + " phút) vào ngày " + date)
                        .suggestions(new ArrayList<>())
                        .availableBays(new ArrayList<>())
                        .suggestedServices(new ArrayList<>())
                        .state(errorState)
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
            
            // Build state tracking - dùng lại state đã khai báo ở đầu method
            // Update state: đã có bay_id từ availableBays
            if (!availableBays.isEmpty()) {
                state = AvailabilityResponse.BookingState.builder()
                        .currentStep(6) // STEP 6: Chọn giờ
                        .hasVehicleId(state.getHasVehicleId())
                        .hasDateTime(state.getHasDateTime())
                        .hasBranchId(state.getHasBranchId())
                        .hasServiceType(state.getHasServiceType())
                        .hasBayId(true) // Đã có danh sách bay
                        .hasTimeSlot(false)
                        .missingData(List.of("time_slot"))
                        .nextAction(getNextActionForStep(6))
                        .build();
            }
            
            // Build response message - nếu có inventory warning, thêm vào message
            String responseMessage = "Tìm thấy " + allSuggestions.size() + " khung giờ trống phù hợp";
            if (inventoryWarning != null && !inventoryWarning.isEmpty()) {
                responseMessage = inventoryWarning + ". " + responseMessage + ". Bạn có thể chọn dịch vụ khác từ danh sách gợi ý.";
                log.info("⚠️ Including inventory warning in response: {}", inventoryWarning);
            }
            
            return AvailabilityResponse.builder()
                    .status("AVAILABLE")
                    .slot(specificSlot)
                    .suggestions(allSuggestions)
                    .availableBays(availableBays)
                    .message(responseMessage)
                    .suggestedServices(suggestedServicesForInventory) // Include suggested services if inventory check failed
                    .state(state)
                    .build();

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Error in checkAvailability() after {} ms: {}", (endTime - startTime), e.getMessage(), e);
            AvailabilityResponse.BookingState state = buildAvailabilityState(request);
            return AvailabilityResponse.builder()
                    .status("FULL")
                    .message("Có lỗi xảy ra khi kiểm tra slot: " + e.getMessage())
                    .suggestions(new ArrayList<>())
                    .availableBays(new ArrayList<>())
                    .suggestedServices(new ArrayList<>())
                    .state(state)
                    .build();
        }
    }

    /**
     * Tìm branch theo UUID ID
     */
    public Branch findBranchById(UUID branchId) {
        return branchService.findById(branchId).orElse(null);
    }
    
    /**
     * Build state tracking từ request
     * Giúp AI biết đang ở bước nào và cần làm gì tiếp theo
     */
    private AvailabilityResponse.BookingState buildAvailabilityState(AvailabilityRequest request) {
        boolean hasVehicleId = false; // checkAvailability không có vehicle_id
        boolean hasDateTime = request.getDateTime() != null && !request.getDateTime().trim().isEmpty();
        boolean hasBranchId = request.getBranchId() != null && !request.getBranchId().trim().isEmpty();
        boolean hasBranchName = request.getBranchName() != null && !request.getBranchName().trim().isEmpty();
        boolean hasServiceType = request.getServiceType() != null && !request.getServiceType().trim().isEmpty();
        boolean hasBayId = false; // checkAvailability không có bay_id
        boolean hasTimeSlot = false; // checkAvailability không có time_slot
        
        // Xác định current_step dựa trên dữ liệu có
        int currentStep = 1;
        List<String> missingData = new ArrayList<>();
        
        if (!hasServiceType) {
            currentStep = 4; // STEP 4: Chọn dịch vụ
            missingData.add("service_type");
        } else if (!hasDateTime) {
            currentStep = 2; // STEP 2: Chọn ngày
            missingData.add("date_time");
        } else if (!hasBranchId && !hasBranchName) {
            currentStep = 3; // STEP 3: Chọn chi nhánh
            missingData.add("branch_id hoặc branch_name");
        } else {
            currentStep = 5; // STEP 5: Chọn bay (đang check availability)
        }
        
        String nextAction = getNextActionForStep(currentStep);
        
        return AvailabilityResponse.BookingState.builder()
                .currentStep(currentStep)
                .hasVehicleId(hasVehicleId)
                .hasDateTime(hasDateTime)
                .hasBranchId(hasBranchId || hasBranchName)
                .hasServiceType(hasServiceType)
                .hasBayId(hasBayId)
                .hasTimeSlot(hasTimeSlot)
                .missingData(missingData)
                .nextAction(nextAction)
                .build();
    }
    
    /**
     * Build state tracking từ createBooking request
     */
    private CreateBookingResponse.BookingState buildCreateBookingState(CreateBookingRequest request) {
        // Tối ưu: Chấp nhận vehicle_id HOẶC vehicle_license_plate
        boolean hasVehicleId = request.getVehicleId() != null && !request.getVehicleId().trim().isEmpty();
        boolean hasVehicleLicensePlate = request.getVehicleLicensePlate() != null && !request.getVehicleLicensePlate().trim().isEmpty();
        boolean hasVehicle = hasVehicleId || hasVehicleLicensePlate;
        
        boolean hasDateTime = request.getDateTime() != null && !request.getDateTime().trim().isEmpty();
        boolean hasBranchId = request.getBranchId() != null && !request.getBranchId().trim().isEmpty();
        boolean hasBranchName = request.getBranchName() != null && !request.getBranchName().trim().isEmpty();
        // serviceType có thể là String hoặc List<String>
        boolean hasServiceType = request.getServiceType() != null && 
                (request.getServiceType() instanceof String ? 
                    !((String) request.getServiceType()).trim().isEmpty() : 
                    request.getServiceType() instanceof List && !((List<?>) request.getServiceType()).isEmpty());
        boolean hasBayId = request.getBayId() != null && !request.getBayId().trim().isEmpty();
        boolean hasBayName = request.getBayName() != null && !request.getBayName().trim().isEmpty();
        boolean hasTimeSlot = hasDateTime && request.getDateTime().contains("T");
        
        // Xác định current_step dựa trên dữ liệu có
        int currentStep = 1;
        List<String> missingData = new ArrayList<>();
        
        if (!hasVehicle) {
            currentStep = 1;
            missingData.add("vehicle_id hoặc vehicle_license_plate");
        } else if (!hasDateTime) {
            currentStep = 2;
            missingData.add("date_time");
        } else if (!hasBranchId && !hasBranchName) {
            currentStep = 3;
            missingData.add("branch_id hoặc branch_name");
        } else if (!hasServiceType) {
            currentStep = 4;
            missingData.add("service_type");
        } else if (!hasBayId && !hasBayName) {
            currentStep = 5;
            missingData.add("bay_id hoặc bay_name");
        } else if (!hasTimeSlot) {
            currentStep = 6;
            missingData.add("time_slot (date_time phải có giờ)");
        } else {
            currentStep = 7; // Đã đủ dữ liệu, có thể tạo booking
        }
        
        String nextAction = getNextActionForStep(currentStep);
        
        return CreateBookingResponse.BookingState.builder()
                .currentStep(currentStep)
                .hasVehicleId(hasVehicle) // Updated: chấp nhận vehicle_id hoặc license_plate
                .hasDateTime(hasDateTime)
                .hasBranchId(hasBranchId || hasBranchName)
                .hasServiceType(hasServiceType)
                .hasBayId(hasBayId || hasBayName)
                .hasTimeSlot(hasTimeSlot)
                .missingData(missingData)
                .nextAction(nextAction)
                .build();
    }
    
    /**
     * Lấy next action cho từng step
     */
    private String getNextActionForStep(int step) {
        switch (step) {
            case 1:
                return "Gọi getCustomerVehicles() và yêu cầu user chọn xe";
            case 2:
                return "Yêu cầu user chọn ngày đặt lịch";
            case 3:
                return "Gọi getBranches() và yêu cầu user chọn chi nhánh";
            case 4:
                return "Yêu cầu user chọn dịch vụ";
            case 5:
                return "Gọi checkAvailability() và yêu cầu user chọn bay";
            case 6:
                return "Yêu cầu user chọn giờ từ danh sách available slots";
            case 7:
                return "Xác nhận thông tin và gọi createBooking()";
            default:
                return "Kiểm tra lại dữ liệu";
        }
    }
    
    /**
     * Validate step-by-step cho checkAvailability
     * Trả về null nếu pass, trả về error response nếu fail
     */
    private AvailabilityResponse validateCheckAvailabilitySteps(AvailabilityRequest request) {
        // STEP 4: Chọn dịch vụ
        if (request.getServiceType() == null || request.getServiceType().trim().isEmpty()) {
            return AvailabilityResponse.builder()
                    .status("MISSING_DATA")
                    .message("Bạn cần chọn dịch vụ trước. (STEP 4: Chọn dịch vụ)")
                    .suggestions(new ArrayList<>())
                    .availableBays(new ArrayList<>())
                    .suggestedServices(new ArrayList<>())
                    .state(buildAvailabilityState(request))
                    .build();
        }
        
        // STEP 2: Chọn ngày
        if (request.getDateTime() == null || request.getDateTime().trim().isEmpty()) {
            return AvailabilityResponse.builder()
                    .status("MISSING_DATA")
                    .message("Bạn cần chọn ngày đặt lịch trước. (STEP 2: Chọn ngày)")
                    .suggestions(new ArrayList<>())
                    .availableBays(new ArrayList<>())
                    .suggestedServices(new ArrayList<>())
                    .state(buildAvailabilityState(request))
                    .build();
        }
        
        // STEP 3: Chọn chi nhánh
        if ((request.getBranchId() == null || request.getBranchId().trim().isEmpty()) &&
            (request.getBranchName() == null || request.getBranchName().trim().isEmpty())) {
            return AvailabilityResponse.builder()
                    .status("MISSING_DATA")
                    .message("Bạn cần chọn chi nhánh trước. (STEP 3: Chọn chi nhánh)")
                    .suggestions(new ArrayList<>())
                    .availableBays(new ArrayList<>())
                    .suggestedServices(new ArrayList<>())
                    .state(buildAvailabilityState(request))
                    .build();
        }
        
        return null; // Pass validation
    }
    
    /**
     * Validate step-by-step cho createBooking
     * Trả về null nếu pass, trả về error response nếu fail
     */
    private CreateBookingResponse validateCreateBookingSteps(CreateBookingRequest request) {
        // STEP 1: Chọn xe - Chấp nhận vehicle_id HOẶC vehicle_license_plate
        boolean hasVehicleInfo = (request.getVehicleId() != null && !request.getVehicleId().trim().isEmpty()) ||
                                 (request.getVehicleLicensePlate() != null && !request.getVehicleLicensePlate().trim().isEmpty());
        if (!hasVehicleInfo) {
            CreateBookingResponse.BookingState state = buildCreateBookingState(request);
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message("Thiếu thông tin xe (vehicle_id hoặc vehicle_license_plate). Bạn cần hoàn thành STEP 1 (Chọn xe) trước.")
                    .state(state)
                    .failedStep(1)
                    .build();
        }
        
        // STEP 2: Chọn ngày
        if (request.getDateTime() == null || request.getDateTime().trim().isEmpty()) {
            CreateBookingResponse.BookingState state = buildCreateBookingState(request);
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message("Thiếu date_time. Bạn cần hoàn thành STEP 2 (Chọn ngày) trước.")
                    .state(state)
                    .failedStep(2)
                    .build();
        }
        
        // STEP 3: Chọn chi nhánh
        if ((request.getBranchId() == null || request.getBranchId().trim().isEmpty()) &&
            (request.getBranchName() == null || request.getBranchName().trim().isEmpty())) {
            CreateBookingResponse.BookingState state = buildCreateBookingState(request);
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message("Thiếu branch_id hoặc branch_name. Bạn cần hoàn thành STEP 3 (Chọn chi nhánh) trước.")
                    .state(state)
                    .failedStep(3)
                    .build();
        }
        
        // STEP 4: Chọn dịch vụ
        boolean hasServiceType = request.getServiceType() != null && 
                (request.getServiceType() instanceof String ? 
                    !((String) request.getServiceType()).trim().isEmpty() : 
                    request.getServiceType() instanceof List && !((List<?>) request.getServiceType()).isEmpty());
        if (!hasServiceType) {
            CreateBookingResponse.BookingState state = buildCreateBookingState(request);
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message("Thiếu service_type. Bạn cần hoàn thành STEP 4 (Chọn dịch vụ) trước.")
                    .state(state)
                    .failedStep(4)
                    .build();
        }
        
        // STEP 5: Chọn bay
        if ((request.getBayId() == null || request.getBayId().trim().isEmpty()) &&
            (request.getBayName() == null || request.getBayName().trim().isEmpty())) {
            CreateBookingResponse.BookingState state = buildCreateBookingState(request);
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message("Thiếu bay_id hoặc bay_name. Bạn cần hoàn thành STEP 5 (Chọn bay) trước.")
                    .state(state)
                    .failedStep(5)
                    .build();
        }
        
        // STEP 6: Chọn giờ
        if (request.getDateTime() == null || !request.getDateTime().contains("T")) {
            CreateBookingResponse.BookingState state = buildCreateBookingState(request);
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message("Thiếu time_slot. date_time phải có giờ (format: YYYY-MM-DDTHH:mm). Bạn cần hoàn thành STEP 6 (Chọn giờ) trước.")
                    .state(state)
                    .failedStep(6)
                    .build();
        }
        
        return null; // Pass validation
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
     * 
     * QUAN TRỌNG: Làm tròn rangeStart về phút tròn (00 hoặc 30) để đảm bảo slots đúng format
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
            
            log.debug("Converting range to slots: {} - {}, serviceDuration: {} minutes", 
                    rangeStart, rangeEnd, serviceDurationMinutes);

            // QUAN TRỌNG: Làm tròn rangeStart về phút tròn (00 hoặc 30) để tạo slots
            // Logic: Làm tròn XUỐNG về slot gần nhất, sau đó check xem slot đó có fit trong range không
            // Ví dụ: 08:30:01 → làm tròn xuống 08:30:00, check slot 08:30:00 + 30min = 09:00:00 có fit trong range 08:30:01-18:00:00 không → CÓ → dùng 08:30:00
            // Ví dụ: 08:31:00 → làm tròn xuống 08:30:00, check slot 08:30:00 + 30min = 09:00:00 có fit trong range 08:31:00-18:00:00 không → CÓ → dùng 08:30:00
            // Ví dụ: 08:30:00 → dùng luôn 08:30:00
            
            int startMinute = rangeStart.getMinute();
            int roundedDownMinute = (startMinute / slotIntervalMinutes) * slotIntervalMinutes;
            LocalTime roundedDown = rangeStart.withMinute(roundedDownMinute).withSecond(0).withNano(0);
            
            // Check xem slot làm tròn xuống có fit trong range không
            // Slot fit nếu: slotStart + serviceDuration <= rangeEnd
            LocalTime testSlotEnd = roundedDown.plusMinutes(serviceDurationMinutes);
            boolean roundedDownFits = !testSlotEnd.isAfter(rangeEnd);
            
            LocalTime roundedStart;
            if (roundedDownFits && !roundedDown.isAfter(rangeEnd)) {
                // Slot làm tròn xuống fit trong range → dùng nó
                roundedStart = roundedDown;
                log.debug("Rounded DOWN rangeStart from {} to {} (slot {} + {}min = {} fits in range)", 
                        rangeStart, roundedStart, roundedStart, serviceDurationMinutes, testSlotEnd);
            } else {
                // Slot làm tròn xuống không fit → làm tròn LÊN slot tiếp theo
                roundedStart = roundedDown.plusMinutes(slotIntervalMinutes);
                log.debug("Rounded UP rangeStart from {} to {} (rounded down slot {} + {}min = {} does not fit in range)", 
                        rangeStart, roundedStart, roundedDown, serviceDurationMinutes, testSlotEnd);
            }
            
            // Đảm bảo roundedStart không vượt quá rangeEnd
            if (!roundedStart.isBefore(rangeEnd)) {
                log.debug("Rounded start {} is after or equal to rangeEnd {}, skipping range", roundedStart, rangeEnd);
                continue;
            }
            
            log.debug("Rounded rangeStart from {} to {}", rangeStart, roundedStart);

            LocalTime current = roundedStart;
            while (current.isBefore(rangeEnd)) {
                LocalTime slotEnd = current.plusMinutes(serviceDurationMinutes);

                // Check nếu slot + duration fit trong range
                // QUAN TRỌNG: slotEnd phải <= rangeEnd (không vượt quá)
                if (!slotEnd.isAfter(rangeEnd)) {
                    String slotTime = current.format(DateTimeFormatter.ofPattern("HH:mm"));
                    if (!slots.contains(slotTime)) {
                        slots.add(slotTime);
                        log.debug("Added slot: {} (slotEnd: {})", slotTime, slotEnd);
                    }
                } else {
                    log.debug("Slot {} (slotEnd: {}) does not fit in range (rangeEnd: {}), stopping", 
                            current, slotEnd, rangeEnd);
                    break; // Slot tiếp theo cũng sẽ không fit, dừng lại
                }

                current = current.plusMinutes(slotIntervalMinutes);
            }
        }

        List<String> sortedSlots = slots.stream().sorted().collect(Collectors.toList());
        log.debug("Converted {} time ranges to {} slots: {}", timeRanges.size(), sortedSlots.size(), sortedSlots);
        return sortedSlots;
    }

    @Transactional
    public CreateBookingResponse createBooking(CreateBookingRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("=== CREATE BOOKING START ===");
        log.info("AI Function: createBooking() called with request: {}", request);
        log.info("Request details - branch_id: '{}', branch_name: '{}', bay_id: '{}', bay_name: '{}', vehicle_id: '{}', service_type: '{}', date_time: '{}'",
                request.getBranchId(), request.getBranchName(), request.getBayId(), request.getBayName(),
                request.getVehicleId(), request.getServiceType(), request.getDateTime());

        try {
            // STEP-BY-STEP VALIDATION: Kiểm tra dữ liệu theo từng bước
            CreateBookingResponse validationError = validateCreateBookingSteps(request);
            if (validationError != null) {
                log.warn("=== VALIDATION FAILED ===");
                log.warn("Step: {}, Failed step: {}, Missing data: {}, Message: {}", 
                        validationError.getState() != null ? validationError.getState().getCurrentStep() : "unknown",
                        validationError.getFailedStep(),
                        validationError.getState() != null ? validationError.getState().getMissingData() : "unknown",
                        validationError.getMessage());
                return validationError;
            }
            
            // Log state tracking
            CreateBookingResponse.BookingState state = buildCreateBookingState(request);
            log.info("=== STATE TRACKING ===");
            log.info("Current step: {}, Has vehicle: {}, Has date: {}, Has branch: {}, Has service: {}, Has bay: {}, Has time: {}",
                    state.getCurrentStep(), state.getHasVehicleId(), state.getHasDateTime(), 
                    state.getHasBranchId(), state.getHasServiceType(), state.getHasBayId(), state.getHasTimeSlot());
            log.info("Missing data: {}, Next action: {}", state.getMissingData(), state.getNextAction());
            
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
                    
                    if (vehicle == null) {
                        log.error("Vehicle not found with vehicle_id: {}", vehicleId);
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Không tìm thấy xe với ID: " + vehicleId + ". Vui lòng kiểm tra lại.")
                                .build();
                    }
                    
                    // CRITICAL: Validate vehicle thuộc về customer đang đặt lịch
                    if (vehicle.getOwnerId() == null || !vehicle.getOwnerId().equals(customer.getUserId())) {
                        log.error("CRITICAL SECURITY: Vehicle {} (owner_id: {}) does not belong to customer {} (user_id: {})",
                                vehicle.getVehicleId(), vehicle.getOwnerId(), customer.getFullName(), customer.getUserId());
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Xe này không thuộc về tài khoản của bạn. Vui lòng chọn xe khác.")
                                .build();
                    }
                    
                    log.info("Vehicle validation passed - vehicle_id: {}, license_plate: {}, owner_id: {}, customer_id: {}",
                            vehicle.getVehicleId(), vehicle.getLicensePlate(), vehicle.getOwnerId(), customer.getUserId());
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
            
            // Final check: vehicle không được null
            if (vehicle == null) {
                log.error("CRITICAL: Vehicle is null after parsing");
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Không thể xác định thông tin xe. Vui lòng thử lại.")
                        .build();
            }

            // 3. Parse branch (nếu có branch_id hoặc branch_name)
            UUID parsedBranchId = null;
            log.info("Parsing branch - branch_id: '{}', branch_name: '{}'", request.getBranchId(), request.getBranchName());
            
            if (request.getBranchId() != null && !request.getBranchId().trim().isEmpty()) {
                try {
                    parsedBranchId = UUID.fromString(request.getBranchId());
                    log.info("Successfully parsed branch_id to UUID: {}", parsedBranchId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid branch_id format: '{}'. Will try to find by name.", request.getBranchId());
                }
            }
            if (parsedBranchId == null && request.getBranchName() != null
                    && !request.getBranchName().trim().isEmpty()) {
                // Tìm branch theo tên với logic matching linh hoạt hơn
                log.info("Finding branch by name: '{}'", request.getBranchName());
                Branch foundBranch = findBranchByNameOrAddress(request.getBranchName().trim());

                if (foundBranch == null) {
                    log.error("Branch not found by name: '{}'", request.getBranchName());
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message("Không tìm thấy chi nhánh: " + request.getBranchName()
                                    + ". Vui lòng kiểm tra lại tên chi nhánh.")
                            .build();
                }

                parsedBranchId = foundBranch.getBranchId();
                log.info("Found branch by name '{}' -> branch_id: {}, branch_name: {}", 
                    request.getBranchName(), parsedBranchId, foundBranch.getBranchName());
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
            log.info("Parsing bay - bay_id: '{}', bay_name: '{}', branch_id: {}", 
                request.getBayId(), request.getBayName(), branchId);
            
            if (request.getBayId() != null && !request.getBayId().trim().isEmpty()) {
                try {
                    parsedBayId = UUID.fromString(request.getBayId());
                    log.info("Successfully parsed bay_id to UUID: {}", parsedBayId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid bay_id format: '{}'. Will try to find by name.", request.getBayId());
                }
            }
            if (parsedBayId == null && request.getBayName() != null && !request.getBayName().trim().isEmpty()) {
                // QUAN TRỌNG: Chỉ tìm bay trong branch đã chọn, KHÔNG tìm toàn bộ hệ thống
                // Vì có thể có nhiều bay cùng tên ở các branch khác nhau
                log.info("Finding bay by name '{}' in branch '{}' ({})", 
                    request.getBayName(), branch.getBranchName(), branchId);
                
                // Ưu tiên tìm exact match trong branch đã chọn
                List<ServiceBay> baysInBranch = serviceBayService.getByBranch(branchId);
                ServiceBay foundBay = baysInBranch.stream()
                        .filter(bay -> bay.getBayName() != null 
                                && bay.getBayName().equalsIgnoreCase(request.getBayName().trim()))
                        .findFirst()
                        .orElseGet(() -> {
                            // Nếu không tìm thấy exact match, thử search by keyword trong branch
                            log.info("Exact match not found, searching by keyword in branch");
                            List<ServiceBay> bays = serviceBayService.searchByKeywordInBranch(branchId,
                                    request.getBayName().trim());
                            if (!bays.isEmpty()) {
                                log.info("Found {} bay(s) by keyword search", bays.size());
                                return bays.get(0);
                            }
                            return null;
                        });

                if (foundBay == null) {
                    log.error("Bay not found by name '{}' in branch '{}' ({})", 
                        request.getBayName(), branch.getBranchName(), branchId);
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message("Không tìm thấy bay: " + request.getBayName() +
                                    " ở chi nhánh " + branch.getBranchName() +
                                    ". Vui lòng kiểm tra lại tên bay.")
                            .build();
                }

                // Validate bay thuộc branch đã chọn (double check)
                if (foundBay.getBranch() == null || !foundBay.getBranch().getBranchId().equals(branchId)) {
                    log.error("Bay '{}' (ID: {}) does not belong to branch '{}' (ID: {}). Bay's branch: {}", 
                        foundBay.getBayName(), foundBay.getBayId(), 
                        branch.getBranchName(), branchId,
                        foundBay.getBranch() != null ? foundBay.getBranch().getBranchName() : "null");
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message("Bay '" + request.getBayName() + "' không thuộc chi nhánh "
                                    + branch.getBranchName())
                            .build();
                }

                parsedBayId = foundBay.getBayId();
                log.info("Found bay by name '{}' -> bay_id: {}, bay_name: {}, branch: {}", 
                    request.getBayName(), parsedBayId, foundBay.getBayName(), 
                    foundBay.getBranch() != null ? foundBay.getBranch().getBranchName() : "null");
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
            log.info("Validating bay - bay_id: {}, bay_name: '{}', branch_id: {}", 
                bayId, bay.getBayName(), branchId);
            
            if (!bay.isAvailableForBooking()) {
                log.error("Bay '{}' (ID: {}) does not allow booking", bay.getBayName(), bayId);
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Service bay '" + bay.getBayName() + "' không cho phép đặt lịch")
                        .build();
            }

            // Validate bay thuộc branch (final check)
            if (bay.getBranch() == null || !bay.getBranch().getBranchId().equals(branchId)) {
                log.error("CRITICAL: Bay '{}' (ID: {}) does not belong to branch '{}' (ID: {}). Bay's branch: {}", 
                    bay.getBayName(), bayId, branch.getBranchName(), branchId,
                    bay.getBranch() != null ? bay.getBranch().getBranchName() + " (ID: " + bay.getBranch().getBranchId() + ")" : "null");
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Bay '" + bay.getBayName() + "' không thuộc chi nhánh " + branch.getBranchName())
                        .build();
            }
            
            log.info("Bay validation passed - bay: '{}' (ID: {}), branch: '{}' (ID: {})", 
                bay.getBayName(), bayId, branch.getBranchName(), branchId);

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
            
            // CRITICAL: Validate date không được trong quá khứ
            LocalDate today = LocalDate.now();
            if (date.isBefore(today)) {
                log.warn("User attempted to book in the past: {} (today: {})", date, today);
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Không thể đặt lịch trong quá khứ. Vui lòng chọn ngày từ hôm nay trở đi.")
                        .build();
            }
            
            log.info("Date validation passed: {} (today: {})", date, today);

            // Parse start time từ dateTime
            LocalTime bookingStartTime = parseTime(request.getDateTime());
            if (bookingStartTime == null) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Không thể parse giờ: " + request.getDateTime())
                        .build();
            }
            
            log.info("Parsed booking start time: {}", bookingStartTime);

            // Tính total duration
            int totalDuration = availableServices.stream()
                    .mapToInt(s -> s.getEstimatedDuration() != null ? s.getEstimatedDuration() : 0)
                    .sum();
            
            // CRITICAL: Validate totalDuration > 0
            if (totalDuration <= 0) {
                log.error("CRITICAL: Total duration is invalid: {} minutes. Services: {}",
                        totalDuration, availableServices.stream()
                                .map(s -> s.getServiceName() + " (duration: " + s.getEstimatedDuration() + ")")
                                .collect(Collectors.joining(", ")));
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Thời lượng dịch vụ không hợp lệ. Vui lòng chọn dịch vụ khác.")
                        .build();
            }
            
            log.info("Total service duration calculated: {} minutes for {} services", totalDuration, availableServices.size());

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
            if (availableServices.isEmpty()) {
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Không có dịch vụ nào được chọn. Vui lòng chọn ít nhất một dịch vụ.")
                        .build();
            }
            
            List<CreateBookingItemRequest> bookingItems = availableServices.stream()
                    .map(service -> CreateBookingItemRequest.builder()
                            .serviceId(service.getServiceId())
                            .serviceName(service.getServiceName())
                            .serviceDescription(service.getDescription())
                            .build())
                    .collect(Collectors.toList());
            
            // CRITICAL: Validate tất cả services có trong bookingItems
            if (bookingItems.size() != availableServices.size()) {
                log.error("CRITICAL: Booking items count mismatch. Expected: {}, Actual: {}",
                        availableServices.size(), bookingItems.size());
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Lỗi khi tạo danh sách dịch vụ. Vui lòng thử lại.")
                        .build();
            }
            
            // CRITICAL: Validate tất cả bookingItems có serviceId
            List<CreateBookingItemRequest> invalidItems = bookingItems.stream()
                    .filter(item -> item.getServiceId() == null)
                    .collect(Collectors.toList());
            if (!invalidItems.isEmpty()) {
                log.error("CRITICAL: Found {} booking items without serviceId: {}",
                        invalidItems.size(),
                        invalidItems.stream().map(CreateBookingItemRequest::getServiceName).collect(Collectors.joining(", ")));
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Một số dịch vụ không có ID hợp lệ. Vui lòng thử lại.")
                        .build();
            }
            
            log.info("Built {} booking items for services: {}", 
                bookingItems.size(), 
                bookingItems.stream().map(CreateBookingItemRequest::getServiceName).collect(Collectors.joining(", ")));

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
            LocalDateTime scheduledStartAt = LocalDateTime.of(date, bookingStartTime);
            LocalDateTime scheduledEndAt = scheduledStartAt.plusMinutes(totalDuration);

            // 10.1. Validate time range của bay có đủ lớn cho total duration
            try {
                AvailableTimeRangesResponse timeRangesResponse = bookingTimeRangeService
                        .getAvailableTimeRanges(bayId, date);
                
                LocalTime startTimeLocal = scheduledStartAt.toLocalTime();
                LocalTime endTimeLocal = scheduledEndAt.toLocalTime();
                
                // CRITICAL: Validate scheduledEndAt không vượt quá working hours mặc định (18:00)
                if (endTimeLocal.isAfter(DEFAULT_WORKING_HOURS_END)) {
                    log.warn("Service end time {} exceeds default working hours end {} for bay {} on date {}",
                            endTimeLocal, DEFAULT_WORKING_HOURS_END, bayId, date);
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message(String.format(
                                    "Thời gian kết thúc dịch vụ (%s) vượt quá giờ làm việc của hệ thống (%s). " +
                                    "Vui lòng chọn thời gian sớm hơn.",
                                    endTimeLocal.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    DEFAULT_WORKING_HOURS_END.format(DateTimeFormatter.ofPattern("HH:mm"))))
                            .build();
                }
                
                // CRITICAL: Validate scheduledStartAt không trước working hours start mặc định (8:00)
                if (startTimeLocal.isBefore(DEFAULT_WORKING_HOURS_START)) {
                    log.warn("Service start time {} is before default working hours start {} for bay {} on date {}",
                            startTimeLocal, DEFAULT_WORKING_HOURS_START, bayId, date);
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message(String.format(
                                    "Thời gian bắt đầu dịch vụ (%s) trước giờ làm việc của hệ thống (%s). " +
                                    "Vui lòng chọn thời gian sau %s.",
                                    startTimeLocal.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    DEFAULT_WORKING_HOURS_START.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    DEFAULT_WORKING_HOURS_START.format(DateTimeFormatter.ofPattern("HH:mm"))))
                            .build();
                }
                
                // Kiểm tra xem có time range nào chứa scheduledStartAt đến scheduledEndAt không
                boolean fitsInTimeRange = timeRangesResponse.getAvailableTimeRanges().stream()
                        .anyMatch(range -> {
                            LocalTime rangeStart = range.getStartTime();
                            LocalTime rangeEnd = range.getEndTime();
                            
                            // Check nếu scheduledStartAt >= rangeStart và scheduledEndAt <= rangeEnd
                            return !startTimeLocal.isBefore(rangeStart) 
                                    && !endTimeLocal.isAfter(rangeEnd);
                        });
                
                if (!fitsInTimeRange) {
                    log.warn("Service duration {} minutes does not fit in any available time range for bay {} on date {}. " +
                            "Start: {}, End: {}", totalDuration, bayId, date, startTimeLocal, endTimeLocal);
                    
                    // Lấy các time ranges phù hợp với total duration
                    List<TimeRangeDto> suitableRanges = filterRangesByDuration(
                            timeRangesResponse.getAvailableTimeRanges(),
                            totalDuration);
                    
                    if (suitableRanges.isEmpty()) {
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message(String.format(
                                        "Không có khung giờ nào đủ lớn cho dịch vụ (thời lượng: %d phút) tại bay '%s' vào ngày %s. " +
                                        "Vui lòng chọn bay hoặc thời gian khác.",
                                        totalDuration, bay.getBayName(), date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                                .build();
                    }
                    
                    // Convert suitable ranges thành slots để gợi ý
                    List<String> suggestedSlots = convertTimeRangesToSlots(
                            suitableRanges,
                            getDefaultWorkingHours(),
                            totalDuration);
                    
                    String suggestionsText = !suggestedSlots.isEmpty() 
                            ? "Các khung giờ khả dụng: " + String.join(", ", suggestedSlots)
                            : "Vui lòng chọn khung giờ khác.";
                    
                    return CreateBookingResponse.builder()
                            .status("FAILED")
                            .message(String.format(
                                    "Khung giờ %s không đủ lớn cho dịch vụ (thời lượng: %d phút) tại bay '%s'. %s",
                                    startTimeLocal.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    totalDuration, bay.getBayName(), suggestionsText))
                            .build();
                }
                
                log.info("Time range validation passed: scheduledStartAt={}, scheduledEndAt={}, totalDuration={} minutes",
                        scheduledStartAt, scheduledEndAt, totalDuration);
            } catch (Exception e) {
                log.error("Error validating time range for bay {} on date {}: {}", bayId, date, e.getMessage(), e);
                // Không fail booking nếu có lỗi trong validation, chỉ log warning
                // Vì có thể có edge cases mà validation này không cover được
            }

            // CRITICAL: Final validation - vehicle không được null (defensive check)
            // Note: Vehicle đã được validate ở trên, nhưng đây là double-check cuối cùng trước khi tạo booking
            if (vehicle == null) {
                log.error("CRITICAL: Vehicle is null at final validation step");
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Vui lòng cung cấp thông tin xe (vehicle_id hoặc vehicle_license_plate)")
                        .build();
            }
            
            // CRITICAL: Final validation - vehicle phải thuộc về customer
            if (!vehicle.getOwnerId().equals(customer.getUserId())) {
                log.error("CRITICAL SECURITY: Final check failed - Vehicle {} owner {} does not match customer {}",
                        vehicle.getVehicleId(), vehicle.getOwnerId(), customer.getUserId());
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Xe này không thuộc về tài khoản của bạn. Vui lòng chọn xe khác.")
                        .build();
            }
            
            // CRITICAL: Double-check bay availability ngay trước khi tạo booking (tránh race condition)
            // Đây là lần check cuối cùng trước khi commit transaction
            boolean isBayAvailable = serviceBayService.isBayAvailableInTimeRange(bayId, scheduledStartAt, scheduledEndAt);
            if (!isBayAvailable) {
                log.error("CRITICAL RACE CONDITION: Bay {} is no longer available in time range {} - {} (checked at last moment before booking)",
                        bayId, scheduledStartAt, scheduledEndAt);
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message(String.format(
                                "Bay '%s' không còn trống trong khung giờ %s - %s. Có thể có người khác đã đặt trước. " +
                                "Vui lòng chọn khung giờ khác.",
                                bay.getBayName(),
                                scheduledStartAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                scheduledEndAt.format(DateTimeFormatter.ofPattern("HH:mm"))))
                        .build();
            }
            
            log.info("Final bay availability check passed: bay_id={}, scheduledStartAt={}, scheduledEndAt={}",
                    bayId, scheduledStartAt, scheduledEndAt);

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
                            .startTime(bookingStartTime)
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

            // Build state tracking - đã đủ dữ liệu, ở STEP 7
            CreateBookingResponse.BookingState bookingState = buildCreateBookingState(request);
            
            // 10. Log thông tin trước khi tạo booking - CRITICAL DATA VALIDATION
            log.info("=== FINAL DATA VALIDATION BEFORE CREATING BOOKING ===");
            log.info("Customer: {} (ID: {}, Phone: {})", customer.getFullName(), customer.getUserId(), customer.getPhoneNumber());
            log.info("Vehicle: {} (ID: {}, License: {}, Owner: {})", 
                    vehicle.getLicensePlate(), vehicle.getVehicleId(), vehicle.getLicensePlate(), vehicle.getOwnerId());
            log.info("Branch: '{}' (ID: {})", branch.getBranchName(), branchId);
            log.info("Bay: '{}' (ID: {}, Branch: {})", bay.getBayName(), bayId, 
                    bay.getBranch() != null ? bay.getBranch().getBranchName() : "null");
            log.info("Services: {}", availableServices.stream()
                    .map(s -> s.getServiceName() + " (ID: " + s.getServiceId() + ", Duration: " + 
                            (s.getEstimatedDuration() != null ? s.getEstimatedDuration() : "null") + " min)")
                    .collect(Collectors.joining(", ")));
            log.info("Booking items count: {} (must match services count: {})", bookingItems.size(), availableServices.size());
            log.info("Total duration: {} minutes", totalDuration);
            log.info("Scheduled time: {} - {} ({} minutes)", 
                    scheduledStartAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    scheduledEndAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    totalDuration);
            log.info("Total price: {} VNĐ", totalPrice);
            
            // CRITICAL: Final validation - đảm bảo tất cả data đúng
            if (!customer.getUserId().equals(vehicle.getOwnerId())) {
                log.error("CRITICAL DATA INCONSISTENCY: Customer {} does not own vehicle {}", customer.getUserId(), vehicle.getVehicleId());
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Dữ liệu không hợp lệ: Xe không thuộc về khách hàng. Vui lòng thử lại.")
                        .state(bookingState)
                        .failedStep(7)
                        .build();
            }
            
            if (!bay.getBranch().getBranchId().equals(branchId)) {
                log.error("CRITICAL DATA INCONSISTENCY: Bay {} belongs to branch {} but request has branch {}",
                        bayId, bay.getBranch().getBranchId(), branchId);
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Dữ liệu không hợp lệ: Bay không thuộc chi nhánh đã chọn. Vui lòng thử lại.")
                        .state(bookingState)
                        .failedStep(5)
                        .build();
            }
            
            if (bookingItems.size() != availableServices.size()) {
                log.error("CRITICAL DATA INCONSISTENCY: Booking items count {} does not match services count {}",
                        bookingItems.size(), availableServices.size());
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Dữ liệu không hợp lệ: Số lượng dịch vụ không khớp. Vui lòng thử lại.")
                        .state(bookingState)
                        .failedStep(4)
                        .build();
            }
            
            log.info("=== ALL VALIDATIONS PASSED - PROCEEDING TO CREATE BOOKING ===");
            
            // 11. Gọi IntegratedBookingService
            BookingInfoDto booking = integratedBookingService.createBookingWithSlot(createRequest);
            
            if (booking == null || booking.getBookingId() == null) {
                log.error("CRITICAL: Booking creation returned null or invalid booking");
                return CreateBookingResponse.builder()
                        .status("FAILED")
                        .message("Không thể tạo booking. Vui lòng thử lại sau.")
                        .state(bookingState)
                        .failedStep(7)
                        .build();
            }
            
            log.info("=== BOOKING CREATED SUCCESSFULLY ===");
            log.info("Booking created successfully - booking_code: {}, booking_id: {}", 
                booking.getBookingCode(), booking.getBookingId());
            log.info("Final booking details - Branch: {}, Bay: {}, Services: {}, Time: {} - {}, Price: {} VNĐ",
                    branch.getBranchName(), bay.getBayName(), 
                    availableServices.stream().map(Service::getServiceName).collect(Collectors.joining(", ")),
                    scheduledStartAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    scheduledEndAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    booking.getTotalPrice() != null ? booking.getTotalPrice() : totalPrice);

            // 11. Build response với state tracking
            // Update state: booking đã hoàn thành
            CreateBookingResponse.BookingState successState = CreateBookingResponse.BookingState.builder()
                    .currentStep(7)
                    .hasVehicleId(true)
                    .hasDateTime(true)
                    .hasBranchId(true)
                    .hasServiceType(true)
                    .hasBayId(true)
                    .hasTimeSlot(true)
                    .missingData(new ArrayList<>())
                    .nextAction("Booking đã hoàn thành")
                    .build();
            
            return CreateBookingResponse.builder()
                    .status("SUCCESS")
                    .bookingCode(booking.getBookingCode())
                    .message("Đặt lịch thành công!")
                    .state(successState)
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
            long endTime = System.currentTimeMillis();
            log.error("=== CREATE BOOKING FAILED (Client Error) ===");
            log.error("Error after {} ms: {}", (endTime - startTime), e.getMessage());
            log.error("Error code: {}, Request: {}", e.getCode(), request);
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("=== CREATE BOOKING FAILED (System Error) ===");
            log.error("Error after {} ms: {}", (endTime - startTime), e.getMessage(), e);
            log.error("Request that caused error: {}", request);
            return CreateBookingResponse.builder()
                    .status("FAILED")
                    .message("Có lỗi xảy ra khi tạo booking: " + e.getMessage() + ". Vui lòng thử lại sau.")
                    .build();
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("=== CREATE BOOKING END (Total time: {} ms) ===", (endTime - startTime));
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
