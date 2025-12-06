package com.kltn.scsms_api_service.core.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.ChatRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.ExtractSelectionRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetBranchesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetCustomerVehiclesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.*;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.token.LoginUserInfo;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleProfileFilterParam;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.service.aiAssistant.AiBookingAssistantService;
import com.kltn.scsms_api_service.core.service.aiAssistant.ExtractionService;
import com.kltn.scsms_api_service.core.service.entityService.BookingDraftService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.service.entityService.VehicleProfileService;
import com.kltn.scsms_api_service.core.utils.DraftContextHolder;
import com.kltn.scsms_api_service.core.utils.PermissionUtils;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Booking Assistant", description = "APIs for AI-powered booking assistant")
@RequestMapping("/ai-assistant")
public class AiBookingAssistantController {

    private final ChatClient aiChatClient;
    private final AiBookingAssistantService aiBookingAssistantService;
    private final VehicleProfileService vehicleProfileService;
    private final ServiceBayService serviceBayService;
    private final BookingDraftService bookingDraftService;
    private final ExtractionService extractionService;
    private final ServiceService serviceService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI booking assistant", description = "Send a message to AI assistant. AI will automatically call functions (checkAvailability, createBooking) when needed.")
    @SwaggerOperation(summary = "Chat with AI booking assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Parameter(description = "Chat request with message and conversation history") @Valid @RequestBody ChatRequest request) {

        long requestStartTime = System.currentTimeMillis();
        log.info("=== USER MESSAGE ===: {}", request.getMessage());
        log.debug("AI Assistant chat request received: message={}, customerPhone={}, historySize={}",
                request.getMessage(), request.getCustomerPhone(),
                request.getConversationHistory() != null ? request.getConversationHistory().size() : 0);

        try {
            // ============================================================
            // PHẦN 1: GET OR CREATE DRAFT
            // ============================================================
            // Lấy customerId từ SecurityContext hoặc request
            UUID customerId = getCustomerId(request);
            String sessionId = request.getSessionId() != null ? request.getSessionId()
                    : java.util.UUID.randomUUID().toString(); // Generate nếu không có

            // CRITICAL: Kiểm tra nếu user muốn bắt đầu đặt lịch mới
            // Nếu user nói "đặt lịch hẹn", "bắt đầu", "đặt lịch mới" → Reset draft
            String userMessage = request.getMessage();
            boolean isNewBookingRequest = isNewBookingRequest(userMessage);

            // Get or create draft
            BookingDraft draft;
            if (request.getDraftId() != null) {
                // Frontend đã gửi draft_id → Lấy draft theo ID
                try {
                    draft = bookingDraftService.getDraft(request.getDraftId());
                    log.info("Using existing draft from request: draft_id={}", request.getDraftId());
                    
                    // CRITICAL: Chỉ reset draft nếu:
                    // 1. User nói rõ ràng muốn bắt đầu mới ("đặt lịch mới", "bắt đầu lại", etc.)
                    // 2. VÀ draft đã complete HOẶC draft chưa có data gì (step 1, không có vehicle)
                    // KHÔNG reset nếu draft đang có data (user đang trong quá trình đặt lịch)
                    if (isNewBookingRequest) {
                        boolean shouldReset = false;
                        
                        // Case 1: Draft đã complete → Reset để bắt đầu mới
                        if (draft.isComplete()) {
                            shouldReset = true;
                            log.info("Draft is complete, will reset for new booking");
                        }
                        // Case 2: Draft chưa có data gì (step 1, không có vehicle) → Reset OK
                        else if (draft.getCurrentStep() == 1 && !draft.hasVehicle()) {
                            shouldReset = true;
                            log.info("Draft is empty (step 1, no vehicle), will reset for new booking");
                        }
                        // Case 3: Draft đang có data → KHÔNG reset (user đang trong quá trình đặt lịch)
                        else {
                            log.info("Draft has data (step={}, hasVehicle={}, hasDate={}, hasBranch={}, hasService={}, hasBay={}). " +
                                    "User message '{}' contains booking keywords but draft is in progress. Will NOT reset.",
                                    draft.getCurrentStep(), draft.hasVehicle(), draft.hasDate(), draft.hasBranch(), 
                                    draft.hasService(), draft.hasBay(), userMessage);
                            shouldReset = false;
                        }
                        
                        if (shouldReset) {
                            log.info("User wants to start new booking. Resetting draft: draft_id={}, current_step={}, is_complete={}", 
                                    draft.getDraftId(), draft.getCurrentStep(), draft.isComplete());
                            draft = bookingDraftService.resetDraft(draft.getDraftId());
                            log.info("Draft reset successfully: draft_id={}, current_step={}", draft.getDraftId(), draft.getCurrentStep());
                        }
                    }
                } catch (Exception e) {
                    log.warn("Draft not found with draft_id={}, creating new draft", request.getDraftId());
                    draft = bookingDraftService.getOrCreateDraft(sessionId, customerId);
                }
            } else {
                // Không có draft_id → Get or create theo session
                draft = bookingDraftService.getOrCreateDraft(sessionId, customerId);
                
                // CRITICAL: Chỉ reset draft nếu:
                // 1. User nói rõ ràng muốn bắt đầu mới
                // 2. VÀ draft đã complete HOẶC draft chưa có data gì
                if (isNewBookingRequest) {
                    boolean shouldReset = false;
                    
                    if (draft.isComplete()) {
                        shouldReset = true;
                        log.info("Draft is complete, will reset for new booking");
                    } else if (draft.getCurrentStep() == 1 && !draft.hasVehicle()) {
                        shouldReset = true;
                        log.info("Draft is empty (step 1, no vehicle), will reset for new booking");
                    } else {
                        log.info("Draft has data. Will NOT reset even though user message contains booking keywords.");
                        shouldReset = false;
                    }
                    
                    if (shouldReset) {
                        log.info("User wants to start new booking. Resetting draft: draft_id={}, current_step={}, is_complete={}", 
                                draft.getDraftId(), draft.getCurrentStep(), draft.isComplete());
                        draft = bookingDraftService.resetDraft(draft.getDraftId());
                        log.info("Draft reset successfully: draft_id={}, current_step={}", draft.getDraftId(), draft.getCurrentStep());
                    }
                }
            }

            log.info("Current Draft State: draft_id={}, current_step={}, status={}, is_complete={}",
                    draft.getDraftId(), draft.getCurrentStep(), draft.getStatus(), draft.isComplete());

            // Set draft context vào ThreadLocal để function configs có thể access
            DraftContextHolder.setDraftId(draft.getDraftId());
            DraftContextHolder.setSessionId(sessionId);

            // ============================================================
            // PHẦN 2: EXTRACT STATE (FALLBACK - vẫn dùng để backward compatible)
            // ============================================================
            ConversationState extractedState = extractStateFromHistory(
                    request.getConversationHistory(),
                    request.getExtractedUuids());

            // Ưu tiên dùng draft data, fallback sang extracted state
            if (draft.hasVehicle() && extractedState.getVehicleId() == null) {
                extractedState.setHasVehicle(true);
                extractedState.setVehicleId(draft.getVehicleId() != null ? draft.getVehicleId().toString() : null);
                extractedState.setVehicleLicensePlate(draft.getVehicleLicensePlate());
            }
            if (draft.hasDate() && extractedState.getDateTime() == null) {
                extractedState.setHasDate(true);
                extractedState.setDateTime(draft.getDateTime());
            }
            if (draft.hasBranch() && extractedState.getBranchId() == null) {
                extractedState.setHasBranch(true);
                extractedState.setBranchId(draft.getBranchId() != null ? draft.getBranchId().toString() : null);
                extractedState.setBranchName(draft.getBranchName());
            }
            if (draft.hasService() && extractedState.getServiceId() == null) {
                extractedState.setHasService(true);
                extractedState.setServiceId(draft.getServiceId() != null ? draft.getServiceId().toString() : null);
                extractedState.setServiceType(draft.getServiceType());
            }
            if (draft.hasBay() && extractedState.getBayId() == null) {
                extractedState.setHasBay(true);
                extractedState.setBayId(draft.getBayId() != null ? draft.getBayId().toString() : null);
                extractedState.setBayName(draft.getBayName());
            }
            if (draft.hasTime() && extractedState.getTimeSlot() == null) {
                extractedState.setHasTime(true);
                extractedState.setTimeSlot(draft.getTimeSlot());
            }

            log.info("Extracted state (with draft): vehicle={}, date={}, branch={}, service={}, bay={}, time={}",
                    extractedState.hasVehicle(), extractedState.hasDate(), extractedState.hasBranch(),
                    extractedState.hasService(), extractedState.hasBay(), extractedState.hasTime());

            // ============================================================
            // PHẦN 3: PARSE USER MESSAGE VÀ UPDATE DRAFT TRƯỚC KHI GỌI AI
            // ============================================================
            // CRITICAL: Parse và update draft TRƯỚC KHI build messages để AI nhận được draft context đầy đủ
            // userMessage đã được lấy ở trên
            List<ChatRequest.ChatMessage> conversationHistory = request.getConversationHistory();

                // NEW FLOW: AI extraction trước, pattern matching fallback sau
                // Step 1: Thử AI extraction trước (ưu tiên - thông minh hơn)
                boolean aiExtractionSuccess = false;
                UUID draftIdBeforeAI = draft.getDraftId();
                
                // Lưu state TRƯỚC AI extraction để so sánh chính xác
                UUID vehicleIdBefore = draft.getVehicleId();
                LocalDateTime dateTimeBefore = draft.getDateTime();
                UUID branchIdBefore = draft.getBranchId();
                UUID serviceIdBefore = draft.getServiceId();
                UUID bayIdBefore = draft.getBayId();
                String timeSlotBefore = draft.getTimeSlot();
                
                // CRITICAL: Auto-load bay list TRƯỚC KHI AI extraction nếu ở step 5
                // Đảm bảo available options có bay list để AI extraction có thể validate
                int actualStepBeforeAI = determineActualStep(draft);
                if (actualStepBeforeAI == 5 && !draft.hasBay() && draft.hasService() && draft.hasDate() && draft.hasBranch()) {
                    log.info("STEP 5 detected, auto-loading bay list before AI extraction");
                    
                    // Lấy AvailabilityResponse từ ThreadLocal hoặc conversation history
                    AvailabilityResponse availabilityResponse = DraftContextHolder.getAvailabilityResponse();
                    if (availabilityResponse == null && conversationHistory != null) {
                        availabilityResponse = parseAvailabilityFromHistory(conversationHistory);
                    }
                    
                    // Nếu không có → Auto-call checkAvailability()
                    if (availabilityResponse == null || availabilityResponse.getAvailableBays() == null) {
                        log.info("No AvailabilityResponse found. Auto-calling checkAvailability() before AI extraction for bay selection.");
                        
                        // Lấy service_type từ draft
                        String serviceType = null;
                        List<DraftService> draftServices = bookingDraftService.getDraftServices(draft.getDraftId());
                        if (!draftServices.isEmpty()) {
                            serviceType = draftServices.get(0).getServiceName();
                        } else if (draft.getServiceType() != null) {
                            serviceType = draft.getServiceType();
                        }
                        
                        if (serviceType != null && draft.hasBranch() && draft.hasDate()) {
                            try {
                                com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest availabilityRequest =
                                        com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest.builder()
                                                .serviceType(serviceType)
                                                .branchId(draft.getBranchId() != null ? draft.getBranchId().toString() : null)
                                                .branchName(draft.getBranchName())
                                                .dateTime(draft.getDateTime() != null ? draft.getDateTime().toString() : null)
                                                .build();
                                
                                availabilityResponse = aiBookingAssistantService.checkAvailability(availabilityRequest);
                                DraftContextHolder.setAvailabilityResponse(availabilityResponse);
                                
                                log.info("Auto-called checkAvailability() before AI extraction for bay selection - found {} available bays",
                                        availabilityResponse.getAvailableBays() != null ? availabilityResponse.getAvailableBays().size() : 0);
                            } catch (Exception e) {
                                log.error("Error auto-calling checkAvailability() before AI extraction for bay selection: {}", e.getMessage(), e);
                            }
                        }
                    }
                }
                
                // CRITICAL: Auto-load time slots TRƯỚC KHI AI extraction nếu ở step 6
                // Đảm bảo available options có time slots để AI extraction có thể validate
                if (actualStepBeforeAI == 6 && !draft.hasTime() && draft.hasBay()) {
                    log.info("STEP 6 detected, auto-loading time slots before AI extraction");
                    
                    // Lấy AvailabilityResponse từ ThreadLocal hoặc conversation history
                    AvailabilityResponse availabilityResponse = DraftContextHolder.getAvailabilityResponse();
                    if (availabilityResponse == null && conversationHistory != null) {
                        availabilityResponse = parseAvailabilityFromHistory(conversationHistory);
                    }
                    
                    // Nếu không có → Auto-call checkAvailability()
                    if (availabilityResponse == null || availabilityResponse.getAvailableBays() == null) {
                        log.info("No AvailabilityResponse found. Auto-calling checkAvailability() before AI extraction.");
                        
                        // Lấy service_type từ draft
                        String serviceType = null;
                        List<DraftService> draftServices = bookingDraftService.getDraftServices(draft.getDraftId());
                        if (!draftServices.isEmpty()) {
                            serviceType = draftServices.get(0).getServiceName();
                        } else if (draft.getServiceType() != null) {
                            serviceType = draft.getServiceType();
                        }
                        
                        if (serviceType != null && draft.hasBranch() && draft.hasDate()) {
                            try {
                                com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest availabilityRequest =
                                        com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest.builder()
                                                .serviceType(serviceType)
                                                .branchId(draft.getBranchId() != null ? draft.getBranchId().toString() : null)
                                                .branchName(draft.getBranchName())
                                                .dateTime(draft.getDateTime() != null ? draft.getDateTime().toString() : null)
                                                .build();
                                
                                availabilityResponse = aiBookingAssistantService.checkAvailability(availabilityRequest);
                                DraftContextHolder.setAvailabilityResponse(availabilityResponse);
                                
                                log.info("Auto-called checkAvailability() before AI extraction - found {} available bays",
                                        availabilityResponse.getAvailableBays() != null ? availabilityResponse.getAvailableBays().size() : 0);
                            } catch (Exception e) {
                                log.error("Error auto-calling checkAvailability() before AI extraction: {}", e.getMessage(), e);
                            }
                        }
                    }
                }
                
                // CRITICAL: Auto-call getCustomerVehicles() TRƯỚC KHI AI extraction nếu ở step 1
                // Đảm bảo vehicles có sẵn trong ThreadLocal khi AI extraction chạy
                if (actualStepBeforeAI == 1 && !draft.hasVehicle()) {
                    log.info("STEP 1 detected, auto-loading vehicles before AI extraction");
                    
                    // Kiểm tra xem đã có vehicles trong ThreadLocal chưa
                    GetCustomerVehiclesResponse vehiclesResponse = DraftContextHolder.getVehiclesResponse();
                    if (vehiclesResponse == null && conversationHistory != null) {
                        vehiclesResponse = parseVehiclesFromHistory(conversationHistory);
                        if (vehiclesResponse != null) {
                            DraftContextHolder.setVehiclesResponse(vehiclesResponse);
                            log.info("Found vehicles from conversation history, loaded into ThreadLocal");
                        }
                    }
                    
                    // Nếu không có → Auto-call getCustomerVehicles()
                    if (vehiclesResponse == null || vehiclesResponse.getVehicles() == null || vehiclesResponse.getVehicles().isEmpty()) {
                        log.info("No vehicles found. Auto-calling getCustomerVehicles() before AI extraction.");
                        
                        try {
                            GetCustomerVehiclesRequest vehiclesRequest =
                                    GetCustomerVehiclesRequest.builder()
                                            .customerId(customerId)
                                            .build();
                            
                            vehiclesResponse = aiBookingAssistantService.getCustomerVehicles(vehiclesRequest);
                            DraftContextHolder.setVehiclesResponse(vehiclesResponse);
                            
                            log.info("Auto-called getCustomerVehicles() before AI extraction - found {} vehicles",
                                    vehiclesResponse.getVehicles() != null ? vehiclesResponse.getVehicles().size() : 0);
                        } catch (Exception e) {
                            log.error("Error auto-calling getCustomerVehicles() before AI extraction: {}", e.getMessage(), e);
                        }
                    }
                }
                
                // CRITICAL: Auto-call getBranches() TRƯỚC KHI AI extraction nếu ở step 3
                // Đảm bảo branches có sẵn trong ThreadLocal khi AI extraction chạy
                if (actualStepBeforeAI == 3 && !draft.hasBranch()) {
                    log.info("STEP 3 detected, auto-loading branches before AI extraction");
                    
                    // Kiểm tra xem đã có branches trong ThreadLocal chưa
                    GetBranchesResponse branchesResponse = DraftContextHolder.getBranchesResponse();
                    if (branchesResponse == null && conversationHistory != null) {
                        branchesResponse = parseBranchesFromHistory(conversationHistory);
                        if (branchesResponse != null) {
                            DraftContextHolder.setBranchesResponse(branchesResponse);
                            log.info("Found branches from conversation history, loaded into ThreadLocal");
                        }
                    }
                    
                    // Nếu không có → Auto-call getBranches()
                    if (branchesResponse == null || branchesResponse.getBranches() == null || branchesResponse.getBranches().isEmpty()) {
                        log.info("No branches found. Auto-calling getBranches() before AI extraction.");
                        
                        try {
                            GetBranchesRequest branchesRequest =
                                    com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetBranchesRequest.builder()
                                            .build();
                            
                            branchesResponse = aiBookingAssistantService.getBranches(branchesRequest);
                            DraftContextHolder.setBranchesResponse(branchesResponse);
                            
                            log.info("Auto-called getBranches() before AI extraction - found {} branches",
                                    branchesResponse.getBranches() != null ? branchesResponse.getBranches().size() : 0);
                        } catch (Exception e) {
                            log.error("Error auto-calling getBranches() before AI extraction: {}", e.getMessage(), e);
                        }
                    }
                }
                
                if (shouldUseAIExtraction(draft, userMessage)) {
                    log.info("Trying AI-powered extraction first (priority)");
                    BookingDraft draftAfterAI = tryAIExtraction(draft, userMessage, conversationHistory);
                    
                    // Check nếu AI extraction thành công bằng cách reload draft từ database
                    // và so sánh với state TRƯỚC AI extraction
                    if (draftAfterAI != null) {
                        try {
                            // Đợi một chút để đảm bảo transaction đã commit
                            // Note: Thread.sleep có thể không tốt, nhưng cần thiết để đảm bảo transaction commit
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                log.warn("Thread interrupted while waiting for transaction commit");
                            }
                            BookingDraft reloadedDraft = bookingDraftService.getDraft(draftIdBeforeAI);
                            
                            // So sánh với state TRƯỚC AI extraction (không dùng draft object vì có thể đã bị thay đổi)
                            boolean hasChanges = !Objects.equals(vehicleIdBefore, reloadedDraft.getVehicleId()) ||
                                                !Objects.equals(dateTimeBefore, reloadedDraft.getDateTime()) ||
                                                !Objects.equals(branchIdBefore, reloadedDraft.getBranchId()) ||
                                                !Objects.equals(serviceIdBefore, reloadedDraft.getServiceId()) ||
                                                !Objects.equals(bayIdBefore, reloadedDraft.getBayId()) ||
                                                !Objects.equals(timeSlotBefore, reloadedDraft.getTimeSlot());
                            
                            if (hasChanges) {
                                draft = reloadedDraft;
                                aiExtractionSuccess = true;
                                log.info("AI extraction succeeded, draft updated. Changes detected: vehicle={}, date={}, branch={}, service={}, bay={}, time={}",
                                        !Objects.equals(vehicleIdBefore, reloadedDraft.getVehicleId()),
                                        !Objects.equals(dateTimeBefore, reloadedDraft.getDateTime()),
                                        !Objects.equals(branchIdBefore, reloadedDraft.getBranchId()),
                                        !Objects.equals(serviceIdBefore, reloadedDraft.getServiceId()),
                                        !Objects.equals(bayIdBefore, reloadedDraft.getBayId()),
                                        !Objects.equals(timeSlotBefore, reloadedDraft.getTimeSlot()));
                            } else {
                                log.info("AI extraction did not result in draft changes, falling back to pattern matching");
                            }
                        } catch (Exception e) {
                            log.warn("Error reloading draft after AI extraction: {}", e.getMessage());
                            // Fallback to pattern matching
                        }
                    } else {
                        log.info("AI extraction did not succeed or confidence too low, falling back to pattern matching");
                    }
                }
                
                // Step 2: Fallback - Pattern matching (chỉ khi AI extraction không thành công)
                boolean patternMatchingDetected = false;
                if (!aiExtractionSuccess) {
                    log.info("Using pattern matching as fallback");
                    BookingDraft draftBeforePattern = bookingDraftService.getDraft(draft.getDraftId());
                    UUID vehicleIdBeforePattern = draftBeforePattern.getVehicleId();
                    LocalDateTime dateTimeBeforePattern = draftBeforePattern.getDateTime();
                    UUID branchIdBeforePattern = draftBeforePattern.getBranchId();
                    UUID serviceIdBeforePattern = draftBeforePattern.getServiceId();
                    UUID bayIdBeforePattern = draftBeforePattern.getBayId();
                    String timeSlotBeforePattern = draftBeforePattern.getTimeSlot();
                    
            draft = parseUserSelectionAndUpdateDraft(draft, userMessage, conversationHistory);
                    
                    // Check nếu pattern matching có detect được gì không
                    try {
                        Thread.sleep(50);
                        BookingDraft draftAfterPattern = bookingDraftService.getDraft(draft.getDraftId());
                        patternMatchingDetected = !Objects.equals(vehicleIdBeforePattern, draftAfterPattern.getVehicleId()) ||
                                                  !Objects.equals(dateTimeBeforePattern, draftAfterPattern.getDateTime()) ||
                                                  !Objects.equals(branchIdBeforePattern, draftAfterPattern.getBranchId()) ||
                                                  !Objects.equals(serviceIdBeforePattern, draftAfterPattern.getServiceId()) ||
                                                  !Objects.equals(bayIdBeforePattern, draftAfterPattern.getBayId()) ||
                                                  !Objects.equals(timeSlotBeforePattern, draftAfterPattern.getTimeSlot());
                        if (patternMatchingDetected) {
                            draft = draftAfterPattern;
                            log.info("Pattern matching detected selection, draft updated");
                        } else {
                            log.info("Pattern matching did not detect any selection");
                        }
                    } catch (Exception e) {
                        log.warn("Error checking pattern matching result: {}", e.getMessage());
                    }
                }

            // CRITICAL: Reload draft từ database sau khi update để có data mới nhất
            // Điều này đảm bảo draft context có đầy đủ thông tin mới nhất (ví dụ: bay_id sau khi user chọn bay)
            try {
                draft = bookingDraftService.getDraft(draft.getDraftId());
                log.info("Reloaded draft from database after update - current_step={}, hasVehicle={}, hasBranch={}, hasService={}, hasBay={}, hasTime={}",
                        draft.getCurrentStep(), draft.hasVehicle(), draft.hasBranch(), draft.hasService(), draft.hasBay(), draft.hasTime());
            } catch (Exception e) {
                log.error("Error reloading draft from database: {}", e.getMessage(), e);
                // Fallback: Continue with current draft object
            }

            // 1. Build conversation messages từ history
            // Frontend gửi toàn bộ conversation history, backend sẽ sử dụng tất cả để AI có
            // đầy đủ context
            List<Message> messages = new ArrayList<>();

            // Add conversation history nếu có (sử dụng toàn bộ để AI có đầy đủ context)
            if (request.getConversationHistory() != null && !request.getConversationHistory().isEmpty()) {
                int historySize = request.getConversationHistory().size();

                log.debug("Conversation history: total={}, using all {} messages", historySize, historySize);

                // Sử dụng toàn bộ conversation history để AI có đầy đủ context
                // Điều này đảm bảo AI có thể nhận biết được vehicle_id, date_time, branch_name,
                // service_type, bay_name, time đã được chọn trong các lượt trả lời trước
                for (ChatRequest.ChatMessage chatMessage : request.getConversationHistory()) {
                    if ("user".equalsIgnoreCase(chatMessage.getRole())) {
                        messages.add(new UserMessage(chatMessage.getContent()));
                    } else if ("assistant".equalsIgnoreCase(chatMessage.getRole())) {
                        messages.add(new AssistantMessage(chatMessage.getContent()));
                    } else if ("tool".equalsIgnoreCase(chatMessage.getRole())) {
                        // ToolResponse message - Spring AI sẽ tự động xử lý
                        // Chúng ta có thể log để debug
                        if (chatMessage.getToolResponse() != null) {
                            log.debug("Received ToolResponse in conversation history: function={}, tool_call_id={}",
                                    chatMessage.getToolName(), chatMessage.getToolCallId());
                        }
                        // Note: Spring AI có thể không hỗ trợ ToolMessage trong conversation history
                        // Nếu không hỗ trợ, chúng ta vẫn có extracted_uuids từ frontend
                    }
                }
            }

            // 1.1. Add draft context message để AI biết dữ liệu đã có (ngăn lặp lại)
            // Ưu tiên dùng draft data, fallback sang extracted state
            // CRITICAL: Draft đã được update và reload, nên draft context sẽ có đầy đủ thông tin mới nhất
            String draftContext = buildDraftContextMessage(draft, extractedState);
            if (draftContext != null && !draftContext.trim().isEmpty()) {
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(draftContext));
                log.info("Added draft context to messages - current_step={}, hasVehicle={}, hasBranch={}, hasService={}, hasBay={}, hasTime={}",
                        draft.getCurrentStep(), draft.hasVehicle(), draft.hasBranch(), draft.hasService(), draft.hasBay(), draft.hasTime());
            }
            
            // 1.1.1. CRITICAL: Detect "thay đổi thời gian" (ambiguous) → Hỏi xác nhận
            String userMsgLower = userMessage.toLowerCase();
            boolean isAmbiguousTimeChange = (userMsgLower.contains("thay đổi thời gian") || 
                                            userMsgLower.contains("đổi thời gian")) &&
                                           !userMsgLower.contains("ngày") && 
                                           !userMsgLower.contains("giờ") &&
                                           !userMsgLower.matches(".*\\b\\d{1,2}:\\d{2}\\b.*");
            
