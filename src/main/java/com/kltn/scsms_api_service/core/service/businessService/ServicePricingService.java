package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.serviceManagement.*;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateLaborCostRequest;
import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.enums.PricingPolicyType;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProcessStepProductService;
import com.kltn.scsms_api_service.core.service.entityService.PriceBookEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Business service để quản lý pricing cho Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePricingService {
    
    private final ServiceService serviceService;
    private final ServicePricingCalculator servicePricingCalculator;
    private final ServiceProcessStepProductService serviceProcessStepProductService;
    private final PriceBookEntityService priceBookEntityService;
    
    /**
     * Lấy thông tin pricing chi tiết của service
     * @param serviceId ID của service
     * @param priceBookId ID của price book (optional)
     * @return ServicePricingDto
     */
    public ServicePricingDto getServicePricing(UUID serviceId, UUID priceBookId) {
        log.info("Getting service pricing for service: {}, priceBook: {}", serviceId, priceBookId);
        
        com.kltn.scsms_api_service.core.entity.Service service = serviceService.getById(serviceId);
        
        // Lấy price book
        PriceBook priceBook = null;
        if (priceBookId != null) {
            priceBook = priceBookEntityService.require(priceBookId);
        } else {
            // Lấy active price book
            priceBook = servicePricingCalculator.pricingBusinessService.resolveActivePriceBook(LocalDateTime.now())
                .orElse(null);
        }
        
        // Tính toán pricing
        BigDecimal calculatedBasePrice = servicePricingCalculator.calculateProductCosts(serviceId, 
            priceBook != null ? priceBook.getId() : null);
        BigDecimal laborCost = service.getLaborCost() != null ? service.getLaborCost() : BigDecimal.ZERO;
        BigDecimal totalEstimatedPrice = calculatedBasePrice.add(laborCost);
        BigDecimal finalPrice = servicePricingCalculator.calculateFinalPrice(serviceId, 
            priceBook != null ? priceBook.getId() : null);
        
        // Lấy chi tiết process steps
        List<ProcessStepPricingDto> processSteps = getProcessStepsPricing(service, priceBook);
        
        // Đếm tổng số sản phẩm
        int totalProducts = processSteps.stream()
            .mapToInt(ProcessStepPricingDto::getProductCount)
            .sum();
        
        return ServicePricingDto.builder()
            .serviceId(serviceId)
            .serviceName(service.getServiceName())
            .serviceUrl(service.getServiceUrl())
            .basePrice(service.getBasePrice())
            .laborCost(laborCost)
            .totalEstimatedPrice(totalEstimatedPrice)
            .finalPrice(finalPrice)
            .lastPriceCalculatedAt(service.getModifiedDate())
            .priceBookId(priceBook != null ? priceBook.getId() : null)
            .priceBookName(priceBook != null ? priceBook.getName() : null)
            .processSteps(processSteps)
            .totalProductCosts(calculatedBasePrice)
            .totalProducts(totalProducts)
            .totalSteps(processSteps.size())
            .build();
    }
    
    /**
     * Lấy thông tin pricing summary của service
     * @param serviceId ID của service
     * @return ServicePricingInfoDto
     */
    public ServicePricingInfoDto getServicePricingInfo(UUID serviceId) {
        log.info("Getting service pricing info for service: {}", serviceId);
        
        com.kltn.scsms_api_service.core.entity.Service service = serviceService.getById(serviceId);
        
        // Tính toán pricing hiện tại
        BigDecimal calculatedBasePrice = servicePricingCalculator.calculateProductCosts(serviceId, null);
        BigDecimal currentLaborCost = service.getLaborCost() != null ? service.getLaborCost() : BigDecimal.ZERO;
        BigDecimal currentTotalPrice = (service.getBasePrice() != null ? service.getBasePrice() : BigDecimal.ZERO)
            .add(currentLaborCost);
        BigDecimal calculatedTotalPrice = calculatedBasePrice.add(currentLaborCost);
        
        // Kiểm tra có cần cập nhật không
        boolean needsUpdate = servicePricingCalculator.needsPriceUpdate(serviceId);
        
        // Lấy thông tin service process
        String serviceProcessName = null;
        Integer totalSteps = 0;
        Integer totalProducts = 0;
        
        if (service.getServiceProcess() != null) {
            serviceProcessName = service.getServiceProcess().getName();
            totalSteps = service.getServiceProcess().getProcessSteps().size();
            totalProducts = serviceProcessStepProductService.findByProcessId(service.getServiceProcess().getId()).size();
        }
        
        // Xác định trạng thái pricing
        String pricingStatus = "UP_TO_DATE";
        if (needsUpdate) {
            pricingStatus = "NEEDS_UPDATE";
        }
        if (calculatedBasePrice.compareTo(BigDecimal.ZERO) < 0) {
            pricingStatus = "ERROR";
        }
        
        return ServicePricingInfoDto.builder()
            .serviceId(serviceId)
            .serviceName(service.getServiceName())
            .currentBasePrice(service.getBasePrice())
            .currentLaborCost(currentLaborCost)
            .currentTotalPrice(currentTotalPrice)
            .calculatedBasePrice(calculatedBasePrice)
            .calculatedTotalPrice(calculatedTotalPrice)
            .needsUpdate(needsUpdate)
            .lastCalculatedAt(service.getModifiedDate())
            .pricingStatus(pricingStatus)
            .serviceProcessId(service.getServiceProcess() != null ? service.getServiceProcess().getId() : null)
            .serviceProcessName(serviceProcessName)
            .totalSteps(totalSteps)
            .totalProducts(totalProducts)
            .build();
    }
    
    /**
     * Tính lại base price cho service
     * @param serviceId ID của service
     * @param priceBookId ID của price book (optional)
     * @return ServicePricingDto
     */
    @Transactional
    public ServicePricingDto recalculateBasePrice(UUID serviceId, UUID priceBookId) {
        log.info("Recalculating base price for service: {}, priceBook: {}", serviceId, priceBookId);
        
        // Cập nhật base price
        servicePricingCalculator.updateServiceBasePrice(serviceId, priceBookId);
        
        // Trả về thông tin pricing mới
        return getServicePricing(serviceId, priceBookId);
    }
    
    /**
     * Cập nhật labor cost cho service
     * @param serviceId ID của service
     * @param request UpdateLaborCostRequest
     * @return ServicePricingInfoDto
     */
    @Transactional
    public ServicePricingInfoDto updateLaborCost(UUID serviceId, UpdateLaborCostRequest request) {
        log.info("Updating labor cost for service: {}, new cost: {}", serviceId, request.getLaborCost());
        
        com.kltn.scsms_api_service.core.entity.Service service = serviceService.getById(serviceId);
        
        // Cập nhật labor cost
        service.setLaborCost(request.getLaborCost());
        serviceService.update(service);
        
        log.info("Updated labor cost for service {}: {}", serviceId, request.getLaborCost());
        
        // Trả về thông tin pricing mới
        return getServicePricingInfo(serviceId);
    }
    
    /**
     * Lấy chi tiết pricing của các process steps
     * @param service Service entity
     * @param priceBook PriceBook entity
     * @return List<ProcessStepPricingDto>
     */
    private List<ProcessStepPricingDto> getProcessStepsPricing(com.kltn.scsms_api_service.core.entity.Service service, PriceBook priceBook) {
        if (service.getServiceProcess() == null) {
            return new ArrayList<>();
        }
        
        List<ProcessStepPricingDto> stepPricings = new ArrayList<>();
        
        for (ServiceProcessStep step : service.getServiceProcess().getProcessSteps()) {
            ProcessStepPricingDto stepPricing = calculateStepPricing(step, priceBook);
            stepPricings.add(stepPricing);
        }
        
        return stepPricings;
    }
    
    /**
     * Tính pricing cho một process step
     * @param step ServiceProcessStep
     * @param priceBook PriceBook
     * @return ProcessStepPricingDto
     */
    private ProcessStepPricingDto calculateStepPricing(ServiceProcessStep step, PriceBook priceBook) {
        List<ProductPricingDto> productPricings = new ArrayList<>();
        BigDecimal stepTotalCost = BigDecimal.ZERO;
        
        for (ServiceProcessStepProduct stepProduct : step.getStepProducts()) {
            ProductPricingDto productPricing = calculateProductPricing(stepProduct, priceBook);
            productPricings.add(productPricing);
            stepTotalCost = stepTotalCost.add(productPricing.getTotalPrice());
        }
        
        return ProcessStepPricingDto.builder()
            .stepId(step.getId())
            .stepName(step.getName())
            .stepDescription(step.getDescription())
            .stepOrder(step.getStepOrder())
            .estimatedTime(step.getEstimatedTime())
            .isRequired(step.getIsRequired())
            .stepTotalCost(stepTotalCost)
            .products(productPricings)
            .productCount(productPricings.size())
            .build();
    }
    
    /**
     * Tính pricing cho một sản phẩm
     * @param stepProduct ServiceProcessStepProduct
     * @param priceBook PriceBook
     * @return ProductPricingDto
     */
    private ProductPricingDto calculateProductPricing(ServiceProcessStepProduct stepProduct, PriceBook priceBook) {
        Product product = stepProduct.getProduct();
        BigDecimal quantity = stepProduct.getQuantity();
        
        // Lấy giá từ PriceBook
        BigDecimal unitPrice = servicePricingCalculator.pricingBusinessService.resolveUnitPrice(product.getProductId());
        BigDecimal totalPrice = unitPrice.multiply(quantity);
        
        // Xác định nguồn giá và policy type
        String priceSource = "PRICE_BOOK";
        PricingPolicyType policyType = PricingPolicyType.FIXED; // Default
        
        // TODO: Lấy thông tin chi tiết từ PriceBookItem để xác định policy type
        
        return ProductPricingDto.builder()
            .productId(product.getProductId())
            .productName(product.getProductName())
            .sku(product.getSku())
            .productType(product.getProductType() != null ? product.getProductType().getProductTypeName() : null)
            .brand(product.getBrand())
            .model(product.getModel())
            .quantity(quantity)
            .unit(stepProduct.getUnit())
            .unitPrice(unitPrice)
            .totalPrice(totalPrice)
            .policyType(policyType)
            .priceSource(priceSource)
            .priceBookId(priceBook != null ? priceBook.getId() : null)
            .priceBookName(priceBook != null ? priceBook.getName() : null)
            .priceCalculatedAt(LocalDateTime.now())
            .build();
    }
}
