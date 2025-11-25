package com.kltn.scsms_api_service.configs.aiAssistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
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
      LocalDate tomorrow = today.plusDays(1);
      String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      String tomorrowStr = tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      String todayDisplay = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      String tomorrowDisplay = tomorrow.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

      // Format template với các giá trị động
      // Tổng cộng có 13 chỗ %s trong template:
      // - Line 407: NGÀY HIỆN TẠI: %s (%s) - 2 chỗ
      // - Line 408: NGÀY MAI: %s (%s) - 2 chỗ
      // - Line 411: "sáng mai" → dateTime = "%sT08:00:00" - 1 chỗ
      // - Line 412: "chiều mai" → dateTime = "%sT14:00:00" - 1 chỗ
      // - Line 413: "chiều nay" → dateTime = "%sT14:00:00" - 1 chỗ
      // - Line 414: "sáng nay" → dateTime = "%sT08:00:00" - 1 chỗ
      // - Line 415: "ngày mai", "mai" → dateTime = "%sT08:00:00" - 1 chỗ
      // - Line 544: Ví dụ parse → dateTime = "%sT08:00:00" - 1 chỗ
      // - Line 565: Ví dụ checkAvailability → date_time="%sT08:00:00" - 1 chỗ
      // - Line 606: 📅 Ngày: Sáng mai (%s) - 1 chỗ
      // - Line 624: ✅ Đặt lịch thành công! ... sáng mai (%s) - 1 chỗ
      return String.format(promptTemplate,
          todayStr, todayDisplay, // Line 407: NGÀY HIỆN TẠI: %s (%s)
          tomorrowStr, tomorrowDisplay, // Line 408: NGÀY MAI: %s (%s)
          tomorrowStr, // Line 411: "sáng mai" hoặc "ngày mai" → dateTime = "%sT08:00:00"
          tomorrowStr, // Line 412: "chiều mai" → dateTime = "%sT14:00:00"
          todayStr, // Line 413: "chiều nay" hoặc "hôm nay" → dateTime = "%sT14:00:00"
          todayStr, // Line 414: "sáng nay" → dateTime = "%sT08:00:00"
          tomorrowStr, // Line 415: "ngày mai", "mai" → dateTime = "%sT08:00:00"
          tomorrowStr, // Line 544: Ví dụ: Parse "sáng mai" → dateTime = "%sT08:00:00"
          tomorrowStr, // Line 565: Ví dụ: checkAvailability date_time="%sT08:00:00"
          tomorrowDisplay, // Line 606: Ví dụ: 📅 Ngày: Sáng mai (%s)
          tomorrowDisplay // Line 624: Ví dụ: "✅ Đặt lịch thành công! ... sáng mai (%s)"
      );
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

  /**
   * Tạo System Prompt Template (optional - để dynamic prompt nếu cần)
   */
  @Bean
  public PromptTemplate systemPromptTemplate() {
    return new PromptTemplate(getSystemPrompt());
  }
}
