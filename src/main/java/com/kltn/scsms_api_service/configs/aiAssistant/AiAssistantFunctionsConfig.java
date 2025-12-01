package com.kltn.scsms_api_service.configs.aiAssistant;

import com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.CreateBookingRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetBranchesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetCustomerVehiclesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.AvailabilityResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.CreateBookingResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetBranchesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetCustomerVehiclesResponse;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.service.aiAssistant.AiBookingAssistantService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.UUID;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AiAssistantFunctionsConfig {

    private final AiBookingAssistantService aiBookingAssistantService;
    private final ServiceBayService serviceBayService;
    
    /**
     * Smart parser để xử lý AI hallucination - tự động parse UUID hoặc tìm kiếm theo tên
     * 
     * @param idString Chuỗi từ AI (có thể là UUID hoặc tên)
     * @param nameFallback Tên dự phòng nếu idString null
     * @param lookupByNameFunction Hàm tìm kiếm theo tên
     * @return UUID nếu tìm thấy, null nếu không tìm thấy
     */
    private UUID smartParseId(String idString, String nameFallback, Function<String, UUID> lookupByNameFunction) {
        // 1. Nếu cả id và name đều null -> bó tay
        if ((idString == null || idString.trim().isEmpty()) && (nameFallback == null || nameFallback.trim().isEmpty())) {
            return null;
        }

        // 2. Ưu tiên xử lý idString trước
        if (idString != null && !idString.trim().isEmpty()) {
            try {
                // Thử parse xem có phải UUID xịn không
                return UUID.fromString(idString);
            } catch (IllegalArgumentException e) {
                // Ahaa! AI đã gửi tên vào trường ID (ví dụ idString = "Gò Vấp")
                log.warn("AI hallucination detected: AI sent name '{}' into ID field. Switching to name lookup.", idString);
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
    
    // Lấy danh sách xe của khách hàng
    @Bean
    @Description("Get list of customer's vehicles. Call this FIRST when user wants to book. Returns vehicle_id needed for booking.")
    public Function<GetCustomerVehiclesRequest, GetCustomerVehiclesResponse> getCustomerVehicles() {
        return request -> {
            log.info("Spring AI Function: getCustomerVehicles() called");
            return aiBookingAssistantService.getCustomerVehicles(request);
        };
    }

    @Bean
    @Description("Get list of active branches. Call this when user needs to select a location.")
    public Function<GetBranchesRequest, GetBranchesResponse> getBranches() {
        return request -> {
            log.info("Spring AI Function: getBranches() called");
            return aiBookingAssistantService.getBranches(request);
        };
    }

    @Bean
    @Description("Check service availability. " +
            "INPUT: service_type (string, e.g. 'Rửa xe'), branch_id (UUID format like '7cd17e0d-529d-48ef-9094-67103811651d' - NOT branch name), " +
            "branch_name (string, optional, use when branch_id is not available), date_time (ISO string like '2025-12-01' or '2025-12-01T08:00:00'). " +
            "IMPORTANT: branch_id MUST be UUID (36 characters), NOT branch name. If you have branch name from conversation, use branch_name field instead. " +
            "OUTPUT: List of services, List of Bays (with bay_id UUID), and List of Time Slots available.")
    public Function<AvailabilityRequest, AvailabilityResponse> checkAvailability() {
        return request -> {
            log.info("Raw Request from AI: branch_id='{}', branch_name='{}'", request.getBranchId(), request.getBranchName());
            
            // Smart parse branch ID - xử lý AI hallucination
            UUID finalBranchId = smartParseId(
                request.getBranchId(),      // Cái AI gửi (có thể là UUID hoặc "Gò Vấp")
                request.getBranchName(),    // Cái AI gửi dự phòng
                (name) -> {                 // Hàm tìm kiếm nếu AI gửi sai
                    // Gọi service tìm branch theo tên và trả về UUID đầu tiên tìm thấy
                    Branch b = aiBookingAssistantService.findBranchByNameOrAddress(name);
                    return b != null ? b.getBranchId() : null;
                }
            );
            
            // Tạo request mới với UUID đã parse được
            AvailabilityRequest processedRequest = AvailabilityRequest.builder()
                .serviceType(request.getServiceType())
                .dateTime(request.getDateTime())
                .branchId(finalBranchId != null ? finalBranchId.toString() : null)
                .branchName(request.getBranchName())
                .vehicleModel(request.getVehicleModel())
                .serviceTypes(request.getServiceTypes())
                .build();
            
            return aiBookingAssistantService.checkAvailability(processedRequest);
        };
    }

    @Bean
    @Description("Create a final booking. ONLY call this when user has CONFIRMED all details (Vehicle, Branch, Service, Bay, Time). " +
            "IMPORTANT: branch_id and bay_id MUST be UUID format, NOT names. Extract UUIDs from previous function responses.")
    public Function<CreateBookingRequest, CreateBookingResponse> createBooking() {
        return request -> {
            log.info("Raw Request from AI: branch_id='{}', branch_name='{}', bay_id='{}', bay_name='{}', vehicle_id='{}'", 
                    request.getBranchId(), request.getBranchName(), request.getBayId(), request.getBayName(), request.getVehicleId());
            
            // Smart parse branch ID
            UUID finalBranchId = smartParseId(
                request.getBranchId(),
                request.getBranchName(),
                (name) -> {
                    Branch b = aiBookingAssistantService.findBranchByNameOrAddress(name);
                    return b != null ? b.getBranchId() : null;
                }
            );
            
            // Smart parse bay ID (cần branchId để tìm chính xác hơn)
            UUID finalBayId = null;
            if (request.getBayId() != null || request.getBayName() != null) {
                finalBayId = smartParseId(
                    request.getBayId(),
                    request.getBayName(),
                    (name) -> {
                        // Tìm bay theo tên, ưu tiên trong branch đã chọn
                        if (finalBranchId != null) {
                            return serviceBayService.searchByKeywordInBranch(finalBranchId, name)
                                    .stream()
                                    .findFirst()
                                    .map(ServiceBay::getBayId)
                                    .orElse(null);
                        } else {
                            return serviceBayService.getByBayName(name)
                                    .map(ServiceBay::getBayId)
                                    .orElse(null);
                        }
                    }
                );
            }
            
            // Smart parse vehicle ID
            UUID finalVehicleId = null;
            if (request.getVehicleId() != null) {
                try {
                    finalVehicleId = UUID.fromString(request.getVehicleId());
                } catch (IllegalArgumentException e) {
                    log.warn("AI hallucination detected: AI sent invalid vehicle_id '{}'. Will use null.", request.getVehicleId());
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
            
            return aiBookingAssistantService.createBooking(processedRequest);
        };
    }

}
