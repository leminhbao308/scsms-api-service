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

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Booking Assistant", description = "APIs for AI-powered booking assistant")
@RequestMapping("/ai-assistant")
public class AiBookingAssistantController {
    
    private final ChatClient aiChatClient;
    

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI booking assistant", 
               description = "Send a message to AI assistant. AI will automatically call functions (checkAvailability, createBooking) when needed.")
    @SwaggerOperation(summary = "Chat with AI booking assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Parameter(description = "Chat request with message and conversation history") 
            @Valid @RequestBody ChatRequest request) {
        
        long requestStartTime = System.currentTimeMillis();
        log.info("AI Assistant chat request received: message={}, customerPhone={}, historySize={}", 
                request.getMessage(), request.getCustomerPhone(),
                request.getConversationHistory() != null ? request.getConversationHistory().size() : 0);
        
        try {
            // 1. Build conversation messages từ history
            // Frontend gửi toàn bộ conversation history, backend sẽ sử dụng tất cả để AI có đầy đủ context
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
            
            // 2. Add current user message
            String userMessage = request.getMessage();
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
            log.info("AI Assistant response: message length={}, requiresAction={}, totalTime={} ms", 
                    aiMessage.length(), requiresAction, (requestEndTime - requestStartTime));
            
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
}

