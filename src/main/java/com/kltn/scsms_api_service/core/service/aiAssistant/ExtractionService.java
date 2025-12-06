package com.kltn.scsms_api_service.core.service.aiAssistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.ChatRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.ExtractSelectionRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.ExtractSelectionResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetBranchesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetCustomerVehiclesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetServicesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.AvailabilityResponse;
import com.kltn.scsms_api_service.core.utils.DraftContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service xử lý AI-powered extraction và validation
 * Sử dụng AI để extract lựa chọn từ user message một cách thông minh
 */
@Service
@Slf4j
public class ExtractionService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public ExtractionService(
            ChatModel chatModel,
            ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    /**
     * Extract lựa chọn từ user message sử dụng AI
     * 
     * @param request ExtractSelectionRequest với user message, draft context, và available options
     * @return ExtractSelectionResponse với extracted data và validation results
     */
    public ExtractSelectionResponse extractUserSelection(ExtractSelectionRequest request) {
        log.info("=== AI EXTRACTION START ===");
        log.info("User message: {}", request.getUserMessage());
        log.info("Current step: {}", request.getCurrentStep());
        log.info("Draft context: hasVehicle={}, hasDate={}, hasBranch={}, hasService={}, hasBay={}, hasTime={}",
                request.getDraftContext().getHasVehicle(),
                request.getDraftContext().getHasDate(),
                request.getDraftContext().getHasBranch(),
                request.getDraftContext().getHasService(),
                request.getDraftContext().getHasBay(),
                request.getDraftContext().getHasTime());
        
        // Log available options để debug
        if (request.getAvailableOptions() != null) {
            int vehiclesCount = request.getAvailableOptions().getVehicles() != null ? 
                    request.getAvailableOptions().getVehicles().size() : 0;
            int branchesCount = request.getAvailableOptions().getBranches() != null ? 
                    request.getAvailableOptions().getBranches().size() : 0;
            int servicesCount = request.getAvailableOptions().getServices() != null ? 
                    request.getAvailableOptions().getServices().size() : 0;
            log.info("Available options: vehicles={}, branches={}, services={}", 
                    vehiclesCount, branchesCount, servicesCount);
        } else {
            log.warn("Available options is NULL - AI extraction will rely on confidence scoring");
        }

        try {
            // Step 1: Build extraction prompt với JSON schema rõ ràng
            String extractionPrompt = buildExtractionPrompt(request);

            // Step 2: Call AI để extract
            // Tạo ChatClient riêng cho extraction (không có functions)
            // KHÔNG có functions → AI sẽ không tự gọi functions, chỉ trả về JSON
            ChatClient extractionClient = ChatClient.builder(chatModel)
                    .defaultAdvisors(new org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor())
                    // KHÔNG có defaultFunctions - AI sẽ không tự gọi functions
                    .build();
            
            // Set options: Lower temperature cho extraction chính xác hơn
            // Note: Prompt đã được cải thiện để yêu cầu JSON format rõ ràng
            // Nếu Spring AI M5 hỗ trợ response_format, có thể thêm sau
            // Note: withTemperature() deprecated in M5, using default temperature
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    // Temperature sẽ dùng default từ application.yml (0.7)
                    // Có thể override trong application.yml nếu cần
                    .build();
            
            ChatResponse aiResponse = extractionClient.prompt()
                    .user(extractionPrompt)
                    .options(options)
                    .call()
                    .chatResponse();

            String aiResponseText = aiResponse.getResult().getOutput().getContent();
            log.info("AI extraction response (raw): {}", aiResponseText);

            // Step 3: Parse AI response thành structured data
            ExtractSelectionResponse.ExtractedData extractedData = parseAIResponse(aiResponseText, request);

            // Step 4: Validate extracted data với available options
            ValidationResult validation = validateExtraction(extractedData, request);

            // Step 5: Build response
            if (validation.getConfidence() >= 0.8 && validation.getErrors().isEmpty()) {
                log.info("Extraction successful with confidence: {}", validation.getConfidence());
                return ExtractSelectionResponse.success(extractedData, validation.getConfidence());
            } else {
                log.warn("Extraction needs clarification. Confidence: {}, Errors: {}", 
                        validation.getConfidence(), validation.getErrors());
                return ExtractSelectionResponse.needsClarification(
                        buildClarificationMessage(validation),
                        validation.getErrors()
                );
            }

        } catch (Exception e) {
            log.error("Error during AI extraction: {}", e.getMessage(), e);
            return ExtractSelectionResponse.needsClarification(
                    "Xin lỗi, tôi không thể hiểu được lựa chọn của bạn. Vui lòng thử lại.",
                    List.of("AI extraction error: " + e.getMessage())
            );
        }
    }

    /**
     * Build extraction prompt cho AI
     */
    private String buildExtractionPrompt(ExtractSelectionRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        // CRITICAL: Thêm current date context để AI biết "ngày mai", "hôm nay" là ngày nào
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        prompt.append("Bạn là AI assistant chuyên extract lựa chọn từ user message trong quy trình đặt lịch.\n\n");
        prompt.append("THÔNG TIN THỜI GIAN (QUAN TRỌNG - DÙNG ĐỂ EXTRACT DATE):\n");
        prompt.append("- Hôm nay: ").append(today.format(dateFormatter)).append(" (").append(today.format(DateTimeFormatter.ISO_LOCAL_DATE)).append(")\n");
        prompt.append("- Ngày mai: ").append(today.plusDays(1).format(dateFormatter)).append(" (").append(today.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)).append(")\n");
        prompt.append("- Ngày kia: ").append(today.plusDays(2).format(dateFormatter)).append(" (").append(today.plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)).append(")\n");
        prompt.append("- Thời gian hiện tại: ").append(now.format(dateTimeFormatter)).append("\n");
        prompt.append("- Năm hiện tại: ").append(today.getYear()).append("\n\n");
        
        prompt.append("THÔNG TIN HIỆN TẠI:\n");
        prompt.append("- Bước hiện tại: ").append(request.getCurrentStep()).append("\n");
        prompt.append("- Draft context:\n");
        prompt.append("  + Vehicle: ").append(request.getDraftContext().getHasVehicle() ? "Đã có" : "Chưa có").append("\n");
        prompt.append("  + Date: ").append(request.getDraftContext().getHasDate() ? "Đã có" : "Chưa có").append("\n");
        prompt.append("  + Branch: ").append(request.getDraftContext().getHasBranch() ? "Đã có" : "Chưa có").append("\n");
        prompt.append("  + Service: ").append(request.getDraftContext().getHasService() ? "Đã có" : "Chưa có").append("\n");
        prompt.append("  + Bay: ").append(request.getDraftContext().getHasBay() ? "Đã có" : "Chưa có").append("\n");
        prompt.append("  + Time: ").append(request.getDraftContext().getHasTime() ? "Đã có" : "Chưa có").append("\n");
        
        // Add available options
        if (request.getAvailableOptions() != null) {
            prompt.append("\nCÁC LỰA CHỌN CÓ SẴN:\n");
            
            if (request.getAvailableOptions().getVehicles() != null && !request.getAvailableOptions().getVehicles().isEmpty()) {
                prompt.append("- Vehicles:\n");
                for (int i = 0; i < request.getAvailableOptions().getVehicles().size(); i++) {
                    ExtractSelectionRequest.VehicleOption v = request.getAvailableOptions().getVehicles().get(i);
                    prompt.append(String.format("  %d. %s (ID: %s)\n", 
                            i + 1, v.getLicensePlate(), v.getVehicleId()));
                }
            }
            
            if (request.getAvailableOptions().getBranches() != null && !request.getAvailableOptions().getBranches().isEmpty()) {
                prompt.append("- Branches:\n");
                for (int i = 0; i < request.getAvailableOptions().getBranches().size(); i++) {
                    ExtractSelectionRequest.BranchOption b = request.getAvailableOptions().getBranches().get(i);
                    prompt.append(String.format("  %d. %s - %s (ID: %s)\n", 
                            i + 1, b.getBranchName(), b.getAddress(), b.getBranchId()));
                }
            }
            
            if (request.getAvailableOptions().getServices() != null && !request.getAvailableOptions().getServices().isEmpty()) {
                prompt.append("- Services:\n");
                for (int i = 0; i < request.getAvailableOptions().getServices().size(); i++) {
                    ExtractSelectionRequest.ServiceOption s = request.getAvailableOptions().getServices().get(i);
                    prompt.append(String.format("  %d. %s (ID: %s)\n", 
                            i + 1, s.getServiceName(), s.getServiceId()));
                }
            }
            
            if (request.getAvailableOptions().getBays() != null && !request.getAvailableOptions().getBays().isEmpty()) {
                prompt.append("- Bays:\n");
                for (int i = 0; i < request.getAvailableOptions().getBays().size(); i++) {
                    ExtractSelectionRequest.BayOption bay = request.getAvailableOptions().getBays().get(i);
                    prompt.append(String.format("  %d. %s (ID: %s)\n", 
                            i + 1, bay.getBayName(), bay.getBayId()));
                }
            }
            
            if (request.getAvailableOptions().getTimeSlots() != null && !request.getAvailableOptions().getTimeSlots().isEmpty()) {
                prompt.append("- Time slots:\n");
                for (int i = 0; i < request.getAvailableOptions().getTimeSlots().size(); i++) {
                    prompt.append(String.format("  %d. %s\n", i + 1, request.getAvailableOptions().getTimeSlots().get(i)));
                }
            }
        }
        
        prompt.append("\nUSER MESSAGE: ").append(request.getUserMessage()).append("\n\n");
        
        prompt.append("NHIỆM VỤ CỦA BẠN:\n");
        prompt.append("1. Phân tích user message và extract lựa chọn\n");
        prompt.append("2. Xác định intent: SELECT (chọn mới), CHANGE (thay đổi), CONFIRM (xác nhận), CANCEL (hủy)\n");
        prompt.append("3. Extract dữ liệu phù hợp với bước hiện tại\n");
        prompt.append("4. Match với available options (nếu có)\n");
        prompt.append("5. BẠN PHẢI TRẢ VỀ JSON FORMAT - KHÔNG được trả về text thông thường\n\n");
        
        prompt.append("JSON SCHEMA (BẮT BUỘC PHẢI TUÂN THỦ):\n");
        prompt.append("{\n");
        prompt.append("  \"intent\": \"SELECT|CHANGE|CONFIRM|CANCEL\",\n");
        prompt.append("  \"vehicle\": {\n");
        prompt.append("    \"license_plate\": \"string hoặc null\",\n");
        prompt.append("    \"vehicle_id\": \"UUID string hoặc null\",\n");
        prompt.append("    \"selection_type\": \"LICENSE_PLATE|INDEX|DESCRIPTION hoặc null\",\n");
        prompt.append("    \"raw_text\": \"string hoặc null\",\n");
        prompt.append("    \"confidence\": 0.0-1.0\n");
        prompt.append("  } hoặc null,\n");
        prompt.append("  \"date\": {\n");
        prompt.append("    \"date_time\": \"ISO 8601 string (YYYY-MM-DDTHH:mm:ss) hoặc null\",\n");
        prompt.append("    \"raw_text\": \"string hoặc null\",\n");
        prompt.append("    \"confidence\": 0.0-1.0\n");
        prompt.append("  } hoặc null,\n");
        prompt.append("\n");
        prompt.append("VÍ DỤ VỀ DATE EXTRACTION (QUAN TRỌNG):\n");
        prompt.append("- \"Ngày mai\" → ").append(today.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)).append("T00:00:00\n");
        prompt.append("- \"Hôm nay\" → ").append(today.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("T00:00:00\n");
        prompt.append("- \"Ngày kia\" → ").append(today.plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)).append("T00:00:00\n");
        prompt.append("- \"07/12\" → ").append(today.getYear()).append("-12-07T00:00:00 (năm hiện tại: ").append(today.getYear()).append(")\n");
        prompt.append("- \"07/12/2025\" → 2025-12-07T00:00:00\n");
        prompt.append("- \"Thứ 2 tuần sau\" → Tính từ hôm nay, tìm thứ 2 tiếp theo\n");
        prompt.append("\n");
        prompt.append("  \"branch\": {\n");
        prompt.append("    \"branch_name\": \"string hoặc null\",\n");
        prompt.append("    \"branch_id\": \"UUID string hoặc null\",\n");
        prompt.append("    \"selection_type\": \"NAME|INDEX|ADDRESS hoặc null\",\n");
        prompt.append("    \"raw_text\": \"string hoặc null\",\n");
        prompt.append("    \"confidence\": 0.0-1.0\n");
        prompt.append("  } hoặc null,\n");
        prompt.append("  \"service\": {\n");
        prompt.append("    \"service_name\": \"string hoặc null\",\n");
        prompt.append("    \"service_id\": \"UUID string hoặc null\",\n");
        prompt.append("    \"selection_type\": \"NAME|INDEX|KEYWORD hoặc null\",\n");
        prompt.append("    \"raw_text\": \"string hoặc null\",\n");
        prompt.append("    \"confidence\": 0.0-1.0\n");
        prompt.append("  } hoặc null,\n");
        prompt.append("  \"bay\": {\n");
        prompt.append("    \"bay_name\": \"string hoặc null\",\n");
        prompt.append("    \"bay_id\": \"UUID string hoặc null\",\n");
        prompt.append("    \"selection_type\": \"NAME|INDEX hoặc null\",\n");
        prompt.append("    \"raw_text\": \"string hoặc null\",\n");
        prompt.append("    \"confidence\": 0.0-1.0\n");
        prompt.append("  } hoặc null,\n");
        prompt.append("  \"time\": {\n");
        prompt.append("    \"time_slot\": \"string (HH:mm) hoặc null\",\n");
        prompt.append("    \"raw_text\": \"string hoặc null\",\n");
        prompt.append("    \"confidence\": 0.0-1.0\n");
        prompt.append("  } hoặc null\n");
        prompt.append("}\n\n");
        
        prompt.append("QUAN TRỌNG NHẤT:\n");
        prompt.append("- BẠN PHẢI TRẢ VỀ JSON FORMAT - KHÔNG được trả về text thông thường\n");
        prompt.append("- KHÔNG được có comment (// hoặc /* */) trong JSON\n");
        prompt.append("- UUID phải là string hợp lệ hoặc null, KHÔNG được là empty string hoặc placeholder\n");
        prompt.append("- Chỉ extract field phù hợp với bước hiện tại\n");
        prompt.append("- Nếu user nói \"xe đầu tiên\" → dùng INDEX và match với vehicles[0], vehicle_id = vehicles[0].vehicle_id\n");
        prompt.append("- Nếu user nói \"chi nhánh Phú Nhuận\" → branch_name = \"Chi nhánh Phú Nhuận\", branch_id = match với available branches\n");
        prompt.append("- Nếu user nói \"chi nhánh thứ 2\" → INDEX = 2, branch_id = branches[1].branch_id\n");
        prompt.append("- Confidence >= 0.8 mới được chấp nhận\n");
        prompt.append("- Nếu không chắc chắn → để confidence < 0.8\n");
        prompt.append("- Nếu không extract được field nào → để null, KHÔNG được để empty string\n");
        prompt.append("\n");
        prompt.append("PHÂN BIỆT DATE VÀ TIME (QUAN TRỌNG):\n");
        prompt.append("- \"date\" (date_time): NGÀY đặt lịch (ví dụ: \"Ngày mai\", \"07/12\", \"2025-12-07T00:00:00\")\n");
        prompt.append("- \"time\" (time_slot): GIỜ đặt lịch trong ngày (ví dụ: \"08:00\", \"13:30\", \"14:00\")\n");
        prompt.append("- Nếu user nói \"thay đổi ngày\" hoặc \"đổi ngày\" → extract vào \"date\" (date_time)\n");
        prompt.append("- Nếu user nói \"thay đổi giờ\" hoặc \"đổi giờ\" hoặc \"đổi giờ đặt lịch\" → extract vào \"time\" (time_slot)\n");
        prompt.append("- Nếu user nói \"thay đổi thời gian\" (không rõ ngày hay giờ) → để tất cả null, confidence < 0.8\n");
        prompt.append("- Nếu user nói \"08:00\" hoặc \"13:30\" → extract vào \"time\" (time_slot), KHÔNG extract vào \"date\"\n");
        prompt.append("- Nếu user nói \"Ngày mai\" hoặc \"07/12\" → extract vào \"date\" (date_time), KHÔNG extract vào \"time\"\n");
        
        return prompt.toString();
    }

    /**
     * Parse AI response thành ExtractedData
     */
    private ExtractSelectionResponse.ExtractedData parseAIResponse(
            String aiResponseText, 
            ExtractSelectionRequest request) {
        
        try {
            // Try to parse JSON from AI response
            String jsonText = extractJsonFromResponse(aiResponseText);
            JsonNode jsonNode = objectMapper.readTree(jsonText);
            
            ExtractSelectionResponse.ExtractedData.ExtractedDataBuilder builder = 
                    ExtractSelectionResponse.ExtractedData.builder();
            
            // Parse intent
            if (jsonNode.has("intent")) {
                builder.intent(jsonNode.get("intent").asText());
            }
            
            // Parse vehicle
            if (jsonNode.has("vehicle") && !jsonNode.get("vehicle").isNull()) {
                JsonNode vehicleNode = jsonNode.get("vehicle");
                UUID vehicleId = null;
                if (vehicleNode.has("vehicle_id") && !vehicleNode.get("vehicle_id").isNull()) {
                    String vehicleIdStr = vehicleNode.get("vehicle_id").asText().trim();
                    if (!vehicleIdStr.isEmpty() && !vehicleIdStr.equals("null") && 
                        !vehicleIdStr.contains("placeholder") && isValidUUID(vehicleIdStr)) {
                        try {
                            vehicleId = UUID.fromString(vehicleIdStr);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid vehicle_id format: {}", vehicleIdStr);
                        }
                    }
                }
                ExtractSelectionResponse.VehicleSelection vehicle = 
                        ExtractSelectionResponse.VehicleSelection.builder()
                                .licensePlate(vehicleNode.has("license_plate") ? vehicleNode.get("license_plate").asText() : null)
                                .vehicleId(vehicleId)
                                .selectionType(vehicleNode.has("selection_type") ? vehicleNode.get("selection_type").asText() : null)
                                .rawText(vehicleNode.has("raw_text") ? vehicleNode.get("raw_text").asText() : null)
                                .confidence(vehicleNode.has("confidence") ? vehicleNode.get("confidence").asDouble() : 0.0)
                                .build();
                builder.vehicle(vehicle);
            }
            
            // Parse date
            if (jsonNode.has("date") && !jsonNode.get("date").isNull()) {
                JsonNode dateNode = jsonNode.get("date");
                LocalDateTime dateTime = null;
                if (dateNode.has("date_time") && !dateNode.get("date_time").isNull()) {
                    try {
                        dateTime = LocalDateTime.parse(dateNode.get("date_time").asText());
                    } catch (Exception e) {
                        log.warn("Could not parse date_time: {}", dateNode.get("date_time").asText());
                    }
                }
                
                // CRITICAL: Normalize relative dates nếu AI extract sai hoặc chưa normalize
                // Backup mechanism để đảm bảo "ngày mai", "hôm nay" được parse đúng
                if (dateTime == null && dateNode.has("raw_text") && !dateNode.get("raw_text").isNull()) {
                    String rawText = dateNode.get("raw_text").asText().toLowerCase();
                    LocalDate today = LocalDate.now();
                    
                    if (rawText.contains("ngày mai") || rawText.contains("tomorrow")) {
                        dateTime = today.plusDays(1).atStartOfDay();
                        log.info("Normalized relative date '{}' to: {}", rawText, dateTime);
                    } else if (rawText.contains("hôm nay") || rawText.contains("today")) {
                        dateTime = today.atStartOfDay();
                        log.info("Normalized relative date '{}' to: {}", rawText, dateTime);
                    } else if (rawText.contains("ngày kia") || rawText.contains("day after tomorrow")) {
                        dateTime = today.plusDays(2).atStartOfDay();
                        log.info("Normalized relative date '{}' to: {}", rawText, dateTime);
                    }
                }
                
                ExtractSelectionResponse.DateSelection date = 
                        ExtractSelectionResponse.DateSelection.builder()
                                .dateTime(dateTime)
                                .rawText(dateNode.has("raw_text") ? dateNode.get("raw_text").asText() : null)
                                .confidence(dateNode.has("confidence") ? dateNode.get("confidence").asDouble() : 0.0)
                                .build();
                builder.date(date);
            }
            
            // Parse branch
            if (jsonNode.has("branch") && !jsonNode.get("branch").isNull()) {
                JsonNode branchNode = jsonNode.get("branch");
                UUID branchId = null;
                if (branchNode.has("branch_id") && !branchNode.get("branch_id").isNull()) {
                    String branchIdStr = branchNode.get("branch_id").asText().trim();
                    if (!branchIdStr.isEmpty() && !branchIdStr.equals("null") && 
                        !branchIdStr.contains("placeholder") && isValidUUID(branchIdStr)) {
                        try {
                            branchId = UUID.fromString(branchIdStr);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid branch_id format: {}", branchIdStr);
                        }
                    }
                }
                ExtractSelectionResponse.BranchSelection branch = 
                        ExtractSelectionResponse.BranchSelection.builder()
                                .branchName(branchNode.has("branch_name") ? branchNode.get("branch_name").asText() : null)
                                .branchId(branchId)
                                .selectionType(branchNode.has("selection_type") ? branchNode.get("selection_type").asText() : null)
                                .rawText(branchNode.has("raw_text") ? branchNode.get("raw_text").asText() : null)
                                .confidence(branchNode.has("confidence") ? branchNode.get("confidence").asDouble() : 0.0)
                                .build();
                builder.branch(branch);
            }
            
            // Parse service
            if (jsonNode.has("service") && !jsonNode.get("service").isNull()) {
                JsonNode serviceNode = jsonNode.get("service");
                UUID serviceId = null;
                if (serviceNode.has("service_id") && !serviceNode.get("service_id").isNull()) {
                    String serviceIdStr = serviceNode.get("service_id").asText().trim();
                    if (!serviceIdStr.isEmpty() && !serviceIdStr.equals("null") && 
                        !serviceIdStr.contains("placeholder") && isValidUUID(serviceIdStr)) {
                        try {
                            serviceId = UUID.fromString(serviceIdStr);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid service_id format: {}", serviceIdStr);
                        }
                    }
                }
                ExtractSelectionResponse.ServiceSelection service = 
                        ExtractSelectionResponse.ServiceSelection.builder()
                                .serviceName(serviceNode.has("service_name") ? serviceNode.get("service_name").asText() : null)
                                .serviceId(serviceId)
                                .selectionType(serviceNode.has("selection_type") ? serviceNode.get("selection_type").asText() : null)
                                .rawText(serviceNode.has("raw_text") ? serviceNode.get("raw_text").asText() : null)
                                .confidence(serviceNode.has("confidence") ? serviceNode.get("confidence").asDouble() : 0.0)
                                .build();
                builder.service(service);
            }
            
            // Parse bay
            if (jsonNode.has("bay") && !jsonNode.get("bay").isNull()) {
                JsonNode bayNode = jsonNode.get("bay");
                UUID bayId = null;
                if (bayNode.has("bay_id") && !bayNode.get("bay_id").isNull()) {
                    String bayIdStr = bayNode.get("bay_id").asText().trim();
                    if (!bayIdStr.isEmpty() && !bayIdStr.equals("null") && 
                        !bayIdStr.contains("placeholder") && isValidUUID(bayIdStr)) {
                        try {
                            bayId = UUID.fromString(bayIdStr);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid bay_id format: {}", bayIdStr);
                        }
                    }
                }
                ExtractSelectionResponse.BaySelection bay = 
                        ExtractSelectionResponse.BaySelection.builder()
                                .bayName(bayNode.has("bay_name") ? bayNode.get("bay_name").asText() : null)
                                .bayId(bayId)
                                .selectionType(bayNode.has("selection_type") ? bayNode.get("selection_type").asText() : null)
                                .rawText(bayNode.has("raw_text") ? bayNode.get("raw_text").asText() : null)
                                .confidence(bayNode.has("confidence") ? bayNode.get("confidence").asDouble() : 0.0)
                                .build();
                builder.bay(bay);
            }
            
            // Parse time
            if (jsonNode.has("time") && !jsonNode.get("time").isNull()) {
                JsonNode timeNode = jsonNode.get("time");
                ExtractSelectionResponse.TimeSelection time = 
                        ExtractSelectionResponse.TimeSelection.builder()
                                .timeSlot(timeNode.has("time_slot") ? timeNode.get("time_slot").asText() : null)
                                .rawText(timeNode.has("raw_text") ? timeNode.get("raw_text").asText() : null)
                                .confidence(timeNode.has("confidence") ? timeNode.get("confidence").asDouble() : 0.0)
                                .build();
                builder.time(time);
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage(), e);
            // Fallback: Return empty extracted data
            return ExtractSelectionResponse.ExtractedData.builder()
                    .intent("UNKNOWN")
                    .build();
        }
    }

    /**
     * Extract JSON từ AI response (có thể có text xung quanh)
     * Xử lý cả markdown code blocks và plain JSON
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";
        }
        
        // Remove markdown code blocks nếu có
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();
        
        // Try to find JSON block (nested braces)
        int braceCount = 0;
        int startIndex = -1;
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c == '{') {
                if (startIndex == -1) {
                    startIndex = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && startIndex != -1) {
                    return cleaned.substring(startIndex, i + 1);
                }
            }
        }
        
        // Fallback: return cleaned response
        return cleaned;
    }
    
    /**
     * Validate UUID string format
     */
    private boolean isValidUUID(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        // UUID format: 8-4-4-4-12 hex digits
        Pattern uuidPattern = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        return uuidPattern.matcher(str.trim()).matches();
    }

    /**
     * Validate extracted data với available options
     */
    private ValidationResult validateExtraction(
            ExtractSelectionResponse.ExtractedData extractedData,
            ExtractSelectionRequest request) {
        
        ValidationResult result = new ValidationResult();
        double totalConfidence = 0.0;
        int count = 0;
        
        // Validate vehicle
        if (extractedData.getVehicle() != null) {
            ExtractSelectionResponse.VehicleSelection vehicle = extractedData.getVehicle();
            UUID matchedVehicleId = matchVehicle(vehicle, request);
            
            if (matchedVehicleId != null) {
                vehicle.setVehicleId(matchedVehicleId);
                totalConfidence += vehicle.getConfidence() != null ? vehicle.getConfidence() : 0.8;
                count++;
            } else {
                // Nếu không có available options hoặc confidence cao, vẫn accept (sẽ match từ database sau)
                boolean hasAvailableOptions = request.getAvailableOptions() != null && 
                                             request.getAvailableOptions().getVehicles() != null &&
                                             !request.getAvailableOptions().getVehicles().isEmpty();
                double vehicleConfidence = vehicle.getConfidence() != null ? vehicle.getConfidence() : 0.0;
                
                if (hasAvailableOptions) {
                    // Có available options nhưng không match → Error
                    result.addError("Vehicle not found in available options: " + vehicle.getLicensePlate());
                } else if (vehicleConfidence >= 0.8 && vehicle.getLicensePlate() != null) {
                    // Không có available options nhưng confidence cao → Accept (sẽ match từ database)
                    log.info("Vehicle extraction accepted without available options (confidence: {}): {}", 
                            vehicleConfidence, vehicle.getLicensePlate());
                    totalConfidence += vehicleConfidence;
                    count++;
                } else {
                    result.addError("Vehicle not found in available options: " + vehicle.getLicensePlate());
                }
            }
        }
        
        // Validate branch
        if (extractedData.getBranch() != null) {
            ExtractSelectionResponse.BranchSelection branch = extractedData.getBranch();
            UUID matchedBranchId = matchBranch(branch, request);
            
            if (matchedBranchId != null) {
                branch.setBranchId(matchedBranchId);
                totalConfidence += branch.getConfidence() != null ? branch.getConfidence() : 0.8;
                count++;
            } else {
                // Nếu không có available options hoặc confidence cao, vẫn accept (sẽ match từ database sau)
                boolean hasAvailableOptions = request.getAvailableOptions() != null && 
                                             request.getAvailableOptions().getBranches() != null &&
                                             !request.getAvailableOptions().getBranches().isEmpty();
                double branchConfidence = branch.getConfidence() != null ? branch.getConfidence() : 0.0;
                
                if (hasAvailableOptions) {
                    // Có available options nhưng không match → Error
                    result.addError("Branch not found in available options: " + branch.getBranchName());
                } else if (branchConfidence >= 0.8 && branch.getBranchName() != null) {
                    // Không có available options nhưng confidence cao → Accept (sẽ match từ database)
                    log.info("Branch extraction accepted without available options (confidence: {}): {}", 
                            branchConfidence, branch.getBranchName());
                    totalConfidence += branchConfidence;
                    count++;
                } else {
                    result.addError("Branch not found in available options: " + branch.getBranchName());
                }
            }
        }
        
        // Validate service
        if (extractedData.getService() != null) {
            ExtractSelectionResponse.ServiceSelection service = extractedData.getService();
            UUID matchedServiceId = matchService(service, request);
            
            if (matchedServiceId != null) {
                service.setServiceId(matchedServiceId);
                totalConfidence += service.getConfidence() != null ? service.getConfidence() : 0.8;
                count++;
            } else {
                // Nếu không có available options hoặc confidence cao, vẫn accept (sẽ match từ database sau)
                boolean hasAvailableOptions = request.getAvailableOptions() != null && 
                                             request.getAvailableOptions().getServices() != null &&
                                             !request.getAvailableOptions().getServices().isEmpty();
                double serviceConfidence = service.getConfidence() != null ? service.getConfidence() : 0.0;
                
                if (hasAvailableOptions) {
                    // Có available options nhưng không match → Error
                    result.addError("Service not found in available options: " + service.getServiceName());
                } else if (serviceConfidence >= 0.8 && service.getServiceName() != null) {
                    // Không có available options nhưng confidence cao → Accept (sẽ match từ database)
                    log.info("Service extraction accepted without available options (confidence: {}): {}", 
                            serviceConfidence, service.getServiceName());
                    totalConfidence += serviceConfidence;
                    count++;
                } else {
                    result.addError("Service not found in available options: " + service.getServiceName());
                }
            }
        }
        
        // Validate bay
        if (extractedData.getBay() != null) {
            ExtractSelectionResponse.BaySelection bay = extractedData.getBay();
            UUID matchedBayId = matchBay(bay, request);
            
            if (matchedBayId != null) {
                bay.setBayId(matchedBayId);
                totalConfidence += bay.getConfidence() != null ? bay.getConfidence() : 0.8;
                count++;
            } else {
                // Nếu không có available options hoặc confidence cao, vẫn accept (sẽ match từ database sau)
                boolean hasAvailableOptions = request.getAvailableOptions() != null && 
                                             request.getAvailableOptions().getBays() != null &&
                                             !request.getAvailableOptions().getBays().isEmpty();
                double bayConfidence = bay.getConfidence() != null ? bay.getConfidence() : 0.0;
                
                if (hasAvailableOptions) {
                    // Có available options nhưng không match → Error
                    result.addError("Bay not found in available options: " + bay.getBayName());
                } else if (bayConfidence >= 0.8 && bay.getBayName() != null) {
                    // Không có available options nhưng confidence cao → Accept (sẽ match từ database)
                    log.info("Bay extraction accepted without available options (confidence: {}): {}", 
                            bayConfidence, bay.getBayName());
                    totalConfidence += bayConfidence;
                    count++;
                } else {
                    result.addError("Bay not found in available options: " + bay.getBayName());
                }
            }
        }
        
        // Validate time
        if (extractedData.getTime() != null) {
            ExtractSelectionResponse.TimeSelection time = extractedData.getTime();
            boolean isValidTime = validateTime(time, request);
            double timeConfidence = time.getConfidence() != null ? time.getConfidence() : 0.0;
            
            if (isValidTime) {
                totalConfidence += timeConfidence;
                count++;
            } else {
                // Nếu không có available options nhưng confidence cao → Accept (sẽ validate sau)
                boolean hasAvailableOptions = request.getAvailableOptions() != null && 
                                             request.getAvailableOptions().getTimeSlots() != null &&
                                             !request.getAvailableOptions().getTimeSlots().isEmpty();
                if (!hasAvailableOptions && timeConfidence >= 0.8 && time.getTimeSlot() != null) {
                    log.info("Time extraction accepted without available options (confidence: {}): {}", 
                            timeConfidence, time.getTimeSlot());
                    totalConfidence += timeConfidence;
                    count++;
                } else {
                    result.addError("Time slot not found in available options: " + time.getTimeSlot());
                }
            }
        }
        
        // Calculate average confidence
        if (count > 0) {
            result.setConfidence(totalConfidence / count);
        } else {
            result.setConfidence(0.0);
        }
        
        return result;
    }

    /**
     * Match vehicle với available options
     */
    private UUID matchVehicle(ExtractSelectionResponse.VehicleSelection vehicle, ExtractSelectionRequest request) {
        if (request.getAvailableOptions() == null || 
            request.getAvailableOptions().getVehicles() == null) {
            return null;
        }
        
        List<ExtractSelectionRequest.VehicleOption> vehicles = request.getAvailableOptions().getVehicles();
        
        // Match by index
        if ("INDEX".equals(vehicle.getSelectionType()) && vehicle.getRawText() != null) {
            Pattern indexPattern = Pattern.compile("(\\d+)");
            java.util.regex.Matcher matcher = indexPattern.matcher(vehicle.getRawText());
            if (matcher.find()) {
                try {
                    int index = Integer.parseInt(matcher.group(1)) - 1; // 0-based
                    if (index >= 0 && index < vehicles.size()) {
                        return vehicles.get(index).getVehicleId();
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        
        // Match by license plate
        if (vehicle.getLicensePlate() != null) {
            String normalizedPlate = vehicle.getLicensePlate().replaceAll("\\s+", "").toUpperCase();
            for (ExtractSelectionRequest.VehicleOption v : vehicles) {
                if (v.getLicensePlate() != null) {
                    String vPlate = v.getLicensePlate().replaceAll("\\s+", "").toUpperCase();
                    if (vPlate.equals(normalizedPlate) || 
                        vPlate.contains(normalizedPlate) || 
                        normalizedPlate.contains(vPlate)) {
                        return v.getVehicleId();
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Match branch với available options
     */
    private UUID matchBranch(ExtractSelectionResponse.BranchSelection branch, ExtractSelectionRequest request) {
        if (request.getAvailableOptions() == null || 
            request.getAvailableOptions().getBranches() == null) {
            return null;
        }
        
        List<ExtractSelectionRequest.BranchOption> branches = request.getAvailableOptions().getBranches();
        
        // Match by index
        if ("INDEX".equals(branch.getSelectionType()) && branch.getRawText() != null) {
            Pattern indexPattern = Pattern.compile("(\\d+)");
            java.util.regex.Matcher matcher = indexPattern.matcher(branch.getRawText());
            if (matcher.find()) {
                try {
                    int index = Integer.parseInt(matcher.group(1)) - 1;
                    if (index >= 0 && index < branches.size()) {
                        return branches.get(index).getBranchId();
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        
        // Match by name
        if (branch.getBranchName() != null) {
            String normalizedName = branch.getBranchName().toLowerCase();
            for (ExtractSelectionRequest.BranchOption b : branches) {
                if (b.getBranchName() != null) {
                    String bName = b.getBranchName().toLowerCase();
                    if (bName.equals(normalizedName) || 
                        bName.contains(normalizedName) || 
                        normalizedName.contains(bName)) {
                        return b.getBranchId();
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Match service với available options
     */
    private UUID matchService(ExtractSelectionResponse.ServiceSelection service, ExtractSelectionRequest request) {
        if (request.getAvailableOptions() == null || 
            request.getAvailableOptions().getServices() == null) {
            return null;
        }
        
        List<ExtractSelectionRequest.ServiceOption> services = request.getAvailableOptions().getServices();
        
        // Match by index
        if ("INDEX".equals(service.getSelectionType()) && service.getRawText() != null) {
            Pattern indexPattern = Pattern.compile("(\\d+)");
            java.util.regex.Matcher matcher = indexPattern.matcher(service.getRawText());
            if (matcher.find()) {
                try {
                    int index = Integer.parseInt(matcher.group(1)) - 1;
                    if (index >= 0 && index < services.size()) {
                        return services.get(index).getServiceId();
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        
        // Match by name
        if (service.getServiceName() != null) {
            String normalizedName = service.getServiceName().toLowerCase();
            for (ExtractSelectionRequest.ServiceOption s : services) {
                if (s.getServiceName() != null) {
                    String sName = s.getServiceName().toLowerCase();
                    if (sName.equals(normalizedName) || 
                        sName.contains(normalizedName) || 
                        normalizedName.contains(sName)) {
                        return s.getServiceId();
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Match bay với available options
     */
    private UUID matchBay(ExtractSelectionResponse.BaySelection bay, ExtractSelectionRequest request) {
        if (request.getAvailableOptions() == null || 
            request.getAvailableOptions().getBays() == null) {
            return null;
        }
        
        List<ExtractSelectionRequest.BayOption> bays = request.getAvailableOptions().getBays();
        
        // Match by index
        if ("INDEX".equals(bay.getSelectionType()) && bay.getRawText() != null) {
            Pattern indexPattern = Pattern.compile("(\\d+)");
            java.util.regex.Matcher matcher = indexPattern.matcher(bay.getRawText());
            if (matcher.find()) {
                try {
                    int index = Integer.parseInt(matcher.group(1)) - 1;
                    if (index >= 0 && index < bays.size()) {
                        return bays.get(index).getBayId();
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        
        // Match by name
        if (bay.getBayName() != null) {
            String normalizedName = bay.getBayName().toLowerCase();
            for (ExtractSelectionRequest.BayOption b : bays) {
                if (b.getBayName() != null) {
                    String bName = b.getBayName().toLowerCase();
                    if (bName.equals(normalizedName) || 
                        bName.contains(normalizedName) || 
                        normalizedName.contains(bName)) {
                        return b.getBayId();
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Validate time slot
     */
    private boolean validateTime(ExtractSelectionResponse.TimeSelection time, ExtractSelectionRequest request) {
        if (request.getAvailableOptions() == null || 
            request.getAvailableOptions().getTimeSlots() == null) {
            return false;
        }
        
        if (time.getTimeSlot() == null) {
            return false;
        }
        
        String normalizedTime = time.getTimeSlot().trim();
        return request.getAvailableOptions().getTimeSlots().contains(normalizedTime);
    }

    /**
     * Build clarification message
     */
    private String buildClarificationMessage(ValidationResult validation) {
        StringBuilder message = new StringBuilder();
        message.append("Tôi không chắc chắn về lựa chọn của bạn. ");
        
        if (!validation.getErrors().isEmpty()) {
            message.append("Vấn đề: ");
            message.append(String.join(", ", validation.getErrors()));
        }
        
        message.append(" Vui lòng xác nhận lại hoặc cung cấp thông tin rõ ràng hơn.");
        
        return message.toString();
    }

    /**
     * Build AvailableOptions từ ThreadLocal responses
     * Fallback: Parse từ conversation history nếu ThreadLocal null
     */
    public ExtractSelectionRequest.AvailableOptions buildAvailableOptions() {
        return buildAvailableOptions(null);
    }
    
    /**
     * Build AvailableOptions từ ThreadLocal responses hoặc conversation history
     * 
     * @param conversationHistory Conversation history để parse tool responses (fallback)
     */
    public ExtractSelectionRequest.AvailableOptions buildAvailableOptions(List<ChatRequest.ChatMessage> conversationHistory) {
        ExtractSelectionRequest.AvailableOptions.AvailableOptionsBuilder builder = 
                ExtractSelectionRequest.AvailableOptions.builder();
        
        // Get vehicles - Ưu tiên ThreadLocal, fallback conversation history
        GetCustomerVehiclesResponse vehiclesResponse = DraftContextHolder.getVehiclesResponse();
        if (vehiclesResponse == null && conversationHistory != null) {
            vehiclesResponse = parseVehiclesFromHistory(conversationHistory);
        }
        if (vehiclesResponse != null && vehiclesResponse.getVehicles() != null) {
            List<ExtractSelectionRequest.VehicleOption> vehicles = new ArrayList<>();
            for (int i = 0; i < vehiclesResponse.getVehicles().size(); i++) {
                GetCustomerVehiclesResponse.VehicleInfo v = vehiclesResponse.getVehicles().get(i);
                vehicles.add(ExtractSelectionRequest.VehicleOption.builder()
                        .vehicleId(v.getVehicleId())
                        .licensePlate(v.getLicensePlate())
                        .index(i + 1)
                        .build());
            }
            builder.vehicles(vehicles);
            log.debug("Added {} vehicles to available options", vehicles.size());
        }
        
        // Get branches - Ưu tiên ThreadLocal, fallback conversation history
        GetBranchesResponse branchesResponse = DraftContextHolder.getBranchesResponse();
        if (branchesResponse == null && conversationHistory != null) {
            branchesResponse = parseBranchesFromHistory(conversationHistory);
        }
        if (branchesResponse != null && branchesResponse.getBranches() != null) {
            List<ExtractSelectionRequest.BranchOption> branches = new ArrayList<>();
            for (int i = 0; i < branchesResponse.getBranches().size(); i++) {
                GetBranchesResponse.BranchInfo b = branchesResponse.getBranches().get(i);
                branches.add(ExtractSelectionRequest.BranchOption.builder()
                        .branchId(b.getBranchId())
                        .branchName(b.getBranchName())
                        .address(b.getAddress())
                        .index(i + 1)
                        .build());
            }
            builder.branches(branches);
            log.debug("Added {} branches to available options", branches.size());
        }
        
        // Get services - Ưu tiên ThreadLocal, fallback conversation history
        GetServicesResponse servicesResponse = DraftContextHolder.getServicesResponse();
        if (servicesResponse == null && conversationHistory != null) {
            servicesResponse = parseServicesFromHistory(conversationHistory);
        }
        if (servicesResponse != null && servicesResponse.getServices() != null) {
            List<ExtractSelectionRequest.ServiceOption> services = new ArrayList<>();
            for (int i = 0; i < servicesResponse.getServices().size(); i++) {
                GetServicesResponse.ServiceInfo s = servicesResponse.getServices().get(i);
                services.add(ExtractSelectionRequest.ServiceOption.builder()
                        .serviceId(s.getServiceId())
                        .serviceName(s.getServiceName())
                        .index(i + 1)
                        .build());
            }
            builder.services(services);
            log.debug("Added {} services to available options", services.size());
        }
        
        // Get bays và time slots - Ưu tiên ThreadLocal, fallback conversation history
        AvailabilityResponse availabilityResponse = DraftContextHolder.getAvailabilityResponse();
        if (availabilityResponse == null && conversationHistory != null) {
            availabilityResponse = parseAvailabilityFromHistory(conversationHistory);
        }
        if (availabilityResponse != null) {
            if (availabilityResponse.getAvailableBays() != null) {
                List<ExtractSelectionRequest.BayOption> bays = new ArrayList<>();
                for (int i = 0; i < availabilityResponse.getAvailableBays().size(); i++) {
                    AvailabilityResponse.AvailableBayInfo bay = availabilityResponse.getAvailableBays().get(i);
                    bays.add(ExtractSelectionRequest.BayOption.builder()
                            .bayId(bay.getBayId())
                            .bayName(bay.getBayName())
                            .index(i + 1)
                            .build());
                }
                builder.bays(bays);
                log.debug("Added {} bays to available options", bays.size());
            }
            
            // Extract time slots từ available bays
            if (availabilityResponse.getAvailableBays() != null) {
                List<String> timeSlots = new ArrayList<>();
                for (AvailabilityResponse.AvailableBayInfo bay : availabilityResponse.getAvailableBays()) {
                    if (bay.getAvailableSlots() != null) {
                        for (String slot : bay.getAvailableSlots()) {
                            if (slot != null && !timeSlots.contains(slot)) {
                                timeSlots.add(slot);
                            }
                        }
                    }
                }
                builder.timeSlots(timeSlots);
                log.debug("Added {} time slots to available options", timeSlots.size());
            }
            
            // Also add suggestions if available
            if (availabilityResponse.getSuggestions() != null) {
                List<String> timeSlots = builder.build().getTimeSlots();
                if (timeSlots == null) {
                    timeSlots = new ArrayList<>();
                }
                for (String suggestion : availabilityResponse.getSuggestions()) {
                    if (suggestion != null && !timeSlots.contains(suggestion)) {
                        timeSlots.add(suggestion);
                    }
                }
                builder.timeSlots(timeSlots);
            }
        }
        
        ExtractSelectionRequest.AvailableOptions result = builder.build();
        log.info("Available options: vehicles={}, branches={}, services={}, bays={}, timeSlots={}",
                result.getVehicles() != null ? result.getVehicles().size() : 0,
                result.getBranches() != null ? result.getBranches().size() : 0,
                result.getServices() != null ? result.getServices().size() : 0,
                result.getBays() != null ? result.getBays().size() : 0,
                result.getTimeSlots() != null ? result.getTimeSlots().size() : 0);
        return result;
    }
    
    /**
     * Parse vehicles từ conversation history
     */
    private GetCustomerVehiclesResponse parseVehiclesFromHistory(List<ChatRequest.ChatMessage> history) {
        if (history == null) return null;
        
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = history.get(i);
            if ("tool".equalsIgnoreCase(msg.getRole()) &&
                "getCustomerVehicles".equalsIgnoreCase(msg.getToolName()) &&
                msg.getToolResponse() != null) {
                try {
                    String jsonStr = objectMapper.writeValueAsString(msg.getToolResponse());
                    return objectMapper.readValue(jsonStr, GetCustomerVehiclesResponse.class);
                } catch (Exception e) {
                    log.warn("Error parsing vehicles from conversation history: {}", e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * Parse branches từ conversation history
     */
    private GetBranchesResponse parseBranchesFromHistory(List<ChatRequest.ChatMessage> history) {
        if (history == null) return null;
        
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = history.get(i);
            if ("tool".equalsIgnoreCase(msg.getRole()) &&
                "getBranches".equalsIgnoreCase(msg.getToolName()) &&
                msg.getToolResponse() != null) {
                try {
                    String jsonStr = objectMapper.writeValueAsString(msg.getToolResponse());
                    return objectMapper.readValue(jsonStr, GetBranchesResponse.class);
                } catch (Exception e) {
                    log.warn("Error parsing branches from conversation history: {}", e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * Parse services từ conversation history
     */
    private GetServicesResponse parseServicesFromHistory(List<ChatRequest.ChatMessage> history) {
        if (history == null) return null;
        
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessage msg = history.get(i);
            if ("tool".equalsIgnoreCase(msg.getRole()) &&
                "getServices".equalsIgnoreCase(msg.getToolName()) &&
                msg.getToolResponse() != null) {
                try {
                    String jsonStr = objectMapper.writeValueAsString(msg.getToolResponse());
                    return objectMapper.readValue(jsonStr, GetServicesResponse.class);
                } catch (Exception e) {
                    log.warn("Error parsing services from conversation history: {}", e.getMessage());
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
                    String jsonStr = objectMapper.writeValueAsString(msg.getToolResponse());
                    return objectMapper.readValue(jsonStr, AvailabilityResponse.class);
                } catch (Exception e) {
                    log.warn("Error parsing availability from conversation history: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Inner class để chứa validation result
     */
    private static class ValidationResult {
        private double confidence = 0.0;
        private List<String> errors = new ArrayList<>();
        
        public double getConfidence() {
            return confidence;
        }
        
        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public void addError(String error) {
            this.errors.add(error);
        }
    }
}

