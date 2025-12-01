package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.ChatRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.ChatResponse;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
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
import java.util.ArrayList;
import java.util.List;
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
    private final com.kltn.scsms_api_service.core.service.aiAssistant.AiBookingAssistantService aiBookingAssistantService;
    private final com.kltn.scsms_api_service.core.service.entityService.VehicleProfileService vehicleProfileService;
    private final com.kltn.scsms_api_service.core.service.entityService.ServiceBayService serviceBayService;

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
            // 0. Extract state từ conversation history để inject vào prompt
            ConversationState extractedState = extractStateFromHistory(request.getConversationHistory());
            log.info("Extracted state from history: vehicle={}, date={}, branch={}, service={}, bay={}, time={}",
                    extractedState.hasVehicle(), extractedState.hasDate(), extractedState.hasBranch(),
                    extractedState.hasService(), extractedState.hasBay(), extractedState.hasTime());
            
            // Log các ID đã extract được
            if (extractedState.getVehicleId() != null) {
                log.info("✅ EXTRACTED vehicle_id: {} (license_plate: {})", 
                        extractedState.getVehicleId(), 
                        extractedState.getVehicleLicensePlate() != null ? extractedState.getVehicleLicensePlate() : "N/A");
            }
            if (extractedState.getBranchId() != null) {
                log.info("✅ EXTRACTED branch_id: {} (branch_name: {})", 
                        extractedState.getBranchId(), 
                        extractedState.getBranchName() != null ? extractedState.getBranchName() : "N/A");
            }
            if (extractedState.getBayId() != null) {
                log.info("✅ EXTRACTED bay_id: {} (bay_name: {})", 
                        extractedState.getBayId(), 
                        extractedState.getBayName() != null ? extractedState.getBayName() : "N/A");
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
                    }
                }
            }
            
            // 1.1. Add state context message để AI biết dữ liệu đã có (ngăn lặp lại)
            if (extractedState.hasAnyData()) {
                String stateContext = buildStateContextMessage(extractedState, request.getConversationHistory());
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(stateContext));
                log.debug("Added state context to messages to prevent duplicate function calls");
            }

            // 1.2. CRITICAL: Nếu chưa có vehicle_id và user message là request đầu tiên
            // hoặc yêu cầu đặt lịch
            // → Tự động inject instruction để AI gọi getCustomerVehicles() NGAY
            String userMessage = request.getMessage();
            List<ChatRequest.ChatMessage> conversationHistory = request.getConversationHistory();
            if (!extractedState.hasVehicle() &&
                    (conversationHistory == null || conversationHistory.isEmpty() ||
                            userMessage.toLowerCase().contains("đặt lịch") ||
                            userMessage.toLowerCase().contains("muốn đặt"))) {
                String autoCallInstruction = """
                        CRITICAL INSTRUCTION - BẠN PHẢI LÀM NGAY:
                        - User chưa có vehicle_id và đang muốn đặt lịch
                        - BẠN PHẢI TỰ ĐỘNG GỌI getCustomerVehicles() NGAY LẬP TỨC
                        - KHÔNG nói "Tôi sẽ lấy danh sách xe" rồi chờ user yêu cầu
                        - PHẢI gọi function NGAY và hiển thị danh sách xe cho user chọn
                        """;
                messages.add(new org.springframework.ai.chat.messages.SystemMessage(autoCallInstruction));
                log.info("Injected auto-call instruction for getCustomerVehicles()");
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
            
            // 7. Build response
            ChatResponse response = ChatResponse.builder()
                .message(aiMessage)
                .functionsCalled(functionsCalled)
                .requiresAction(requiresAction)
                .actionType(actionType)
                .build();
            
            long requestEndTime = System.currentTimeMillis();
            log.info("=== AI RESPONSE ===: {}", aiMessage);
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
        }
    }

    /**
     * Extract state từ conversation history để biết dữ liệu đã có
     */
    private ConversationState extractStateFromHistory(List<ChatRequest.ChatMessage> history) {
        ConversationState state = new ConversationState();

        if (history == null || history.isEmpty()) {
            return state;
        }

        // Pattern để detect các thông tin đã có
        // QUAN TRỌNG: Chỉ detect khi THỰC SỰ đã chọn, không phải chỉ nói về
        // Pattern cải thiện: Chỉ match biển số thực tế (có ít nhất 3 ký tự, có số và chữ)
        // Format: 52S2-27069, 69A-56789, 77AC-33697, etc.
        Pattern vehiclePattern = Pattern.compile(
                "(?:Bạn đã chọn xe|chọn xe|xe với biển số|xe số|biển số)\\s+([A-Z0-9]{2,}[\\-\\s]?[A-Z0-9]{2,})",
                Pattern.CASE_INSENSITIVE);
        Pattern datePattern = Pattern.compile("(?:Bạn đã chọn ngày|đặt lịch vào ngày|ngày)\\s+([0-9/\\-]+|mai|hôm nay)",
                Pattern.CASE_INSENSITIVE);
        // Pattern để extract branch name - CHỈ match khi THỰC SỰ đã chọn, KHÔNG match câu hỏi
        // Pattern 1: "Bạn đã chọn chi nhánh X" (từ assistant response) - CHỈ match "đã chọn"
        // Pattern 2: "Chọn chi nhánh X" (từ user message khi chọn) - CHỈ match "Chọn" (không có "muốn")
        // Pattern 3: "Tôi muốn chọn chi nhánh X" (từ user message) - CHỈ match "muốn chọn" (không phải "muốn chọn nào")
        // KHÔNG match: "Bạn muốn chọn chi nhánh nào?" (câu hỏi - có "muốn" nhưng không có "đã")
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
                            log.warn("Invalid license plate format detected: '{}' (too short or missing letters/numbers)", licensePlate);
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
                                        (contentLower.contains("nào") || contentLower.contains("gì") || contentLower.contains("đâu"));
                    
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
                                log.debug("Detected branch selection from pattern: branch_name={}", extractedBranchName);
                            } else {
                                extractedBranchName = null; // Invalid, try fallback
                            }
                        }
                    }
                    
                    // Fallback: extract từ các pattern khác nếu pattern matching fail
                    if (extractedBranchName == null && !isQuestion) {
                        if (content.contains("Gò Vấp") || content.contains("gò vấp") || content.contains("Gò vấp") ||
                            content.contains("go vap") || content.contains("Go Vap") || content.contains("gò vâsp")) {
                            state.setBranchName("Chi Nhánh Gò Vấp");
                            log.debug("Detected branch selection from fallback: branch_name=Chi Nhánh Gò Vấp");
                        } else if (content.contains("Cầu Giấy") || content.contains("cầu giấy") || content.contains("Cầu giấy") ||
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
                            Pattern goVapBayPattern = Pattern.compile("(?:Gò Vấp Bay|Bay)\\s*([0-9]+)", Pattern.CASE_INSENSITIVE);
                            Matcher goVapMatcher = goVapBayPattern.matcher(content);
                            if (goVapMatcher.find()) {
                                state.setBayName("Gò Vấp Bay " + goVapMatcher.group(1));
                            } else {
                                state.setBayName("Gò Vấp Bay 1"); // Default
                            }
                        } else if (content.contains("Cầu Giấy Bay")) {
                            Pattern cauGiayBayPattern = Pattern.compile("(?:Cầu Giấy Bay|Bay)\\s*([0-9]+)", Pattern.CASE_INSENSITIVE);
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

        // BƯỚC 2: Extract UUID từ database dựa trên license plate và branch name đã tìm được
        extractUuidsFromDatabase(state, history);
        
        // Log extracted IDs sau khi extract
        if (state.getVehicleId() != null) {
            log.info("✅ EXTRACTED vehicle_id: {} (license_plate: {})", 
                    state.getVehicleId(), state.getVehicleLicensePlate());
        }
        if (state.getBranchId() != null) {
            log.info("✅ EXTRACTED branch_id: {} (branch_name: {})", 
                    state.getBranchId(), state.getBranchName());
        }
        if (state.getBayId() != null) {
            log.info("✅ EXTRACTED bay_id: {} (bay_name: {})", 
                    state.getBayId(), state.getBayName());
        }
        
        return state;
    }
    
    /**
     * Extract UUID từ database dựa trên license plate và branch name đã extract từ conversation history
     * Đây là giải pháp backend-driven: Backend tự động query database để lấy UUID, không phụ thuộc vào AI
     */
    private void extractUuidsFromDatabase(ConversationState state, List<ChatRequest.ChatMessage> history) {
        // Extract vehicle_id từ license plate
        if (state.hasVehicle() && state.getVehicleLicensePlate() != null) {
            try {
                // Lấy ownerId từ SecurityContext
                com.kltn.scsms_api_service.core.dto.token.LoginUserInfo currentUser = 
                    com.kltn.scsms_api_service.core.utils.PermissionUtils.getCurrentUser();
                
                if (currentUser != null && currentUser.getSub() != null) {
                    UUID ownerId = UUID.fromString(currentUser.getSub());
                    
                    // Query database để lấy vehicle_id
                    com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleProfileFilterParam filterParam = 
                        new com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleProfileFilterParam();
                    filterParam.setPage(0);
                    filterParam.setSize(1000);
                    filterParam.setOwnerId(ownerId);
                    
                    java.util.Optional<com.kltn.scsms_api_service.core.entity.VehicleProfile> vehicle = 
                        vehicleProfileService.getAllVehicleProfilesByOwnerIdWithFilters(ownerId, filterParam)
                        .getContent()
                        .stream()
                        .filter(v -> v.getLicensePlate() != null && 
                                    (v.getLicensePlate().equalsIgnoreCase(state.getVehicleLicensePlate()) ||
                                     v.getLicensePlate().replaceAll("\\s+", "").equalsIgnoreCase(state.getVehicleLicensePlate().replaceAll("\\s+", ""))) &&
                                    !v.getIsDeleted() &&
                                    v.getIsActive())
                        .findFirst();
                    
                    if (vehicle.isPresent()) {
                        state.setVehicleId(vehicle.get().getVehicleId().toString());
                        log.info("✅ Backend extracted vehicle_id: {} for license_plate: {} (ownerId: {})", 
                                state.getVehicleId(), state.getVehicleLicensePlate(), ownerId);
                    } else {
                        log.warn("⚠️ Cannot find vehicle_id for license_plate: {} (ownerId: {})", 
                                state.getVehicleLicensePlate(), ownerId);
                    }
                }
            } catch (Exception e) {
                log.error("Error extracting vehicle_id from database: {}", e.getMessage(), e);
            }
        }
        
        // Extract branch_id từ branch name
        if (state.hasBranch() && state.getBranchName() != null) {
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
                    log.info("✅ Backend extracted branch_id: {} for branch_name: {} (normalized from: {})", 
                            state.getBranchId(), branch.getBranchName(), state.getBranchName());
                } else {
                    log.warn("⚠️ Cannot find branch_id for branch_name: {} (normalized: {})", 
                            state.getBranchName(), normalizedBranchName);
                }
            } catch (Exception e) {
                log.error("Error extracting branch_id from database: {}", e.getMessage(), e);
            }
        }
        
        // Extract bay_id từ bay name và branch_id
        if (state.hasBay() && state.getBayName() != null && state.getBranchId() != null) {
            try {
                UUID branchId = UUID.fromString(state.getBranchId());
                
                // Tìm bay theo tên trong branch đã chọn
                List<com.kltn.scsms_api_service.core.entity.ServiceBay> baysInBranch = 
                    serviceBayService.getByBranch(branchId);
                
                com.kltn.scsms_api_service.core.entity.ServiceBay foundBay = baysInBranch.stream()
                        .filter(bay -> bay.getBayName() != null && 
                                (bay.getBayName().equalsIgnoreCase(state.getBayName()) ||
                                 bay.getBayName().replaceAll("\\s+", "").equalsIgnoreCase(state.getBayName().replaceAll("\\s+", ""))) &&
                                !bay.getIsDeleted() &&
                                bay.getIsActive())
                        .findFirst()
                        .orElse(null);
                
                if (foundBay == null) {
                    // Thử search by keyword nếu exact match không tìm thấy
                    List<com.kltn.scsms_api_service.core.entity.ServiceBay> bays = 
                        serviceBayService.searchByKeywordInBranch(branchId, state.getBayName());
                    if (!bays.isEmpty()) {
                        foundBay = bays.get(0);
                    }
                }
                
                if (foundBay != null) {
                    state.setBayId(foundBay.getBayId().toString());
                    // Update bay name với tên chính xác từ database
                    state.setBayName(foundBay.getBayName());
                    log.info("✅ Backend extracted bay_id: {} for bay_name: {} (branch_id: {})", 
                            state.getBayId(), foundBay.getBayName(), branchId);
                } else {
                    log.warn("⚠️ Cannot find bay_id for bay_name: {} in branch_id: {}", 
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
                } else if (content.contains("Cầu Giấy") || content.contains("cầu giấy") || content.contains("Cầu giấy")) {
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
            
            // Pattern 2: "Chọn chi nhánh X" hoặc "Tôi muốn chọn chi nhánh X" (từ user message)
            if (content.contains("Chọn chi nhánh") || content.contains("chọn chi nhánh") ||
                content.contains("Tôi muốn chọn chi nhánh") || content.contains("tôi muốn chọn chi nhánh")) {
                if (content.contains("Gò Vấp") || content.contains("gò vấp") || content.contains("Gò vấp") ||
                    content.contains("go vap") || content.contains("Go Vap")) {
                    selectedBranchName = "Chi Nhánh Gò Vấp";
                } else if (content.contains("Cầu Giấy") || content.contains("cầu giấy") || content.contains("Cầu giấy") ||
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

        return selectedBranchName != null ? new String[] { selectedBranchName, null } : null;
    }

    /**
     * Build state context message để inject vào prompt
     */
    private String buildStateContextMessage(ConversationState state, List<ChatRequest.ChatMessage> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("THÔNG TIN ĐÃ CÓ TRONG CONVERSATION HISTORY (KHÔNG CẦN HỎI LẠI):\n\n");

        if (state.hasVehicle()) {
            sb.append("- ✅ Đã có vehicle_id (xe đã được chọn)\n");
            sb.append("  → KHÔNG được gọi getCustomerVehicles() lại\n");
            
            // Backend đã extract UUID, inject trực tiếp vào message
            if (state.getVehicleId() != null) {
                sb.append("  → 🎯 BACKEND ĐÃ EXTRACT UUID: vehicle_id = '").append(state.getVehicleId()).append("'\n");
                sb.append("  → 🎯 BACKEND ĐÃ EXTRACT: vehicle_license_plate = '").append(state.getVehicleLicensePlate() != null ? state.getVehicleLicensePlate() : "N/A").append("'\n");
                sb.append("  → BẮT BUỘC: Khi gọi createBooking(), PHẢI dùng vehicle_id này:\n");
                sb.append("    createBooking(vehicle_id='").append(state.getVehicleId()).append("', vehicle_license_plate='").append(state.getVehicleLicensePlate() != null ? state.getVehicleLicensePlate() : "").append("')\n");
                sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID khác, PHẢI dùng vehicle_id trên\n");
            } else {
                sb.append("  → ⚠️ Backend chưa extract được vehicle_id, bạn cần extract từ TOOL response getCustomerVehicles()\n");
                if (state.getVehicleLicensePlate() != null) {
                    sb.append("  → Biển số xe đã chọn: '").append(state.getVehicleLicensePlate()).append("'\n");
                    sb.append("  → CÁCH LẤY vehicle_id:\n");
                    sb.append("    1. Tìm TOOL response getCustomerVehicles() GẦN NHẤT trong conversation history\n");
                    sb.append("    2. So sánh biển số '").append(state.getVehicleLicensePlate()).append("' với danh sách vehicles\n");
                    sb.append("    3. Tìm vehicle có license_plate match\n");
                    sb.append("    4. Extract vehicle_id (UUID) từ vehicle đó\n");
                    sb.append("    5. Truyền vehicle_id đó vào createBooking()\n");
                }
            }
            sb.append("  → Ví dụ SAI: createBooking(vehicle_id='vehicle_id_cua_52S2-27069', vehicle_license_plate='null') → Placeholder, license_plate null\n");
            sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID hoặc placeholder\n");
        }
        if (state.hasDate()) {
            sb.append("- Đã có date_time (ngày đã được chọn)\n");
        }
        if (state.hasBranch()) {
            sb.append("- ✅ Đã có branch_id (chi nhánh đã được chọn)\n");
            sb.append("  → KHÔNG được gọi getBranches() lại\n");
            
            // Backend đã extract UUID, inject trực tiếp vào message
            if (state.getBranchId() != null) {
                sb.append("  → 🎯 BACKEND ĐÃ EXTRACT UUID: branch_id = '").append(state.getBranchId()).append("'\n");
                sb.append("  → 🎯 BACKEND ĐÃ EXTRACT: branch_name = '").append(state.getBranchName() != null ? state.getBranchName() : "N/A").append("'\n");
                sb.append("  → 🚨 CRITICAL: Khi gọi checkAvailability(), BẮT BUỘC phải dùng branch_id này:\n");
                sb.append("    checkAvailability(branch_id='").append(state.getBranchId()).append("', branch_name='").append(state.getBranchName() != null ? state.getBranchName() : "").append("')\n");
                sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID khác, PHẢI dùng branch_id trên\n");
                sb.append("  → TUYỆT ĐỐI KHÔNG được dùng branch_id của chi nhánh khác (ví dụ: nếu đã chọn Gò Vấp thì KHÔNG được dùng branch_id của Cầu Giấy)\n");
            } else {
                // Fallback: Extract branch name từ history nếu chưa có
                String[] branchInfo = extractBranchInfoFromHistory(history);
                if (branchInfo != null && branchInfo[0] != null) {
                    sb.append("  → Chi nhánh đã chọn: ").append(branchInfo[0]).append("\n");
                    sb.append("  → ⚠️ Backend chưa extract được branch_id, bạn cần extract từ TOOL response getBranches()\n");
                    sb.append("  → CÁCH LẤY branch_id:\n");
                    sb.append("    1. Tìm TOOL response getBranches() GẦN NHẤT trong conversation history\n");
                    sb.append("    2. So sánh tên chi nhánh \"").append(branchInfo[0]).append("\" với danh sách branches\n");
                    sb.append("    3. Tìm branch có branch_name match với \"").append(branchInfo[0]).append("\"\n");
                    sb.append("    4. Extract branch_id (UUID) từ branch đó\n");
                    sb.append("    5. Truyền branch_id đó vào checkAvailability()\n");
                }
                sb.append("  → Ví dụ SAI: checkAvailability(branch_id='b5d7f1d2-3f6a-4c7e-8118-8f6b0b89a2a5', branch_name='null') → UUID tự bịa ra\n");
                sb.append("  → Fallback: Nếu không tìm thấy branch_id → Truyền branch_name (KHÔNG được để null)\n");
                sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID hoặc placeholder\n");
            }
        }
        if (state.hasService()) {
            sb.append("- Đã có service_type (dịch vụ đã được chọn)\n");
        }
        if (state.hasBay()) {
            sb.append("- ✅ Đã có bay_id (bay đã được chọn)\n");
            sb.append("  → KHÔNG được gọi checkAvailability() lại để lấy bay\n");
            
            // Backend đã extract UUID, inject trực tiếp vào message
            if (state.getBayId() != null) {
                sb.append("  → 🎯 BACKEND ĐÃ EXTRACT UUID: bay_id = '").append(state.getBayId()).append("'\n");
                sb.append("  → 🎯 BACKEND ĐÃ EXTRACT: bay_name = '").append(state.getBayName() != null ? state.getBayName() : "N/A").append("'\n");
                sb.append("  → BẮT BUỘC: Khi gọi createBooking(), PHẢI dùng bay_id này:\n");
                sb.append("    createBooking(bay_id='").append(state.getBayId()).append("', bay_name='").append(state.getBayName() != null ? state.getBayName() : "").append("')\n");
                sb.append("  → TUYỆT ĐỐI KHÔNG được tự bịa ra UUID khác, PHẢI dùng bay_id trên\n");
            } else {
                sb.append("  → ⚠️ Backend chưa extract được bay_id, bạn cần extract từ TOOL response checkAvailability()\n");
                if (state.getBayName() != null) {
                    sb.append("  → Tên bay đã chọn: '").append(state.getBayName()).append("'\n");
                    sb.append("  → CÁCH LẤY bay_id:\n");
                    sb.append("    1. Tìm TOOL response checkAvailability() GẦN NHẤT trong conversation history\n");
                    sb.append("    2. So sánh tên bay '").append(state.getBayName()).append("' với danh sách available_bays\n");
                    sb.append("    3. Tìm bay có bay_name match\n");
                    sb.append("    4. Extract bay_id (UUID) từ bay đó\n");
                    sb.append("    5. Truyền bay_id đó vào createBooking()\n");
                }
            }
            sb.append("  → Ví dụ SAI: createBooking(bay_id='bay_id_cua_Gò_Vấp_Bay_1', bay_name='null') → Placeholder, bay_name null\n");
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
        public String getVehicleId() { return vehicleId; }
        public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
        public String getVehicleLicensePlate() { return vehicleLicensePlate; }
        public void setVehicleLicensePlate(String vehicleLicensePlate) { this.vehicleLicensePlate = vehicleLicensePlate; }
        public String getBranchId() { return branchId; }
        public void setBranchId(String branchId) { this.branchId = branchId; }
        public String getBranchName() { return branchName; }
        public void setBranchName(String branchName) { this.branchName = branchName; }
        public String getBayId() { return bayId; }
        public void setBayId(String bayId) { this.bayId = bayId; }
        public String getBayName() { return bayName; }
        public void setBayName(String bayName) { this.bayName = bayName; }
    }
}
