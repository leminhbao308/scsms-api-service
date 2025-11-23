package com.kltn.scsms_api_service.configs.aiAssistant;

import com.kltn.scsms_api_service.core.dto.aiAssistant.request.AvailabilityRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.CreateBookingRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetBranchesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.request.GetCustomerVehiclesRequest;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.AvailabilityResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.CreateBookingResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetBranchesResponse;
import com.kltn.scsms_api_service.core.dto.aiAssistant.response.GetCustomerVehiclesResponse;
import com.kltn.scsms_api_service.core.service.aiAssistant.AiBookingAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AiAssistantFunctionsConfig {
    
    private final AiBookingAssistantService aiBookingAssistantService;
    
    @Bean
    public Function<AvailabilityRequest, AvailabilityResponse> checkAvailability() {
        return request -> {
            log.info("Spring AI Function: checkAvailability() called");
            return aiBookingAssistantService.checkAvailability(request);
        };
    }
    
    @Bean
    public Function<CreateBookingRequest, CreateBookingResponse> createBooking() {
        return request -> {
            log.info("Spring AI Function: createBooking() called");
            return aiBookingAssistantService.createBooking(request);
        };
    }
    
    @Bean
    public Function<GetCustomerVehiclesRequest, GetCustomerVehiclesResponse> getCustomerVehicles() {
        return request -> {
            log.info("Spring AI Function: getCustomerVehicles() called");
            return aiBookingAssistantService.getCustomerVehicles(request);
        };
    }
    
    @Bean
    public Function<GetBranchesRequest, GetBranchesResponse> getBranches() {
        return request -> {
            log.info("Spring AI Function: getBranches() called");
            return aiBookingAssistantService.getBranches(request);
        };
    }
}

