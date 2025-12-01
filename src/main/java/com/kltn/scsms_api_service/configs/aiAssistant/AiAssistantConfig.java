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
      LocalDate tomorrow = today.plusDays(1);
      String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      String tomorrowStr = tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      String todayDisplay = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      @SuppressWarnings("unused")
      String tomorrowDisplay = tomorrow.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); // Giữ lại để dùng sau này nếu cần

      // Format template với các giá trị động
      // Tổng cộng có 9 chỗ %s trong template:
      // - Line 2: "Thời gian hiện tại: %s (%s)" - 2 chỗ (todayStr, todayDisplay)
      // - Line 10: "nếu hôm nay là %s, thì 'mai' là %s" - 2 chỗ (todayStr, tomorrowStr)
      // - Line 11: "dùng ngày hiện tại (%s)" - 1 chỗ (todayStr)
      // - Line 12: "dùng ngày mai (%s) với giờ 08:00:00 → '%sT08:00:00'" - 2 chỗ (tomorrowStr, tomorrowStr)
      // - Line 13: "dùng ngày mai (%s) với giờ 14:00:00 → '%sT14:00:00'" - 2 chỗ (tomorrowStr, tomorrowStr)
      return String.format(promptTemplate,
          todayStr, todayDisplay, // Line 2: "Thời gian hiện tại: %s (%s)"
          todayStr, tomorrowStr, // Line 10: "nếu hôm nay là %s, thì 'mai' là %s"
          todayStr, // Line 11: "dùng ngày hiện tại (%s)"
          tomorrowStr, tomorrowStr, // Line 12: "dùng ngày mai (%s) với giờ 08:00:00 → '%sT08:00:00'"
          tomorrowStr, tomorrowStr // Line 13: "dùng ngày mai (%s) với giờ 14:00:00 → '%sT14:00:00'"
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
}
