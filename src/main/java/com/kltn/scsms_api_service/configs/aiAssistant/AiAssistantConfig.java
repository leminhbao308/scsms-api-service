package com.kltn.scsms_api_service.configs.aiAssistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class AiAssistantConfig {

    private static final String PROMPT_TEMPLATE_CLASSPATH = "configs/aiAssistant/promtTemplate.txt";

    private static String getSystemPrompt() {
        try {
            // Đọc từ classpath (src/main/resources/)
            ClassPathResource resource = new ClassPathResource(PROMPT_TEMPLATE_CLASSPATH);
            if (!resource.exists()) {
                throw new IllegalStateException("Không tìm thấy file prompt template: " + PROMPT_TEMPLATE_CLASSPATH);
            }

            String promptTemplate = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            // Tính toán ngày hiện tại và ngày mai
            LocalDate today = LocalDate.now();
            String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String todayDisplay = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Format template với các giá trị động
            // Prompt mới chỉ có 2 chỗ %s: "Thời gian hiện tại: %s (%s)"
            String formattedPrompt = String.format(promptTemplate, todayStr, todayDisplay);

            // ===== THÊM STRICT SYSTEM INSTRUCTION (OPTIMIZED) =====
            // Rút gọn từ ~80 dòng xuống ~30 dòng, giữ lại các rules quan trọng nhất
            String strictSystemInstruction = """


CRITICAL RULES:
1. Check draft context [CO]/[CHUA] internally, NEVER print STATE/JSON to user
2. Follow STEP 1→2→3→4→5→6→7 strictly. STEP 5 (bay) BEFORE STEP 6 (time)
3. Wait for user answer before moving to next step
4. Extract branch_id from getBranches() TOOL response, NEVER invent UUID. Fallback: use branch_name
5. Check conversation history before calling functions. If [CO] in draft context → DON'T call function
6. If function returns "FULL"/"FAILED" → Check "state" field → Go back to indicated step
                    """;

            formattedPrompt = formattedPrompt + strictSystemInstruction;
            // ===== KẾT THÚC =====

            return formattedPrompt;
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file prompt template: " + PROMPT_TEMPLATE_CLASSPATH, e);
        }
    }

    @Bean
    public ChatClient aiChatClient(ChatModel chatModel) {
        // Spring AI 1.0.0-M5 CẦN đăng ký functions tường minh
        // Sử dụng .defaultFunctions() với tên của @Bean functions
        // Tên phải trùng với tên method trong AiAssistantFunctionsConfig
        // System prompt được tạo động với ngày hiện tại để AI parse chính xác
        return ChatClient.builder(chatModel)
                .defaultSystem(getSystemPrompt())
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultFunctions("checkAvailability", "createBooking", "getCustomerVehicles", "getBranches", "getServices", "extractUserSelection")
                .build();
    }

    /**
     * ChatClient riêng cho AI extraction - KHÔNG có functions
     * Để AI không tự gọi functions khi extract, chỉ trả về JSON
     */
    @Bean(name = "extractionChatClient")
    public ChatClient extractionChatClient(ChatModel chatModel) {
        // ChatClient riêng cho extraction, không có functions
        // Chỉ dùng để extract data, không gọi functions
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // KHÔNG có defaultFunctions - AI sẽ không tự gọi functions
                .build();
    }
}
