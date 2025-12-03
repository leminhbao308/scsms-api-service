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

            // ===== THÊM STRICT SYSTEM INSTRUCTION =====
            String strictSystemInstruction = """


                    CRITICAL INSTRUCTION - YOU MUST FOLLOW STRICTLY:

                    1. YOU MUST check STATE internally (in your thinking) before EVERY response, but DO NOT print STATE to user.
                       - Check STATE format internally:
                         {
                           "current_step": [number],
                           "vehicle_id": "[UUID or null]",
                           "date_time": "[ISO string or null]",
                           "branch_id": "[UUID or null]",
                           "service_type": "[string or null]",
                           "bay_id": "[UUID or null]",
                           "time_slot": "[string or null]",
                           "missing_data": ["list of missing fields"],
                           "next_action": "[what to do next]"
                         }
                       - Use STATE to guide your response, but ONLY reply with friendly message to user
                       - DO NOT print "=== STATE HIỆN TẠI ===", JSON, or any debug information to user
                       - Example: If STATE shows current_step=1, missing_data=["vehicle_id"], reply: "Để bắt đầu, tôi cần biết xe của bạn..." (NOT the STATE JSON)

                    2. YOU MUST NOT skip steps. Follow this order STRICTLY:
                       STEP 1 → STEP 2 → STEP 3 → STEP 4 → STEP 5 → STEP 6 → STEP 7
                       
                       CRITICAL ABOUT STEP 5 AND STEP 6:
                       - STEP 5 (Chọn bay) MUST be done BEFORE STEP 6 (Chọn giờ)
                       - In STEP 5: ONLY show list of bays, DO NOT show time slots
                       - In STEP 6: ONLY show time slots AFTER user has selected a bay
                       - DO NOT skip STEP 5 and jump directly to showing time slots
                       - When checkAvailability() returns both bays and time slots, show ONLY bays in STEP 5

                    3. YOU MUST wait for user to answer before moving to next step.

                    4. IF user tries to skip steps, YOU MUST say:
                       "Xin lỗi, bạn cần hoàn thành STEP [X] trước. [Question for STEP X]"
                       And keep current_step unchanged.

                    5. IF any required data is missing (check field "state" in TOOL response), YOU MUST NOT call createBooking().
                       Instead, go back to the missing step indicated by "state.current_step" or "state.missing_data".

                    6. IF checkAvailability() or createBooking() returns status="FULL" or "FAILED":
                       - Check field "state" in the response
                       - Use "state.current_step" to know which step you're at
                       - Use "state.missing_data" to know what data is missing
                       - Use "state.next_action" to know what to do next
                       - Go back to the step indicated by "state.current_step" or "failed_step"

                    7. REMEMBER: State management is YOUR responsibility. Check STATE internally before every action.
                       The backend will help you by providing "state" field in every response, but YOU must use it INTERNALLY.
                       DO NOT show STATE to user - only use it to guide your friendly response.

                    8. ALWAYS use branch_id from STEP 3 (when user selected branch) for STEP 5 (checkAvailability).
                       DO NOT use branch_id from old checkAvailability() responses.
                       CRITICAL: You MUST extract branch_id from the MOST RECENT getBranches() TOOL response in conversation history.
                       If user selected "Chi Nhánh Gò Vấp", find it in the getBranches() response and use its branch_id.
                       NEVER invent or hallucinate a branch_id UUID - it MUST come from the TOOL response.
                       If you cannot find branch_id in conversation history → Call getBranches() again, DO NOT invent UUID.
                       CRITICAL FALLBACK: If you cannot find branch_id in TOOL response → You MUST pass branch_name (DO NOT leave it as null).
                       Examples of WRONG behavior (DO NOT DO THIS):
                       - Sending branch_id='b2b5b7d6-0453-4da9-bbd6-f2d9ef9c9c73', branch_name='null' (invented UUID, null name)
                       - Sending branch_id='<branch_id_of_Gò_Vấp>', branch_name='null' (placeholder text, null name)
                       - Sending branch_id='2b4f7f1d-ef9a-4c30-8f8e-1f6b1e9e2b12', branch_name='null' (invented UUID, null name)
                       CORRECT behavior:
                       - Search conversation history for getBranches() TOOL response
                       - Find branch with branch_name matching user's selection (e.g., "Chi Nhánh Gò Vấp")
                       - Extract the branch_id (UUID) from that TOOL response (e.g., "7cd17e0d-529d-48ef-9094-67103811651d")
                       - Use that exact UUID in checkAvailability() call: checkAvailability(branch_id='7cd17e0d-529d-48ef-9094-67103811651d', branch_name='Chi Nhánh Gò Vấp')
                       - FALLBACK: If branch_id not found → Use branch_name: checkAvailability(branch_id='null', branch_name='Chi Nhánh Gò Vấp')
                       - NEVER send both branch_id and branch_name as null

                    9. CRITICAL: PREVENT DUPLICATE FUNCTION CALLS - YOU MUST CHECK CONVERSATION HISTORY FIRST:
                       - BEFORE calling ANY function, READ the entire conversation history
                       - If history shows "Bạn đã chọn xe" or vehicle was selected → DO NOT call getCustomerVehicles() again
                       - If history shows "Bạn đã chọn chi nhánh" or branch was selected → DO NOT call getBranches() again
                       - If user repeats information already in history → ONLY confirm it, DO NOT call function again
                       - Backend will inject state context message showing what data you already have - USE IT
                       - Example: If state context says "[CO] Đã có vehicle_id" → DO NOT call getCustomerVehicles()
                       - Example: User says "52S2-27069" but history shows vehicle already selected → Say "Bạn đã chọn xe 52S2-27069 rồi. Bây giờ bạn muốn đặt lịch vào ngày nào?" (DO NOT call getCustomerVehicles())
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
                .defaultFunctions("checkAvailability", "createBooking", "getCustomerVehicles", "getBranches")
                .build();
    }
}
