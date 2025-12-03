package com.kltn.scsms_api_service.configs.aiAssistant;

import com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.CreateBookingRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetBranchesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetCustomerVehiclesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetServicesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.AvailabilityResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.CreateBookingResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetBranchesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetCustomerVehiclesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetServicesResponse;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.entity.VehicleProfile;
import com.kltn.scsms_api_service.core.service.aiAssistant.AiBookingAssistantService;
import com.kltn.scsms_api_service.core.service.entityService.BookingDraftService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.service.entityService.VehicleProfileService;
import com.kltn.scsms_api_service.core.utils.PermissionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AiAssistantFunctionsConfig {

    private final AiBookingAssistantService aiBookingAssistantService;
    private final ServiceBayService serviceBayService;
    private final VehicleProfileService vehicleProfileService;
    private final BookingDraftService bookingDraftService;
    private final ServiceService serviceService;

    /**
     * Smart parser để xử lý AI hallucination - tự động parse UUID hoặc tìm kiếm
     * theo tên
     *
     * @param idString             Chuỗi từ AI (có thể là UUID hoặc tên)
     * @param nameFallback         Tên dự phòng nếu idString null
     * @param lookupByNameFunction Hàm tìm kiếm theo tên
     * @return UUID nếu tìm thấy, null nếu không tìm thấy
     */
    private UUID smartParseId(String idString, String nameFallback, Function<String, UUID> lookupByNameFunction) {
        // 1. Nếu cả id và name đều null -> bó tay
        if ((idString == null || idString.trim().isEmpty())
                && (nameFallback == null || nameFallback.trim().isEmpty())) {
            return null;
        }

        // 2. Ưu tiên xử lý idString trước
        if (idString != null && !idString.trim().isEmpty()) {
            try {
                // Thử parse xem có phải UUID xịn không
                return UUID.fromString(idString);
            } catch (IllegalArgumentException e) {
                // Ahaa! AI đã gửi tên vào trường ID (ví dụ idString = "Gò Vấp")
                log.warn("AI hallucination detected: AI sent name '{}' into ID field. Switching to name lookup.",
                        idString);
                // Dùng cái chuỗi "Gò Vấp" đó để tìm ID luôn
                return lookupByNameFunction.apply(idString);
            }
        }

        // 3. Nếu idString null, dùng nameFallback để tìm
        if (nameFallback != null && !nameFallback.trim().isEmpty()) {
            return lookupByNameFunction.apply(nameFallback);
        }

        return null;
    }

    /**
     * Tìm vehicle theo license plate của customer hiện tại (từ SecurityContext)
     * BẮT BUỘC phải filter theo ownerId để đảm bảo tìm đúng xe của khách hàng
     */
    private VehicleProfile findVehicleByLicensePlate(String licensePlate) {
        try {
            // Lấy customerId từ SecurityContext (token authentication)
            LoginUserInfo currentUser = PermissionUtils.getCurrentUser();

            UUID ownerId = null;
            if (currentUser != null && currentUser.getSub() != null) {
                try {
                    ownerId = UUID.fromString(currentUser.getSub());
                    log.debug("Finding vehicle by license plate '{}' for ownerId: {}", licensePlate, ownerId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid user ID format in SecurityContext: {}", currentUser.getSub());
                }
            }

            if (ownerId == null) {
                log.error("Cannot find vehicle by license plate '{}': No authenticated user found in SecurityContext",
                        licensePlate);
                return null;
            }

            // Tìm vehicle theo license plate VÀ ownerId - BẮT BUỘC filter theo ownerId
            final UUID finalOwnerId = ownerId; // Make effectively final for lambda
            VehicleProfileFilterParam filterParam = new VehicleProfileFilterParam();
            filterParam.setPage(0);
            filterParam.setSize(1000); // Lấy nhiều để tìm
            filterParam.setOwnerId(finalOwnerId); // BẮT BUỘC: Filter theo ownerId của customer hiện tại
            // Không set sort để tránh lỗi với createdAt field

            return vehicleProfileService.getAllVehicleProfilesWithFilters(filterParam)
                    .getContent()
                    .stream()
                    .filter(v -> v.getLicensePlate() != null &&
                            (v.getLicensePlate().equalsIgnoreCase(licensePlate) ||
                                    v.getLicensePlate().replaceAll("\\s+", "")
                                            .equalsIgnoreCase(licensePlate.replaceAll("\\s+", "")))
                            &&
                            !v.getIsDeleted() &&
                            v.getIsActive() &&
                            v.getOwnerId().equals(finalOwnerId)) // Double check: đảm bảo vehicle thuộc về customer đúng
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error finding vehicle by license plate '{}': {}", licensePlate, e.getMessage());
            return null;
        }
    }

    // Lấy danh sách xe của khách hàng
    @Bean
    @Description("Get list of customer's vehicles. Call this FIRST when user wants to book. Returns vehicle_id needed for booking. " +
            "CRITICAL: DO NOT call this if draft already has vehicle_id. Check draft context first.")
    public Function<GetCustomerVehiclesRequest, GetCustomerVehiclesResponse> getCustomerVehicles() {
        return request -> {
            log.info("=== AI FUNCTION CALL ===: getCustomerVehicles()");

            // CRITICAL: Validate draft data before proceeding
            // If draft already has vehicle_id, reject the call to prevent unnecessary function calls
            UUID draftId = DraftContextHolder.getDraftId();
            if (draftId != null) {
                try {
                    BookingDraft draft = bookingDraftService.getDraft(draftId);

                    if (draft.hasVehicle()) {
                        log.warn("REJECTED getCustomerVehicles() call - Draft already has vehicle_id: {}, license_plate: {}",
                                draft.getVehicleId(), draft.getVehicleLicensePlate());
                        return GetCustomerVehiclesResponse.builder()
                                .status("ALREADY_SELECTED")
                                .message(String.format("Bạn đã chọn xe '%s' rồi. Không cần chọn lại. Hãy tiếp tục bước tiếp theo.",
                                        draft.getVehicleLicensePlate()))
                                .vehicles(List.of()) // Empty list to indicate no action needed
                                .build();
                    }

                    log.info("Draft validation passed: No vehicle selected yet, proceeding with getCustomerVehicles()");
                } catch (Exception e) {
                    log.warn("Could not validate draft: {}", e.getMessage());
                    // Continue anyway, let service layer handle
                }
            }

            GetCustomerVehiclesResponse response = aiBookingAssistantService.getCustomerVehicles(request);

            // Log tool response để controller có thể parse
            if (response.getVehicles() != null && !response.getVehicles().isEmpty()) {
                log.info("Tool Response: getCustomerVehicles() returned {} vehicles", response.getVehicles().size());
                response.getVehicles().forEach(vehicle -> {
                    log.info("   → vehicle_id: {}, license_plate: {}",
                            vehicle.getVehicleId(), vehicle.getLicensePlate());
                });
            }

            // CRITICAL: Lưu response vào ThreadLocal để controller có thể lấy vehicle_id
            // khi user chọn xe
            DraftContextHolder.setVehiclesResponse(response);
            log.debug("Saved getCustomerVehicles() response to ThreadLocal for vehicle_id lookup");

            return response;
        };
    }

    @Bean
    @Description("Search services by keyword. Call this at STEP 4 when user mentions a service (e.g., 'Rửa xe', 'Bảo dưỡng'). "
            +
            "This function searches for services matching the keyword. " +
            "If multiple services found, list them for user to choose. " +
            "Only after user has selected a specific service, proceed to STEP 5 (checkAvailability). " +
            "INPUT: keyword (string, e.g., 'Rửa xe'), branch_id (optional UUID). " +
            "OUTPUT: List of services with service_id, service_name, description, estimated_duration, price.")
    public Function<GetServicesRequest, GetServicesResponse> getServices() {
        return request -> {
            log.info("=== AI FUNCTION CALL ===: getServices(keyword='{}', branch_id='{}')",
                    request.getKeyword(), request.getBranchId());

            GetServicesResponse response = aiBookingAssistantService.getServices(request);

            // Log tool response
            if (response.getServices() != null && !response.getServices().isEmpty()) {
                log.info("Tool Response: getServices() returned {} services", response.getServices().size());
                response.getServices().forEach(service -> {
                    log.info("   → service_id: {}, service_name: {}, duration: {} min, price: {}",
                            service.getServiceId(), service.getServiceName(),
                            service.getEstimatedDuration(), service.getPrice());
                });
            } else {
                log.info("Tool Response: getServices() returned no services (status: {})", response.getStatus());
            }

            // CRITICAL: Lưu response vào ThreadLocal để controller có thể lấy service_name
            // khi user chọn dịch vụ
            DraftContextHolder.setServicesResponse(response);
            log.debug("Saved getServices() response to ThreadLocal for service_name lookup");

            return response;
        };
    }

    @Bean
    @Description("Get list of active branches. Call this when user needs to select a location. " +
            "CRITICAL: DO NOT call this if draft already has branch_id. Check draft context first.")
    public Function<GetBranchesRequest, GetBranchesResponse> getBranches() {
        return request -> {
            log.info("=== AI FUNCTION CALL ===: getBranches()");

            // CRITICAL: Validate draft data before proceeding
            // If draft already has branch_id, reject the call to prevent unnecessary function calls
            UUID draftId = DraftContextHolder.getDraftId();
            if (draftId != null) {
                try {
                    BookingDraft draft = bookingDraftService.getDraft(draftId);

                    if (draft.hasBranch()) {
                        log.warn("REJECTED getBranches() call - Draft already has branch_id: {}, branch_name: {}",
                                draft.getBranchId(), draft.getBranchName());
                        return GetBranchesResponse.builder()
                                .status("ALREADY_SELECTED")
                                .message(String.format("Bạn đã chọn chi nhánh '%s' rồi. Không cần chọn lại. Hãy tiếp tục bước tiếp theo.",
                                        draft.getBranchName()))
                                .branches(List.of()) // Empty list to indicate no action needed
                                .build();
                    }

                    log.info("Draft validation passed: No branch selected yet, proceeding with getBranches()");
                } catch (Exception e) {
                    log.warn("Could not validate draft: {}", e.getMessage());
                    // Continue anyway, let service layer handle
                }
            }

            GetBranchesResponse response = aiBookingAssistantService.getBranches(request);

            // Log tool response
            if (response.getBranches() != null && !response.getBranches().isEmpty()) {
                log.info("Tool Response: getBranches() returned {} branches", response.getBranches().size());
                response.getBranches().forEach(branch -> {
                    log.info("   → branch_id: {}, branch_name: {}",
                            branch.getBranchId(), branch.getBranchName());
                });
            }

            // CRITICAL: Lưu response vào ThreadLocal để controller có thể lấy branch_id khi
            // user chọn chi nhánh
            DraftContextHolder.setBranchesResponse(response);
            log.debug("Saved getBranches() response to ThreadLocal for branch_id lookup");

            return response;
        };
    }

    @Bean
    @Description("Check service availability. " +
            "INPUT: service_type (string, e.g. 'Rửa xe'), branch_id (UUID format like '7cd17e0d-529d-48ef-9094-67103811651d' - NOT branch name), "
            +
            "branch_name (string, optional, use when branch_id is not available), date_time (ISO string like '2025-12-01' or '2025-12-01T08:00:00'). "
            +
            "IMPORTANT: branch_id MUST be UUID (36 characters), NOT branch name. If you have branch name from conversation, use branch_name field instead. "
            +
            "OUTPUT: List of services, List of Bays (with bay_id UUID), and List of Time Slots available.")
    public Function<AvailabilityRequest, AvailabilityResponse> checkAvailability() {
        return request -> {
            log.info("=== AI FUNCTION CALL ===: checkAvailability(service_type='{}', branch_name='{}', date_time='{}')",
                    request.getServiceType(), request.getBranchName(), request.getDateTime());
            log.debug("Raw Request from AI: branch_id='{}', branch_name='{}', service_type='{}', date_time='{}'",
                    request.getBranchId(), request.getBranchName(), request.getServiceType(), request.getDateTime());

            // CRITICAL: Log để debug AI hallucination (chỉ log WARN/ERROR)
            if (request.getBranchId() != null && !request.getBranchId().trim().isEmpty()) {
                // Check xem có phải UUID format không
                try {
                    UUID.fromString(request.getBranchId());
                    log.debug("AI provided branch_id is valid UUID format: {}", request.getBranchId());
                } catch (IllegalArgumentException e) {
                    log.warn("AI HALLUCINATION: AI sent non-UUID value '{}' into branch_id field",
                            request.getBranchId());
                }
            }

            // Smart parse branch ID - xử lý AI hallucination
            UUID finalBranchId = null;

            // Bước 1: Thử parse UUID từ branch_id
            if (request.getBranchId() != null && !request.getBranchId().trim().isEmpty()) {
                try {
                    UUID parsedUuid = UUID.fromString(request.getBranchId());
                    // CRITICAL: Validate UUID có tồn tại trong database không
                    Branch branchById = aiBookingAssistantService.findBranchById(parsedUuid);
                    if (branchById != null) {
                        finalBranchId = parsedUuid;
                        log.debug("Smart parse: Valid UUID found in database: {} (branch: '{}')",
                                finalBranchId, branchById.getBranchName());
                    } else {
                        log.warn("AI HALLUCINATION: UUID '{}' does NOT exist in database. Falling back to name lookup.",
                                request.getBranchId());
                        // UUID không tồn tại, fallback sang tìm theo tên
                        if (request.getBranchName() != null && !request.getBranchName().trim().isEmpty()) {
                            Branch b = aiBookingAssistantService
                                    .findBranchByNameOrAddress(request.getBranchName().trim());
                            if (b != null) {
                                finalBranchId = b.getBranchId();
                                log.warn(
                                        "Fixed: Found branch by name '{}' with UUID: {}. Using this instead of invalid UUID '{}'.",
                                        b.getBranchName(), finalBranchId, request.getBranchId());
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Không phải UUID format, dùng smartParseId
                    log.warn("AI HALLUCINATION: AI sent non-UUID value '{}' into branch_id field",
                            request.getBranchId());
                    finalBranchId = smartParseId(
                            request.getBranchId(),
                            request.getBranchName(),
                            (name) -> {
                                Branch b = aiBookingAssistantService.findBranchByNameOrAddress(name);
                                return b != null ? b.getBranchId() : null;
                            });
                }
            }

            // Bước 2: Nếu vẫn chưa có, thử tìm theo tên
            if (finalBranchId == null && request.getBranchName() != null && !request.getBranchName().trim().isEmpty()) {
                Branch b = aiBookingAssistantService.findBranchByNameOrAddress(request.getBranchName().trim());
                if (b != null) {
                    finalBranchId = b.getBranchId();
                    log.debug("Smart parse: Found branch by name '{}' with UUID: {}", b.getBranchName(), finalBranchId);
                }
            }

            log.info("Final parsed branch_id: {} (from original: branch_id='{}', branch_name='{}')",
                    finalBranchId, request.getBranchId(), request.getBranchName());

            // Bước 3: Nếu có draft, ưu tiên dùng draft data
            UUID draftId = DraftContextHolder.getDraftId();
            if (draftId != null) {
                try {
                    BookingDraft draft = bookingDraftService.getDraft(draftId);

                    // Ưu tiên dùng branch_id từ draft
                    if (draft.getBranchId() != null && finalBranchId == null) {
                        finalBranchId = draft.getBranchId();
                        log.info("Using branch_id from draft: {}", finalBranchId);
                    }

                    // Ưu tiên dùng date_time từ draft
                    if (draft.getDateTime() != null && request.getDateTime() == null) {
                        request.setDateTime(draft.getDateTime().toString());
                        log.info("Using date_time from draft: {}", draft.getDateTime());
                    }

                    // Ưu tiên dùng service_type từ draft
                    if (draft.getServiceType() != null && request.getServiceType() == null) {
                        request.setServiceType(draft.getServiceType());
                        log.info("Using service_type from draft: {}", draft.getServiceType());
                    }
                } catch (Exception e) {
                    log.warn("Could not get draft: {}", e.getMessage());
                }
            }

            // Tạo request mới với UUID đã parse được
            AvailabilityRequest processedRequest = AvailabilityRequest.builder()
                    .serviceType(request.getServiceType())
                    .dateTime(request.getDateTime())
                    .branchId(finalBranchId != null ? finalBranchId.toString() : null)
                    .branchName(request.getBranchName())
                    .vehicleModel(request.getVehicleModel())
                    .serviceTypes(request.getServiceTypes())
                    .build();

            AvailabilityResponse response = aiBookingAssistantService.checkAvailability(processedRequest);

            // Log tool response
            if (response.getAvailableBays() != null && !response.getAvailableBays().isEmpty()) {
                log.info("Tool Response: checkAvailability() returned {} available bays",
                        response.getAvailableBays().size());
                response.getAvailableBays().forEach(bay -> {
                    log.info("   → bay_id: {}, bay_name: {}, available_slots: {}",
                            bay.getBayId(), bay.getBayName(),
                            bay.getAvailableSlots() != null ? bay.getAvailableSlots().size() : 0);
                });
            }

            // CRITICAL: Lưu response vào ThreadLocal để controller có thể lấy bay_id khi
            // user chọn bay
            DraftContextHolder.setAvailabilityResponse(response);
            log.debug("Saved checkAvailability() response to ThreadLocal for bay_id lookup");

            // Auto-update draft với branch_id, date_time, và service (nếu chưa có)
            if (draftId != null) {
                try {
                    BookingDraft draft = bookingDraftService.getDraft(draftId);

                    // FALLBACK LOGIC: Nếu draft chưa có service và checkAvailability() tìm thấy
                    // service
                    // → Tự động lưu service vào draft để đảm bảo service được lưu ngay cả khi AI bỏ
                    // qua getServices()
                    if (!draft.hasService() && request.getServiceType() != null &&
                            response.getStatus() != null &&
                            (response.getStatus().equals("AVAILABLE") || response.getStatus().equals("FULL"))) {

                        // Tìm service từ request.getServiceType() bằng cách search trong database
                        List<Service> foundServices = serviceService
                                .searchByKeyword(request.getServiceType());

                        Service foundService = null;
                        if (!foundServices.isEmpty()) {
                            // Tìm exact match trước
                            for (Service s : foundServices) {
                                if (s.getServiceName().equalsIgnoreCase(request.getServiceType())) {
                                    foundService = s;
                                    break;
                                }
                            }
                            // Nếu không có exact match, lấy service đầu tiên
                            if (foundService == null) {
                                foundService = foundServices.get(0);
                            }
                        }

                        if (foundService != null) {
                            // Lưu service vào draft (cả vào bảng quan hệ)
                            bookingDraftService.addServiceToDraft(
                                    draftId,
                                    foundService.getServiceId(),
                                    foundService.getServiceName());

                            // Cập nhật service_id và service_type trong draft (cho dịch vụ đầu tiên)
                            BookingDraftService.DraftUpdate serviceUpdate = BookingDraftService.DraftUpdate
                                    .builder()
                                    .serviceId(foundService.getServiceId())
                                    .serviceType(foundService.getServiceName())
                                    .build();

                            bookingDraftService.updateDraft(draftId, "SERVICE_FALLBACK",
                                    "Auto-saved service from checkAvailability() fallback", serviceUpdate);

                            log.info(
                                    "FALLBACK: Auto-saved service to draft: service_id={}, service_name={} (from checkAvailability())",
                                    foundService.getServiceId(), foundService.getServiceName());
                        } else {
                            log.warn("FALLBACK: Could not find service '{}' in database to save to draft",
                                    request.getServiceType());
                        }
                    }

                    // Update branch_id và date_time nếu có
                    BookingDraftService.DraftUpdate update = BookingDraftService.DraftUpdate
                            .builder()
                            .branchId(finalBranchId)
                            .branchName(request.getBranchName())
                            .build();

                    if (finalBranchId != null || request.getBranchName() != null) {
                        bookingDraftService.updateDraft(draftId, "AVAILABILITY_CHECK",
                                "Auto-update from checkAvailability()", update);
                        log.info("Auto-updated draft with branch info from checkAvailability()");
                    }
                } catch (Exception e) {
                    log.warn("Could not auto-update draft: {}", e.getMessage());
                }
            }

            return response;
        };
    }

    @Bean
    @Description("Create a final booking. ONLY call this when user has CONFIRMED all details (Vehicle, Branch, Service, Bay, Time). "
            +
            "IMPORTANT: branch_id and bay_id MUST be UUID format, NOT names. Extract UUIDs from previous function responses. " +
            "CRITICAL: This function will ONLY proceed if draft has all required data. If data is missing, it will return error asking user to complete missing steps.")
    public Function<CreateBookingRequest, CreateBookingResponse> createBooking() {
        return request -> {
            log.info(
                    "=== AI FUNCTION CALL ===: createBooking(service_type='{}', branch_name='{}', bay_name='{}', vehicle_license_plate='{}')",
                    request.getServiceType(), request.getBranchName(), request.getBayName(),
                    request.getVehicleLicensePlate());
            log.debug(
                    "Raw Request from AI: branch_id='{}', branch_name='{}', bay_id='{}', bay_name='{}', vehicle_id='{}'",
                    request.getBranchId(), request.getBranchName(), request.getBayId(), request.getBayName(),
                    request.getVehicleId());

            // CRITICAL: Validate draft data before proceeding
            UUID validationDraftId = DraftContextHolder.getDraftId();
            if (validationDraftId != null) {
                try {
                    BookingDraft draft = bookingDraftService.getDraft(validationDraftId);

                    // Validate draft has all required data
                    if (!draft.hasVehicle()) {
                        log.warn("Cannot create booking: Draft missing vehicle");
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Bạn chưa chọn xe. Vui lòng chọn xe trước khi đặt lịch.")
                                .failedStep(1)
                                .build();
                    }
                    if (!draft.hasDate()) {
                        log.warn("Cannot create booking: Draft missing date");
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Bạn chưa chọn ngày. Vui lòng chọn ngày đặt lịch trước khi đặt lịch.")
                                .failedStep(2)
                                .build();
                    }
                    if (!draft.hasBranch()) {
                        log.warn("Cannot create booking: Draft missing branch");
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Bạn chưa chọn chi nhánh. Vui lòng chọn chi nhánh trước khi đặt lịch.")
                                .failedStep(3)
                                .build();
                    }
                    if (!draft.hasService()) {
                        log.warn("Cannot create booking: Draft missing service");
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Bạn chưa chọn dịch vụ. Vui lòng chọn dịch vụ trước khi đặt lịch.")
                                .failedStep(4)
                                .build();
                    }
                    if (!draft.hasBay()) {
                        log.warn("Cannot create booking: Draft missing bay");
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Bạn chưa chọn bay. Vui lòng chọn bay trước khi đặt lịch.")
                                .failedStep(5)
                                .build();
                    }
                    if (!draft.hasTime()) {
                        log.warn("Cannot create booking: Draft missing time");
                        return CreateBookingResponse.builder()
                                .status("FAILED")
                                .message("Bạn chưa chọn giờ. Vui lòng chọn giờ đặt lịch trước khi đặt lịch.")
                                .failedStep(6)
                                .build();
                    }

                    log.info("Draft validation passed: All required data present");
                } catch (Exception e) {
                    log.warn("Could not validate draft: {}", e.getMessage());
                    // Continue anyway, let service layer validate
                }
            }

            // Smart parse branch ID
            UUID parsedBranchId = smartParseId(
                    request.getBranchId(),
                    request.getBranchName(),
                    (name) -> {
                        Branch b = aiBookingAssistantService.findBranchByNameOrAddress(name);
                        return b != null ? b.getBranchId() : null;
                    });
            UUID finalBranchId = parsedBranchId; // Will be updated from draft if needed

            // Bước 4: Nếu có draft, ưu tiên dùng draft data và fill missing fields
            UUID draftId = DraftContextHolder.getDraftId();
            if (draftId != null) {
                try {
                    BookingDraft draft = bookingDraftService.getDraft(draftId);

                    log.info("Using draft data to fill missing fields in createBooking()");

                    // Update finalBranchId từ draft nếu cần
                    if (finalBranchId == null && draft.getBranchId() != null) {
                        finalBranchId = draft.getBranchId();
                        log.info("   Using branch_id from draft: {}", finalBranchId);
                    }
                } catch (Exception e) {
                    log.warn("Could not get draft: {}", e.getMessage());
                }
            }

            // Smart parse vehicle ID - với fallback tìm theo license plate
            UUID finalVehicleId = null;
            if (request.getVehicleId() != null && !request.getVehicleId().trim().isEmpty()) {
                try {
                    UUID parsedUuid = UUID.fromString(request.getVehicleId());
                    // Validate UUID có tồn tại trong database không
                    VehicleProfile vehicleById = vehicleProfileService.getVehicleProfileById(parsedUuid);
                    if (vehicleById != null) {
                        finalVehicleId = parsedUuid;
                        log.debug("Smart parse: Valid vehicle UUID found in database: {} (license_plate: '{}')",
                                finalVehicleId, vehicleById.getLicensePlate());
                    } else {
                        log.warn(
                                "AI HALLUCINATION: Vehicle UUID '{}' does NOT exist in database. Falling back to license plate lookup.",
                                request.getVehicleId());
                        // UUID không tồn tại, fallback sang tìm theo license plate
                        if (request.getVehicleLicensePlate() != null
                                && !request.getVehicleLicensePlate().trim().isEmpty()) {
                            VehicleProfile v = findVehicleByLicensePlate(request.getVehicleLicensePlate().trim());
                            if (v != null) {
                                finalVehicleId = v.getVehicleId();
                                log.warn(
                                        "Fixed: Found vehicle by license plate '{}' with UUID: {}. Using this instead of invalid UUID '{}'.",
                                        v.getLicensePlate(), finalVehicleId, request.getVehicleId());
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Không phải UUID format, dùng license plate
                    log.warn("AI HALLUCINATION: AI sent non-UUID value '{}' into vehicle_id field",
                            request.getVehicleId());
                    if (request.getVehicleLicensePlate() != null
                            && !request.getVehicleLicensePlate().trim().isEmpty()) {
                        VehicleProfile v = findVehicleByLicensePlate(request.getVehicleLicensePlate().trim());
                        if (v != null) {
                            finalVehicleId = v.getVehicleId();
                            log.debug("Smart parse: Found vehicle by license plate '{}' with UUID: {}",
                                    v.getLicensePlate(), finalVehicleId);
                        }
                    }
                }
            } else if (request.getVehicleLicensePlate() != null && !request.getVehicleLicensePlate().trim().isEmpty()) {
                // Nếu không có vehicle_id, tìm theo license plate
                VehicleProfile v = findVehicleByLicensePlate(request.getVehicleLicensePlate().trim());
                if (v != null) {
                    finalVehicleId = v.getVehicleId();
                    log.debug("Smart parse: Found vehicle by license plate '{}' with UUID: {}", v.getLicensePlate(),
                            finalVehicleId);
                } else {
                    log.warn("ERROR: Vehicle not found by license plate: '{}'", request.getVehicleLicensePlate());
                }
            }

            // Update vehicle_license_plate nếu có finalVehicleId nhưng chưa có license
            // plate
            if (finalVehicleId != null && (request.getVehicleLicensePlate() == null
                    || request.getVehicleLicensePlate().trim().isEmpty())) {
                VehicleProfile v = vehicleProfileService.getVehicleProfileById(finalVehicleId);
                if (v != null) {
                    request.setVehicleLicensePlate(v.getLicensePlate());
                    log.debug("Auto-filled vehicle_license_plate from vehicle_id: {}", v.getLicensePlate());
                }
            }

            // Smart parse bay ID (sau khi đã có finalBranchId từ draft nếu có)
            UUID finalBayId = null;
            if (request.getBayId() != null || request.getBayName() != null) {
                final UUID branchIdForBay = finalBranchId; // Final reference for lambda
                finalBayId = smartParseId(
                        request.getBayId(),
                        request.getBayName(),
                        (name) -> {
                            // Tìm bay theo tên, ưu tiên trong branch đã chọn
                            if (branchIdForBay != null) {
                                return serviceBayService.searchByKeywordInBranch(branchIdForBay, name)
                                        .stream()
                                        .findFirst()
                                        .map(ServiceBay::getBayId)
                                        .orElse(null);
                            } else {
                                return serviceBayService.getByBayName(name)
                                        .map(ServiceBay::getBayId)
                                        .orElse(null);
                            }
                        });
            }

            // Fill missing fields từ draft (tiếp tục)
            if (draftId != null) {
                try {
                    BookingDraft draft = bookingDraftService.getDraft(draftId);

                    // Fill missing fields từ draft
                    if (finalVehicleId == null && draft.getVehicleId() != null) {
                        finalVehicleId = draft.getVehicleId();
                        log.info("   Using vehicle_id from draft: {}", finalVehicleId);
                    }
                    if (request.getVehicleLicensePlate() == null && draft.getVehicleLicensePlate() != null) {
                        request.setVehicleLicensePlate(draft.getVehicleLicensePlate());
                        log.info("   Using vehicle_license_plate from draft: {}", draft.getVehicleLicensePlate());
                    }
                    if (request.getBranchName() == null && draft.getBranchName() != null) {
                        request.setBranchName(draft.getBranchName());
                        log.info("   Using branch_name from draft: {}", draft.getBranchName());
                    }
                    if (finalBayId == null && draft.getBayId() != null) {
                        finalBayId = draft.getBayId();
                        log.info("   Using bay_id from draft: {}", finalBayId);
                    }
                    if (request.getBayName() == null && draft.getBayName() != null) {
                        request.setBayName(draft.getBayName());
                        log.info("   Using bay_name from draft: {}", draft.getBayName());
                    }
                    // Lấy tất cả dịch vụ từ bảng quan hệ booking_draft_services
                    List<DraftService> draftServices = bookingDraftService
                            .getDraftServices(draftId);

                    if (!draftServices.isEmpty()) {
                        // Chuyển đổi thành List<String> (service names)
                        List<String> serviceNames = draftServices.stream()
                                .map(DraftService::getServiceName)
                                .collect(Collectors.toList());

                        // Nếu chỉ có 1 dịch vụ → dùng String, nếu nhiều hơn → dùng List<String>
                        if (serviceNames.size() == 1) {
                            request.setServiceType(serviceNames.get(0));
                            log.info("   Using service_type from draft (single): {}", serviceNames.get(0));
                        } else {
                            request.setServiceType(serviceNames);
                            log.info("   Using service_types from draft (multiple, {} services): {}",
                                    serviceNames.size(), String.join(", ", serviceNames));
                        }
                    } else if (draft.getServiceType() != null) {
                        // Fallback: Nếu không có trong bảng quan hệ, dùng service_type cũ (tương thích
                        // ngược)
                        request.setServiceType(draft.getServiceType());
                        log.info("   Using service_type from draft (fallback): {}", draft.getServiceType());
                    }
                    if (request.getDateTime() == null && draft.getDateTime() != null) {
                        request.setDateTime(draft.getDateTime().toString());
                        log.info("   Using date_time from draft: {}", draft.getDateTime());
                    }
                } catch (Exception e) {
                    log.warn("Could not get draft: {}", e.getMessage());
                }
            }

            // Tạo request mới với UUID đã parse được
            CreateBookingRequest processedRequest = CreateBookingRequest.builder()
                    .customerPhone(request.getCustomerPhone())
                    .serviceType(request.getServiceType())
                    .dateTime(request.getDateTime())
                    .branchId(finalBranchId != null ? finalBranchId.toString() : null)
                    .branchName(request.getBranchName())
                    .bayId(finalBayId != null ? finalBayId.toString() : null)
                    .bayName(request.getBayName())
                    .vehicleId(finalVehicleId != null ? finalVehicleId.toString() : null)
                    .vehicleLicensePlate(request.getVehicleLicensePlate())
                    .notes(request.getNotes())
                    .build();

            CreateBookingResponse response = aiBookingAssistantService.createBooking(processedRequest);

            // Log tool response
            log.info("Tool Response: createBooking() - status: {}, booking_code: {}",
                    response.getStatus(),
                    response.getBookingCode() != null ? response.getBookingCode() : "N/A");

            // Nếu booking thành công → Xóa draft thực sự khỏi database
            if (draftId != null && "SUCCESS".equalsIgnoreCase(response.getStatus())) {
                try {
                    bookingDraftService.deleteDraft(draftId);
                    log.info("Deleted draft after successful booking: draft_id={}", draftId);
                } catch (Exception e) {
                    log.warn("Could not delete draft after successful booking: {}", e.getMessage());
                }
            }

            return response;
        };
    }

}