            if (isAmbiguousTimeChange && draft.hasDate() && draft.hasTime()) {
                String clarificationInstruction = """
                    QUAN TRỌNG: User muốn "thay đổi thời gian" nhưng không rõ là đổi NGÀY hay đổi GIỜ.
                    Bạn PHẢI hỏi xác nhận:
                    - "Bạn muốn thay đổi NGÀY đặt lịch hay GIỜ đặt lịch?"
                    - Nếu user nói "ngày" hoặc "đổi ngày" → Đổi date_time (ngày đặt lịch)
                    - Nếu user nói "giờ" hoặc "đổi giờ" → Đổi time_slot (giờ đặt lịch, format HH:mm)
                    KHÔNG được tự động đoán, PHẢI hỏi xác nhận trước.
                    """;
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(clarificationInstruction));
                log.info("Detected ambiguous 'thay đổi thời gian', injected clarification instruction");
            }
            
            // 1.1.2. CRITICAL: Nếu AI extraction fail và pattern matching cũng không detect được
            // → Inject instruction để AI KHÔNG tự động chọn, PHẢI yêu cầu user chọn lại
            if (!aiExtractionSuccess && !patternMatchingDetected && !isAmbiguousTimeChange) {
                int actualStep = determineActualStep(draft);
                String noSelectionInstruction = buildNoSelectionInstruction(actualStep, draft, userMessage);
                if (noSelectionInstruction != null && !noSelectionInstruction.trim().isEmpty()) {
                    messages.add(new org.springframework.ai.chat.messages.SystemMessage(noSelectionInstruction));
                    log.info("Injected instruction: AI extraction and pattern matching both failed, AI must ask user to select again");
                }
            }

            // 1.2. CRITICAL: Xác định actual step TRƯỚC KHI xử lý auto-load và prevent instructions
            // Sử dụng determineActualStep() để xác định step thực tế dựa trên data đã có
            // CRITICAL: Draft đã được update và reload, nên actualStep sẽ chính xác
            int actualStep = determineActualStep(draft);
            log.info("Determined actual step: {} (draft.current_step={})", actualStep, draft.getCurrentStep());

            // 1.3. CRITICAL: Auto-call getCustomerVehicles() nếu ở STEP 1 và chưa có vehicle
            // → Tự động GỌI getCustomerVehicles() TRỰC TIẾP từ backend và inject kết quả
            // PHẢI CHẠY TRƯỚC prevent instructions để đảm bảo load data khi cần
            if (actualStep == 1 && !draft.hasVehicle()) {
                log.info("Auto-calling getCustomerVehicles() directly from backend - STEP 1 (actualStep={}), no vehicle selected", actualStep);

                // Gọi function trực tiếp từ backend
                try {
                    GetCustomerVehiclesRequest vehiclesRequest =
                            GetCustomerVehiclesRequest.builder()
                                    .customerId(customerId)
                                    .build();

                    GetCustomerVehiclesResponse vehiclesResponse =
                            aiBookingAssistantService.getCustomerVehicles(vehiclesRequest);

                    // Lưu response vào ThreadLocal để parse user selection sau này
                    DraftContextHolder.setVehiclesResponse(vehiclesResponse);

                    // Inject instruction để AI sử dụng kết quả đã có
                    if (vehiclesResponse.getVehicles() != null && !vehiclesResponse.getVehicles().isEmpty()) {
                        String autoCallInstruction = String.format("""
                                CRITICAL INSTRUCTION - BẠN PHẢI LÀM NGAY:
                                - Backend đã TỰ ĐỘNG gọi getCustomerVehicles() và có kết quả
                                - Danh sách xe đã được load sẵn, bạn KHÔNG cần gọi function lại
                                - BẠN PHẢI hiển thị danh sách xe cho user ngay lập tức
                                - Format: "1. [biển số], 2. [biển số], ..."
                                - Sau đó hỏi: "Bạn muốn chọn xe nào? Vui lòng cho tôi biết số thứ tự hoặc biển số."
                                - KHÔNG được hỏi lại "Bạn muốn đặt lịch không?" hoặc "Tôi sẽ lấy danh sách xe"
                                                                
                                DANH SÁCH XE ĐÃ LOAD:
                                %s
                                """, formatVehiclesList(vehiclesResponse));
                        messages.add(new org.springframework.ai.chat.messages.SystemMessage(autoCallInstruction));

                        log.info("Auto-called getCustomerVehicles() - found {} vehicles", vehiclesResponse.getVehicles().size());
                    } else {
                        // Không có xe
                        String noVehiclesInstruction = """
                                CRITICAL INSTRUCTION:
                                - Backend đã gọi getCustomerVehicles() nhưng user chưa có xe nào
                                - BẠN PHẢI thông báo: "Bạn chưa có xe nào trong hệ thống. Vui lòng tạo xe mới trước khi đặt lịch."
                                - KHÔNG được hỏi lại về việc đặt lịch
                                """;
                        messages.add(new org.springframework.ai.chat.messages.SystemMessage(noVehiclesInstruction));
                        log.info("Auto-called getCustomerVehicles() - no vehicles found");
                    }
                } catch (Exception e) {
                    log.error("Error auto-calling getCustomerVehicles(): {}", e.getMessage(), e);
                    // Fallback: Inject instruction để AI gọi function
                    String autoCallInstruction = """
                            CRITICAL INSTRUCTION - BẠN PHẢI LÀM NGAY:
                            - User đang ở STEP 1 (chọn xe) và chưa có vehicle_id
                            - BẠN PHẢI TỰ ĐỘNG GỌI getCustomerVehicles() NGAY LẬP TỨC
                            - KHÔNG nói "Tôi sẽ lấy danh sách xe" rồi chờ user yêu cầu
                            - PHẢI gọi function NGAY và hiển thị danh sách xe cho user chọn
                            - KHÔNG được hỏi lại user về việc có muốn đặt lịch không
                            """;
                    messages.add(new org.springframework.ai.chat.messages.SystemMessage(autoCallInstruction));
                }
            }

            // 1.4. CRITICAL: Auto-call getBranches() nếu ở STEP 3 và chưa có branch
            // Gọi khi: (1) đang ở STEP 3 (xác định bằng determineActualStep) VÀ (2) chưa có branch trong draft
            // PHẢI CHẠY TRƯỚC prevent instructions để đảm bảo load data khi cần
            if (actualStep == 3 && !draft.hasBranch()) {
                log.info("Auto-calling getBranches() directly from backend - STEP 3 (actualStep={}), no branch selected", actualStep);

                // Gọi function trực tiếp từ backend
                try {
                    GetBranchesRequest branchesRequest =
                            com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetBranchesRequest.builder()
                                    .build();

                    GetBranchesResponse branchesResponse =
                            aiBookingAssistantService.getBranches(branchesRequest);

                    // Lưu response vào ThreadLocal để parse user selection sau này
                    DraftContextHolder.setBranchesResponse(branchesResponse);

                    // Inject instruction để AI sử dụng kết quả đã có
                    if (branchesResponse.getBranches() != null && !branchesResponse.getBranches().isEmpty()) {
                        String autoCallInstruction = String.format("""
                                CRITICAL INSTRUCTION - BẠN PHẢI LÀM NGAY:
                                - Backend đã TỰ ĐỘNG gọi getBranches() và có kết quả
                                - Danh sách chi nhánh đã được load sẵn, bạn KHÔNG cần gọi function lại
                                - BẠN PHẢI hiển thị danh sách chi nhánh cho user ngay lập tức
                                - Format: "1. [tên chi nhánh] - [địa chỉ], 2. [tên chi nhánh] - [địa chỉ], ..."
                                - Sau đó hỏi: "Bạn muốn chọn chi nhánh nào? Vui lòng cho tôi biết số thứ tự hoặc tên chi nhánh."
                                - KHÔNG được hỏi lại "Bạn muốn chọn chi nhánh không?" hoặc "Tôi sẽ lấy danh sách chi nhánh"
                                                                
                                DANH SÁCH CHI NHÁNH ĐÃ LOAD:
                                %s
                                """, formatBranchesList(branchesResponse));
                        messages.add(new org.springframework.ai.chat.messages.SystemMessage(autoCallInstruction));

                        log.info("Auto-called getBranches() - found {} branches", branchesResponse.getBranches().size());
                    } else {
                        // Không có chi nhánh
                        String noBranchesInstruction = """
                                CRITICAL INSTRUCTION:
                                - Backend đã gọi getBranches() nhưng không có chi nhánh nào
                                - BẠN PHẢI thông báo: "Hiện tại hệ thống chưa có chi nhánh nào. Vui lòng liên hệ admin."
                                - KHÔNG được hỏi lại về việc chọn chi nhánh
                                """;
                        messages.add(new org.springframework.ai.chat.messages.SystemMessage(noBranchesInstruction));
                        log.info("Auto-called getBranches() - no branches found");
                    }
                } catch (Exception e) {
                    log.error("Error auto-calling getBranches(): {}", e.getMessage(), e);
                    // Fallback: Inject instruction để AI gọi function
                    String autoCallInstruction = """
                            CRITICAL INSTRUCTION - BẠN PHẢI LÀM NGAY:
                            - User đang ở STEP 3 (chọn chi nhánh) và chưa có branch_id
                            - BẠN PHẢI TỰ ĐỘNG GỌI getBranches() NGAY LẬP TỨC
                            - KHÔNG nói "Tôi sẽ lấy danh sách chi nhánh" rồi chờ user yêu cầu
                            - PHẢI gọi function NGAY và hiển thị danh sách chi nhánh cho user chọn
                            - KHÔNG được hỏi lại user về việc có muốn chọn chi nhánh không
                            """;
                    messages.add(new org.springframework.ai.chat.messages.SystemMessage(autoCallInstruction));
                }
            }

            // 1.5. CRITICAL: Inject instruction mạnh để ngăn AI gọi lại function khi đã có dữ liệu
            // CHẠY SAU auto-load để đảm bảo không conflict
            if (draft.hasBranch()) {
                String preventBranchCallInstruction = """
                        ⚠️ CRITICAL - TUYỆT ĐỐI KHÔNG GỌI getBranches() ⚠️
                        - Draft đã có branch_id và branch_name đầy đủ
                        - BẠN ĐANG Ở STEP 4 (chọn dịch vụ), KHÔNG phải STEP 3 (chọn chi nhánh)
                        - TUYỆT ĐỐI KHÔNG được gọi getBranches() khi draft đã có branch_id
                        - TUYỆT ĐỐI KHÔNG được hỏi lại user về chi nhánh
                        - Nếu user nói về dịch vụ (ví dụ: "rửa xe", "bảo dưỡng") → CHỈ gọi getServices(keyword)
                        - Nếu user nhắc lại chi nhánh → CHỈ xác nhận lại, KHÔNG gọi getBranches()
                        """;
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(preventBranchCallInstruction));
                log.info("Injected instruction to prevent getBranches() call - branch already selected");
            }

            if (draft.hasVehicle()) {
                String preventVehicleCallInstruction = """
                        ⚠️ CRITICAL - TUYỆT ĐỐI KHÔNG GỌI getCustomerVehicles() ⚠️
                        - Draft đã có vehicle_id và vehicle_license_plate đầy đủ
                        - TUYỆT ĐỐI KHÔNG được gọi getCustomerVehicles() khi draft đã có vehicle_id
                        - TUYỆT ĐỐI KHÔNG được hỏi lại user về xe
                        - Nếu user nhắc lại biển số → CHỈ xác nhận lại, KHÔNG gọi getCustomerVehicles()
                        """;
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(preventVehicleCallInstruction));
                log.info("Injected instruction to prevent getCustomerVehicles() call - vehicle already selected");
            }

            // 1.5.1. CRITICAL: Auto-call checkAvailability() nếu ở STEP 5 và chưa có bay
            // → Tự động GỌI checkAvailability() TRỰC TIẾP từ backend và inject kết quả (danh sách bay)
            // PHẢI CHẠY TRƯỚC prevent instructions để đảm bảo load data khi cần
            if (actualStep == 5 && !draft.hasBay() && draft.hasService() && draft.hasDate() && draft.hasBranch()) {
                log.info("Auto-calling checkAvailability() directly from backend - STEP 5 (actualStep={}), no bay selected", actualStep);

                // Gọi function trực tiếp từ backend
                try {
                    // Lấy service_type từ draft
                    String serviceType = null;
                    List<DraftService> draftServices = bookingDraftService.getDraftServices(draft.getDraftId());
                    if (!draftServices.isEmpty()) {
                        serviceType = draftServices.get(0).getServiceName();
                    } else if (draft.getServiceType() != null) {
                        serviceType = draft.getServiceType();
                    }

                    if (serviceType != null && draft.hasBranch() && draft.hasDate()) {
                        com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest availabilityRequest =
                                com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest.builder()
                                        .serviceType(serviceType)
                                        .branchId(draft.getBranchId() != null ? draft.getBranchId().toString() : null)
                                        .branchName(draft.getBranchName())
                                        .dateTime(draft.getDateTime() != null ? draft.getDateTime().toString() : null)
                                        .build();

                        AvailabilityResponse availabilityResponse = aiBookingAssistantService.checkAvailability(availabilityRequest);
                        DraftContextHolder.setAvailabilityResponse(availabilityResponse);

                        // Inject instruction để AI sử dụng kết quả đã có
                        if (availabilityResponse != null && availabilityResponse.getAvailableBays() != null && !availabilityResponse.getAvailableBays().isEmpty()) {
                            String autoCallInstruction = String.format("""
                                    CRITICAL INSTRUCTION - BẠN PHẢI LÀM NGAY:
                                    - Backend đã TỰ ĐỘNG gọi checkAvailability() và có kết quả
                                    - Danh sách bay/khu vực đã được load sẵn, bạn KHÔNG cần gọi function lại
                                    - BẠN PHẢI hiển thị danh sách bay cho user ngay lập tức
                                    - Format: "1. [tên bay], 2. [tên bay], ..."
                                    - Sau đó hỏi: "Bạn muốn chọn bay/khu vực nào? Vui lòng cho tôi biết số thứ tự hoặc tên bay."
                                    - KHÔNG được hỏi lại "Bạn muốn chọn bay không?" hoặc "Tôi sẽ kiểm tra availability"
                                    
                                    DANH SÁCH BAY ĐÃ LOAD:
                                    %s
                                    """, formatBaysList(availabilityResponse));
                            messages.add(new org.springframework.ai.chat.messages.SystemMessage(autoCallInstruction));

                            log.info("Auto-called checkAvailability() - found {} available bays", availabilityResponse.getAvailableBays().size());
                        } else {
                            // Không có bay
                            String noBaysInstruction = """
                                    CRITICAL INSTRUCTION:
                                    - Backend đã gọi checkAvailability() nhưng không có bay nào khả dụng
                                    - BẠN PHẢI thông báo: "Hiện tại không có bay/khu vực nào khả dụng cho ngày và dịch vụ bạn đã chọn. Vui lòng chọn ngày khác hoặc dịch vụ khác."
                                    - KHÔNG được hỏi lại về việc chọn bay
                                    """;
                            messages.add(new org.springframework.ai.chat.messages.SystemMessage(noBaysInstruction));
                            log.info("Auto-called checkAvailability() - no available bays found");
                        }
                    } else {
                        log.warn("Cannot auto-call checkAvailability() for bay selection: missing required data - serviceType={}, hasBranch={}, hasDate={}",
                                serviceType, draft.hasBranch(), draft.hasDate());
                    }
                } catch (Exception e) {
                    log.error("Error auto-calling checkAvailability() for bay selection: {}", e.getMessage(), e);
                    // Fallback: Inject instruction để AI gọi function
                    String autoCallInstruction = """
                            CRITICAL INSTRUCTION - BẠN PHẢI LÀM NGAY:
                            - User đang ở STEP 5 (chọn bay/khu vực) và chưa có bay_id
                            - BẠN PHẢI TỰ ĐỘNG GỌI checkAvailability() NGAY LẬP TỨC
                            - KHÔNG nói "Tôi sẽ kiểm tra availability" rồi chờ user yêu cầu
                            - PHẢI gọi function NGAY và hiển thị danh sách bay cho user chọn
                            - KHÔNG được hỏi lại user về việc có muốn chọn bay không
                            """;
                    messages.add(new org.springframework.ai.chat.messages.SystemMessage(autoCallInstruction));
                }
            }

            // 1.6. CRITICAL: Sau khi chọn bay thành công, tự động lấy danh sách time slots và inject cho AI
            // PHẢI CHẠY TRƯỚC KHI GỌI AI để AI có thể hiển thị time slots ngay
            if (draft.hasBay() && !draft.hasTime()) {
                log.info("Bay selected, loading time slots for bay_id: {}, bay_name: {}",
                        draft.getBayId(), draft.getBayName());

                // Lấy AvailabilityResponse từ ThreadLocal (từ lần gọi checkAvailability trước đó)
                AvailabilityResponse availabilityResponse =
                        DraftContextHolder.getAvailabilityResponse();

                // Nếu không có AvailabilityResponse trong ThreadLocal → Tự động gọi checkAvailability() lại
                if (availabilityResponse == null || availabilityResponse.getAvailableBays() == null) {
                    log.info("No AvailabilityResponse found in ThreadLocal. Auto-calling checkAvailability() to get time slots.");

                    // Lấy service_type từ draft (ưu tiên từ DraftService, fallback sang serviceType)
                    String serviceType = null;
                    List<DraftService> draftServices =
                            bookingDraftService.getDraftServices(draft.getDraftId());
                    if (!draftServices.isEmpty()) {
                        // Lấy service đầu tiên (primary service)
                        serviceType = draftServices.get(0).getServiceName();
                    } else if (draft.getServiceType() != null) {
                        serviceType = draft.getServiceType();
                    }

                    // Kiểm tra đủ dữ liệu để gọi checkAvailability()
                    if (serviceType != null && draft.hasBranch() && draft.hasDate()) {
                        try {
                            // Build AvailabilityRequest từ draft data
                            com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest availabilityRequest =
                                    com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest.builder()
                                            .serviceType(serviceType)
                                            .branchId(draft.getBranchId() != null ? draft.getBranchId().toString() : null)
                                            .branchName(draft.getBranchName())
                                            .dateTime(draft.getDateTime() != null ? draft.getDateTime().toString() : null)
                                            .build();

                            // Gọi checkAvailability() trực tiếp từ service
                            availabilityResponse = aiBookingAssistantService.checkAvailability(availabilityRequest);

                            // Lưu response vào ThreadLocal để dùng sau này
                            com.kltn.scsms_api_service.core.utils.DraftContextHolder.setAvailabilityResponse(availabilityResponse);

                            log.info("Auto-called checkAvailability() - found {} available bays",
                                    availabilityResponse.getAvailableBays() != null ? availabilityResponse.getAvailableBays().size() : 0);
                        } catch (Exception e) {
                            log.error("Error auto-calling checkAvailability(): {}", e.getMessage(), e);
                            availabilityResponse = null;
                        }
                    } else {
                        log.warn("Cannot auto-call checkAvailability(): missing required data - serviceType={}, hasBranch={}, hasDate={}",
                                serviceType, draft.hasBranch(), draft.hasDate());
                    }
                }

                // Tìm bay đã chọn trong availableBays và lấy time slots
                if (availabilityResponse != null && availabilityResponse.getAvailableBays() != null) {
                    // Tìm bay đã chọn trong availableBays
                    for (com.kltn.scsms_api_service.core.dto.aiAssistant.response.AvailabilityResponse.AvailableBayInfo bay :
                            availabilityResponse.getAvailableBays()) {
                        if (bay.getBayId() != null && bay.getBayId().equals(draft.getBayId())) {
                            // Tìm thấy bay đã chọn
                            List<String> timeSlots = bay.getAvailableSlots();
                            if (timeSlots != null && !timeSlots.isEmpty()) {
                                // Format và làm tròn thời gian (9:01 → 9:00, 9:31 → 9:30)
                                List<String> roundedSlots = formatAndRoundTimeSlots(timeSlots);

                                // Inject instruction cho AI để hiển thị danh sách time slots
                                String timeSlotsInstruction = String.format("""
                                        CRITICAL INSTRUCTION - BẠN PHẢI LÀM NGAY:
                                        - User đã chọn bay '%s' thành công
                                        - Backend đã TỰ ĐỘNG gọi checkAvailability() và lấy danh sách thời gian trống từ bay này
                                        - Danh sách thời gian đã được load sẵn, bạn KHÔNG cần gọi function lại
                                        - BẠN PHẢI hiển thị danh sách thời gian trống cho user ngay lập tức
                                        - Format: "1. 08:00, 2. 08:30, 3. 09:00, ..."
                                        - Sau đó hỏi: "Bạn muốn chọn giờ nào? Vui lòng cho tôi biết số thứ tự hoặc giờ (ví dụ: 08:00)."
                                        - KHÔNG được nói "bạn cần chọn giờ trước" hoặc "bạn muốn chọn giờ nào" mà không hiển thị danh sách
                                                                                
                                        DANH SÁCH THỜI GIAN TRỐNG ĐÃ LOAD:
                                        %s
                                        """, draft.getBayName(), formatTimeSlotsList(roundedSlots));
                                messages.add(new org.springframework.ai.chat.messages.SystemMessage(timeSlotsInstruction));

                                log.info("Injected time slots instruction - found {} slots for bay: {}",
                                        roundedSlots.size(), draft.getBayName());
                            } else {
                                log.warn("Bay {} has no available slots in AvailabilityResponse", draft.getBayName());
                            }
                            break;
                        }
                    }
                } else {
                    log.warn("Could not get AvailabilityResponse even after auto-calling checkAvailability(). AI should call checkAvailability() manually.");
                }
            }

            // Fallback: Add state context nếu draft chưa có data
            if (!draft.hasVehicle() && !draft.hasDate() && !draft.hasBranch() && extractedState.hasAnyData()) {
                String stateContext = buildStateContextMessage(extractedState, request.getConversationHistory());
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(stateContext));
                log.debug("Added fallback state context to messages");
            }

            // 1.3. Validate draft data trước khi tiếp tục
            // Kiểm tra xem draft có đủ dữ liệu và đúng bước không
            // CRITICAL: Sử dụng actualStep thay vì draft.getCurrentStep() để validation chính xác
            int actualStepForValidation = determineActualStep(draft);
            String validationError = validateDraftBeforeProceeding(draft, actualStepForValidation);
            if (validationError != null) {
                log.warn("Draft validation failed: {}", validationError);
                // Inject error message vào prompt để AI thông báo cho user
                String errorInstruction = String.format("""
                        QUAN TRỌNG - CÓ LỖI XẢY RA:
                        - %s
                        - BẠN PHẢI thông báo lỗi này cho user và yêu cầu user nhập lại cho chính xác
                        - KHÔNG được tiếp tục bước tiếp theo khi có lỗi
                        - PHẢI hỏi lại user về thông tin bị thiếu hoặc sai
                        """, validationError);
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(errorInstruction));
            }

            // 1.4. CRITICAL: Inject instruction mạnh để ngăn AI gọi checkAvailability() khi đã có đầy đủ data
            // Nếu draft đã complete (step 7), TUYỆT ĐỐI không được gọi checkAvailability() hoặc yêu cầu chọn bay/time
            if (draft.isComplete() || actualStep >= 7) {
                String preventCheckAvailabilityInstruction = """
                        ⚠️ CRITICAL - DRAFT ĐÃ HOÀN THÀNH - TUYỆT ĐỐI KHÔNG GỌI checkAvailability() ⚠️
                        - Draft đã có ĐẦY ĐỦ dữ liệu: vehicle, date, branch, service, bay, time
                        - BẠN ĐANG Ở STEP 7 (hoàn thành), KHÔNG phải STEP 5 (chọn bay) hay STEP 6 (chọn giờ)
                        - TUYỆT ĐỐI KHÔNG được gọi checkAvailability() khi draft đã complete
                        - TUYỆT ĐỐI KHÔNG được yêu cầu user chọn bay hoặc chọn giờ
                        - TUYỆT ĐỐI KHÔNG được nói "cần hoàn thành bước chọn bay" hoặc "cần chọn giờ"
                        - Nếu user đã chọn đầy đủ → CHỈ hỏi xác nhận để tạo booking
                        - Nếu user chưa xác nhận → CHỈ tóm tắt thông tin và hỏi xác nhận
                        """;
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(preventCheckAvailabilityInstruction));
                log.info("Injected instruction to prevent checkAvailability() call - draft is complete");
            }

            // 1.5. Kiểm tra xác nhận trước khi cho phép createBooking
            // Nếu user đang ở step 7 và đã có đủ dữ liệu, kiểm tra xác nhận
            // CRITICAL: Kiểm tra cả userMessage hiện tại và conversationHistory
            boolean isAtFinalStep = actualStep >= 7 && draft.isComplete();
            boolean userHasConfirmed = hasUserConfirmed(userMessage, conversationHistory);
            if (isAtFinalStep && !userHasConfirmed) {
                // User chưa xác nhận, inject instruction để AI hỏi xác nhận
                String confirmInstruction = """
                        QUAN TRỌNG - CHƯA CÓ XÁC NHẬN:
                        - User chưa xác nhận đặt lịch
                        - Draft đã có ĐẦY ĐỦ dữ liệu: vehicle, date, branch, service, bay, time
                        - BẠN PHẢI tóm tắt thông tin đặt lịch và hỏi xác nhận từ user
                        - CHỈ KHI user nói "OK" hoặc "Xác nhận" → MỚI được gọi createBooking()
                        - TUYỆT ĐỐI KHÔNG được gọi createBooking() nếu chưa có xác nhận
                        - Nếu user message không phải là xác nhận → CHỈ hỏi lại xác nhận, KHÔNG gọi createBooking()
                        - TUYỆT ĐỐI KHÔNG được yêu cầu user chọn bay hoặc chọn giờ khi draft đã complete
                        """;
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(confirmInstruction));
                log.info("User has not confirmed booking yet, AI should ask for confirmation");
            } else if (isAtFinalStep && userHasConfirmed) {
                // User đã xác nhận, inject instruction để AI gọi createBooking()
                String confirmInstruction = """
                        ✅ CRITICAL - USER ĐÃ XÁC NHẬN:
                        - User đã xác nhận đặt lịch (trong message hiện tại hoặc conversation history)
                        - Draft đã có ĐẦY ĐỦ dữ liệu: vehicle, date, branch, service, bay, time
                        - BẠN PHẢI gọi createBooking() NGAY LẬP TỨC
                        - KHÔNG được hỏi lại xác nhận
                        - KHÔNG được tóm tắt lại thông tin
                        - PHẢI gọi createBooking() với đầy đủ thông tin từ draft
                        """;
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(confirmInstruction));
                log.info("User has confirmed booking, AI should call createBooking() immediately");
            }

            // 2. Add current user message
            messages.add(new UserMessage(userMessage));

            // 3. Gọi ChatClient với conversation history
            long aiCallStartTime = System.currentTimeMillis();
            log.debug("Calling OpenAI API with {} messages", messages.size());

            // ChatClient sẽ tự động:
            // - Inject System Prompt
            // - Phân tích message với context từ history
            // - Quyết định có cần gọi functions không
            // - Gọi functions nếu cần
            // - Tạo response
            org.springframework.ai.chat.model.ChatResponse springAiChatResponse;

            if (messages.size() > 1) {
                // Có conversation history, pass tất cả messages
                springAiChatResponse = aiChatClient.prompt()
                        .messages(messages)
                        .call()
                        .chatResponse();
            } else {
                // Không có history, chỉ pass user message (backward compatible)
                springAiChatResponse = aiChatClient.prompt()
                        .user(userMessage)
                        .call()
                        .chatResponse();
            }

            long aiCallEndTime = System.currentTimeMillis();
            log.info("OpenAI API call completed in {} ms", (aiCallEndTime - aiCallStartTime));

            // 4. Extract response message
            String aiMessage = springAiChatResponse.getResult().getOutput().getContent();

            // 4.1. Clean response: Remove STATE JSON if AI accidentally includes it
            // This is a safety net in case AI still prints STATE despite instructions
            if (aiMessage != null) {
                // Remove STATE JSON blocks
                aiMessage = aiMessage.replaceAll("(?s)=== STATE HIỆN TẠI ===.*?=== NỘI DUNG TRẢ LỜI ===", "");
                aiMessage = aiMessage.replaceAll("(?s)\\{\\s*\"current_step\".*?\\}", "");
                aiMessage = aiMessage.replaceAll("(?s)\"current_step\".*?\"next_action\".*?\\}", "");
                // Remove any remaining STATE markers
                aiMessage = aiMessage.replaceAll("=== STATE HIỆN TẠI ===", "");
                aiMessage = aiMessage.replaceAll("=== NỘI DUNG TRẢ LỜI ===", "");
                // Trim whitespace
                aiMessage = aiMessage.trim();

                // If message becomes empty after cleaning, use default message
                if (aiMessage.isEmpty()) {
                    aiMessage = "Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.";
                }
            } else {
                aiMessage = "Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.";
            }

            // 5. Extract functions called (nếu có)
            List<String> functionsCalled = new ArrayList<>();
            // Note: Spring AI có thể expose function calls, nhưng cần check API
            // Tạm thời để empty, có thể cải thiện sau

            // 6. Determine if action is required
            boolean requiresAction = aiMessage.toLowerCase().contains("xác nhận") ||
                    aiMessage.toLowerCase().contains("bạn có muốn");
            String actionType = requiresAction ? "CONFIRM_BOOKING" : null;

            // 7. Parse user message và update draft đã được thực hiện TRƯỚC KHI build messages (xem PHẦN 3 ở trên)
            // Draft đã được update và reload, nên không cần parse lại ở đây

            // 7.1. Auto-load time slots đã được thực hiện TRƯỚC KHI gọi AI (xem phần 1.6 ở trên)
            // Logic đã được di chuyển lên trước khi gọi AI để đảm bảo AI nhận được instruction đúng lúc

            // 8. Build response với draft data
            // CRITICAL: Reload draft một lần nữa để đảm bảo có data mới nhất trước khi trả về
            try {
                draft = bookingDraftService.getDraft(draft.getDraftId());
                log.debug("Reloaded draft before building response - current_step={}, hasBay={}, hasTime={}",
                        draft.getCurrentStep(), draft.hasBay(), draft.hasTime());
            } catch (Exception e) {
                log.warn("Error reloading draft before building response: {}", e.getMessage());
                // Fallback: Continue with current draft object
            }
            ChatResponse.DraftData draftData = ChatResponse.DraftData.builder()
                    .currentStep(draft.getCurrentStep())
                    .hasVehicle(draft.hasVehicle())
                    .hasDate(draft.hasDate())
                    .hasBranch(draft.hasBranch())
                    .hasService(draft.hasService())
                    .hasBay(draft.hasBay())
                    .hasTime(draft.hasTime())
                    .isComplete(draft.isComplete())
                    .build();

            ChatResponse response = ChatResponse.builder()
                    .message(aiMessage)
                    .functionsCalled(functionsCalled)
                    .requiresAction(requiresAction)
                    .actionType(actionType)
                    .draftId(draft.getDraftId())
                    .draftData(draftData)
                    .build();

            long requestEndTime = System.currentTimeMillis();
            log.info("=== AI RESPONSE ===: {}", aiMessage);
            log.info("Draft State in Response: draft_id={}, current_step={}, is_complete={}",
                    draft.getDraftId(), draft.getCurrentStep(), draft.isComplete());
            log.debug("AI Assistant response: message length={}, requiresAction={}, totalTime={} ms",
                    aiMessage != null ? aiMessage.length() : 0, requiresAction, (requestEndTime - requestStartTime));

            return ResponseBuilder.success(response);

        } catch (Exception e) {
            long requestEndTime = System.currentTimeMillis();
            log.error("Error in AI Assistant chat after {} ms: {}",
                    (requestEndTime - requestStartTime), e.getMessage(), e);
            ChatResponse errorResponse = ChatResponse.builder()
                    .message("Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.")
                    .requiresAction(false)
                    .build();
            return ResponseBuilder.success(errorResponse);
        } finally {
            // Clear ThreadLocal sau khi request hoàn thành
            com.kltn.scsms_api_service.core.utils.DraftContextHolder.clear();
        }
    }

    /**
     * Xóa booking draft khi user click "Clear Chat" trên UI
     * Endpoint này sẽ xóa draft thực sự khỏi database (hard delete)
     */
    @PostMapping("/draft/{draftId}")
    @Operation(summary = "Delete booking draft", description = "Delete a booking draft when user clears chat. This permanently removes the draft from database.")
    @SwaggerOperation(summary = "Delete booking draft")
    public ResponseEntity<ApiResponse<Void>> clearDraft(
            @Parameter(description = "Draft ID to delete") @PathVariable UUID draftId) {

        log.info("Deleting draft (hard delete): draft_id={}", draftId);

        try {
            // Xóa draft thực sự khỏi database
            bookingDraftService.deleteDraft(draftId);

            log.info("Successfully deleted draft: draft_id={}", draftId);
            return ResponseBuilder.success("Draft deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting draft: draft_id={}, error: {}", draftId, e.getMessage(), e);
            return ResponseBuilder.badRequest("Failed to delete draft: " + e.getMessage());
        }
    }

    /**
     * Xóa draft theo session ID (alternative endpoint)
     * Useful when frontend only has session_id but not draft_id
     * Endpoint này sẽ xóa draft thực sự khỏi database (hard delete)
     */
    @PostMapping("/draft/session/{sessionId}")
    @Operation(summary = "Delete booking draft by session", description = "Delete a booking draft by session ID when user clears chat. This permanently removes the draft from database.")
    @SwaggerOperation(summary = "Delete booking draft by session")
    public ResponseEntity<ApiResponse<Void>> clearDraftBySession(
            @Parameter(description = "Session ID to delete draft") @PathVariable String sessionId) {

        log.info("Deleting draft by session (hard delete): session_id={}", sessionId);

        try {
            // Xóa draft thực sự khỏi database
            bookingDraftService.deleteDraftBySession(sessionId);

            log.info("Successfully deleted draft by session: session_id={}", sessionId);
            return ResponseBuilder.success("Draft deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting draft by session: session_id={}, error: {}", sessionId, e.getMessage(), e);
            return ResponseBuilder.badRequest("Failed to delete draft: " + e.getMessage());
        }
    }

    /**
     * Extract state từ conversation history để biết dữ liệu đã có
     * Ưu tiên sử dụng extracted_uuids từ frontend nếu có
     */
    private ConversationState extractStateFromHistory(
            List<ChatRequest.ChatMessage> history,
            ChatRequest.ExtractedUuids extractedUuids) {
        ConversationState state = new ConversationState();

        // BƯỚC 1: Ưu tiên sử dụng extracted_uuids từ frontend (nếu có)
        // Điều này giúp tránh query database lại và đảm bảo UUIDs chính xác
        if (extractedUuids != null) {
            if (extractedUuids.getVehicleId() != null && !extractedUuids.getVehicleId().trim().isEmpty()) {
                state.setHasVehicle(true);
                state.setVehicleId(extractedUuids.getVehicleId());
                state.setVehicleLicensePlate(extractedUuids.getVehicleLicensePlate());
                log.info("Using vehicle_id from frontend extracted_uuids: {} (license_plate: {})",
                        extractedUuids.getVehicleId(), extractedUuids.getVehicleLicensePlate());
            }
            if (extractedUuids.getBranchId() != null && !extractedUuids.getBranchId().trim().isEmpty()) {
                state.setHasBranch(true);
                state.setBranchId(extractedUuids.getBranchId());
                state.setBranchName(extractedUuids.getBranchName());
                log.info("Using branch_id from frontend extracted_uuids: {} (branch_name: {})",
                        extractedUuids.getBranchId(), extractedUuids.getBranchName());
            }
            if (extractedUuids.getBayId() != null && !extractedUuids.getBayId().trim().isEmpty()) {
                state.setHasBay(true);
                state.setBayId(extractedUuids.getBayId());
                state.setBayName(extractedUuids.getBayName());
                log.info("Using bay_id from frontend extracted_uuids: {} (bay_name: {})",
                        extractedUuids.getBayId(), extractedUuids.getBayName());
            }
            if (extractedUuids.getServiceType() != null && !extractedUuids.getServiceType().trim().isEmpty()) {
                state.setHasService(true);
            }
        }

        if (history == null || history.isEmpty()) {
            return state;
        }

        // Pattern để detect các thông tin đã có
        // QUAN TRỌNG: Chỉ detect khi THỰC SỰ đã chọn, không phải chỉ nói về
        // Pattern cải thiện: Chỉ match biển số thực tế (có ít nhất 3 ký tự, có số và
        // chữ)
        // Format: 52S2-27069, 69A-56789, 77AC-33697, etc.
        Pattern vehiclePattern = Pattern.compile(
                "(?:Bạn đã chọn xe|chọn xe|xe với biển số|xe số|biển số)\\s+([A-Z0-9]{2,}[\\-\\s]?[A-Z0-9]{2,})",
                Pattern.CASE_INSENSITIVE);
        Pattern datePattern = Pattern.compile("(?:Bạn đã chọn ngày|đặt lịch vào ngày|ngày)\\s+([0-9/\\-]+|mai|hôm nay)",
                Pattern.CASE_INSENSITIVE);
        // Pattern để extract branch name - CHỈ match khi THỰC SỰ đã chọn, KHÔNG match
        // câu hỏi
        // Pattern 1: "Bạn đã chọn chi nhánh X" (từ assistant response) - CHỈ match "đã
        // chọn"
        // Pattern 2: "Chọn chi nhánh X" (từ user message khi chọn) - CHỈ match "Chọn"
        // (không có "muốn")
        // Pattern 3: "Tôi muốn chọn chi nhánh X" (từ user message) - CHỈ match "muốn
        // chọn" (không phải "muốn chọn nào")
        // KHÔNG match: "Bạn muốn chọn chi nhánh nào?" (câu hỏi - có "muốn" nhưng không
        // có "đã")
        Pattern branchPattern = Pattern.compile(
                "(?:Bạn đã chọn chi nhánh|Bạn đã chọn Chi Nhánh|Chọn chi nhánh|chọn chi nhánh|Tôi muốn chọn chi nhánh|tôi muốn chọn chi nhánh)\\s+([A-Za-zÀ-ỹ0-9\\s\\-]+)",
                Pattern.CASE_INSENSITIVE);
        Pattern servicePattern = Pattern.compile("(?:rửa xe|dịch vụ|service)\\s+([A-Za-zÀ-ỹ\\s]+)",
                Pattern.CASE_INSENSITIVE);
        Pattern bayPattern = Pattern.compile("(?:Bạn đã chọn bay|Bay|bay)\\s+([A-Za-z0-9\\s]+)",
                Pattern.CASE_INSENSITIVE);
        Pattern timePattern = Pattern.compile("(?:Bạn đã chọn giờ|giờ|thời gian)\\s+([0-9]{1,2}:[0-9]{2})",
                Pattern.CASE_INSENSITIVE);

        // Check trong toàn bộ history
        for (ChatRequest.ChatMessage msg : history) {
            String content = msg.getContent();
            if (content == null)
                continue;
            String contentLower = content.toLowerCase();

            // Check vehicle - CHỈ detect khi THỰC SỰ đã chọn xe, không phải chỉ nói về xe
            if (!state.hasVehicle()) {
                boolean hasVehicle = false;

                // Pattern 1: "Bạn đã chọn xe X" (từ assistant response)
                if (contentLower.contains("bạn đã chọn xe") &&
                        !contentLower.contains("lấy") &&
                        !contentLower.contains("danh sách")) {
                    hasVehicle = true;
                }
                // Pattern 2: "xe với biển số X" (từ assistant response)
                else if (contentLower.contains("xe với biển số") &&
                        !contentLower.contains("lấy") &&
                        !contentLower.contains("danh sách")) {
                    hasVehicle = true;
                }
                // Pattern 3: "chọn xe X" với biển số cụ thể (từ user hoặc assistant)
                else if (vehiclePattern.matcher(content).find() &&
                        !contentLower.contains("lấy") &&
                        !contentLower.contains("danh sách") &&
                        !contentLower.contains("tôi sẽ")) {
                    hasVehicle = true;
                }

                if (hasVehicle) {
                    state.setHasVehicle(true);
                    // Extract vehicle license plate từ content
                    Matcher vehicleMatcher = vehiclePattern.matcher(content);
                    if (vehicleMatcher.find()) {
                        String licensePlate = vehicleMatcher.group(1).trim();
                        // Validate: license plate phải có ít nhất 4 ký tự và chứa cả chữ và số
                        if (licensePlate.length() >= 4 &&
                                licensePlate.matches(".*[A-Za-z].*") &&
                                licensePlate.matches(".*[0-9].*")) {
                            state.setVehicleLicensePlate(licensePlate);
                            log.debug("Detected vehicle selection: license_plate={}", licensePlate);
                        } else {
                            log.warn(
                                    "Invalid license plate format detected: '{}' (too short or missing letters/numbers)",
                                    licensePlate);
                        }
                    }
                    log.debug("Detected vehicle selection from: {}",
                            content.substring(0, Math.min(50, content.length())));
                }
            }

            // Check date
            if (!state.hasDate()) {
                if (datePattern.matcher(content).find() ||
                        contentLower.contains("đã chọn ngày") ||
                        contentLower.contains("đặt lịch vào ngày")) {
                    state.setHasDate(true);
                }
            }

            // Check branch - cải thiện detection, CHỈ detect khi THỰC SỰ đã chọn
            if (!state.hasBranch()) {
                boolean hasBranch = false;

                // Pattern 1: "Bạn đã chọn chi nhánh X" (từ assistant response)
                if (contentLower.contains("bạn đã chọn chi nhánh") &&
                        !contentLower.contains("lấy") &&
                        !contentLower.contains("danh sách")) {
                    hasBranch = true;
                }
                // Pattern 2: "Chi nhánh X" với tên cụ thể (khi user chọn, không phải danh sách)
                else if (contentLower.contains("chi nhánh") &&
                        (contentLower.contains("gò vấp") ||
                                contentLower.contains("cầu giấy") ||
                                contentLower.contains("premium") ||
                                contentLower.contains("premia"))
                        &&
                        !contentLower.contains("danh sách") &&
                        !contentLower.contains("bạn có thể chọn")) {
                    hasBranch = true;
                }
                // Pattern 3: "Bạn đã chọn chi nhánh Cầu Giấy" (từ assistant response)
                else if (branchPattern.matcher(content).find() &&
                        !contentLower.contains("lấy") &&
                        !contentLower.contains("danh sách")) {
                    hasBranch = true;
                }

                if (hasBranch) {
                    state.setHasBranch(true);
                    // Extract branch name từ content - ưu tiên pattern matching
                    String extractedBranchName = null;

                    // CRITICAL: Chỉ extract nếu KHÔNG phải câu hỏi
                    // Exclude: "Bạn muốn chọn chi nhánh nào?" (có "muốn" nhưng không có "đã")
                    boolean isQuestion = contentLower.contains("bạn muốn chọn") &&
                            !contentLower.contains("bạn đã chọn") &&
                            (contentLower.contains("nào") || contentLower.contains("gì")
                                    || contentLower.contains("đâu"));

                    if (!isQuestion) {
                        // Thử pattern matching trước
                        Matcher branchMatcher = branchPattern.matcher(content);
                        if (branchMatcher.find()) {
                            extractedBranchName = branchMatcher.group(1).trim();
                            // Validate: không phải là từ "nào", "gì", "đâu" (câu hỏi)
                            if (extractedBranchName != null &&
                                    !extractedBranchName.equalsIgnoreCase("nào") &&
                                    !extractedBranchName.equalsIgnoreCase("gì") &&
                                    !extractedBranchName.equalsIgnoreCase("đâu") &&
                                    extractedBranchName.length() > 2) {
                                state.setBranchName(extractedBranchName);
                                log.debug("Detected branch selection from pattern: branch_name={}",
                                        extractedBranchName);
                            } else {
                                extractedBranchName = null; // Invalid, try fallback
                            }
                        }
                    }

                    // Fallback: extract từ các pattern khác nếu pattern matching fail
                    if (extractedBranchName == null && !isQuestion) {
                        if (content.contains("Gò Vấp") || content.contains("gò vấp") || content.contains("Gò vấp") ||
                                content.contains("go vap") || content.contains("Go Vap")
                                || content.contains("gò vâsp")) {
                            state.setBranchName("Chi Nhánh Gò Vấp");
                            log.debug("Detected branch selection from fallback: branch_name=Chi Nhánh Gò Vấp");
                        } else if (content.contains("Cầu Giấy") || content.contains("cầu giấy")
                                || content.contains("Cầu giấy") ||
                                content.contains("cau giay") || content.contains("Cau Giay")) {
                            state.setBranchName("Chi nhánh Cầu Giấy");
                            log.debug("Detected branch selection from fallback: branch_name=Chi nhánh Cầu Giấy");
                        } else if (content.contains("Premium") || content.contains("premium")) {
                            state.setBranchName("Chi nhánh Premium");
                            log.debug("Detected branch selection from fallback: branch_name=Chi nhánh Premium");
                        } else if (content.contains("Premia") || content.contains("premia")) {
                            state.setBranchName("Chi nhánh Premia 2");
                            log.debug("Detected branch selection from fallback: branch_name=Chi nhánh Premia 2");
                        }
                    }

                    log.debug("Detected branch selection from: {} (isQuestion: {})",
                            content.substring(0, Math.min(50, content.length())), isQuestion);
                }
            }

            // Check service - CHỈ detect khi THỰC SỰ đã chọn dịch vụ
            if (!state.hasService()) {
                boolean hasService = false;

                // Pattern 1: "Bạn đã chọn dịch vụ X" hoặc "Bạn muốn sử dụng dịch vụ X"
                if ((contentLower.contains("bạn đã chọn") || contentLower.contains("bạn muốn sử dụng")) &&
                        contentLower.contains("dịch vụ") &&
                        !contentLower.contains("gì") &&
                        !contentLower.contains("nào")) {
                    hasService = true;
                }
                // Pattern 2: "rửa xe" với tên cụ thể (rửa xe cơ bản, rửa xe nhanh)
                else if (contentLower.contains("rửa xe") &&
                        (contentLower.contains("cơ bản") ||
                                contentLower.contains("nhanh") ||
                                contentLower.contains("đã chọn"))) {
                    hasService = true;
                }
                // Pattern 3: Service pattern với tên cụ thể
                else if (servicePattern.matcher(content).find() &&
                        !contentLower.contains("gì") &&
                        !contentLower.contains("nào")) {
                    hasService = true;
                }

                if (hasService) {
                    state.setHasService(true);
                    log.debug("Detected service selection from: {}",
                            content.substring(0, Math.min(50, content.length())));
                }
            }

            // Check bay - cải thiện detection và extract bay name
            if (!state.hasBay()) {
                boolean hasBay = false;

                // Pattern 1: "Bạn đã chọn bay X" (từ assistant response)
                if (contentLower.contains("bạn đã chọn bay") &&
                        !contentLower.contains("lấy") &&
                        !contentLower.contains("danh sách")) {
                    hasBay = true;
                }
                // Pattern 2: "Bay X" hoặc "Gò Vấp Bay 1" (từ user message khi chọn)
                else if (bayPattern.matcher(content).find() &&
                        !contentLower.contains("lấy") &&
                        !contentLower.contains("danh sách")) {
                    hasBay = true;
                }

                if (hasBay) {
                    state.setHasBay(true);
                    // Extract bay name từ content
                    Matcher bayMatcher = bayPattern.matcher(content);
                    if (bayMatcher.find()) {
                        String bayName = bayMatcher.group(1).trim();
                        state.setBayName(bayName);
                        log.debug("Detected bay selection: bay_name={}", bayName);
                    } else {
                        // Fallback: extract từ các pattern khác
                        if (content.contains("Gò Vấp Bay")) {
                            // Extract "Gò Vấp Bay 1" hoặc "Bay 1"
                            Pattern goVapBayPattern = Pattern.compile("(?:Gò Vấp Bay|Bay)\\s*([0-9]+)",
                                    Pattern.CASE_INSENSITIVE);
                            Matcher goVapMatcher = goVapBayPattern.matcher(content);
                            if (goVapMatcher.find()) {
                                state.setBayName("Gò Vấp Bay " + goVapMatcher.group(1));
                            } else {
                                state.setBayName("Gò Vấp Bay 1"); // Default
                            }
                        } else if (content.contains("Cầu Giấy Bay")) {
                            Pattern cauGiayBayPattern = Pattern.compile("(?:Cầu Giấy Bay|Bay)\\s*([0-9]+)",
                                    Pattern.CASE_INSENSITIVE);
                            Matcher cauGiayMatcher = cauGiayBayPattern.matcher(content);
                            if (cauGiayMatcher.find()) {
                                state.setBayName("Cầu Giấy Bay " + cauGiayMatcher.group(1));
                            } else {
                                state.setBayName("Cầu Giấy Bay 1"); // Default
                            }
                        }
                    }
                    log.debug("Detected bay selection from: {}",
                            content.substring(0, Math.min(50, content.length())));
                }
            }

            // Check time - cải thiện detection
            if (!state.hasTime()) {
                // Pattern 1: "Bạn đã chọn giờ 08:00"
                if (timePattern.matcher(content).find()) {
                    state.setHasTime(true);
                }
                // Pattern 2: Format giờ "08:00", "14:30" trong content
                else if (content.matches(".*\\b\\d{1,2}:\\d{2}\\b.*")) {
                    // Chỉ set nếu không phải là danh sách giờ (nhiều giờ = danh sách, 1 giờ = đã
                    // chọn)
                    String[] timeMatches = content.split("\\b\\d{1,2}:\\d{2}\\b");
                    if (timeMatches.length <= 2) { // Chỉ có 1-2 giờ = có thể đã chọn
                        state.setHasTime(true);
                    }
                }
            }
        }

        // BƯỚC 2: Extract UUID từ database dựa trên license plate và branch name đã tìm
        // được
        // CHỈ extract những UUIDs chưa có từ extracted_uuids (tránh query database
        // không cần thiết)
        // Vẫn extract từ history để lấy các thông tin khác (date, service, time) và các
        // UUIDs chưa có
        extractUuidsFromDatabase(state, history, extractedUuids);

        // Log extracted IDs sau khi extract
        if (state.getVehicleId() != null) {
            log.info("EXTRACTED vehicle_id: {} (license_plate: {})",
                    state.getVehicleId(), state.getVehicleLicensePlate());
        }
        if (state.getBranchId() != null) {
            log.info("EXTRACTED branch_id: {} (branch_name: {})",
                    state.getBranchId(), state.getBranchName());
        }
        if (state.getBayId() != null) {
            log.info("EXTRACTED bay_id: {} (bay_name: {})",
                    state.getBayId(), state.getBayName());
        }

        return state;
    }

    /**
     * Extract UUID từ database dựa trên license plate và branch name đã extract từ
     * conversation history
     * Đây là giải pháp backend-driven: Backend tự động query database để lấy UUID,
     * không phụ thuộc vào AI
     * CHỈ extract những UUIDs chưa có từ extracted_uuids (tránh query database
     * không cần thiết)
     */
    private void extractUuidsFromDatabase(
            ConversationState state,
            List<ChatRequest.ChatMessage> history,
            ChatRequest.ExtractedUuids extractedUuids) {
        // Extract vehicle_id từ license plate - CHỈ nếu chưa có từ extracted_uuids
        if (state.hasVehicle() && state.getVehicleLicensePlate() != null &&
                (extractedUuids == null || extractedUuids.getVehicleId() == null)) {
            try {
                // Lấy ownerId từ SecurityContext
                com.kltn.scsms_api_service.core.dto.token.LoginUserInfo currentUser = com.kltn.scsms_api_service.core.utils.PermissionUtils
                        .getCurrentUser();

                if (currentUser != null && currentUser.getSub() != null) {
                    UUID ownerId = UUID.fromString(currentUser.getSub());

                    // Query database để lấy vehicle_id
                    com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleProfileFilterParam filterParam = new com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleProfileFilterParam();
                    filterParam.setPage(0);
                    filterParam.setSize(1000);
                    filterParam.setOwnerId(ownerId);

                    java.util.Optional<com.kltn.scsms_api_service.core.entity.VehicleProfile> vehicle = vehicleProfileService
                            .getAllVehicleProfilesByOwnerIdWithFilters(ownerId, filterParam)
                            .getContent()
                            .stream()
                            .filter(v -> v.getLicensePlate() != null &&
                                    (v.getLicensePlate().equalsIgnoreCase(state.getVehicleLicensePlate()) ||
                                            v.getLicensePlate().replaceAll("\\s+", "").equalsIgnoreCase(
                                                    state.getVehicleLicensePlate().replaceAll("\\s+", "")))
                                    &&
                                    !v.getIsDeleted() &&
                                    v.getIsActive())
                            .findFirst();

                    if (vehicle.isPresent()) {
                        state.setVehicleId(vehicle.get().getVehicleId().toString());
                        log.info("Backend extracted vehicle_id: {} for license_plate: {} (ownerId: {})",
                                state.getVehicleId(), state.getVehicleLicensePlate(), ownerId);
                    } else {
                        log.warn("Cannot find vehicle_id for license_plate: {} (ownerId: {})",
                                state.getVehicleLicensePlate(), ownerId);
                    }
                }
            } catch (Exception e) {
                log.error("Error extracting vehicle_id from database: {}", e.getMessage(), e);
            }
        }

        // Extract branch_id từ branch name - CHỈ nếu chưa có từ extracted_uuids
        if (state.hasBranch() && state.getBranchName() != null &&
                (extractedUuids == null || extractedUuids.getBranchId() == null)) {
            try {
                // Normalize branch name để query database đúng
                String normalizedBranchName = normalizeBranchName(state.getBranchName());

                // Query database để lấy branch_id từ branch name
                // Thử nhiều cách: exact match, contains, fuzzy match
                com.kltn.scsms_api_service.core.entity.Branch branch = null;

                // Thử 1: Exact match với normalized name
                branch = aiBookingAssistantService.findBranchByNameOrAddress(normalizedBranchName);

                // Thử 2: Nếu không tìm thấy, thử với original name
                if (branch == null && !state.getBranchName().equals(normalizedBranchName)) {
                    branch = aiBookingAssistantService.findBranchByNameOrAddress(state.getBranchName());
                }

                // Thử 3: Nếu vẫn không tìm thấy, thử với các variations
                if (branch == null) {
                    String[] variations = generateBranchNameVariations(state.getBranchName());
                    for (String variation : variations) {
                        branch = aiBookingAssistantService.findBranchByNameOrAddress(variation);
                        if (branch != null) {
                            log.info("Found branch using variation: '{}' -> '{}'", variation, branch.getBranchName());
                            break;
                        }
                    }
                }

                if (branch != null) {
                    state.setBranchId(branch.getBranchId().toString());
                    // Update branch name với tên chính xác từ database
                    state.setBranchName(branch.getBranchName());
                    log.info("Backend extracted branch_id: {} for branch_name: {} (normalized from: {})",
                            state.getBranchId(), branch.getBranchName(), state.getBranchName());
                } else {
                    log.warn("Cannot find branch_id for branch_name: {} (normalized: {})",
                            state.getBranchName(), normalizedBranchName);
                }
            } catch (Exception e) {
                log.error("Error extracting branch_id from database: {}", e.getMessage(), e);
            }
        }

        // Extract bay_id từ bay name và branch_id - CHỈ nếu chưa có từ extracted_uuids
        if (state.hasBay() && state.getBayName() != null && state.getBranchId() != null &&
                (extractedUuids == null || extractedUuids.getBayId() == null)) {
            try {
                UUID branchId = UUID.fromString(state.getBranchId());

                // Tìm bay theo tên trong branch đã chọn
                List<ServiceBay> baysInBranch = serviceBayService
                        .getByBranch(branchId);

                com.kltn.scsms_api_service.core.entity.ServiceBay foundBay = baysInBranch.stream()
                        .filter(bay -> bay.getBayName() != null &&
                                (bay.getBayName().equalsIgnoreCase(state.getBayName()) ||
                                        bay.getBayName().replaceAll("\\s+", "")
                                                .equalsIgnoreCase(state.getBayName().replaceAll("\\s+", "")))
                                &&
                                !bay.getIsDeleted() &&
                                bay.getIsActive())
                        .findFirst()
                        .orElse(null);

                if (foundBay == null) {
                    // Thử search by keyword nếu exact match không tìm thấy
                    List<ServiceBay> bays = serviceBayService
                            .searchByKeywordInBranch(branchId, state.getBayName());
                    if (!bays.isEmpty()) {
                        foundBay = bays.get(0);
                    }
                }

                if (foundBay != null) {
                    state.setBayId(foundBay.getBayId().toString());
                    // Update bay name với tên chính xác từ database
                    state.setBayName(foundBay.getBayName());
                    log.info("Backend extracted bay_id: {} for bay_name: {} (branch_id: {})",
                            state.getBayId(), foundBay.getBayName(), branchId);
                } else {
                    log.warn("Cannot find bay_id for bay_name: {} in branch_id: {}",
                            state.getBayName(), branchId);
                }
            } catch (Exception e) {
                log.error("Error extracting bay_id from database: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Normalize branch name để query database đúng
     * Ví dụ: "gò vâsp" -> "Chi Nhánh Gò Vấp", "Gò Vấp" -> "Chi Nhánh Gò Vấp"
     */
    private String normalizeBranchName(String branchName) {
        if (branchName == null) {
            return null;
        }

        String normalized = branchName.toLowerCase().trim();

        // Normalize các variations của branch names
        if (normalized.contains("gò vấp") || normalized.contains("go vap") || normalized.contains("gò vâsp")) {
            return "Chi Nhánh Gò Vấp";
        } else if (normalized.contains("cầu giấy") || normalized.contains("cau giay")) {
            return "Chi nhánh Cầu Giấy";
        } else if (normalized.contains("premium")) {
            return "Chi nhánh Premium";
        } else if (normalized.contains("premia")) {
            return "Chi nhánh Premia 2";
        }

        // Nếu không match, return original (có thể đã đúng format)
        return branchName;
    }

    /**
     * Generate các variations của branch name để thử query
     */
    private String[] generateBranchNameVariations(String branchName) {
        if (branchName == null) {
            return new String[0];
        }

        List<String> variations = new ArrayList<>();
        String normalized = branchName.toLowerCase().trim();

        // Thêm variations dựa trên branch name
        if (normalized.contains("gò") || normalized.contains("go")) {
            variations.add("Chi Nhánh Gò Vấp");
            variations.add("Gò Vấp");
            variations.add("go vap");
        }
        if (normalized.contains("cầu") || normalized.contains("cau")) {
            variations.add("Chi nhánh Cầu Giấy");
            variations.add("Cầu Giấy");
            variations.add("cau giay");
        }
        if (normalized.contains("premium")) {
            variations.add("Chi nhánh Premium");
            variations.add("Premium");
        }
        if (normalized.contains("premia")) {
            variations.add("Chi nhánh Premia 2");
            variations.add("Premia 2");
        }

        return variations.toArray(new String[0]);
    }

    /**
     * Extract branch_id và branch_name từ conversation history (từ TOOL response
     * getBranches())
     */
    private String[] extractBranchInfoFromHistory(List<ChatRequest.ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return null;
        }

        // Tìm branch name đã được chọn từ user hoặc assistant messages
        String selectedBranchName = null;

        // Tìm branch name đã được chọn - tìm từ cuối lên đầu
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = history.get(i);
            String content = msg.getContent();
            if (content == null)
                continue;

            // Pattern 1: "Bạn đã chọn chi nhánh X" (từ assistant response)
            if (content.contains("Bạn đã chọn chi nhánh") ||
                    content.contains("Bạn đã chọn Chi Nhánh")) {
                // Extract branch name
                if (content.contains("Gò Vấp") || content.contains("gò vấp") || content.contains("Gò vấp")) {
                    selectedBranchName = "Chi Nhánh Gò Vấp";
                } else if (content.contains("Cầu Giấy") || content.contains("cầu giấy")
                        || content.contains("Cầu giấy")) {
                    selectedBranchName = "Chi nhánh Cầu Giấy";
                } else if (content.contains("Premium") || content.contains("premium")) {
                    selectedBranchName = "Chi nhánh Premium";
                } else if (content.contains("Premia") || content.contains("premia")) {
                    selectedBranchName = "Chi nhánh Premia 2";
                }
                if (selectedBranchName != null) {
                    break;
                }
            }

            // Pattern 2: "Chọn chi nhánh X" hoặc "Tôi muốn chọn chi nhánh X" (từ user
            // message)
            if (content.contains("Chọn chi nhánh") || content.contains("chọn chi nhánh") ||
                    content.contains("Tôi muốn chọn chi nhánh") || content.contains("tôi muốn chọn chi nhánh")) {
                if (content.contains("Gò Vấp") || content.contains("gò vấp") || content.contains("Gò vấp") ||
                        content.contains("go vap") || content.contains("Go Vap")) {
                    selectedBranchName = "Chi Nhánh Gò Vấp";
                } else if (content.contains("Cầu Giấy") || content.contains("cầu giấy") || content.contains("Cầu giấy")
                        ||
                        content.contains("cau giay") || content.contains("Cau Giay")) {
                    selectedBranchName = "Chi nhánh Cầu Giấy";
                } else if (content.contains("Premium") || content.contains("premium")) {
                    selectedBranchName = "Chi nhánh Premium";
                } else if (content.contains("Premia") || content.contains("premia")) {
                    selectedBranchName = "Chi nhánh Premia 2";
                }
                if (selectedBranchName != null) {
                    break;
                }
            }
        }

        return selectedBranchName != null ? new String[]{selectedBranchName, null} : null;
    }

    /**
     * Build state context message để inject vào prompt
     */
    /**
     * Lấy customerId từ SecurityContext hoặc request
     */
    private UUID getCustomerId(ChatRequest request) {
        // Ưu tiên từ request
        if (request.getCustomerId() != null) {
            return request.getCustomerId();
        }

        // Lấy từ SecurityContext
        try {
            com.kltn.scsms_api_service.core.dto.token.LoginUserInfo currentUser = com.kltn.scsms_api_service.core.utils.PermissionUtils
                    .getCurrentUser();
            if (currentUser != null && currentUser.getSub() != null) {
                return UUID.fromString(currentUser.getSub());
            }
        } catch (Exception e) {
            log.debug("Could not get customerId from SecurityContext: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Kiểm tra xem user message có phải là yêu cầu bắt đầu đặt lịch mới không
     * Nếu user nói "đặt lịch hẹn", "bắt đầu", "đặt lịch mới" → Cần reset draft
     */
    private boolean isNewBookingRequest(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return false;
        }
        
        String msgLower = userMessage.toLowerCase().trim();
        
        // Các keywords cho yêu cầu bắt đầu đặt lịch mới
        boolean isNewBooking = msgLower.contains("đặt lịch hẹn") ||
                              msgLower.contains("muốn đặt lịch") ||
                              msgLower.contains("cần đặt lịch") ||
                              msgLower.contains("bắt đầu") ||
                              msgLower.contains("đặt lịch mới") ||
                              msgLower.contains("tạo booking mới") ||
                              (msgLower.contains("đặt lịch") && 
                               (msgLower.contains("mới") || msgLower.contains("lại")));
        
        return isNewBooking;
    }

    /**
     * Kiểm tra xem user message có phải là yêu cầu đặt lịch không
     */
    private boolean isBookingRequest(String userMessage, List<ChatRequest.ChatMessage> conversationHistory) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return false;
        }

        String msgLower = userMessage.toLowerCase();

        // Kiểm tra các pattern phổ biến cho yêu cầu đặt lịch
        boolean containsBookingKeywords = msgLower.contains("đặt lịch") ||
                msgLower.contains("muốn đặt") ||
                msgLower.contains("cần đặt") ||
                msgLower.contains("đặt chỗ") ||
                msgLower.contains("book") ||
                msgLower.contains("booking") ||
                msgLower.contains("hẹn") ||
                msgLower.contains("lịch hẹn");

        // Nếu là conversation đầu tiên (không có history), coi như yêu cầu đặt lịch
        if ((conversationHistory == null || conversationHistory.isEmpty()) && containsBookingKeywords) {
            return true;
        }

        // Nếu có keyword đặt lịch → là yêu cầu đặt lịch
        if (containsBookingKeywords) {
            return true;
        }

        return false;
    }

    /**
     * Kiểm tra xem user đã xác nhận booking chưa
     * CRITICAL: Kiểm tra cả userMessage hiện tại và conversationHistory
     *
     * @param userMessage         Message hiện tại từ user (có thể null)
     * @param conversationHistory Lịch sử hội thoại (có thể null hoặc empty)
     * @return true nếu user đã xác nhận, false nếu chưa
     */
    private boolean hasUserConfirmed(String userMessage, List<ChatRequest.ChatMessage> conversationHistory) {
        // CRITICAL: Kiểm tra userMessage hiện tại TRƯỚC (quan trọng nhất)
        if (userMessage != null && !userMessage.trim().isEmpty()) {
            String content = userMessage.toLowerCase().trim();
            // Kiểm tra các pattern xác nhận trong message hiện tại
            if (isConfirmationMessage(content)) {
                log.info("Found user confirmation in current message: {}", userMessage);
                return true;
            }
        }

        // Nếu không tìm thấy trong message hiện tại, kiểm tra conversationHistory (backward compatible)
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return false;
        }

        // Tìm trong conversation history xem có message xác nhận từ user không
        for (int i = conversationHistory.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = conversationHistory.get(i);
            if ("user".equalsIgnoreCase(msg.getRole()) && msg.getContent() != null) {
                String content = msg.getContent().toLowerCase();
                // Kiểm tra các pattern xác nhận
                if (isConfirmationMessage(content)) {
                    // Kiểm tra xem AI đã hỏi xác nhận trước đó chưa
                    for (int j = i - 1; j >= 0 && j >= i - 5; j--) {
                        ChatRequest.ChatMessage prevMsg = conversationHistory.get(j);
                        if ("assistant".equalsIgnoreCase(prevMsg.getRole()) &&
                                prevMsg.getContent() != null) {
                            String prevContent = prevMsg.getContent().toLowerCase();
                            if (prevContent.contains("xác nhận") ||
                                    prevContent.contains("xin hãy xác nhận") ||
                                    prevContent.contains("bạn có muốn")) {
                                log.info("Found user confirmation in conversation history");
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Kiểm tra xem một message có phải là xác nhận không
     *
     * @param content Nội dung message (đã lowercase)
     * @return true nếu là xác nhận, false nếu không
     */
    private boolean isConfirmationMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // Loại bỏ khoảng trắng thừa
        content = content.trim();

        // Kiểm tra các pattern xác nhận
        return content.contains("ok") ||
                content.contains("xác nhận") ||
                content.contains("đúng") ||
                content.contains("chính xác") ||
                content.contains("đồng ý") ||
                content.contains("xác nhận đặt lịch") ||
                content.contains("tiến hành") ||
                content.contains("được") ||
                content.equals("ok") ||
                content.equals("xác nhận") ||
                content.equals("đồng ý") ||
                content.equals("được rồi") ||
                content.equals("được") ||
                content.equals("yes") ||
                content.equals("y");
    }

    /**
     * Kiểm tra xem một message có phải là từ chối không
     *
     * @param content Nội dung message (đã lowercase)
     * @return true nếu là từ chối, false nếu không
     */
    private boolean isRejectionMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        content = content.trim();

        return content.contains("không") ||
                content.contains("sai") ||
                content.contains("không phải") ||
                content.contains("không đúng") ||
                content.equals("no");
    }

    /**
     * Format danh sách xe để hiển thị cho user
     */
    /**
     * Format và làm tròn thời gian (9:01 → 9:00, 9:31 → 9:30)
     * Chỉ giữ lại các slot hợp lệ (phút là 00 hoặc 30)
     */
    private List<String> formatAndRoundTimeSlots(List<String> timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> roundedSlots = new ArrayList<>();
        for (String slot : timeSlots) {
            if (slot == null || slot.trim().isEmpty()) {
                continue;
            }

            try {
                // Parse time slot (format: "HH:mm")
                String[] parts = slot.split(":");
                if (parts.length == 2) {
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);

                    // Làm tròn: 9:01 → 9:00, 9:31 → 9:30
                    // Nếu phút <= 15 → làm tròn xuống 00
                    // Nếu phút > 15 và <= 45 → làm tròn xuống 30
                    // Nếu phút > 45 → làm tròn lên giờ tiếp theo với phút 00
                    int roundedMinute;
                    if (minute <= 15) {
                        roundedMinute = 0;
                    } else if (minute <= 45) {
                        roundedMinute = 30;
                    } else {
                        // Làm tròn lên giờ tiếp theo
                        hour = (hour + 1) % 24;
                        roundedMinute = 0;
                    }

                    String roundedSlot = String.format("%02d:%02d", hour, roundedMinute);
                    if (!roundedSlots.contains(roundedSlot)) {
                        roundedSlots.add(roundedSlot);
                    }
                }
            } catch (Exception e) {
                log.debug("Error rounding time slot: {}, error: {}", slot, e.getMessage());
                // Nếu không parse được, giữ nguyên
                if (!roundedSlots.contains(slot)) {
                    roundedSlots.add(slot);
                }
            }
        }

        // Sort time slots
        roundedSlots.sort(String::compareTo);

        return roundedSlots;
    }

    /**
     * Format danh sách time slots để hiển thị cho AI
     */
    private String formatTimeSlotsList(List<String> timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return "Không có thời gian trống";
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (String slot : timeSlots) {
            sb.append(String.format("%d. %s", index++, slot));
            if (index <= timeSlots.size()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Format danh sách chi nhánh để hiển thị cho AI
     */
    private String formatBranchesList(com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetBranchesResponse response) {
        if (response == null || response.getBranches() == null || response.getBranches().isEmpty()) {
            return "Không có chi nhánh nào";
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetBranchesResponse.BranchInfo branch :
                response.getBranches()) {
            sb.append(String.format("%d. %s - %s\n",
                    index++,
                    branch.getBranchName() != null ? branch.getBranchName() : "N/A",
                    branch.getAddress() != null ? branch.getAddress() : "N/A"));
        }
        return sb.toString();
    }

    private String formatVehiclesList(com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetCustomerVehiclesResponse response) {
        if (response.getVehicles() == null || response.getVehicles().isEmpty()) {
            return "Không có xe nào";
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetCustomerVehiclesResponse.VehicleInfo vehicle : response.getVehicles()) {
            sb.append(String.format("%d. %s", index++, vehicle.getLicensePlate()));
            if (index <= response.getVehicles().size()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Format danh sách bay từ AvailabilityResponse để hiển thị cho AI
     */
    private String formatBaysList(AvailabilityResponse response) {
        if (response == null || response.getAvailableBays() == null || response.getAvailableBays().isEmpty()) {
            return "Không có bay nào";
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (AvailabilityResponse.AvailableBayInfo bay : response.getAvailableBays()) {
            sb.append(String.format("%d. %s", index++, bay.getBayName() != null ? bay.getBayName() : "N/A"));
            if (index <= response.getAvailableBays().size()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Validate draft data trước khi tiếp tục bước tiếp theo
     * Trả về error message nếu có lỗi, null nếu OK
     */
    private String validateDraftBeforeProceeding(BookingDraft draft, int expectedStep) {
        if (draft == null) {
            return "Draft không tồn tại. Vui lòng bắt đầu lại.";
        }

        // Xác định step thực tế dựa trên data đã có (không dùng current_step vì có thể chưa update)
        int actualStep = determineActualStep(draft);

        // CRITICAL: Nếu draft đã complete (step 7), không cần validate nữa
        if (actualStep >= 7 && draft.isComplete()) {
            return null; // Draft đã complete, không có lỗi
        }

        // Kiểm tra step hiện tại có đúng không
        if (actualStep < expectedStep) {
            return String.format("Bạn đang ở bước %d, nhưng cần hoàn thành bước %d trước. Vui lòng hoàn thành các bước trước đó.",
                    actualStep, expectedStep);
        }

        // Kiểm tra dữ liệu theo từng step - dựa trên actualStep, không phải expectedStep
        // QUAN TRỌNG: Validation này chỉ để đảm bảo data đầy đủ, không block việc chọn bay khi đã có đủ data
        switch (actualStep) {
            case 1:
                // STEP 1: Không cần validate gì, đây là bước đầu
                break;
            case 2:
                // STEP 2: Cần có vehicle
                if (!draft.hasVehicle()) {
                    return "Bạn chưa chọn xe. Vui lòng chọn xe trước khi tiếp tục.";
                }
                break;
            case 3:
                // STEP 3: Cần có vehicle và date
                if (!draft.hasVehicle()) {
                    return "Bạn chưa chọn xe. Vui lòng chọn xe trước khi tiếp tục.";
                }
                if (!draft.hasDate()) {
                    return "Bạn chưa chọn ngày. Vui lòng chọn ngày đặt lịch trước khi tiếp tục.";
                }
                break;
            case 4:
                // STEP 4: Cần có vehicle, date, branch
                if (!draft.hasVehicle()) {
                    return "Bạn chưa chọn xe. Vui lòng chọn xe trước khi tiếp tục.";
                }
                if (!draft.hasDate()) {
                    return "Bạn chưa chọn ngày. Vui lòng chọn ngày đặt lịch trước khi tiếp tục.";
                }
                if (!draft.hasBranch()) {
                    return "Bạn chưa chọn chi nhánh. Vui lòng chọn chi nhánh trước khi tiếp tục.";
                }
                break;
            case 5:
                // STEP 5: Cần có vehicle, date, branch, service
                if (!draft.hasVehicle()) {
                    return "Bạn chưa chọn xe. Vui lòng chọn xe trước khi tiếp tục.";
                }
                if (!draft.hasDate()) {
                    return "Bạn chưa chọn ngày. Vui lòng chọn ngày đặt lịch trước khi tiếp tục.";
                }
                if (!draft.hasBranch()) {
                    return "Bạn chưa chọn chi nhánh. Vui lòng chọn chi nhánh trước khi tiếp tục.";
                }
                if (!draft.hasService()) {
                    return "Bạn chưa chọn dịch vụ. Vui lòng chọn dịch vụ trước khi tiếp tục.";
                }
                break;
            case 6:
                // STEP 6: Cần có vehicle, date, branch, service, bay
                if (!draft.hasVehicle()) {
                    return "Bạn chưa chọn xe. Vui lòng chọn xe trước khi tiếp tục.";
                }
                if (!draft.hasDate()) {
                    return "Bạn chưa chọn ngày. Vui lòng chọn ngày đặt lịch trước khi tiếp tục.";
                }
                if (!draft.hasBranch()) {
                    return "Bạn chưa chọn chi nhánh. Vui lòng chọn chi nhánh trước khi tiếp tục.";
                }
                if (!draft.hasService()) {
                    return "Bạn chưa chọn dịch vụ. Vui lòng chọn dịch vụ trước khi tiếp tục.";
                }
                if (!draft.hasBay()) {
                    return "Bạn chưa chọn bay. Vui lòng chọn bay trước khi tiếp tục.";
                }
                break;
            case 7:
                // STEP 7: Cần có đầy đủ tất cả
                if (!draft.hasVehicle()) {
                    return "Bạn chưa chọn xe. Vui lòng chọn xe trước khi tiếp tục.";
                }
                if (!draft.hasDate()) {
                    return "Bạn chưa chọn ngày. Vui lòng chọn ngày đặt lịch trước khi tiếp tục.";
                }
                if (!draft.hasBranch()) {
                    return "Bạn chưa chọn chi nhánh. Vui lòng chọn chi nhánh trước khi tiếp tục.";
                }
                if (!draft.hasService()) {
                    return "Bạn chưa chọn dịch vụ. Vui lòng chọn dịch vụ trước khi tiếp tục.";
                }
                if (!draft.hasBay()) {
                    return "Bạn chưa chọn bay. Vui lòng chọn bay trước khi tiếp tục.";
                }
                if (!draft.hasTime()) {
                    return "Bạn chưa chọn giờ. Vui lòng chọn giờ đặt lịch trước khi tiếp tục.";
                }
                break;
        }

        return null; // OK
    }

    /**
     * Xác định step thực tế dựa trên data đã có trong draft
     * Không dùng current_step vì có thể chưa được update
     */
    private int determineActualStep(BookingDraft draft) {
        if (!draft.hasVehicle()) {
            return 1;
        } else if (!draft.hasDate()) {
            return 2;
        } else if (!draft.hasBranch()) {
            return 3;
        } else if (!draft.hasService()) {
            return 4;
        } else if (!draft.hasBay()) {
            return 5;
        } else if (!draft.hasTime()) {
            return 6;
        } else {
            return 7; // Đầy đủ, sẵn sàng tạo booking
        }
    }
    
    /**
     * Build instruction để AI KHÔNG tự động chọn khi extraction fail
     * Dùng khi cả AI extraction và pattern matching đều không detect được selection
     */
    private String buildNoSelectionInstruction(int actualStep, BookingDraft draft, String userMessage) {
        StringBuilder instruction = new StringBuilder();
        instruction.append("⚠️ CRITICAL - KHÔNG TỰ ĐỘNG CHỌN ⚠️\n");
        instruction.append("Backend đã thử AI extraction và pattern matching nhưng KHÔNG detect được lựa chọn từ user message.\n");
        instruction.append("User message: \"").append(userMessage).append("\"\n\n");
        
        switch (actualStep) {
            case 1:
                instruction.append("BẠN ĐANG Ở STEP 1 (chọn xe).\n");
                instruction.append("User chưa chọn xe rõ ràng (không có biển số, không có số thứ tự).\n");
                instruction.append("TUYỆT ĐỐI KHÔNG được tự động chọn xe đầu tiên.\n");
                instruction.append("PHẢI yêu cầu user chọn lại bằng cách:\n");
                instruction.append("- Hiển thị lại danh sách xe (nếu đã có từ getCustomerVehicles())\n");
                instruction.append("- Hỏi: \"Bạn muốn chọn xe nào? Vui lòng cho tôi biết số thứ tự hoặc biển số.\"\n");
                instruction.append("- CHỜ user chọn rõ ràng trước khi tiếp tục.\n");
                break;
            case 2:
                instruction.append("BẠN ĐANG Ở STEP 2 (chọn ngày).\n");
                instruction.append("User chưa chọn ngày rõ ràng.\n");
                instruction.append("TUYỆT ĐỐI KHÔNG được tự động chọn ngày.\n");
                instruction.append("PHẢI yêu cầu user chọn lại: \"Bạn muốn đặt lịch vào ngày nào? (ví dụ: ngày mai, 15/12, hôm nay)\"\n");
                break;
            case 3:
                instruction.append("BẠN ĐANG Ở STEP 3 (chọn chi nhánh).\n");
                instruction.append("User chưa chọn chi nhánh rõ ràng.\n");
                instruction.append("TUYỆT ĐỐI KHÔNG được tự động chọn chi nhánh đầu tiên.\n");
                instruction.append("PHẢI yêu cầu user chọn lại: \"Bạn muốn chọn chi nhánh nào? Vui lòng cho tôi biết số thứ tự hoặc tên chi nhánh.\"\n");
                break;
            case 4:
                instruction.append("BẠN ĐANG Ở STEP 4 (chọn dịch vụ).\n");
                instruction.append("User chưa chọn dịch vụ rõ ràng.\n");
                instruction.append("TUYỆT ĐỐI KHÔNG được tự động chọn dịch vụ.\n");
                instruction.append("PHẢI yêu cầu user chọn lại: \"Bạn muốn đặt dịch vụ gì? (ví dụ: rửa xe, bảo dưỡng)\"\n");
                break;
            case 5:
                instruction.append("BẠN ĐANG Ở STEP 5 (chọn bay/khu vực).\n");
                instruction.append("User chưa chọn bay rõ ràng.\n");
                instruction.append("TUYỆT ĐỐI KHÔNG được tự động chọn bay đầu tiên.\n");
                instruction.append("PHẢI yêu cầu user chọn lại: \"Bạn muốn chọn bay/khu vực nào? Vui lòng cho tôi biết số thứ tự hoặc tên bay.\"\n");
                break;
            case 6:
                instruction.append("BẠN ĐANG Ở STEP 6 (chọn giờ).\n");
                instruction.append("User chưa chọn giờ rõ ràng.\n");
                instruction.append("TUYỆT ĐỐI KHÔNG được tự động chọn giờ đầu tiên.\n");
                instruction.append("PHẢI yêu cầu user chọn lại: \"Bạn muốn chọn giờ nào? Vui lòng cho tôi biết số thứ tự hoặc giờ (ví dụ: 08:00).\"\n");
                break;
            default:
                return null; // Không cần instruction cho step khác
        }
        
        instruction.append("\nQUAN TRỌNG:\n");
        instruction.append("- KHÔNG được tự động chọn bất kỳ option nào\n");
        instruction.append("- KHÔNG được giả định user muốn chọn option đầu tiên\n");
        instruction.append("- PHẢI yêu cầu user chọn lại một cách rõ ràng\n");
        instruction.append("- CHỜ user trả lời trước khi tiếp tục\n");
        
        return instruction.toString();
    }

    /**
     * Build draft context message để inject vào prompt
     * Message này giúp AI biết state hiện tại từ draft
     */
    private String buildDraftContextMessage(
            com.kltn.scsms_api_service.core.entity.BookingDraft draft,
            ConversationState extractedState) {

        if (draft == null) {
            return null;
        }

        StringBuilder context = new StringBuilder();
        context.append("═══════════════════════════════════════════════════════════════\n");
        context.append("DRAFT STATE (Single Source of Truth)\n");
        context.append("═══════════════════════════════════════════════════════════════\n");
        context.append("draft_id: ").append(draft.getDraftId()).append("\n");
        context.append("current_step: ").append(draft.getCurrentStep()).append("\n");
        context.append("status: ").append(draft.getStatus()).append("\n");
        context.append("\n");
        context.append("Dữ liệu đã có:\n");

        if (draft.hasVehicle()) {
            context.append("[CO] vehicle_id: ").append(draft.getVehicleId()).append("\n");
            context.append("[CO] vehicle_license_plate: ").append(draft.getVehicleLicensePlate()).append("\n");
        } else {
            context.append("[CHUA] vehicle_id: null (CHƯA CÓ)\n");
        }

        if (draft.hasDate()) {
            context.append("[CO] date_time: ").append(draft.getDateTime()).append("\n");
        } else {
            context.append("[CHUA] date_time: null (CHƯA CÓ)\n");
        }

        if (draft.hasBranch()) {
            context.append("[CO] branch_id: ").append(draft.getBranchId()).append("\n");
            context.append("[CO] branch_name: ").append(draft.getBranchName()).append("\n");
        } else {
            context.append("[CHUA] branch_id: null (CHƯA CÓ)\n");
        }

        // Hiển thị tất cả dịch vụ từ bảng quan hệ (ưu tiên)
        List<com.kltn.scsms_api_service.core.entity.DraftService> draftServices =
                bookingDraftService.getDraftServices(draft.getDraftId());

        if (!draftServices.isEmpty()) {
            context.append("[CO] services (").append(draftServices.size()).append("): ");
            List<String> serviceNames = draftServices.stream()
                    .map(com.kltn.scsms_api_service.core.entity.DraftService::getServiceName)
                    .collect(java.util.stream.Collectors.toList());
            context.append(String.join(", ", serviceNames)).append("\n");
        } else if (draft.getServiceType() != null) {
            // Fallback: Nếu không có trong bảng quan hệ, dùng service_type cũ (tương thích ngược)
            context.append("[CO] service_type: ").append(draft.getServiceType()).append("\n");
        } else {
            context.append("[CHUA] service_type: null (CHƯA CÓ)\n");
        }

        if (draft.hasBay()) {
            context.append("[CO] bay_id: ").append(draft.getBayId()).append("\n");
            context.append("[CO] bay_name: ").append(draft.getBayName()).append("\n");
        } else {
            context.append("[CHUA] bay_id: null (CHƯA CÓ)\n");
        }

        if (draft.hasTime()) {
            context.append("[CO] time_slot: ").append(draft.getTimeSlot()).append("\n");
        } else {
            context.append("[CHUA] time_slot: null (CHƯA CÓ)\n");
        }
        
        // CRITICAL: Thêm instruction rõ ràng về step tiếp theo
        context.append("\n");
        context.append("STEP HIỆN TẠI VÀ BƯỚC TIẾP THEO:\n");
        if (!draft.hasVehicle()) {
            context.append("- STEP 1: Chọn xe (CHƯA CÓ)\n");
        } else if (!draft.hasDate()) {
            context.append("- STEP 2: Chọn ngày đặt lịch (CHƯA CÓ)\n");
        } else if (!draft.hasBranch()) {
            context.append("- STEP 3: Chọn chi nhánh (CHƯA CÓ)\n");
        } else if (!draft.hasService()) {
            context.append("- STEP 4: Chọn dịch vụ (CHƯA CÓ)\n");
        } else if (!draft.hasBay()) {
            context.append("- STEP 5: Chọn bay/khu vực (CHƯA CÓ) ← BẠN ĐANG Ở ĐÂY\n");
            context.append("- Sau khi user chọn bay → Chuyển sang STEP 6 (chọn giờ)\n");
        } else if (!draft.hasTime()) {
            context.append("- STEP 6: Chọn giờ đặt lịch (CHƯA CÓ) ← BẠN ĐANG Ở ĐÂY\n");
            context.append("- Bay đã được chọn: ").append(draft.getBayName()).append("\n");
            context.append("- Cần yêu cầu user chọn giờ từ danh sách time slots\n");
            context.append("- TUYỆT ĐỐI KHÔNG được yêu cầu xác nhận booking khi chưa có time_slot\n");
        } else {
            context.append("- STEP 7: Xác nhận và tạo booking (ĐÃ ĐẦY ĐỦ) ← BẠN ĐANG Ở ĐÂY\n");
            context.append("- Tất cả dữ liệu đã đầy đủ, có thể tạo booking\n");
        }

        context.append("\n");
        context.append("QUAN TRỌNG:\n");
        context.append("- Backend sẽ TỰ ĐỘNG update draft khi bạn gọi functions\n");
        context.append("- Bạn KHÔNG cần extract UUIDs từ tool responses\n");
        context.append("- Nếu draft đã có dữ liệu → KHÔNG gọi function lại\n");
        context.append("- Chỉ focus vào conversation với user\n");

        // CRITICAL: Thêm instruction rõ ràng khi draft đã complete
        if (draft.isComplete() || (draft.hasVehicle() && draft.hasDate() && draft.hasBranch() && draft.hasService() && draft.hasBay() && draft.hasTime())) {
            context.append("\n");
            context.append("⚠️ CRITICAL - DRAFT ĐÃ HOÀN THÀNH ⚠️\n");
            context.append("- Draft đã có ĐẦY ĐỦ dữ liệu: vehicle, date, branch, service, bay, time\n");
            context.append("- BẠN ĐANG Ở STEP 7 (hoàn thành), KHÔNG phải STEP 5 (chọn bay) hay STEP 6 (chọn giờ)\n");
            context.append("- TUYỆT ĐỐI KHÔNG được gọi checkAvailability() khi draft đã complete\n");
            context.append("- TUYỆT ĐỐI KHÔNG được yêu cầu user chọn bay hoặc chọn giờ\n");
            context.append("- TUYỆT ĐỐI KHÔNG được nói 'cần hoàn thành bước chọn bay' hoặc 'cần chọn giờ'\n");
            context.append("- Nếu user chưa xác nhận → CHỈ tóm tắt thông tin và hỏi xác nhận để tạo booking\n");
            context.append("- Nếu user đã xác nhận → GỌI createBooking() ngay lập tức\n");
        }

        context.append("═══════════════════════════════════════════════════════════════\n");

        return context.toString();
    }

    /**
     * Parse user message để detect user đã chọn gì và update draft
     * Đây là phần quan trọng nhất: Log chi tiết user message và kết quả phân tích
     */
    private BookingDraft parseUserSelectionAndUpdateDraft(
            BookingDraft draft,
            String userMessage,
            List<ChatRequest.ChatMessage> conversationHistory) {

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return draft;
        }

        boolean hasUpdate = false;
        BookingDraftService.DraftUpdate update = BookingDraftService.DraftUpdate.builder().build();
        String updateType = null;

        log.info("═══════════════════════════════════════════════════════════════");
        log.info("PARSING USER MESSAGE FOR SELECTIONS");
        log.info("USER MESSAGE: {}", userMessage);
        log.info("Current Draft: draft_id={}, current_step={}", draft.getDraftId(), draft.getCurrentStep());

        String userMsgLower = userMessage.toLowerCase();

        // ========== PARSE VEHICLE SELECTION ==========
        // Cho phép update vehicle nếu user muốn đổi (không chỉ check !draft.hasVehicle())
        if (!draft.hasVehicle() || userMsgLower.contains("chọn") || userMsgLower.contains("đổi")) {
            String selectedVehicle = extractVehicleSelectionFromMessage(userMessage, conversationHistory);
            if (selectedVehicle != null) {
                log.info("DETECTED VEHICLE SELECTION: {}", selectedVehicle);

                // Tìm vehicle_id từ conversation history (tool response)
                UUID vehicleId = findVehicleIdFromToolResponse(selectedVehicle, conversationHistory);

                // Nếu không tìm thấy trong history, query database
                if (vehicleId == null) {
                    vehicleId = findVehicleIdFromDatabase(selectedVehicle);
                }

                if (vehicleId != null) {
                    update.setVehicleId(vehicleId);
                    update.setVehicleLicensePlate(selectedVehicle);
                    updateType = "VEHICLE";
                    hasUpdate = true;
                    log.info("EXTRACTED vehicle_id: {} from license_plate: {}", vehicleId, selectedVehicle);
                } else {
                    // Chỉ có license plate, không có UUID (fallback - sẽ tìm sau khi có thêm context)
                    update.setVehicleLicensePlate(selectedVehicle);
                    updateType = "VEHICLE";
                    hasUpdate = true;
                    log.warn("Could not find vehicle_id, only license_plate: {}", selectedVehicle);
                }
            }
        }

        // ========== PARSE DATE SELECTION ==========
        // Cho phép update date nếu user muốn đổi (không chỉ check !draft.hasDate())
        // Detect "đổi ngày" hoặc có pattern ngày tháng
        boolean wantsToChangeDate = userMsgLower.contains("đổi ngày") || 
                                    userMsgLower.contains("thay đổi ngày") ||
                                    userMsgLower.contains("đổi ngày đặt lịch") ||
                                    userMsgLower.contains("thay đổi ngày đặt lịch");
        
        // CHỈ extract date nếu KHÔNG phải là "đổi giờ" (tránh nhầm lẫn)
        boolean isTimeChange = userMsgLower.contains("đổi giờ") || 
                               userMsgLower.contains("thay đổi giờ") ||
                               userMsgLower.contains("đổi giờ đặt lịch");
        
        if ((!draft.hasDate() || userMsgLower.contains("chọn") || userMsgLower.contains("đổi") || wantsToChangeDate) &&
            !isTimeChange &&
            (userMsgLower.contains("ngày") || userMsgLower.contains("mai") ||
             userMsgLower.contains("hôm nay") || userMessage.matches(".*\\d{1,2}/\\d{1,2}.*"))) {
            java.time.LocalDateTime dateTime = extractDateTimeFromMessage(userMessage);
            if (dateTime != null) {
                update.setDateTime(dateTime);
                updateType = "DATE";
                hasUpdate = true;
                log.info("DETECTED DATE SELECTION: {}", dateTime);
            }
        }

        // ========== PARSE BRANCH SELECTION ==========
        // Cho phép update branch nếu user muốn đổi (không chỉ check !draft.hasBranch())
        // Nhưng phải loại trừ câu hỏi như "Bạn có những chi nhánh nào?"
        boolean isQuestion = userMsgLower.contains("bạn có") || userMsgLower.contains("những chi nhánh nào") ||
                userMsgLower.contains("chi nhánh nào") || userMsgLower.contains("có những");

        if ((!draft.hasBranch() || userMsgLower.contains("chọn") || userMsgLower.contains("đổi")) &&
                (userMsgLower.contains("chi nhánh") || userMsgLower.contains("chọn chi nhánh")) &&
                !isQuestion) {
            String selectedBranch = extractBranchSelectionFromMessage(userMessage);
            if (selectedBranch != null) {
                log.info("DETECTED BRANCH SELECTION: {}", selectedBranch);

                // Tìm branch_id từ conversation history hoặc ThreadLocal
                UUID branchId = findBranchIdFromToolResponse(selectedBranch, conversationHistory);

                // Nếu không tìm thấy trong tool response, query database
                if (branchId == null) {
                    branchId = findBranchIdFromDatabase(selectedBranch);
                }

                if (branchId != null) {
                    update.setBranchId(branchId);
                    // Lưu tên chi nhánh đầy đủ từ database nếu có
                    com.kltn.scsms_api_service.core.entity.Branch branch = aiBookingAssistantService.findBranchByNameOrAddress(selectedBranch);
                    if (branch != null) {
                        update.setBranchName(branch.getBranchName());
                    } else {
                        update.setBranchName(selectedBranch);
                    }
                    updateType = "BRANCH";
                    hasUpdate = true;
                    log.info("EXTRACTED branch_id: {} from branch_name: {}", branchId, selectedBranch);
                } else {
                    // Chỉ có branch name, vẫn lưu để có thể tìm sau
                    update.setBranchName(selectedBranch);
                    updateType = "BRANCH";
                    hasUpdate = true;
                    log.warn("Could not find branch_id, only branch_name: {}. Will try to find later.", selectedBranch);
                }
            }
        }

        // ========== PARSE SERVICE SELECTION ==========
        // Cho phép user đổi dịch vụ (không chỉ check !draft.hasService())
        // Nếu user nói về dịch vụ mới → Update lại service_id và service_name
        String selectedService = extractServiceSelectionFromMessage(userMessage, conversationHistory, draft);
        if (selectedService != null) {
            log.info("DETECTED SERVICE SELECTION: {}", selectedService);

            // QUAN TRỌNG: Tìm service_id và service_name từ ThreadLocal (getServices response)
            // PHẢI validate với tool response trước khi lưu vào draft
            GetServicesResponse.ServiceInfo serviceInfo =
                    findServiceInfoFromToolResponse(selectedService, conversationHistory);

            if (serviceInfo != null) {
                // Tìm thấy trong tool response → Validate thành công → Lưu vào draft
                update.setServiceId(serviceInfo.getServiceId());
                update.setServiceType(serviceInfo.getServiceName());
                updateType = "SERVICE";
                hasUpdate = true;
                log.info("EXTRACTED service_id: {}, service_name: {} from user selection: {} (VALIDATED from tool response)",
                        serviceInfo.getServiceId(), serviceInfo.getServiceName(), selectedService);
            } else {
                // KHÔNG tìm thấy trong tool response → Query database để tìm serviceId từ serviceName
                log.info("Service not found in tool response, querying database for serviceName: {}", selectedService);
                com.kltn.scsms_api_service.core.entity.Service serviceFromDb = 
                        findServiceFromDatabase(selectedService);
                
                if (serviceFromDb != null) {
                    // Tìm thấy trong database → Validate thành công → Lưu vào draft
                    update.setServiceId(serviceFromDb.getServiceId());
                    update.setServiceType(serviceFromDb.getServiceName());
                    updateType = "SERVICE";
                    hasUpdate = true;
                    log.info("EXTRACTED service_id: {}, service_name: {} from user selection: {} (VALIDATED from database)",
                            serviceFromDb.getServiceId(), serviceFromDb.getServiceName(), selectedService);
                } else {
                    // KHÔNG tìm thấy trong database → KHÔNG lưu vào draft
                // Yêu cầu AI gọi getServices() trước để validate
                    log.warn("Could not find service in database for selection: {}. " +
                            "AI should call getServices() first to validate service exists.", selectedService);
                // KHÔNG update draft - để AI gọi getServices() trước
                }
            }
        }

        // ========== PARSE BAY SELECTION ==========
        // Cho phép update bay nếu user muốn đổi (không chỉ check !draft.hasBay())
        if ((!draft.hasBay() || userMsgLower.contains("chọn") || userMsgLower.contains("đổi")) &&
            (userMsgLower.contains("bay") || 
             userMsgLower.contains("khu vực") || 
             userMsgLower.contains("chọn bay") ||
             userMsgLower.contains("chọn khu vực"))) {
            String selectedBay = extractBaySelectionFromMessage(userMessage, conversationHistory);
            if (selectedBay != null) {
                log.info("DETECTED BAY SELECTION: {}", selectedBay);

                // Tìm bay_id từ conversation history hoặc ThreadLocal
                UUID bayId = findBayIdFromToolResponse(selectedBay, conversationHistory);

                // Nếu không tìm thấy trong tool response, query database
                if (bayId == null) {
                    bayId = findBayIdFromDatabase(selectedBay, draft.getBranchId());
                }

                if (bayId != null) {
                    update.setBayId(bayId);
                    // Lưu tên bay đầy đủ từ database nếu có
                    try {
                        com.kltn.scsms_api_service.core.entity.ServiceBay bay = serviceBayService.getById(bayId);
                        if (bay != null && bay.getBayName() != null) {
                            update.setBayName(bay.getBayName());
                        } else {
                            update.setBayName(selectedBay);
                        }
                    } catch (Exception e) {
                        // Nếu không tìm thấy, dùng tên từ user message
                        update.setBayName(selectedBay);
                        log.debug("Could not get bay name from database for bay_id: {}, using: {}", bayId, selectedBay);
                    }
                    updateType = "BAY";
                    hasUpdate = true;
                    log.info("EXTRACTED bay_id: {} from bay_name: {}", bayId, selectedBay);
                } else {
                    // Chỉ có bay name, vẫn lưu để có thể tìm sau
                    update.setBayName(selectedBay);
                    updateType = "BAY";
                    hasUpdate = true;
                    log.warn("Could not find bay_id, only bay_name: {}. Will try to find later.", selectedBay);
                }
            }
        }

        // ========== PARSE TIME SELECTION ==========
        // Cho phép update time nếu user muốn đổi (không chỉ check !draft.hasTime())
        // Detect "đổi giờ" hoặc có pattern HH:mm
        boolean wantsToChangeTime = userMsgLower.contains("đổi giờ") || 
                                   userMsgLower.contains("thay đổi giờ") ||
                                   userMsgLower.contains("đổi giờ đặt lịch") ||
                                   userMsgLower.contains("thay đổi giờ đặt lịch");
        
        if ((!draft.hasTime() || userMsgLower.contains("chọn") || userMsgLower.contains("đổi") || wantsToChangeTime) &&
            (userMessage.matches(".*\\b\\d{1,2}:\\d{2}\\b.*") || wantsToChangeTime)) {
            // Nếu user nói "đổi giờ" nhưng chưa có giờ cụ thể → Không update, để AI hỏi
            if (wantsToChangeTime && !userMessage.matches(".*\\b\\d{1,2}:\\d{2}\\b.*")) {
                log.info("User wants to change time but no specific time provided. Will let AI ask for time selection.");
                // Không update, để AI hỏi user chọn giờ cụ thể
            } else {
                String timeSlot = extractTimeSlotFromMessage(userMessage);
                if (timeSlot != null) {
                    update.setTimeSlot(timeSlot);
                    updateType = "TIME";
                    hasUpdate = true;
                    log.info("DETECTED TIME SELECTION: {}", timeSlot);
                }
            }
        }

        // ========== UPDATE DRAFT ==========
        if (hasUpdate) {
            log.info("UPDATING DRAFT: draft_id={}, update_type={}", draft.getDraftId(), updateType);
            draft = bookingDraftService.updateDraft(draft.getDraftId(), updateType, userMessage, update);
            log.info("Draft updated successfully");

            // QUAN TRỌNG: Sau khi chọn bay thành công, tự động lấy danh sách time slots
            if ("BAY".equals(updateType) && draft.hasBay() && !draft.hasTime()) {
                log.info("Bay selected successfully, preparing to load time slots for AI");
                // Time slots sẽ được inject vào messages sau khi draft update
            }
        } else {
            log.info("No selection detected in user message");
        }

        log.info("═══════════════════════════════════════════════════════════════");
        return draft;
    }

    /**
     * Quyết định có nên dùng AI extraction không
     * 
     * CHIẾN LƯỢC: Luôn ưu tiên AI extraction trước (trừ một số trường hợp đặc biệt)
     * AI extraction có thể hiểu context tốt hơn và extract chính xác hơn
     * Pattern matching chỉ là fallback khi AI extraction không thành công
     * 
     * Sử dụng AI extraction khi:
     * 1. User message không rỗng
     * 2. Không phải là câu hỏi hoặc yêu cầu thông tin (có thể skip AI extraction)
     * 3. Có vẻ là lựa chọn hoặc confirmation
     */
    private boolean shouldUseAIExtraction(BookingDraft draft, String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return false;
        }
        
        String userMsgLower = userMessage.toLowerCase().trim();
        
        // Skip AI extraction cho các câu hỏi hoặc yêu cầu thông tin rõ ràng
        boolean isQuestion = userMsgLower.startsWith("là gì") ||
                            userMsgLower.startsWith("là ai") ||
                            userMsgLower.startsWith("ở đâu") ||
                            userMsgLower.startsWith("khi nào") ||
                            userMsgLower.startsWith("tại sao") ||
                            userMsgLower.startsWith("như thế nào") ||
                            userMsgLower.contains("giải thích") ||
                            userMsgLower.contains("hướng dẫn");
        
        if (isQuestion) {
            log.debug("Detected question/information request, skipping AI extraction");
            return false;
        }
        
        // Luôn thử AI extraction cho:
        // 1. Confirmation keywords (ưu tiên cao)
        boolean hasConfirmationKeywords = userMsgLower.equals("có") ||
                                         userMsgLower.equals("đúng") ||
                                         userMsgLower.equals("ok") ||
                                         userMsgLower.equals("xác nhận") ||
                                         userMsgLower.equals("đồng ý") ||
                                         userMsgLower.equals("được") ||
                                         userMsgLower.equals("yes") ||
                                         userMsgLower.equals("y");
        
        if (hasConfirmationKeywords) {
            log.debug("Detected confirmation message, will try AI extraction");
            return true;
        }
        
        // 2. Selection keywords
        boolean hasSelectionKeywords = userMsgLower.contains("chọn") || 
                                      userMsgLower.contains("muốn") ||
                                      userMsgLower.contains("xe") ||
                                      userMsgLower.contains("chi nhánh") ||
                                      userMsgLower.contains("dịch vụ") ||
                                      userMsgLower.contains("bay") ||
                                      userMsgLower.contains("khu vực") ||
                                      userMsgLower.contains("giờ");
        
        // 3. Patterns có vẻ là lựa chọn (biển số xe, ngày tháng, giờ)
        boolean hasSelectionPatterns = userMsgLower.matches(".*\\d{1,2}:\\d{2}.*") ||  // Time: "08:00"
                                      userMsgLower.matches(".*\\d{1,2}/\\d{1,2}.*") ||  // Date: "07/12"
                                      userMsgLower.matches(".*\\d{2,4}-\\d{4,5}.*") ||  // License plate: "69D1-27069"
                                      userMsgLower.matches(".*[A-Z]{2,4}\\d{1,4}-\\d{4,5}.*"); // License plate: "69D1-27069"
        
        // 4. Chỉ là số hoặc text ngắn (có thể là lựa chọn theo index hoặc tên)
        boolean isShortSelection = userMessage.trim().length() <= 50 && 
                                   (userMsgLower.matches("^\\d+$") || // Chỉ số: "1", "2"
                                    userMsgLower.matches("^[a-zà-ỹ\\s]{1,30}$")); // Text ngắn: "Khu vực 1"
        
        // Luôn thử AI extraction nếu có bất kỳ dấu hiệu nào của lựa chọn
        boolean shouldUse = hasConfirmationKeywords || hasSelectionKeywords || hasSelectionPatterns || isShortSelection;
        
        if (shouldUse) {
            log.debug("Will try AI extraction - hasConfirmationKeywords={}, hasSelectionKeywords={}, hasSelectionPatterns={}, isShortSelection={}",
                    hasConfirmationKeywords, hasSelectionKeywords, hasSelectionPatterns, isShortSelection);
        } else {
            log.debug("Skipping AI extraction - message does not appear to be a selection");
        }
        
        return shouldUse;
    }

    /**
     * Thử dùng AI extraction để extract lựa chọn từ user message
     */
    private BookingDraft tryAIExtraction(
            BookingDraft draft,
            String userMessage,
            List<ChatRequest.ChatMessage> conversationHistory) {
        
        log.info("=== TRYING AI EXTRACTION ===");
        
        try {
            // Build ExtractSelectionRequest
            ExtractSelectionRequest.DraftContext draftContext = 
                    ExtractSelectionRequest.DraftContext.builder()
                            .draftId(draft.getDraftId())
                            .hasVehicle(draft.hasVehicle())
                            .hasDate(draft.hasDate())
                            .hasBranch(draft.hasBranch())
                            .hasService(draft.hasService())
                            .hasBay(draft.hasBay())
                            .hasTime(draft.hasTime())
                            .currentStep(draft.getCurrentStep())
                            .build();
            
            // Build available options từ ThreadLocal hoặc conversation history (fallback)
            ExtractSelectionRequest.AvailableOptions availableOptions = 
                    extractionService.buildAvailableOptions(conversationHistory);
            
            ExtractSelectionRequest extractRequest = ExtractSelectionRequest.builder()
                    .userMessage(userMessage)
                    .draftContext(draftContext)
                    .availableOptions(availableOptions)
                    .currentStep(draft.getCurrentStep())
                    .build();
            
            // Call extraction service
            ExtractSelectionResponse response = extractionService.extractUserSelection(extractRequest);
            
            // Xử lý confirmation riêng - không cần available options
            String userMsgLower = userMessage.toLowerCase().trim();
            boolean isConfirmation = userMsgLower.equals("có") ||
                                    userMsgLower.equals("đúng") ||
                                    userMsgLower.equals("ok") ||
                                    userMsgLower.equals("xác nhận") ||
                                    userMsgLower.equals("đồng ý") ||
                                    userMsgLower.equals("được") ||
                                    userMsgLower.equals("yes") ||
                                    userMsgLower.equals("y");
            
            if (isConfirmation && response.getExtractedData() != null) {
                ExtractSelectionResponse.ExtractedData data = response.getExtractedData();
                // Check intent từ AI response (nếu có)
                // Nếu AI trả về intent="CONFIRM" hoặc tất cả fields null → Xử lý confirmation
                if (data.getVehicle() == null && data.getDate() == null && 
                    data.getBranch() == null && data.getService() == null && 
                    data.getBay() == null && data.getTime() == null) {
                    // AI không extract được gì → Có thể là confirmation
                    log.info("Detected confirmation message, checking draft state");
                    
                    // Nếu draft đã có service và current_step=5 → User xác nhận service, chuyển sang bay selection
                    if (draft.hasService() && draft.getCurrentStep() == 5) {
                        log.info("User confirmed service selection. Service already saved in draft. No update needed.");
                        return draft; // Service đã có, không cần update
                    }
                    // Nếu draft đã có bay và current_step=6 → User xác nhận bay, chuyển sang time selection
                    if (draft.hasBay() && draft.getCurrentStep() == 6) {
                        log.info("User confirmed bay selection. Bay already saved in draft. No update needed.");
                        return draft; // Bay đã có, không cần update
                    }
                }
            }
            
            // Nếu extraction thành công và confidence >= 0.8
            if (response.getStatus() != null && 
                "SUCCESS".equals(response.getStatus()) &&
                response.getConfidence() != null &&
                response.getConfidence() >= 0.8 &&
                response.getExtractedData() != null) {
                
                log.info("AI extraction successful with confidence: {}", response.getConfidence());
                
                // Update draft với extracted data
                BookingDraftService.DraftUpdate update = 
                        BookingDraftService.DraftUpdate.builder().build();
                String updateType = null;
                boolean hasUpdate = false;
                
                ExtractSelectionResponse.ExtractedData data = response.getExtractedData();
                
                // Update vehicle
                // CRITICAL: KHÔNG trust ID từ AI - chỉ validate name/licensePlate với database
                if (data.getVehicle() != null) {
                    String selectionType = data.getVehicle().getSelectionType();
                    GetCustomerVehiclesResponse vehiclesResponse = DraftContextHolder.getVehiclesResponse();
                    UUID matchedVehicleId = null;
                    String matchedLicensePlate = null;
                    
                    // Case 1: INDEX selection (user chọn bằng số thứ tự, ví dụ: "xe thứ 1")
                    if ("INDEX".equals(selectionType) && data.getVehicle().getRawText() != null) {
                        log.info("Processing INDEX selection for vehicle - raw_text: '{}'", data.getVehicle().getRawText());
                        int index = parseIndexFromText(data.getVehicle().getRawText());
                        log.info("Parsed index from '{}': {}", data.getVehicle().getRawText(), index);
                        
                        if (index > 0) {
                            // Bước 1: Tìm trong tool response (ThreadLocal)
                            if (vehiclesResponse != null && vehiclesResponse.getVehicles() != null) {
                                log.info("Checking ThreadLocal vehicles - total: {}, looking for index: {}", 
                                        vehiclesResponse.getVehicles().size(), index);
                                if (index > 0 && index <= vehiclesResponse.getVehicles().size()) {
                                    GetCustomerVehiclesResponse.VehicleInfo vehicleInfoByIndex =
                                        vehiclesResponse.getVehicles().get(index - 1);
                                    matchedVehicleId = vehicleInfoByIndex.getVehicleId();
                                    matchedLicensePlate = vehicleInfoByIndex.getLicensePlate();
                                    log.info("Found vehicle by index {} from ThreadLocal: {} ({})", 
                                            index, matchedLicensePlate, matchedVehicleId);
                                } else {
                                    log.warn("Index {} is out of range (total vehicles: {})", 
                                            index, vehiclesResponse.getVehicles().size());
                                }
                            } else {
                                log.warn("No vehicles in ThreadLocal (vehiclesResponse is null or empty)");
                            }
                            
                            // Bước 2: Nếu không tìm thấy trong ThreadLocal, parse từ conversation history
                            if (matchedVehicleId == null && conversationHistory != null) {
                                log.info("Vehicle not found in ThreadLocal, trying conversation history");
                                GetCustomerVehiclesResponse parsedResponse = parseVehiclesFromHistory(conversationHistory);
                                if (parsedResponse != null && parsedResponse.getVehicles() != null) {
                                    log.info("Found vehicles in conversation history - total: {}, looking for index: {}", 
                                            parsedResponse.getVehicles().size(), index);
                                    if (index > 0 && index <= parsedResponse.getVehicles().size()) {
                                        GetCustomerVehiclesResponse.VehicleInfo vehicleInfoByIndex =
                                            parsedResponse.getVehicles().get(index - 1);
                                        matchedVehicleId = vehicleInfoByIndex.getVehicleId();
                                        matchedLicensePlate = vehicleInfoByIndex.getLicensePlate();
                                        log.info("Found vehicle by index {} from conversation history: {} ({})", 
                                                index, matchedLicensePlate, matchedVehicleId);
                                    } else {
                                        log.warn("Index {} is out of range in conversation history (total vehicles: {})", 
                                                index, parsedResponse.getVehicles().size());
                                    }
                                } else {
                                    log.warn("No vehicles found in conversation history");
                                }
                            }
                        } else {
                            log.warn("Failed to parse index from raw_text: '{}'", data.getVehicle().getRawText());
                        }
                    }
                    // Case 2: NAME selection (user chọn bằng license plate, ví dụ: "69D1-27069")
                    else if (data.getVehicle().getLicensePlate() != null) {
                        // Tìm vehicle từ tool response hoặc database bằng licensePlate
                        
                        // Bước 1: Tìm trong tool response (ThreadLocal)
                        if (vehiclesResponse != null && vehiclesResponse.getVehicles() != null) {
                            for (GetCustomerVehiclesResponse.VehicleInfo v : vehiclesResponse.getVehicles()) {
                                if (v.getLicensePlate() != null && 
                                    v.getLicensePlate().equalsIgnoreCase(data.getVehicle().getLicensePlate())) {
                                    matchedVehicleId = v.getVehicleId();
                                    matchedLicensePlate = v.getLicensePlate();
                                    break;
                                }
                            }
                        }
                        
                        // Bước 2: Nếu không tìm thấy trong ThreadLocal, parse từ conversation history
                        if (matchedVehicleId == null && conversationHistory != null) {
                            GetCustomerVehiclesResponse parsedResponse = parseVehiclesFromHistory(conversationHistory);
                            if (parsedResponse != null && parsedResponse.getVehicles() != null) {
                                for (GetCustomerVehiclesResponse.VehicleInfo v : parsedResponse.getVehicles()) {
                                    if (v.getLicensePlate() != null && 
                                        v.getLicensePlate().equalsIgnoreCase(data.getVehicle().getLicensePlate())) {
                                        matchedVehicleId = v.getVehicleId();
                                        matchedLicensePlate = v.getLicensePlate();
                                        log.info("Found vehicle_id from conversation history: {} for license_plate: {}", 
                                                matchedVehicleId, matchedLicensePlate);
                                        break;
                                    }
                                }
                            }
                        }
                        
                        // Bước 3: Nếu không tìm thấy trong tool response, query database
                        if (matchedVehicleId == null) {
                            try {
                                // Tìm vehicle từ tool response của getCustomerVehicles (nếu có customerId)
                                // Hoặc query trực tiếp từ database bằng licensePlate
                                // Note: VehicleProfileService không có search method, cần dùng cách khác
                                // Tạm thời skip database query cho vehicle vì cần customerId
                                // Vehicle sẽ được validate từ tool response là đủ
                                log.debug("Vehicle not found in tool response or history. Skipping database query (requires customerId).");
                            } catch (Exception e) {
                                log.warn("Error querying database for vehicle with licensePlate '{}': {}", 
                                        data.getVehicle().getLicensePlate(), e.getMessage());
                            }
                        }
                    }
                    
                    if (matchedVehicleId != null) {
                        update.setVehicleId(matchedVehicleId);
                        update.setVehicleLicensePlate(matchedLicensePlate);
                        updateType = "VEHICLE";
                        hasUpdate = true;
                        log.info("AI extracted vehicle (selection_type: {}, raw_text: {}), validated and found vehicleId: {}, licensePlate: {}", 
                                selectionType, data.getVehicle().getRawText(), matchedVehicleId, matchedLicensePlate);
                    } else {
                        log.warn("AI extracted vehicle (selection_type: {}, raw_text: {}, license_plate: {}) but not found in tool response or database", 
                                selectionType, data.getVehicle().getRawText(), data.getVehicle().getLicensePlate());
                    }
                }
                
                // Update date
                if (data.getDate() != null && data.getDate().getDateTime() != null) {
                    update.setDateTime(data.getDate().getDateTime());
                    updateType = "DATE";
                    hasUpdate = true;
                    log.info("AI extracted date: {}", data.getDate().getDateTime());
                }
                
                // Update branch
                // CRITICAL: KHÔNG trust ID từ AI - chỉ validate name với database
                if (data.getBranch() != null) {
                    String selectionType = data.getBranch().getSelectionType();
                    GetBranchesResponse branchesResponse = DraftContextHolder.getBranchesResponse();
                    UUID matchedBranchId = null;
                    String matchedBranchName = null;
                    
                    // Case 1: INDEX selection (user chọn bằng số thứ tự, ví dụ: "chi nhánh thứ 2")
                    if ("INDEX".equals(selectionType) && data.getBranch().getRawText() != null) {
                        log.info("Processing INDEX selection for branch - raw_text: '{}'", data.getBranch().getRawText());
                        int index = parseIndexFromText(data.getBranch().getRawText());
                        log.info("Parsed index from '{}': {}", data.getBranch().getRawText(), index);
                        
                        if (index > 0) {
                            // Bước 1: Tìm trong tool response (ThreadLocal)
                            if (branchesResponse != null && branchesResponse.getBranches() != null) {
                                log.info("Checking ThreadLocal branches - total: {}, looking for index: {}", 
                                        branchesResponse.getBranches().size(), index);
                                if (index > 0 && index <= branchesResponse.getBranches().size()) {
                                    GetBranchesResponse.BranchInfo branchInfoByIndex =
                                        branchesResponse.getBranches().get(index - 1);
                                    matchedBranchId = branchInfoByIndex.getBranchId();
                                    matchedBranchName = branchInfoByIndex.getBranchName();
                                    log.info("Found branch by index {} from ThreadLocal: {} ({})", 
                                            index, matchedBranchName, matchedBranchId);
                                } else {
                                    log.warn("Index {} is out of range (total branches: {})", 
                                            index, branchesResponse.getBranches().size());
                                }
                            } else {
                                log.warn("No branches in ThreadLocal (branchesResponse is null or empty)");
                            }
                            
                            // Bước 2: Nếu không tìm thấy trong ThreadLocal, parse từ conversation history
                            if (matchedBranchId == null && conversationHistory != null) {
                                log.info("Branch not found in ThreadLocal, trying conversation history");
                                GetBranchesResponse parsedResponse = parseBranchesFromHistory(conversationHistory);
                                if (parsedResponse != null && parsedResponse.getBranches() != null) {
                                    log.info("Found branches in conversation history - total: {}, looking for index: {}", 
                                            parsedResponse.getBranches().size(), index);
                                    if (index > 0 && index <= parsedResponse.getBranches().size()) {
                                        GetBranchesResponse.BranchInfo branchInfoByIndex =
                                            parsedResponse.getBranches().get(index - 1);
                                        matchedBranchId = branchInfoByIndex.getBranchId();
                                        matchedBranchName = branchInfoByIndex.getBranchName();
                                        log.info("Found branch by index {} from conversation history: {} ({})", 
                                                index, matchedBranchName, matchedBranchId);
                                    } else {
                                        log.warn("Index {} is out of range in conversation history (total branches: {})", 
                                                index, parsedResponse.getBranches().size());
                                    }
                                } else {
                                    log.warn("No branches found in conversation history");
                                }
                            }
                        } else {
                            log.warn("Failed to parse index from raw_text: '{}'", data.getBranch().getRawText());
                        }
                    }
                    // Case 2: NAME selection (user chọn bằng tên chi nhánh, ví dụ: "Chi nhánh Phú Nhuận")
                    else if (data.getBranch().getBranchName() != null) {
                        // Tìm branch từ tool response hoặc database bằng branchName
                        
                        // Bước 1: Tìm trong tool response (ThreadLocal)
                        if (branchesResponse != null && branchesResponse.getBranches() != null) {
                            String searchName = data.getBranch().getBranchName().toLowerCase();
                            for (GetBranchesResponse.BranchInfo b : branchesResponse.getBranches()) {
                                if (b.getBranchName() != null) {
                                    String branchNameLower = b.getBranchName().toLowerCase();
                                    if (branchNameLower.equals(searchName) || 
                                        branchNameLower.contains(searchName) || 
                                        searchName.contains(branchNameLower)) {
                                        matchedBranchId = b.getBranchId();
                                        matchedBranchName = b.getBranchName();
                                        break;
                                    }
                                }
                            }
                        }
                        
                        // Bước 2: Nếu không tìm thấy trong ThreadLocal, parse từ conversation history
                        if (matchedBranchId == null && conversationHistory != null) {
                            GetBranchesResponse parsedResponse = parseBranchesFromHistory(conversationHistory);
                            if (parsedResponse != null && parsedResponse.getBranches() != null) {
                                String searchName = data.getBranch().getBranchName().toLowerCase();
                                for (GetBranchesResponse.BranchInfo b : parsedResponse.getBranches()) {
                                    if (b.getBranchName() != null) {
                                        String branchNameLower = b.getBranchName().toLowerCase();
                                        if (branchNameLower.equals(searchName) || 
                                            branchNameLower.contains(searchName) || 
                                            searchName.contains(branchNameLower)) {
                                            matchedBranchId = b.getBranchId();
                                            matchedBranchName = b.getBranchName();
                                            log.info("Found branch_id from conversation history: {} for branch_name: {}", 
                                                    matchedBranchId, matchedBranchName);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Bước 3: Nếu không tìm thấy trong tool response, query database
                        if (matchedBranchId == null) {
                            try {
                                Branch branch = aiBookingAssistantService.findBranchByNameOrAddress(data.getBranch().getBranchName());
                                if (branch != null) {
                                    matchedBranchId = branch.getBranchId();
                                    matchedBranchName = branch.getBranchName();
                                }
                            } catch (Exception e) {
                                log.warn("Error querying database for branch with name '{}': {}", 
                                        data.getBranch().getBranchName(), e.getMessage());
                            }
                        }
                    }
                    
                    if (matchedBranchId != null) {
                        update.setBranchId(matchedBranchId);
                        update.setBranchName(matchedBranchName);
                        updateType = "BRANCH";
                        hasUpdate = true;
                        log.info("AI extracted branch (selection_type: {}, raw_text: {}), validated and found branchId: {}, branchName: {}", 
                                selectionType, data.getBranch().getRawText() != null ? data.getBranch().getRawText() : data.getBranch().getBranchName(), 
                                matchedBranchId, matchedBranchName);
                    } else {
                        log.warn("AI extracted branch (selection_type: {}, raw_text: {}, branch_name: {}) but not found in tool response or database", 
                                selectionType, data.getBranch().getRawText(), data.getBranch().getBranchName());
                    }
                }
                
                // Update service
                // PHÂN BIỆT selection type:
                // - NAME/INDEX selection → Lưu ngay (user đã chọn cụ thể)
                // - KEYWORD selection → Tùy số lượng services (1 service → hỏi xác nhận, nhiều → yêu cầu chọn cụ thể)
                // - CONFIRM intent → Lấy từ conversation history
                if (data.getService() != null && data.getService().getServiceName() != null) {
                    String selectionType = data.getService().getSelectionType();
                    boolean shouldSaveImmediately = false;
                    String reason = "";
                    
                    // Case 1: NAME selection (user chọn cụ thể bằng tên) → Lưu ngay
                    if ("NAME".equals(selectionType)) {
                        shouldSaveImmediately = true;
                        reason = "NAME selection (user chose specific service name)";
                    }
                    // Case 2: INDEX selection (user chọn cụ thể bằng số) → Lưu ngay
                    else if ("INDEX".equals(selectionType)) {
                        shouldSaveImmediately = true;
                        reason = "INDEX selection (user chose specific service by index)";
                    }
                    // Case 3: KEYWORD selection (user nói keyword chung) → Tùy số lượng services
                    else if ("KEYWORD".equals(selectionType)) {
                        // Check số lượng services từ tool response
                        GetServicesResponse servicesResponse = DraftContextHolder.getServicesResponse();
                        if (servicesResponse == null) {
                            servicesResponse = parseServicesFromHistory(conversationHistory);
                        }
                        
                        if (servicesResponse != null && servicesResponse.getServices() != null) {
                            int serviceCount = servicesResponse.getServices().size();
                            if (serviceCount == 1) {
                                // Chỉ có 1 service → Hỏi xác nhận (không lưu ngay)
                                shouldSaveImmediately = false;
                                reason = "KEYWORD selection with 1 service (need confirmation)";
                            } else if (serviceCount > 1) {
                                // Nhiều services → Yêu cầu chọn cụ thể (không lưu ngay)
                                shouldSaveImmediately = false;
                                reason = "KEYWORD selection with multiple services (need specific selection)";
                            } else {
                                // Không có service → Không lưu
                                shouldSaveImmediately = false;
                                reason = "KEYWORD selection with no services found";
                            }
                        } else {
                            // Không có tool response → Không lưu, yêu cầu AI gọi getServices()
                            shouldSaveImmediately = false;
                            reason = "KEYWORD selection but no tool response (AI should call getServices() first)";
                        }
                    }
                    // Case 4: CONFIRM intent (user nói "đúng", "có", "ok") → Lấy từ conversation history
                    else if (isConfirmation) {
                        shouldSaveImmediately = true;
                        reason = "CONFIRM intent (user confirmed, will get service from conversation history)";
                        
                        // Lấy service từ conversation history
                        GetServicesResponse servicesResponse = parseServicesFromHistory(conversationHistory);
                        if (servicesResponse == null) {
                            servicesResponse = DraftContextHolder.getServicesResponse();
                        }
                        
                        if (servicesResponse != null && servicesResponse.getServices() != null && 
                            !servicesResponse.getServices().isEmpty()) {
                            // Lấy service đầu tiên từ tool response (service được đề xuất)
                            String serviceNameFromHistory = servicesResponse.getServices().get(0).getServiceName();
                            log.info("User confirmed. Getting service from conversation history: {}", serviceNameFromHistory);
                            // Override service name với service từ conversation history
                            data.getService().setServiceName(serviceNameFromHistory);
                        }
                    }
                    // Case 5: Service đã có trong draft → Coi như thành công
                    else if (draft.hasService()) {
                        shouldSaveImmediately = true;
                        reason = "Service already in draft (user already confirmed)";
                    }
                    
                    // Quyết định có lưu hay không
                    if (shouldSaveImmediately) {
                        log.info("AI extracted service name '{}' with selection_type='{}'. Reason: {}. Will save to draft.",
                                data.getService().getServiceName(), selectionType, reason);
                        
                        // Luôn validate serviceName với database, không dùng serviceId từ AI
                        GetServicesResponse.ServiceInfo serviceInfo = 
                                findServiceInfoFromToolResponse(data.getService().getServiceName(), conversationHistory);
                        
                        UUID matchedServiceId = null;
                        String matchedServiceName = null;
                        
                        // Bước 1: Tìm trong tool response
                        if (serviceInfo != null && serviceInfo.getServiceId() != null) {
                            matchedServiceId = serviceInfo.getServiceId();
                            matchedServiceName = serviceInfo.getServiceName();
                            log.info("AI extracted service name '{}', found serviceId from tool response: {}", 
                                    data.getService().getServiceName(), matchedServiceId);
                        } else {
                            // Bước 2: Xử lý INDEX selection - lấy service theo index từ tool response
                            if ("INDEX".equals(selectionType) && data.getService().getRawText() != null) {
                                int index = parseIndexFromText(data.getService().getRawText());
                                if (index > 0) {
                                    GetServicesResponse parsedResponse = parseServicesFromHistory(conversationHistory);
                                    if (parsedResponse == null) {
                                        parsedResponse = DraftContextHolder.getServicesResponse();
                                    }
                                    if (parsedResponse != null && parsedResponse.getServices() != null &&
                                        index > 0 && index <= parsedResponse.getServices().size()) {
                                        GetServicesResponse.ServiceInfo serviceInfoByIndex =
                                            parsedResponse.getServices().get(index - 1);
                                        matchedServiceId = serviceInfoByIndex.getServiceId();
                                        matchedServiceName = serviceInfoByIndex.getServiceName();
                                        log.info("Found service by index {}: {} ({})", index, matchedServiceName, matchedServiceId);
                                    }
                                }
                            }
                            
                            // Bước 3: Nếu không tìm thấy trong tool response, query database
                            if (matchedServiceId == null) {
                                log.info("Service not found in tool response, querying database for serviceName: {}", 
                                        data.getService().getServiceName());
                                com.kltn.scsms_api_service.core.entity.Service serviceFromDb = 
                                        findServiceFromDatabase(data.getService().getServiceName());
                                
                                if (serviceFromDb != null) {
                                    matchedServiceId = serviceFromDb.getServiceId();
                                    matchedServiceName = serviceFromDb.getServiceName();
                                    log.info("AI extracted service name '{}', found serviceId from database: {}", 
                                            data.getService().getServiceName(), matchedServiceId);
                                }
                            }
                        }
                        
                        if (matchedServiceId != null) {
                            // Check nếu service đã có trong draft
                            boolean serviceAlreadyInDraft = draft.getServiceId() != null && 
                                                            draft.getServiceId().equals(matchedServiceId);
                            
                            if (serviceAlreadyInDraft) {
                                // Service đã có trong draft → Coi như thành công
                                log.info("AI extracted service '{}' which is already in draft. User confirmed selection.",
                                        data.getService().getServiceName());
                                updateType = "SERVICE";
                                hasUpdate = true;
                            } else {
                                // Service chưa có → Update draft
                                update.setServiceId(matchedServiceId);
                                update.setServiceType(matchedServiceName);
                                updateType = "SERVICE";
                                hasUpdate = true;
                                log.info("AI extracted service name '{}', validated and saved with serviceId: {} (reason: {})",
                                        data.getService().getServiceName(), matchedServiceId, reason);
                            }
                        } else {
                            // Không tìm thấy trong database → Log warning, không lưu
                            log.warn("AI extracted service name '{}' but service not found in tool response or database. " +
                                    "Will not save to draft. AI should call getServices() first to validate.", 
                                    data.getService().getServiceName());
                        }
                    } else {
                        // User chưa chọn cụ thể hoặc cần xác nhận → KHÔNG lưu service vào draft
                        // AI sẽ hỏi xác nhận hoặc yêu cầu chọn cụ thể
                        log.info("AI extracted service name '{}' with selection_type='{}'. Reason: {}. Will NOT save to draft. " +
                                "AI should ask for confirmation or specific selection.",
                                data.getService().getServiceName(), selectionType, reason);
                    }
                }
                
                // Update bay
                // CRITICAL: KHÔNG trust ID từ AI - chỉ validate name với database
                if (data.getBay() != null && data.getBay().getBayName() != null) {
                    // Tìm bay từ tool response hoặc database bằng bayName
                    AvailabilityResponse availabilityResponse = DraftContextHolder.getAvailabilityResponse();
                    UUID matchedBayId = null;
                    String matchedBayName = null;
                    
                    // Bước 1: Tìm trong tool response
                    if (availabilityResponse != null && availabilityResponse.getAvailableBays() != null) {
                        String searchName = data.getBay().getBayName().toLowerCase();
                        for (AvailabilityResponse.AvailableBayInfo b : availabilityResponse.getAvailableBays()) {
                            if (b.getBayName() != null) {
                                String bayNameLower = b.getBayName().toLowerCase();
                                if (bayNameLower.equals(searchName) || 
                                    bayNameLower.contains(searchName) || 
                                    searchName.contains(bayNameLower)) {
                                    matchedBayId = b.getBayId();
                                    matchedBayName = b.getBayName();
                                    break;
                                }
                            }
                        }
                    }
                    
                    // Bước 2: Xử lý INDEX selection ("đầu tiên", "thứ hai", etc.)
                    if (matchedBayId == null && "INDEX".equals(data.getBay().getSelectionType())) {
                        int index = parseIndexFromText(data.getBay().getRawText());
                        if (index > 0) {
                            // Tìm bay theo index trong available options
                            AvailabilityResponse parsedResponse = parseAvailabilityFromHistory(conversationHistory);
                            if (parsedResponse == null) {
                                parsedResponse = DraftContextHolder.getAvailabilityResponse();
                            }
                            
                            if (parsedResponse != null && parsedResponse.getAvailableBays() != null &&
                                index > 0 && index <= parsedResponse.getAvailableBays().size()) {
                                AvailabilityResponse.AvailableBayInfo bay = 
                                    parsedResponse.getAvailableBays().get(index - 1);
                                matchedBayId = bay.getBayId();
                                matchedBayName = bay.getBayName();
                                log.info("Found bay by index {}: {} ({})", index, matchedBayName, matchedBayId);
                            }
                        }
                    }
                    
                    // Bước 3: Nếu không tìm thấy trong tool response, tìm từ conversation history
                    if (matchedBayId == null) {
                        matchedBayId = findBayIdFromToolResponse(data.getBay().getBayName(), conversationHistory);
                        if (matchedBayId != null) {
                            // Lấy bay name từ conversation history
                            AvailabilityResponse parsedResponse = parseAvailabilityFromHistory(conversationHistory);
                            if (parsedResponse != null && parsedResponse.getAvailableBays() != null) {
                                for (AvailabilityResponse.AvailableBayInfo b : parsedResponse.getAvailableBays()) {
                                    if (b.getBayId().equals(matchedBayId)) {
                                        matchedBayName = b.getBayName();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bước 4: Nếu vẫn không tìm thấy, tìm từ database (fallback cuối cùng)
                    if (matchedBayId == null && draft.getBranchId() != null) {
                        UUID bayIdFromDb = findBayIdFromDatabase(data.getBay().getBayName(), draft.getBranchId());
                        if (bayIdFromDb != null) {
                            matchedBayId = bayIdFromDb;
                            // Lấy bay name từ database
                            try {
                                ServiceBay bay = serviceBayService.getById(bayIdFromDb);
                                if (bay != null && bay.getBayName() != null) {
                                    matchedBayName = bay.getBayName();
                                } else {
                                    matchedBayName = data.getBay().getBayName();
                                }
                            } catch (Exception e) {
                                log.debug("Could not get bay name from database for bay_id: {}, using extracted name: {}", 
                                        bayIdFromDb, data.getBay().getBayName());
                                matchedBayName = data.getBay().getBayName();
                            }
                            log.info("Found bay_id from database (fallback): {} for bay_name: {}", 
                                    bayIdFromDb, data.getBay().getBayName());
                        }
                    }
                    
                    // Lưu bay ngay cả khi chưa có bay_id (chỉ cần bay_name với confidence cao)
                    if (matchedBayId != null) {
                        update.setBayId(matchedBayId);
                        update.setBayName(matchedBayName != null ? matchedBayName : data.getBay().getBayName());
                        updateType = "BAY";
                        hasUpdate = true;
                        log.info("AI extracted bay name '{}', validated and found bayId: {}", 
                                data.getBay().getBayName(), matchedBayId);
                    } else {
                        // Vẫn lưu bay_name để có thể tìm bay_id sau
                        // Nếu confidence >= 0.8 → Lưu bay_name ngay
                        double bayConfidence = data.getBay().getConfidence() != null ? data.getBay().getConfidence() : 0.0;
                        if (bayConfidence >= 0.8) {
                            update.setBayName(data.getBay().getBayName());
                            updateType = "BAY";
                            hasUpdate = true;
                            log.info("AI extracted bay name '{}' with high confidence ({}), saved bay_name without bay_id. Will try to find bay_id later.", 
                                    data.getBay().getBayName(), bayConfidence);
                            
                            // Thử tìm bay_id từ database nếu có branch_id (đã được tìm ở Bước 4 ở trên, nhưng thử lại để chắc chắn)
                            if (draft.getBranchId() != null) {
                                UUID bayIdFromDb = findBayIdFromDatabase(data.getBay().getBayName(), draft.getBranchId());
                                if (bayIdFromDb != null) {
                                    update.setBayId(bayIdFromDb);
                                    log.info("Found bay_id from database (retry): {} for bay_name: {}", bayIdFromDb, data.getBay().getBayName());
                                } else {
                                    log.warn("Could not find bay_id from database for bay_name: '{}' in branch_id: {}. Bay will be saved with bay_name only.", 
                                            data.getBay().getBayName(), draft.getBranchId());
                                }
                            } else {
                                log.warn("Cannot find bay_id from database because branch_id is null. Bay will be saved with bay_name only: {}", 
                                        data.getBay().getBayName());
                            }
                        } else {
                            log.warn("AI extracted bay name '{}' but confidence too low ({}), not saving", 
                                    data.getBay().getBayName(), bayConfidence);
                        }
                    }
                }
                
                // Update time
                if (data.getTime() != null && data.getTime().getTimeSlot() != null) {
                    update.setTimeSlot(data.getTime().getTimeSlot());
                    updateType = "TIME";
                    hasUpdate = true;
                    log.info("AI extracted time: {}", data.getTime().getTimeSlot());
                }
                
                // Update draft nếu có thay đổi
                if (hasUpdate) {
                    draft = bookingDraftService.updateDraft(
                            draft.getDraftId(), 
                            updateType + "_AI", 
                            userMessage, 
                            update);
                    log.info("Draft updated successfully from AI extraction");
                }
            } else {
                log.info("AI extraction did not succeed or confidence too low. Status: {}, Confidence: {}", 
                        response.getStatus(), response.getConfidence());
            }
            
        } catch (Exception e) {
            log.error("Error during AI extraction: {}", e.getMessage(), e);
            // Fallback: Continue with current draft
        }
        
        return draft;
    }

    // ========== HELPER METHODS FOR PARSING ==========

    /**
     * Extract vehicle selection từ user message
     * Ưu tiên tìm từ tool response (ThreadLocal), sau đó mới dùng regex pattern
     * Pattern: "chọn xe 52S2-27069" hoặc "52S2-27069" hoặc "xe số 2"
     */
    private String extractVehicleSelectionFromMessage(String userMessage, List<ChatRequest.ChatMessage> history) {
        // Pattern 1: "xe số X", "chọn số X", "xe thứ X", "chọn xe thứ X", "tôi muốn chọn xe thứ X" - tìm trong tool response
        Pattern numberPattern = Pattern.compile("(?:xe\\s*(?:số|thứ)|chọn\\s*(?:xe\\s*)?(?:số|thứ)|số|thứ)\\s*(\\d+)",
                Pattern.CASE_INSENSITIVE);
        Matcher numberMatcher = numberPattern.matcher(userMessage);
        if (numberMatcher.find()) {
            String numberStr = numberMatcher.group(1);
            // Tìm vehicle theo số thứ tự trong tool response
            GetCustomerVehiclesResponse vehiclesResponse =
                    DraftContextHolder.getVehiclesResponse();

            if (vehiclesResponse != null && vehiclesResponse.getVehicles() != null) {
                try {
                    int index = Integer.parseInt(numberStr) - 1; // Convert to 0-based index
                    if (index >= 0 && index < vehiclesResponse.getVehicles().size()) {
                        String licensePlate = vehiclesResponse.getVehicles().get(index).getLicensePlate();
                        log.info("Found vehicle by number {}: {}", numberStr, licensePlate);
                        return licensePlate;
                    }
                } catch (NumberFormatException e) {
                    log.debug("Invalid vehicle number: {}", numberStr);
                }
            }
            // Fallback: tìm trong conversation history
            try {
                int index = Integer.parseInt(numberStr) - 1;
                return findVehicleByIndex(index, history);
            } catch (NumberFormatException e) {
                log.debug("Invalid vehicle number: {}", numberStr);
            }
        }

        // Pattern 2: Tìm exact match trong tool response (nếu user nói biển số cụ thể)
        GetCustomerVehiclesResponse vehiclesResponse =
                DraftContextHolder.getVehiclesResponse();

        if (vehiclesResponse != null && vehiclesResponse.getVehicles() != null) {
            for (GetCustomerVehiclesResponse.VehicleInfo vehicle :
                    vehiclesResponse.getVehicles()) {
                if (vehicle.getLicensePlate() != null) {
                    String lp = vehicle.getLicensePlate();
                    String lpNormalized = lp.replaceAll("\\s+", "").toUpperCase();
                    String msgNormalized = userMessage.replaceAll("\\s+", "").toUpperCase();

                    // Match biển số trong message
                    if (userMessage.contains(lp) ||
                            msgNormalized.contains(lpNormalized) ||
                            lpNormalized.contains(msgNormalized)) {
                        log.info("Found vehicle by license plate match: {}", lp);
                        return lp;
                    }
                }
            }
        }

        // Pattern 3: "chọn xe X" hoặc "xe X" - extract biển số
        Pattern pattern = Pattern.compile(
                "(?:chọn xe|xe)\\s+([A-Z0-9]{2,}[\\-\\s]?[A-Z0-9]{2,})",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(userMessage);
        if (matcher.find()) {
            String licensePlate = matcher.group(1).trim();
            // Validate: phải có ít nhất 4 ký tự và chứa cả chữ và số
            if (licensePlate.length() >= 4 &&
                    licensePlate.matches(".*[A-Za-z].*") &&
                    licensePlate.matches(".*[0-9].*")) {
                return licensePlate;
            }
        }

        // Pattern 4: Chỉ có biển số (52S2-27069) - standalone pattern
        Pattern licensePattern = Pattern.compile("\\b([A-Z0-9]{2,}[\\-\\s]?[A-Z0-9]{2,})\\b");
        matcher = licensePattern.matcher(userMessage);
        if (matcher.find()) {
            String licensePlate = matcher.group(1).trim();
            // Validate: phải có ít nhất 4 ký tự và chứa cả chữ và số
            if (licensePlate.length() >= 4 &&
                    licensePlate.matches(".*[A-Za-z].*") &&
                    licensePlate.matches(".*[0-9].*")) {
                return licensePlate;
            }
        }

        return null;
    }

    /**
     * Tìm vehicle theo index trong tool response
     */
    private String findVehicleByIndex(int index, List<ChatRequest.ChatMessage> history) {
        if (history == null || index < 0)
            return null;

        for (int i = history.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = history.get(i);
            if ("tool".equalsIgnoreCase(msg.getRole()) &&
                    "getCustomerVehicles".equalsIgnoreCase(msg.getToolName()) &&
                    msg.getToolResponse() != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    String jsonStr = mapper.writeValueAsString(msg.getToolResponse());
                    com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(jsonStr);

                    if (root.has("vehicles") && root.get("vehicles").isArray()) {
                        com.fasterxml.jackson.databind.JsonNode vehicles = root.get("vehicles");
                        if (index < vehicles.size()) {
                            com.fasterxml.jackson.databind.JsonNode vehicle = vehicles.get(index);
                            if (vehicle.has("license_plate")) {
                                return vehicle.get("license_plate").asText();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("Error parsing vehicle by index: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Tìm vehicle_id từ tool response dựa trên license plate
     * Ưu tiên lấy từ ThreadLocal (function response vừa được gọi), fallback sang conversation history
     */
    private UUID findVehicleIdFromToolResponse(String licensePlate, List<ChatRequest.ChatMessage> history) {
        if (licensePlate == null)
            return null;

        String normalizedLicensePlate = licensePlate.replaceAll("\\s+", "").toUpperCase();

        // BƯỚC 1: Ưu tiên lấy từ ThreadLocal (function response vừa được gọi trong request này)
        GetCustomerVehiclesResponse vehiclesResponse =
                DraftContextHolder.getVehiclesResponse();

        if (vehiclesResponse != null && vehiclesResponse.getVehicles() != null) {
            for (GetCustomerVehiclesResponse.VehicleInfo vehicle :
                    vehiclesResponse.getVehicles()) {
                if (vehicle.getLicensePlate() != null) {
                    String lp = vehicle.getLicensePlate().replaceAll("\\s+", "").toUpperCase();
                    if (lp.equals(normalizedLicensePlate)) {
                        log.info("Found vehicle_id from ThreadLocal (function response): {} for license_plate: {}",
                                vehicle.getVehicleId(), licensePlate);
                        return vehicle.getVehicleId();
                    }
                }
            }
        }

        // BƯỚC 2: Fallback - tìm trong conversation history (nếu frontend gửi tool responses)
        if (history != null) {
            for (int i = history.size() - 1; i >= 0; i--) {
                ChatRequest.ChatMessage msg = history.get(i);
                if ("tool".equalsIgnoreCase(msg.getRole()) &&
                        "getCustomerVehicles".equalsIgnoreCase(msg.getToolName()) &&
                        msg.getToolResponse() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonStr = mapper.writeValueAsString(msg.getToolResponse());
                        JsonNode root = mapper.readTree(jsonStr);

                        if (root.has("vehicles") && root.get("vehicles").isArray()) {
                            for (JsonNode vehicle : root.get("vehicles")) {
                                if (vehicle.has("license_plate")) {
                                    String lp = vehicle.get("license_plate").asText().replaceAll("\\s+", "").toUpperCase();
                                    if (lp.equals(normalizedLicensePlate)) {
                                        if (vehicle.has("vehicle_id")) {
                                            String vehicleIdStr = vehicle.get("vehicle_id").asText();
                                            log.info("Found vehicle_id from conversation history: {} for license_plate: {}",
                                                    vehicleIdStr, licensePlate);
                                            return UUID.fromString(vehicleIdStr);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error parsing vehicle from conversation history: {}", e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    /**
     * Tìm vehicle_id từ database dựa trên license plate
     * Fallback khi không tìm thấy trong conversation history
     */
    private UUID findVehicleIdFromDatabase(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return null;
        }

        try {
            // Lấy ownerId từ SecurityContext
            LoginUserInfo currentUser =
                    PermissionUtils.getCurrentUser();

            if (currentUser == null || currentUser.getSub() == null) {
                log.debug("Cannot find vehicle by license plate '{}': No authenticated user", licensePlate);
                return null;
            }

            UUID ownerId = UUID.fromString(currentUser.getSub());
            String normalizedLicensePlate = licensePlate.replaceAll("\\s+", "").toUpperCase();

            // Query database để lấy vehicle_id
            VehicleProfileFilterParam filterParam =
                    new VehicleProfileFilterParam();
            filterParam.setPage(0);
            filterParam.setSize(1000);
            filterParam.setOwnerId(ownerId);

            java.util.Optional<VehicleProfile> vehicle =
                    vehicleProfileService.getAllVehicleProfilesByOwnerIdWithFilters(ownerId, filterParam)
                            .getContent()
                            .stream()
                            .filter(v -> v.getLicensePlate() != null &&
                                    (v.getLicensePlate().equalsIgnoreCase(licensePlate) ||
                                            v.getLicensePlate().replaceAll("\\s+", "").equalsIgnoreCase(normalizedLicensePlate)) &&
                                    !v.getIsDeleted() &&
                                    v.getIsActive() &&
                                    v.getOwnerId().equals(ownerId))
                            .findFirst();

            if (vehicle.isPresent()) {
                UUID foundVehicleId = vehicle.get().getVehicleId();
                log.info("Found vehicle_id from database: {} for license_plate: {} (ownerId: {})",
                        foundVehicleId, licensePlate, ownerId);
                return foundVehicleId;
            } else {
                log.warn("Vehicle not found in database for license_plate: {} (ownerId: {})",
                        licensePlate, ownerId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error finding vehicle_id from database for license_plate '{}': {}",
                    licensePlate, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract date/time từ user message
     */
    private java.time.LocalDateTime extractDateTimeFromMessage(String userMessage) {
        String msgLower = userMessage.toLowerCase();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // "mai" hoặc "ngày mai"
        if (msgLower.contains("mai") || msgLower.contains("ngày mai")) {
            return now.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        }

        // "hôm nay"
        if (msgLower.contains("hôm nay")) {
            return now.withHour(8).withMinute(0).withSecond(0).withNano(0);
        }

        // Pattern: "2/12" hoặc "02/12"
        Pattern datePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})");
        Matcher matcher = datePattern.matcher(userMessage);
        if (matcher.find()) {
            try {
                int day = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int year = now.getYear();
                return java.time.LocalDateTime.of(year, month, day, 8, 0);
            } catch (Exception e) {
                log.debug("Error parsing date: {}", e.getMessage());
            }
        }

        return null;
    }

    /**
     * Extract branch selection từ user message
     * Ưu tiên tìm từ tool response (ThreadLocal), không hard code tên chi nhánh
     */
    private String extractBranchSelectionFromMessage(String userMessage) {
        String msgLower = userMessage.toLowerCase();

        // Pattern 1: "chọn chi nhánh số X", "chi nhánh thứ X", "tôi chọn chi nhánh thứ X" - tìm trong tool response
        Pattern numberPattern = Pattern.compile("(?:chọn\\s*)?(?:chi\\s*nhánh\\s*)?(?:số|thứ)\\s*(\\d+)",
                Pattern.CASE_INSENSITIVE);
        Matcher numberMatcher = numberPattern.matcher(userMessage);
        if (numberMatcher.find()) {
            String numberStr = numberMatcher.group(1);
            // Tìm branch theo số thứ tự trong tool response
            GetBranchesResponse branchesResponse =
                    DraftContextHolder.getBranchesResponse();

            if (branchesResponse != null && branchesResponse.getBranches() != null) {
                try {
                    int index = Integer.parseInt(numberStr) - 1; // Convert to 0-based index
                    if (index >= 0 && index < branchesResponse.getBranches().size()) {
                        String branchName = branchesResponse.getBranches().get(index).getBranchName();
                        log.info("Found branch by number {}: {}", numberStr, branchName);
                        return branchName;
                    }
                } catch (NumberFormatException e) {
                    log.debug("Invalid branch number: {}", numberStr);
                }
            }
            // Nếu không tìm thấy trong tool response, return null để tìm trong findBranchIdFromToolResponse
            return null;
        }

        // Pattern 2: Tìm exact match trong tool response (nếu user nói tên chi nhánh cụ thể)
        GetBranchesResponse branchesResponse =
                DraftContextHolder.getBranchesResponse();

        if (branchesResponse != null && branchesResponse.getBranches() != null) {
            for (GetBranchesResponse.BranchInfo branch :
                    branchesResponse.getBranches()) {
                if (branch.getBranchName() != null) {
                    String branchNameLower = branch.getBranchName().toLowerCase();
                    // Match tên chi nhánh hoặc keywords trong tên
                    // Cải thiện matching: "Chi Nhánh Gò Vấp" sẽ match với "Gò Vấp" hoặc "Chi Nhánh Gò Vấp"
                    if (userMessage.contains(branch.getBranchName()) ||
                            branchNameLower.contains(msgLower) ||
                            msgLower.contains(branchNameLower) ||
                            // Match keywords: "gò vấp", "cầu giấy", "premium", etc.
                            (msgLower.contains("gò vấp") && branchNameLower.contains("gò vấp")) ||
                            (msgLower.contains("cầu giấy") && branchNameLower.contains("cầu giấy")) ||
                            (msgLower.contains("premium") && branchNameLower.contains("premium")) ||
                            (msgLower.contains("premia") && branchNameLower.contains("premia"))) {
                        log.info("Found branch by name match: {} (from user message: {})", branch.getBranchName(), userMessage);
                        return branch.getBranchName();
                    }
                }
            }
        }

        // Pattern 3: Extract từ user message nếu có format "chi nhánh X" hoặc chỉ "X"
        // FIX: Chỉ extract tên branch, không extract toàn bộ câu
        // Pattern: "chi nhánh [Tên]" hoặc "[Tên]" sau từ khóa "chi nhánh"
        // LOẠI TRỪ câu hỏi: "Bạn có những chi nhánh nào?", "Chi nhánh nào?", etc.
        boolean isQuestionPattern = msgLower.contains("bạn có") || msgLower.contains("những chi nhánh nào") ||
                msgLower.contains("chi nhánh nào") || msgLower.contains("có những") ||
                msgLower.contains("muốn biết") || msgLower.contains("cho tôi biết");

        if (!isQuestionPattern) {
            // Pattern cải thiện: Extract tên branch đầy đủ sau "chi nhánh"
            // Ví dụ: "tôi chọn chi nhánh Phú nhuận" → extract "Phú nhuận"
            // Pattern ưu tiên: "chi nhánh" + tên branch (có thể có nhiều từ)
            Pattern branchPattern1 = Pattern.compile(
                    "chi\\s*nhánh\\s+([A-Za-zÀ-ỹ][A-Za-zÀ-ỹ\\s\\-]+?)(?:\\s|$|\\?|!|,|\\.)",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = branchPattern1.matcher(userMessage);
            if (matcher1.find()) {
                String extractedName = matcher1.group(1).trim();
                
                // Loại bỏ các từ không phải tên branch
                extractedName = extractedName.replaceAll("^(?:tôi|muốn|chọn|đặt|đổi|quan)\\s+", "").trim();
                extractedName = extractedName.replaceAll("\\s+(?:cho|tôi|bạn|nào|gì|đâu)$", "").trim();
                
                // Nếu extract được và có ít nhất 2 ký tự
                if (extractedName.length() >= 2) {
                    // Tìm trong tool response để validate và lấy tên đầy đủ
                    if (branchesResponse != null && branchesResponse.getBranches() != null) {
                        for (GetBranchesResponse.BranchInfo branch :
                                branchesResponse.getBranches()) {
                            if (branch.getBranchName() != null) {
                                String branchNameLower = branch.getBranchName().toLowerCase();
                                String extractedLower = extractedName.toLowerCase();
                                // Match exact hoặc partial (extracted name có trong branch name)
                                if (branchNameLower.contains(extractedLower) || 
                                    extractedLower.contains(branchNameLower)) {
                                    log.info("Found branch by extracted name '{}': {}", extractedName, branch.getBranchName());
                                    return branch.getBranchName(); // Return tên đầy đủ từ tool response
                                }
                            }
                        }
                    }
                    // Nếu không tìm thấy trong tool response, return extracted name để tìm trong database
                    log.info("Extracted branch name from message: {}", extractedName);
                    return extractedName;
                }
            }
            
            // Pattern fallback: "chọn/đặt/muốn" + "chi nhánh" + tên
            Pattern branchPattern2 = Pattern.compile(
                    "(?:chọn|đặt|muốn|tôi\\s+muốn\\s+chọn|tôi\\s+muốn\\s+đặt|tôi\\s+chọn|tôi\\s+đổi)\\s+(?:chi\\s*nhánh\\s+)?([A-Za-zÀ-ỹ][A-Za-zÀ-ỹ\\s\\-]+?)(?:\\s|$|\\?|!|,|\\.)",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = branchPattern2.matcher(userMessage);
            if (matcher2.find()) {
                String extractedName = matcher2.group(1).trim();
                
                // Loại bỏ các từ không phải tên branch
                extractedName = extractedName.replaceAll("^(?:tôi|muốn|chọn|đặt|đổi|quan)\\s+", "").trim();
                extractedName = extractedName.replaceAll("\\s+(?:cho|tôi|bạn|nào|gì|đâu)$", "").trim();

                // Kiểm tra lại xem có phải câu hỏi không (sau khi extract)
                String extractedLower = extractedName.toLowerCase();
                if (extractedLower.contains("bạn có") || extractedLower.contains("những") ||
                        extractedLower.contains("nào") || extractedLower.length() < 3) {
                    log.debug("Skipping branch extraction - appears to be a question or invalid: {}", extractedName);
                    return null;
                }

                // Tìm trong tool response để validate và lấy tên đầy đủ
                if (branchesResponse != null && branchesResponse.getBranches() != null) {
                    for (GetBranchesResponse.BranchInfo branch :
                            branchesResponse.getBranches()) {
                        if (branch.getBranchName() != null) {
                            String branchNameLower = branch.getBranchName().toLowerCase();
                            // Match exact hoặc partial
                            if (branch.getBranchName().equalsIgnoreCase(extractedName) ||
                                    branchNameLower.contains(extractedLower) ||
                                    extractedLower.contains(branchNameLower)) {
                                log.info("Found branch by extracted name '{}': {}", extractedName, branch.getBranchName());
                                return branch.getBranchName(); // Return tên đầy đủ từ tool response
                            }
                        }
                    }
                }
                // Nếu không tìm thấy trong tool response, return extracted name để tìm trong database
                log.info("Extracted branch name from message: {}", extractedName);
                return extractedName;
            }
        }

        return null;
    }

    /**
     * Tìm branch_id từ tool response dựa trên branch name
     * Ưu tiên lấy từ ThreadLocal (function response vừa được gọi), fallback sang conversation history
     */
    private UUID findBranchIdFromToolResponse(String branchName, List<ChatRequest.ChatMessage> history) {
        if (branchName == null)
            return null;

        // BƯỚC 1: Ưu tiên lấy từ ThreadLocal (function response vừa được gọi trong request này)
        GetBranchesResponse branchesResponse =
                DraftContextHolder.getBranchesResponse();

        if (branchesResponse != null && branchesResponse.getBranches() != null) {
            for (GetBranchesResponse.BranchInfo branch :
                    branchesResponse.getBranches()) {
                if (branch.getBranchName() != null && branch.getBranchName().equalsIgnoreCase(branchName)) {
                    log.info("Found branch_id from ThreadLocal (function response): {} for branch_name: {}",
                            branch.getBranchId(), branchName);
                    return branch.getBranchId();
                }
            }
        }

        // BƯỚC 2: Fallback - tìm trong conversation history (nếu frontend gửi tool responses)
        if (history != null) {
            for (int i = history.size() - 1; i >= 0; i--) {
                ChatRequest.ChatMessage msg = history.get(i);
                if ("tool".equalsIgnoreCase(msg.getRole()) &&
                        "getBranches".equalsIgnoreCase(msg.getToolName()) &&
                        msg.getToolResponse() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonStr = mapper.writeValueAsString(msg.getToolResponse());
                        JsonNode root = mapper.readTree(jsonStr);

                        if (root.has("branches") && root.get("branches").isArray()) {
                            for (JsonNode branch : root.get("branches")) {
                                if (branch.has("branch_name")) {
                                    String bn = branch.get("branch_name").asText();
                                    // Match exact hoặc partial (ví dụ: "Gò Vấp" match với "Chi Nhánh Gò Vấp")
                                    String bnLower = bn.toLowerCase();
                                    String branchNameLower = branchName.toLowerCase();
                                    if (bn.equalsIgnoreCase(branchName) ||
                                            bnLower.contains(branchNameLower) ||
                                            branchNameLower.contains(bnLower)) {
                                        if (branch.has("branch_id")) {
                                            String branchIdStr = branch.get("branch_id").asText();
                                            log.info("Found branch_id from conversation history: {} for branch_name: {} (matched with: {})",
                                                    branchIdStr, branchName, bn);
                                            return UUID.fromString(branchIdStr);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error parsing branch from conversation history: {}", e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    /**
     * Tìm branch_id từ database dựa trên branch name
     * Fallback khi không tìm thấy trong tool response
     */
    private UUID findBranchIdFromDatabase(String branchName) {
        if (branchName == null || branchName.trim().isEmpty()) {
            return null;
        }

        try {
            // Query database để lấy branch_id từ branch name
            Branch branch =
                    aiBookingAssistantService.findBranchByNameOrAddress(branchName.trim());

            if (branch != null) {
                log.info("Found branch_id from database: {} for branch_name: {} (matched with: {})",
                        branch.getBranchId(), branchName, branch.getBranchName());
                return branch.getBranchId();
            } else {
                log.warn("Could not find branch in database for branch_name: {}", branchName);
            }
        } catch (Exception e) {
            log.error("Error finding branch_id from database for branch_name '{}': {}", branchName, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Extract service selection từ user message
     */
    /**
     * Extract service selection từ user message
     * Không hard code - chỉ dùng dynamic patterns từ tool response
     * Hỗ trợ: "dịch vụ X", "chọn số X", tên dịch vụ cụ thể từ tool response
     * Hỗ trợ xác nhận: "đúng", "có", "ok", "chọn dịch vụ này" khi có service cần xác nhận
     */
    private String extractServiceSelectionFromMessage(String userMessage, List<ChatRequest.ChatMessage> history, BookingDraft draft) {
        String msgLower = userMessage.toLowerCase();

        // CRITICAL: Nếu đang ở step 1 (chọn vehicle) → KHÔNG extract service
        // Vì user có thể nói "tôi chọn xe 69D1-27069" và pattern sẽ extract "xe" thành service
        if (draft.getCurrentStep() == 1 && !draft.hasVehicle()) {
            log.debug("Skipping service extraction - current step is 1 (vehicle selection). User message: {}", userMessage);
            return null;
        }

        // Pattern 0: Xác nhận dịch vụ khi có service cần xác nhận
        // Nếu user nói "đúng", "có", "ok", "chọn dịch vụ này" → Lấy service từ conversation history hoặc tool response
        GetServicesResponse servicesResponse = DraftContextHolder.getServicesResponse();
        
        // Kiểm tra xem user có xác nhận không
        boolean isConfirmation = isConfirmationMessage(msgLower);
        boolean isRejection = isRejectionMessage(msgLower);
        
        if (isConfirmation && !isRejection) {
            // User xác nhận → Ưu tiên lấy từ conversation history (tool response gần nhất)
            // Bước 1: Parse conversation history để lấy tool response gần nhất
            GetServicesResponse historyResponse = parseServicesFromHistory(history);
            if (historyResponse != null && historyResponse.getServices() != null && 
                !historyResponse.getServices().isEmpty()) {
                // Lấy service đầu tiên từ conversation history (service được đề xuất)
                String serviceName = historyResponse.getServices().get(0).getServiceName();
                log.info("User confirmed service selection: {} (from conversation history)", serviceName);
                return serviceName;
            }
            
            // Bước 2: Fallback - lấy từ ThreadLocal (tool response vừa được gọi)
            if (servicesResponse != null && 
                servicesResponse.getServices() != null && 
                !servicesResponse.getServices().isEmpty()) {
                String serviceName = servicesResponse.getServices().get(0).getServiceName();
                log.info("User confirmed service selection: {} (from ThreadLocal tool response)", serviceName);
                return serviceName;
            }
            
            // Bước 3: Fallback - lấy từ draft nếu đã có service
            if (draft.hasService() && draft.getServiceType() != null) {
                log.info("User confirmed service selection: {} (from draft, tool response not available)", draft.getServiceType());
                return draft.getServiceType();
            }
            
            // Không có service nào để xác nhận
            log.warn("User confirmed but no service found in conversation history, tool response or draft");
            return null;
        } else if (isRejection) {
            // User từ chối → Không lưu, AI sẽ hỏi lại
            log.info("User rejected service. Will ask for another service.");
            return null;
        }

        // Pattern 1: "chọn số X" hoặc "số X" - tìm trong tool response (INDEX selection)
        Pattern numberPattern = Pattern.compile("(?:chọn|số|dịch\\s*vụ\\s*(?:số|thứ)?)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher numberMatcher = numberPattern.matcher(userMessage);
        if (numberMatcher.find()) {
            String numberStr = numberMatcher.group(1);
            // Tìm service theo số thứ tự trong tool response
            if (servicesResponse == null) {
                servicesResponse = DraftContextHolder.getServicesResponse();
            }
            if (servicesResponse == null && history != null) {
                servicesResponse = parseServicesFromHistory(history);
            }

            if (servicesResponse != null && servicesResponse.getServices() != null) {
                try {
                    int index = Integer.parseInt(numberStr) - 1; // Convert to 0-based index
                    if (index >= 0 && index < servicesResponse.getServices().size()) {
                        String serviceName = servicesResponse.getServices().get(index).getServiceName();
                        log.info("Found service by number {}: {} (INDEX selection - will save immediately)", numberStr, serviceName);
                        return serviceName;
                    }
                } catch (NumberFormatException e) {
                    log.debug("Invalid service number: {}", numberStr);
                }
            }
        }

        // Pattern 2: "Tôi chọn X" hoặc "chọn X" - user chọn cụ thể bằng tên (NAME selection)
        // Pattern này ưu tiên hơn Pattern 3 vì user đã chọn cụ thể
        // CRITICAL: Loại trừ các từ không phải service: "xe", "chi nhánh", "bay", "khu vực", "ngày", "giờ", "thời gian"
        Pattern choosePattern = Pattern.compile(
                "(?:tôi\\s+)?(?:muốn\\s+)?(?:chọn|đặt|đổi)\\s+(?:dịch\\s*vụ\\s+)?([A-Za-zÀ-ỹ][A-Za-zÀ-ỹ\\s\\-]+?)(?:\\s|$|\\?|!|,|\\.)",
                Pattern.CASE_INSENSITIVE);
        Matcher chooseMatcher = choosePattern.matcher(userMessage);
        if (chooseMatcher.find()) {
            String extractedService = chooseMatcher.group(1).trim();
            // Loại bỏ các từ không phải tên service
            extractedService = extractedService.replaceAll("^(?:tôi|muốn|chọn|đặt|đổi|dịch\\s*vụ)\\s+", "").trim();
            extractedService = extractedService.replaceAll("\\s+(?:cho|tôi|bạn|nào|gì|đâu)$", "").trim();
            
            // CRITICAL: Blacklist các từ không phải service
            String extractedLowerCheck = extractedService.toLowerCase();
            String[] blacklist = {"xe", "chi nhánh", "bay", "khu vực", "ngày", "giờ", "thời gian", "ngày mai", "ngày kia", "hôm nay"};
            for (String blacklisted : blacklist) {
                if (extractedLowerCheck.equals(blacklisted) || extractedLowerCheck.startsWith(blacklisted + " ")) {
                    log.debug("Skipping service extraction - extracted text '{}' matches blacklist: {}", extractedService, blacklisted);
                    return null;
                }
            }
            
            if (extractedService.length() >= 2) {
                // Tìm trong tool response hoặc conversation history
                if (servicesResponse == null) {
                    servicesResponse = DraftContextHolder.getServicesResponse();
                }
                if (servicesResponse == null && history != null) {
                    servicesResponse = parseServicesFromHistory(history);
                }
                
                if (servicesResponse != null && servicesResponse.getServices() != null) {
                    String extractedLower = extractedService.toLowerCase();
                    for (GetServicesResponse.ServiceInfo service : servicesResponse.getServices()) {
                        if (service.getServiceName() != null) {
                            String serviceNameLower = service.getServiceName().toLowerCase();
                            // Match exact hoặc partial (extracted name có trong service name hoặc ngược lại)
                            if (service.getServiceName().equalsIgnoreCase(extractedService) ||
                                serviceNameLower.contains(extractedLower) ||
                                extractedLower.contains(serviceNameLower)) {
                                log.info("Found service by extracted name '{}': {} (NAME selection - will save immediately)", 
                                        extractedService, service.getServiceName());
                                return service.getServiceName(); // Return tên đầy đủ từ tool response
                            }
                        }
                    }
                }
                
                // Nếu không tìm thấy trong tool response, vẫn return extracted name để tìm trong database
                log.info("Extracted service name from 'chọn' pattern: {} (will try to find in database)", extractedService);
                return extractedService;
            }
        }

        // Pattern 3: Tìm exact match trong tool response (nếu user nói tên dịch vụ cụ thể)
        // QUAN TRỌNG: Chỉ tìm trong tool response - KHÔNG extract nếu không có tool response
        if (servicesResponse == null) {
            servicesResponse = DraftContextHolder.getServicesResponse();
        }
        if (servicesResponse == null && history != null) {
            servicesResponse = parseServicesFromHistory(history);
        }

        if (servicesResponse != null && servicesResponse.getServices() != null) {
            for (GetServicesResponse.ServiceInfo service :
                    servicesResponse.getServices()) {
                if (service.getServiceName() != null) {
                    String serviceNameLower = service.getServiceName().toLowerCase();
                    // Match tên dịch vụ hoặc keywords trong tên
                    if (userMessage.contains(service.getServiceName()) ||
                            serviceNameLower.contains(msgLower) ||
                            msgLower.contains(serviceNameLower) ||
                            // Match keywords: "rửa xe", "bảo dưỡng", etc. nếu có trong tên dịch vụ
                            (msgLower.contains("rửa xe") && serviceNameLower.contains("rửa xe")) ||
                            (msgLower.contains("bảo dưỡng") && serviceNameLower.contains("bảo dưỡng")) ||
                            (msgLower.contains("sửa chữa") && serviceNameLower.contains("sửa chữa"))) {
                        log.info("Found service by name match in tool response: {}", service.getServiceName());
                        return service.getServiceName();
                    }
                }
            }
        } else {
            // KHÔNG có tool response → AI chưa gọi getServices()
            // Log warning và return null để yêu cầu AI gọi getServices() trước
            log.warn("No tool response found for services. AI should call getServices() first when user mentions a service.");
        }

        // Pattern 3: "dịch vụ X" hoặc "chọn dịch vụ X" - CHỈ extract nếu có tool response để validate
        if (msgLower.contains("dịch vụ")) {
            Pattern pattern = Pattern.compile("(?:chọn\\s+)?dịch\\s+vụ\\s+([A-Za-zÀ-ỹ\\s]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(userMessage);
            if (matcher.find()) {
                String extractedService = matcher.group(1).trim();
                // CHỈ return nếu có tool response để validate
                if (servicesResponse != null && servicesResponse.getServices() != null) {
                    for (GetServicesResponse.ServiceInfo service :
                            servicesResponse.getServices()) {
                        if (service.getServiceName() != null &&
                                service.getServiceName().equalsIgnoreCase(extractedService)) {
                            log.info("Found service by extracted name in tool response: {}", service.getServiceName());
                            return service.getServiceName();
                        }
                    }
                }
                // Nếu không có tool response hoặc không tìm thấy → return null
                // Để yêu cầu AI gọi getServices() trước
                log.warn("Extracted service '{}' but no tool response to validate. AI should call getServices() first.",
                        extractedService);
                return null;
            }
        }

        return null;
    }

    /**
     * Tìm service_id và service_name từ tool response dựa trên user selection
     * Ưu tiên lấy từ ThreadLocal (function response vừa được gọi), fallback sang conversation history
     *
     * @return ServiceInfo với service_id và service_name, hoặc null nếu không tìm thấy
     */
    private GetServicesResponse.ServiceInfo
    findServiceInfoFromToolResponse(String selectedService, List<ChatRequest.ChatMessage> history) {
        if (selectedService == null)
            return null;

        // BƯỚC 1: Ưu tiên lấy từ ThreadLocal (function response vừa được gọi trong request này)
        GetServicesResponse servicesResponse =
                DraftContextHolder.getServicesResponse();

        if (servicesResponse != null && servicesResponse.getServices() != null) {
            for (GetServicesResponse.ServiceInfo service :
                    servicesResponse.getServices()) {
                if (service.getServiceName() != null &&
                        (service.getServiceName().equalsIgnoreCase(selectedService) ||
                                service.getServiceName().contains(selectedService) ||
                                selectedService.contains(service.getServiceName()))) {
                    log.info("Found service from ThreadLocal (function response): service_id={}, service_name={} for selection: {}",
                            service.getServiceId(), service.getServiceName(), selectedService);
                    return service;
                }
            }
        }

        // BƯỚC 2: Fallback - tìm trong conversation history (nếu frontend gửi tool responses)
        if (history != null) {
            for (int i = history.size() - 1; i >= 0; i--) {
                ChatRequest.ChatMessage msg = history.get(i);
                if ("tool".equalsIgnoreCase(msg.getRole()) &&
                        "getServices".equalsIgnoreCase(msg.getToolName()) &&
                        msg.getToolResponse() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonStr = mapper.writeValueAsString(msg.getToolResponse());
                        JsonNode root = mapper.readTree(jsonStr);

                        if (root.has("services") && root.get("services").isArray()) {
                            for (JsonNode service : root.get("services")) {
                                if (service.has("service_name")) {
                                    String sn = service.get("service_name").asText();
                                    if (sn.equalsIgnoreCase(selectedService) ||
                                            sn.contains(selectedService) ||
                                            selectedService.contains(sn)) {
                                        // Tạo ServiceInfo từ JSON
                                        GetServicesResponse.ServiceInfo serviceInfo =
                                                GetServicesResponse.ServiceInfo.builder()
                                                        .serviceId(service.has("service_id") ?
                                                                java.util.UUID.fromString(service.get("service_id").asText()) : null)
                                                        .serviceName(sn)
                                                        .description(service.has("description") ? service.get("description").asText() : null)
                                                        .estimatedDuration(service.has("estimated_duration") ? service.get("estimated_duration").asInt() : null)
                                                        .price(service.has("price") ? new java.math.BigDecimal(service.get("price").asText()) : null)
                                                        .build();

                                        log.info("Found service from conversation history: service_id={}, service_name={} for selection: {}",
                                                serviceInfo.getServiceId(), serviceInfo.getServiceName(), selectedService);
                                        return serviceInfo;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error parsing service from conversation history: {}", e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    /**
     * Tìm service từ database dựa trên serviceName
     * Ưu tiên exact match, sau đó tìm theo keyword
     * 
     * @param serviceName Tên dịch vụ cần tìm
     * @return Service entity nếu tìm thấy, null nếu không tìm thấy
     */
    private com.kltn.scsms_api_service.core.entity.Service findServiceFromDatabase(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            return null;
        }
        
        try {
            // BƯỚC 1: Tìm theo keyword (searchByKeyword)
            List<com.kltn.scsms_api_service.core.entity.Service> foundServices = 
                    serviceService.searchByKeyword(serviceName.trim());
            
            if (foundServices.isEmpty()) {
                log.debug("No services found in database for keyword: {}", serviceName);
                return null;
            }
            
            // BƯỚC 2: Ưu tiên exact match (tên dịch vụ khớp chính xác)
            for (com.kltn.scsms_api_service.core.entity.Service service : foundServices) {
                if (service.getServiceName() != null && 
                    service.getServiceName().equalsIgnoreCase(serviceName.trim())) {
                    log.info("Found exact match service in database: service_id={}, service_name={}", 
                            service.getServiceId(), service.getServiceName());
                    return service;
                }
            }
            
            // BƯỚC 3: Nếu không có exact match, kiểm tra partial match
            for (com.kltn.scsms_api_service.core.entity.Service service : foundServices) {
                if (service.getServiceName() != null) {
                    String serviceNameLower = service.getServiceName().toLowerCase();
                    String searchNameLower = serviceName.trim().toLowerCase();
                    
                    // Match nếu serviceName chứa searchName hoặc ngược lại
                    if (serviceNameLower.contains(searchNameLower) || 
                        searchNameLower.contains(serviceNameLower)) {
                        log.info("Found partial match service in database: service_id={}, service_name={} (searched: {})", 
                                service.getServiceId(), service.getServiceName(), serviceName);
                        return service;
                    }
                }
            }
            
            // BƯỚC 4: Nếu không có match nào, lấy service đầu tiên (fallback)
            // Chỉ khi có 1 service duy nhất để tránh chọn sai
            if (foundServices.size() == 1) {
                log.info("Found single service in database (fallback): service_id={}, service_name={} (searched: {})", 
                        foundServices.get(0).getServiceId(), foundServices.get(0).getServiceName(), serviceName);
                return foundServices.get(0);
            }
            
            // Nếu có nhiều services và không match → return null
            log.warn("Multiple services found in database for '{}' but no exact/partial match. " +
                    "Found {} services. Will not auto-select.", serviceName, foundServices.size());
            return null;
            
        } catch (Exception e) {
            log.error("Error querying database for service: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract bay selection từ user message
     * Hỗ trợ: "Gò Vấp Bay 4", "Bay 4", "bay số 4", "khu vực 2", etc.
     */
    private String extractBaySelectionFromMessage(String userMessage, List<ChatRequest.ChatMessage> history) {
        String msgLower = userMessage.toLowerCase();

        // Pattern 1: "bay số X", "bay thứ X", "chọn bay thứ X" - tìm trong tool response
        Pattern numberPattern = Pattern.compile("(?:bay\\s*(?:số|thứ)|chọn\\s*(?:bay\\s*)?(?:số|thứ)|số|thứ)\\s*(\\d+)",
                Pattern.CASE_INSENSITIVE);
        Matcher numberMatcher = numberPattern.matcher(userMessage);
        if (numberMatcher.find()) {
            String bayNum = numberMatcher.group(1);
            // Tìm bay theo số thứ tự trong tool response
            AvailabilityResponse availabilityResponse =
                    DraftContextHolder.getAvailabilityResponse();
            
            // Fallback: Parse từ conversation history
            if (availabilityResponse == null && history != null) {
                availabilityResponse = parseAvailabilityFromHistory(history);
            }

            if (availabilityResponse != null && availabilityResponse.getAvailableBays() != null) {
                try {
                    int index = Integer.parseInt(bayNum) - 1; // Convert to 0-based index
                    if (index >= 0 && index < availabilityResponse.getAvailableBays().size()) {
                        String bayName = availabilityResponse.getAvailableBays().get(index).getBayName();
                        log.info("Found bay by number {}: {}", bayNum, bayName);
                        return bayName;
                    }
                } catch (NumberFormatException e) {
                    log.debug("Invalid bay number: {}", bayNum);
                }
            }
        }

        // Pattern 2: Tìm exact match trong tool response (nếu user nói tên bay cụ thể)
        AvailabilityResponse availabilityResponse =
                DraftContextHolder.getAvailabilityResponse();
        
        // Fallback: Parse từ conversation history
        if (availabilityResponse == null && history != null) {
            availabilityResponse = parseAvailabilityFromHistory(history);
        }

        if (availabilityResponse != null && availabilityResponse.getAvailableBays() != null) {
            for (AvailabilityResponse.AvailableBayInfo bay :
                    availabilityResponse.getAvailableBays()) {
                if (bay.getBayName() != null) {
                    String bayNameLower = bay.getBayName().toLowerCase();
                    // Match tên bay hoặc keywords trong tên
                    if (userMessage.contains(bay.getBayName()) ||
                            bayNameLower.contains(msgLower) ||
                            msgLower.contains(bayNameLower)) {
                        log.info("Found bay by name match: {} (from user message: {})", bay.getBayName(), userMessage);
                        return bay.getBayName();
                    }
                }
            }
        }

        // Pattern 3: Extract "khu vực X", "khu vực số X", "khu vực đầu tiên", "khu vực thứ hai", etc.
        Pattern khuVucPattern = Pattern.compile("khu\\s*vực\\s*(?:số\\s*)?(\\d+|đầu\\s*tiên|thứ\\s*(?:hai|ba|bốn|năm|sáu|bảy|tám|chín|mười))", Pattern.CASE_INSENSITIVE);
        Matcher khuVucMatcher = khuVucPattern.matcher(userMessage);
        if (khuVucMatcher.find()) {
            String khuVucText = khuVucMatcher.group(1);
            int index = parseIndexFromText(khuVucText);
            
            // Tìm trong tool response theo index hoặc số
            if (availabilityResponse != null && availabilityResponse.getAvailableBays() != null) {
                if (index > 0 && index <= availabilityResponse.getAvailableBays().size()) {
                    // Tìm theo index
                    AvailabilityResponse.AvailableBayInfo bay = availabilityResponse.getAvailableBays().get(index - 1);
                    log.info("Found bay by khu vực index {}: {}", index, bay.getBayName());
                    return bay.getBayName();
                } else {
                    // Tìm theo số trong tên
                    for (AvailabilityResponse.AvailableBayInfo bay : availabilityResponse.getAvailableBays()) {
                        if (bay.getBayName() != null && bay.getBayName().contains("Khu vực " + khuVucText)) {
                            log.info("Found bay by khu vực '{}': {}", khuVucText, bay.getBayName());
                            return bay.getBayName();
                        }
                    }
                }
            }
            // Fallback: Return "Khu vực X" hoặc "Khu vực {index}"
            if (index > 0) {
                return "Khu vực " + index;
            }
            return "Khu vực " + khuVucText;
        }
        
        // Pattern 4: Extract từ user message nếu có format "bay X" hoặc "Gò Vấp Bay 4"
        Pattern bayPattern = Pattern.compile("(?:bay|Bay)\\s*(\\d+)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = bayPattern.matcher(userMessage);
        if (matcher.find()) {
            String bayNum = matcher.group(1);
            // Tìm trong tool response để validate và lấy tên đầy đủ
            if (availabilityResponse != null && availabilityResponse.getAvailableBays() != null) {
                for (com.kltn.scsms_api_service.core.dto.aiAssistant.response.AvailabilityResponse.AvailableBayInfo bay :
                        availabilityResponse.getAvailableBays()) {
                    if (bay.getBayName() != null && bay.getBayName().contains("Bay " + bayNum)) {
                        log.info("Found bay by extracted number '{}': {}", bayNum, bay.getBayName());
                        return bay.getBayName(); // Return tên đầy đủ từ tool response
                    }
                }
            }
            // Nếu không tìm thấy trong tool response, construct tên từ user message
            if (msgLower.contains("gò vấp")) {
                return "Gò Vấp Bay " + bayNum;
            }
            if (msgLower.contains("cầu giấy")) {
                return "Cầu Giấy Bay " + bayNum;
            }
            return "Bay " + bayNum;
        }

        // Pattern 6: Extract full bay name nếu user nói đầy đủ (ví dụ: "Gò Vấp Bay 4")
        Pattern fullBayPattern = Pattern.compile("([A-Za-zÀ-ỹ\\s]+)\\s+Bay\\s+(\\d+)",
                Pattern.CASE_INSENSITIVE);
        Matcher fullMatcher = fullBayPattern.matcher(userMessage);
        if (fullMatcher.find()) {
            String location = fullMatcher.group(1).trim();
            String bayNum = fullMatcher.group(2);
            String fullBayName = location + " Bay " + bayNum;
            log.info("Extracted full bay name from message: {}", fullBayName);
            return fullBayName;
        }

        return null;
    }
    
    /**
     * Parse index từ text ("đầu tiên" = 1, "thứ hai" = 2, "thứ ba" = 3, etc.)
     */
    private int parseIndexFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        String textLower = text.toLowerCase().trim();
        
        // "đầu tiên", "đầu", "số 1", "1"
        if (textLower.contains("đầu tiên") || textLower.equals("đầu") || 
            textLower.equals("1") || textLower.contains("số 1")) {
            return 1;
        }
        
        // "thứ hai", "thứ 2", "số 2", "2"
        if (textLower.contains("thứ hai") || textLower.contains("thứ 2") || 
            textLower.equals("2") || textLower.contains("số 2")) {
            return 2;
        }
        
        // "thứ ba", "thứ 3", "số 3", "3"
        if (textLower.contains("thứ ba") || textLower.contains("thứ 3") || 
            textLower.equals("3") || textLower.contains("số 3")) {
            return 3;
        }
        
        // Parse số từ text
        Pattern numberPattern = Pattern.compile("(\\d+)");
        Matcher matcher = numberPattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        return 0;
    }
    
    /**
     * Parse GetCustomerVehiclesResponse từ conversation history
     */
    private GetCustomerVehiclesResponse parseVehiclesFromHistory(List<ChatRequest.ChatMessage> history) {
        if (history == null) return null;
        
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = history.get(i);
            if ("tool".equalsIgnoreCase(msg.getRole()) &&
                "getCustomerVehicles".equalsIgnoreCase(msg.getToolName()) &&
                msg.getToolResponse() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonStr = mapper.writeValueAsString(msg.getToolResponse());
                    return mapper.readValue(jsonStr, GetCustomerVehiclesResponse.class);
                } catch (Exception e) {
                    log.debug("Error parsing GetCustomerVehiclesResponse from history: {}", e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * Parse GetBranchesResponse từ conversation history
     */
    private GetBranchesResponse parseBranchesFromHistory(List<ChatRequest.ChatMessage> history) {
        if (history == null) return null;
        
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = history.get(i);
            if ("tool".equalsIgnoreCase(msg.getRole()) &&
                "getBranches".equalsIgnoreCase(msg.getToolName()) &&
                msg.getToolResponse() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonStr = mapper.writeValueAsString(msg.getToolResponse());
                    return mapper.readValue(jsonStr, GetBranchesResponse.class);
                } catch (Exception e) {
                    log.debug("Error parsing GetBranchesResponse from history: {}", e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * Parse GetServicesResponse từ conversation history
     */
    private GetServicesResponse parseServicesFromHistory(List<ChatRequest.ChatMessage> history) {
        if (history == null) return null;
        
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = history.get(i);
            if ("tool".equalsIgnoreCase(msg.getRole()) &&
                "getServices".equalsIgnoreCase(msg.getToolName()) &&
                msg.getToolResponse() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonStr = mapper.writeValueAsString(msg.getToolResponse());
                    return mapper.readValue(jsonStr, GetServicesResponse.class);
                } catch (Exception e) {
                    log.debug("Error parsing GetServicesResponse from history: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Parse availability từ conversation history
     */
    private AvailabilityResponse parseAvailabilityFromHistory(List<ChatRequest.ChatMessage> history) {
        if (history == null) return null;
        
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = history.get(i);
            if ("tool".equalsIgnoreCase(msg.getRole()) &&
                "checkAvailability".equalsIgnoreCase(msg.getToolName()) &&
                msg.getToolResponse() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonStr = mapper.writeValueAsString(msg.getToolResponse());
                    return mapper.readValue(jsonStr, AvailabilityResponse.class);
                } catch (Exception e) {
                    log.warn("Error parsing availability from conversation history: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Tìm bay_id từ tool response dựa trên bay name
     * Ưu tiên lấy từ ThreadLocal (function response vừa được gọi), fallback sang conversation history
     */
    private UUID findBayIdFromToolResponse(String bayName, List<ChatRequest.ChatMessage> history) {
        if (bayName == null)
            return null;

        // BƯỚC 1: Ưu tiên lấy từ ThreadLocal (function response vừa được gọi trong request này)
        AvailabilityResponse availabilityResponse =
                DraftContextHolder.getAvailabilityResponse();

        if (availabilityResponse != null && availabilityResponse.getAvailableBays() != null) {
            for (AvailabilityResponse.AvailableBayInfo bay :
                    availabilityResponse.getAvailableBays()) {
                if (bay.getBayName() != null &&
                        (bay.getBayName().equalsIgnoreCase(bayName) ||
                                bay.getBayName().contains(bayName) ||
                                bayName.contains(bay.getBayName()))) {
                    log.info("Found bay_id from ThreadLocal (function response): {} for bay_name: {}",
                            bay.getBayId(), bayName);
                    return bay.getBayId();
                }
            }
        }

        // BƯỚC 2: Fallback - tìm trong conversation history (nếu frontend gửi tool responses)
        if (history != null) {
            for (int i = history.size() - 1; i >= 0; i--) {
                ChatRequest.ChatMessage msg = history.get(i);
                if ("tool".equalsIgnoreCase(msg.getRole()) &&
                        "checkAvailability".equalsIgnoreCase(msg.getToolName()) &&
                        msg.getToolResponse() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonStr = mapper.writeValueAsString(msg.getToolResponse());
                        JsonNode root = mapper.readTree(jsonStr);

                        if (root.has("available_bays") && root.get("available_bays").isArray()) {
                            for (JsonNode bay : root.get("available_bays")) {
                                if (bay.has("bay_name")) {
                                    String bn = bay.get("bay_name").asText();
                                    // Match exact hoặc partial (ví dụ: "Bay 4" match với "Gò Vấp Bay 4")
                                    String bnLower = bn.toLowerCase();
                                    String bayNameLower = bayName.toLowerCase();
                                    if (bn.equalsIgnoreCase(bayName) ||
                                            bnLower.contains(bayNameLower) ||
                                            bayNameLower.contains(bnLower)) {
                                        if (bay.has("bay_id")) {
                                            String bayIdStr = bay.get("bay_id").asText();
                                            log.info("Found bay_id from conversation history: {} for bay_name: {} (matched with: {})",
                                                    bayIdStr, bayName, bn);
                                            return UUID.fromString(bayIdStr);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error parsing bay from conversation history: {}", e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    /**
     * Tìm bay_id từ database dựa trên bay name và branch_id
     * Fallback khi không tìm thấy trong tool response
     */
    private UUID findBayIdFromDatabase(String bayName, UUID branchId) {
        if (bayName == null || bayName.trim().isEmpty()) {
            return null;
        }

        try {
            // Query database để lấy bay_id từ bay name và branch_id
            if (branchId != null) {
                // Tìm bay trong branch cụ thể
                List<ServiceBay> bays =
                        serviceBayService.getByBranch(branchId);

                if (bays != null && !bays.isEmpty()) {
                    String searchNameLower = bayName.toLowerCase().trim();
                    String searchNameNormalized = bayName.replaceAll("\\s+", " ").trim();
                    
                    // Extract số từ "Khu vực X" hoặc "Bay X"
                    Pattern numberPattern = Pattern.compile("(?:khu\\s*vực|bay)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
                    Matcher numberMatcher = numberPattern.matcher(bayName);
                    String extractedNumber = null;
                    if (numberMatcher.find()) {
                        extractedNumber = numberMatcher.group(1);
                    }
                    
                    for (ServiceBay bay : bays) {
                        if (bay.getBayName() == null || bay.getIsDeleted() || !bay.getIsActive()) {
                            continue;
                        }
                        
                        String bayNameLower = bay.getBayName().toLowerCase().trim();
                        String bayNameNormalized = bay.getBayName().replaceAll("\\s+", " ").trim();
                        
                        // Match 1: Exact match (case-insensitive)
                            if (bay.getBayName().equalsIgnoreCase(bayName) ||
                            bayNameNormalized.equalsIgnoreCase(searchNameNormalized)) {
                            log.info("Found bay_id from database (exact match): {} for bay_name: {} (matched with: {})",
                                    bay.getBayId(), bayName, bay.getBayName());
                            return bay.getBayId();
                        }
                        
                        // Match 2: Partial match
                        if (bayNameLower.contains(searchNameLower) || 
                                    searchNameLower.contains(bayNameLower)) {
                            log.info("Found bay_id from database (partial match): {} for bay_name: {} (matched with: {})",
                                        bay.getBayId(), bayName, bay.getBayName());
                                return bay.getBayId();
                            }
                        
                        // Match 3: Match số trong tên (ví dụ: "Khu vực 1" match với "Khu vực 1")
                        if (extractedNumber != null) {
                            Pattern bayNumberPattern = Pattern.compile("(?:khu\\s*vực|bay)\\s*" + extractedNumber, Pattern.CASE_INSENSITIVE);
                            if (bayNumberPattern.matcher(bay.getBayName()).find()) {
                                log.info("Found bay_id from database (number match): {} for bay_name: {} (matched with: {})",
                                        bay.getBayId(), bayName, bay.getBayName());
                                return bay.getBayId();
                            }
                        }
                    }
                    
                    // Nếu không tìm thấy exact/partial match, thử search by keyword
                    try {
                        List<ServiceBay> keywordBays = serviceBayService.searchByKeywordInBranch(branchId, bayName);
                        if (keywordBays != null && !keywordBays.isEmpty()) {
                            ServiceBay foundBay = keywordBays.get(0);
                            log.info("Found bay_id from database (keyword search): {} for bay_name: {} (matched with: {})",
                                    foundBay.getBayId(), bayName, foundBay.getBayName());
                            return foundBay.getBayId();
                        }
                    } catch (Exception e) {
                        log.debug("Keyword search failed for bay_name: {}, error: {}", bayName, e.getMessage());
                    }
                }
            } else {
                // Nếu không có branch_id, thử tìm theo tên bay
                java.util.Optional<ServiceBay> bayOpt =
                        serviceBayService.getByBayName(bayName);

                if (bayOpt.isPresent()) {
                    ServiceBay bay = bayOpt.get();
                    log.info("Found bay_id from database (by name only): {} for bay_name: {}",
                            bay.getBayId(), bayName);
                    return bay.getBayId();
                }
            }

            log.warn("Could not find bay in database for bay_name: '{}', branch_id: {}. Tried exact match, partial match, number match, and keyword search.",
                    bayName, branchId);
        } catch (Exception e) {
            log.error("Error finding bay_id from database for bay_name '{}', branch_id '{}': {}",
                    bayName, branchId, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Extract time slot từ user message
     */
    private String extractTimeSlotFromMessage(String userMessage) {
        // Pattern: "08:00" hoặc "8:00"
        Pattern pattern = Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\b");
        Matcher matcher = pattern.matcher(userMessage);
        if (matcher.find()) {
            // Validate: giờ từ 0-23, phút từ 0-59
            try {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = Integer.parseInt(matcher.group(2));
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return String.format("%02d:%02d", hour, minute);
                }
            } catch (Exception e) {
                log.debug("Error parsing time slot: {}", e.getMessage());
            }
        }
        return null;
    }

    private String buildStateContextMessage(ConversationState state, List<ChatRequest.ChatMessage> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("THÔNG TIN ĐÃ CÓ TRONG CONVERSATION HISTORY (KHÔNG CẦN HỎI LẠI):\n\n");

        if (state.hasVehicle()) {
            sb.append("- [CO] Đã có vehicle_id (xe đã được chọn)\n");
            sb.append("  → KHÔNG được gọi getCustomerVehicles() lại\n");

            // Backend đã extract UUID, inject trực tiếp vào message
            if (state.getVehicleId() != null) {
                sb.append("  → BACKEND ĐÃ EXTRACT UUID: vehicle_id = '").append(state.getVehicleId()).append("'\n");
                sb.append("  → BACKEND ĐÃ EXTRACT: vehicle_license_plate = '")
                        .append(state.getVehicleLicensePlate() != null ? state.getVehicleLicensePlate() : "N/A")
                        .append("'\n");
                sb.append("  → BẮT BUỘC: Khi gọi createBooking(), PHẢI dùng vehicle_id này:\n");
                sb.append("    createBooking(vehicle_id='").append(state.getVehicleId())
                        .append("', vehicle_license_plate='")
                        .append(state.getVehicleLicensePlate() != null ? state.getVehicleLicensePlate() : "")
                        .append("')\n");
                sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID khác, PHẢI dùng vehicle_id trên\n");
            } else {
                sb.append(
                        "  → Backend chưa extract được vehicle_id, bạn cần extract từ TOOL response getCustomerVehicles()\n");
                if (state.getVehicleLicensePlate() != null) {
                    sb.append("  → Biển số xe đã chọn: '").append(state.getVehicleLicensePlate()).append("'\n");
                    sb.append("  → CÁCH LẤY vehicle_id:\n");
                    sb.append("    1. Tìm TOOL response getCustomerVehicles() GẦN NHẤT trong conversation history\n");
                    sb.append("    2. So sánh biển số '").append(state.getVehicleLicensePlate())
                            .append("' với danh sách vehicles\n");
                    sb.append("    3. Tìm vehicle có license_plate match\n");
                    sb.append("    4. Extract vehicle_id (UUID) từ vehicle đó\n");
                    sb.append("    5. Truyền vehicle_id đó vào createBooking()\n");
                }
            }
            sb.append(
                    "  → Ví dụ SAI: createBooking(vehicle_id='vehicle_id_cua_52S2-27069', vehicle_license_plate='null') → Placeholder, license_plate null\n");
            sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID hoặc placeholder\n");
        }
        if (state.hasDate()) {
            sb.append("- Đã có date_time (ngày đã được chọn)\n");
        }
        if (state.hasBranch()) {
            sb.append("- [CO] Đã có branch_id (chi nhánh đã được chọn)\n");
            sb.append("  → KHÔNG được gọi getBranches() lại\n");

            // Backend đã extract UUID, inject trực tiếp vào message
            if (state.getBranchId() != null) {
                sb.append("  → BACKEND ĐÃ EXTRACT UUID: branch_id = '").append(state.getBranchId()).append("'\n");
                sb.append("  → BACKEND ĐÃ EXTRACT: branch_name = '")
                        .append(state.getBranchName() != null ? state.getBranchName() : "N/A").append("'\n");
                sb.append("  → CRITICAL: Khi gọi checkAvailability(), BẮT BUỘC phải dùng branch_id này:\n");
                sb.append("    checkAvailability(branch_id='").append(state.getBranchId()).append("', branch_name='")
                        .append(state.getBranchName() != null ? state.getBranchName() : "").append("')\n");
                sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID khác, PHẢI dùng branch_id trên\n");
                sb.append(
                        "  → TUYỆT ĐỐI KHÔNG được dùng branch_id của chi nhánh khác (ví dụ: nếu đã chọn Gò Vấp thì KHÔNG được dùng branch_id của Cầu Giấy)\n");
            } else {
                // Fallback: Extract branch name từ history nếu chưa có
                String[] branchInfo = extractBranchInfoFromHistory(history);
                if (branchInfo != null && branchInfo[0] != null) {
                    sb.append("  → Chi nhánh đã chọn: ").append(branchInfo[0]).append("\n");
                    sb.append(
                            "  → Backend chưa extract được branch_id, bạn cần extract từ TOOL response getBranches()\n");
                    sb.append("  → CÁCH LẤY branch_id:\n");
                    sb.append("    1. Tìm TOOL response getBranches() GẦN NHẤT trong conversation history\n");
                    sb.append("    2. So sánh tên chi nhánh \"").append(branchInfo[0])
                            .append("\" với danh sách branches\n");
                    sb.append("    3. Tìm branch có branch_name match với \"").append(branchInfo[0]).append("\"\n");
                    sb.append("    4. Extract branch_id (UUID) từ branch đó\n");
                    sb.append("    5. Truyền branch_id đó vào checkAvailability()\n");
                }
                sb.append(
                        "  → Ví dụ SAI: checkAvailability(branch_id='b5d7f1d2-3f6a-4c7e-8118-8f6b0b89a2a5', branch_name='null') → UUID tự bịa ra\n");
                sb.append("  → Fallback: Nếu không tìm thấy branch_id → Truyền branch_name (KHÔNG được để null)\n");
                sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID hoặc placeholder\n");
            }
        }
        if (state.hasService()) {
            sb.append("- Đã có service_type (dịch vụ đã được chọn)\n");
        }
        if (state.hasBay()) {
            sb.append("- [CO] Đã có bay_id (bay đã được chọn)\n");
            sb.append("  → KHÔNG được gọi checkAvailability() lại để lấy bay\n");

            // Backend đã extract UUID, inject trực tiếp vào message
            if (state.getBayId() != null) {
                sb.append("  → BACKEND ĐÃ EXTRACT UUID: bay_id = '").append(state.getBayId()).append("'\n");
                sb.append("  → BACKEND ĐÃ EXTRACT: bay_name = '")
                        .append(state.getBayName() != null ? state.getBayName() : "N/A").append("'\n");
                sb.append("  → BẮT BUỘC: Khi gọi createBooking(), PHẢI dùng bay_id này:\n");
                sb.append("    createBooking(bay_id='").append(state.getBayId()).append("', bay_name='")
                        .append(state.getBayName() != null ? state.getBayName() : "").append("')\n");
                sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID khác, PHẢI dùng bay_id trên\n");
            } else {
                sb.append(
                        "  → Backend chưa extract được bay_id, bạn cần extract từ TOOL response checkAvailability()\n");
                if (state.getBayName() != null) {
                    sb.append("  → Tên bay đã chọn: '").append(state.getBayName()).append("'\n");
                    sb.append("  → CÁCH LẤY bay_id:\n");
                    sb.append("    1. Tìm TOOL response checkAvailability() GẦN NHẤT trong conversation history\n");
                    sb.append("    2. So sánh tên bay '").append(state.getBayName())
                            .append("' với danh sách available_bays\n");
                    sb.append("    3. Tìm bay có bay_name match\n");
                    sb.append("    4. Extract bay_id (UUID) từ bay đó\n");
                    sb.append("    5. Truyền bay_id đó vào createBooking()\n");
                }
            }
            sb.append(
                    "  → Ví dụ SAI: createBooking(bay_id='bay_id_cua_Gò_Vấp_Bay_1', bay_name='null') → Placeholder, bay_name null\n");
            sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID hoặc placeholder\n");
        }
        if (state.hasTime()) {
            sb.append("- Đã có time_slot (giờ đã được chọn)\n");
        }

        sb.append("\nQUY TẮC:\n");
        sb.append("- Nếu user nhắc lại thông tin đã có → CHỈ xác nhận lại, KHÔNG gọi function lại\n");
        sb.append("- Nếu đã có dữ liệu trong history → Dùng dữ liệu đó, KHÔNG hỏi lại\n");
        sb.append("- Nếu user nói lại biển số/chi nhánh/dịch vụ đã chọn → Xác nhận và tiếp tục bước tiếp theo\n");
        sb.append(
                "- Khi gọi checkAvailability() → PHẢI tìm branch_id trong TOOL response getBranches() GẦN NHẤT, KHÔNG được tự bịa ra UUID\n");

        return sb.toString();
    }

    /**
     * Inner class để track conversation state
     */
    private static class ConversationState {
        private boolean hasVehicle = false;
        private boolean hasDate = false;
        private boolean hasBranch = false;
        private boolean hasService = false;
        private boolean hasBay = false;
        private boolean hasTime = false;

        // UUID đã extract từ conversation history (backend-driven)
        private String vehicleId = null;
        private String vehicleLicensePlate = null;
        private String branchId = null;
        private String branchName = null;
        private String bayId = null;
        private String bayName = null;
        private LocalDateTime dateTime = null;
        private String serviceId = null;
        private String serviceType = null;
        private String timeSlot = null;

        public boolean hasVehicle() {
            return hasVehicle;
        }

        public void setHasVehicle(boolean hasVehicle) {
            this.hasVehicle = hasVehicle;
        }

        public boolean hasDate() {
            return hasDate;
        }

        public void setHasDate(boolean hasDate) {
            this.hasDate = hasDate;
        }

        public boolean hasBranch() {
            return hasBranch;
        }

        public void setHasBranch(boolean hasBranch) {
            this.hasBranch = hasBranch;
        }

        public boolean hasService() {
            return hasService;
        }

        public void setHasService(boolean hasService) {
            this.hasService = hasService;
        }

        public boolean hasBay() {
            return hasBay;
        }

        public void setHasBay(boolean hasBay) {
            this.hasBay = hasBay;
        }

        public boolean hasTime() {
            return hasTime;
        }

        public void setHasTime(boolean hasTime) {
            this.hasTime = hasTime;
        }

        public boolean hasAnyData() {
            return hasVehicle || hasDate || hasBranch || hasService || hasBay || hasTime;
        }

        // Getters và setters cho UUID
        public String getVehicleId() {
            return vehicleId;
        }

        public void setVehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
        }

        public String getVehicleLicensePlate() {
            return vehicleLicensePlate;
        }

        public void setVehicleLicensePlate(String vehicleLicensePlate) {
            this.vehicleLicensePlate = vehicleLicensePlate;
        }

        public String getBranchId() {
            return branchId;
        }

        public void setBranchId(String branchId) {
            this.branchId = branchId;
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        public String getBayId() {
            return bayId;
        }

        public void setBayId(String bayId) {
            this.bayId = bayId;
        }

        public String getBayName() {
            return bayName;
        }

        public void setBayName(String bayName) {
            this.bayName = bayName;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public String getTimeSlot() {
            return timeSlot;
        }

        public void setTimeSlot(String timeSlot) {
            this.timeSlot = timeSlot;
        }
    }
}
