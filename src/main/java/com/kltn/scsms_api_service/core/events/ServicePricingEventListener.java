package com.kltn.scsms_api_service.core.events;

import com.kltn.scsms_api_service.core.service.businessService.ServicePricingCalculator;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Event listener để xử lý các sự kiện liên quan đến pricing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServicePricingEventListener {
    
    private final ServicePricingCalculator servicePricingCalculator;
    
    /**
     * Xử lý sự kiện khi Service Process được cập nhật
     * Tự động cập nhật base price cho các service sử dụng process này
     */
    @EventListener
    @Async
    public void handleServiceProcessUpdated(ServiceProcessUpdatedEvent event) {
        log.info("Handling ServiceProcessUpdatedEvent for process: {}, affected services: {}", 
            event.getServiceProcessId(), event.getAffectedServiceIds());
        
        try {
            // Cập nhật base price cho từng service bị ảnh hưởng
            for (UUID serviceId : event.getAffectedServiceIds()) {
                try {
                    servicePricingCalculator.updateServiceBasePrice(serviceId, event.getPriceBookId());
                    log.info("Updated base price for service {} after process update", serviceId);
                } catch (Exception e) {
                    log.error("Failed to update base price for service {}: {}", serviceId, e.getMessage(), e);
                }
            }
            
            log.info("Completed processing ServiceProcessUpdatedEvent for process: {}", event.getServiceProcessId());
            
        } catch (Exception e) {
            log.error("Error handling ServiceProcessUpdatedEvent: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Xử lý sự kiện khi Price Book được cập nhật
     * Tự động cập nhật base price cho các service sử dụng sản phẩm trong price book này
     */
    @EventListener
    @Async
    public void handlePriceBookUpdated(PriceBookUpdatedEvent event) {
        log.info("Handling PriceBookUpdatedEvent for price book: {}, affected services: {}", 
            event.getPriceBookId(), event.getAffectedServiceIds());
        
        try {
            // Cập nhật base price cho từng service bị ảnh hưởng
            for (UUID serviceId : event.getAffectedServiceIds()) {
                try {
                    servicePricingCalculator.updateServiceBasePrice(serviceId, event.getPriceBookId());
                    log.info("Updated base price for service {} after price book update", serviceId);
                } catch (Exception e) {
                    log.error("Failed to update base price for service {}: {}", serviceId, e.getMessage(), e);
                }
            }
            
            log.info("Completed processing PriceBookUpdatedEvent for price book: {}", event.getPriceBookId());
            
        } catch (Exception e) {
            log.error("Error handling PriceBookUpdatedEvent: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Xử lý sự kiện khi Service được tạo mới
     * Tự động tính base price cho service mới
     */
    @EventListener
    @Async
    public void handleServiceCreated(ServiceCreatedEvent event) {
        log.info("Handling ServiceCreatedEvent for service: {}", event.getServiceId());
        
        try {
            // Tính base price cho service mới
            servicePricingCalculator.updateServiceBasePrice(event.getServiceId(), event.getPriceBookId());
            log.info("Calculated base price for new service: {}", event.getServiceId());
            
        } catch (Exception e) {
            log.error("Error handling ServiceCreatedEvent for service {}: {}", event.getServiceId(), e.getMessage(), e);
        }
    }
}
