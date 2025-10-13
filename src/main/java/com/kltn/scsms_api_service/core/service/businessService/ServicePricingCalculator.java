package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.entity.ServiceProcessStepProduct;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProcessStepProductService;
import com.kltn.scsms_api_service.exception.ServerSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service để tính toán giá cho Service
 * - basePrice: Tổng chi phí sản phẩm (tự động tính từ ServiceProcess)
 * - laborCost: Tiền công lao động (nhập thủ công)
 * - finalPrice: basePrice + laborCost + markup (từ PriceBook)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePricingCalculator {
    
    private final ServiceService serviceService;
    private final ServiceProcessStepProductService serviceProcessStepProductService;
    public final PricingBusinessService pricingBusinessService;
    
    /**
     * Tính tổng chi phí sản phẩm cho một service process
     * @param processId ID của service process
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book)
     * @return Tổng chi phí sản phẩm
     */
    public BigDecimal calculateProductCosts(UUID processId, UUID priceBookId) {
        return calculateProductCosts(processId, null, priceBookId);
    }
    
    /**
     * Tính tổng chi phí sản phẩm cho một service process với branch context
     * @param processId ID của service process
     * @param branchId ID của chi nhánh (null cho global pricing)
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book cho branch)
     * @return Tổng chi phí sản phẩm
     */
    public BigDecimal calculateProductCosts(UUID processId, UUID branchId, UUID priceBookId) {
        log.info("Calculating product costs for service process: {}, branch: {}, priceBook: {}", processId, branchId, priceBookId);
        
        BigDecimal totalProductCosts = BigDecimal.ZERO;
        
        // Lấy tất cả sản phẩm trong service process
        var stepProducts = serviceProcessStepProductService.findByProcessId(processId);
        
        for (ServiceProcessStepProduct stepProduct : stepProducts) {
            try {
                // Lấy giá sản phẩm từ PriceBook với branch context
                BigDecimal productPrice = pricingBusinessService.resolveUnitPrice(stepProduct.getProduct().getProductId(), branchId, priceBookId);
                
                // Tính chi phí cho sản phẩm này
                BigDecimal productCost = productPrice.multiply(stepProduct.getQuantity());
                totalProductCosts = totalProductCosts.add(productCost);
                
                log.debug("Product: {}, Quantity: {}, Unit Price: {}, Cost: {}", 
                    stepProduct.getProduct().getProductName(),
                    stepProduct.getQuantity(),
                    productPrice,
                    productCost);
                    
            } catch (Exception e) {
                log.error("Error calculating price for product {} in process {}", 
                    stepProduct.getProduct().getProductId(), processId, e);
                throw new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, 
                    "Cannot calculate price for product: " + stepProduct.getProduct().getProductName());
            }
        }
        
        log.info("Total product costs for service process {}: {}", processId, totalProductCosts);
        return totalProductCosts;
    }
    
    /**
     * Tính tổng chi phí sản phẩm cho một service (wrapper method)
     * @param serviceId ID của service
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book)
     * @return Tổng chi phí sản phẩm
     */
    public BigDecimal calculateProductCostsForService(UUID serviceId, UUID priceBookId) {
        return calculateProductCostsForBranch(serviceId, null, priceBookId);
    }
    
    /**
     * Tính tổng chi phí sản phẩm cho một service với branch context
     * @param serviceId ID của service
     * @param branchId ID của chi nhánh (null cho global pricing)
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book cho branch)
     * @return Tổng chi phí sản phẩm
     */
    public BigDecimal calculateProductCostsForBranch(UUID serviceId, UUID branchId, UUID priceBookId) {
        log.info("Calculating product costs for service: {}, branch: {}, priceBook: {}", serviceId, branchId, priceBookId);
        
        Service service = serviceService.getById(serviceId);
        
        if (service.getServiceProcess() == null) {
            log.warn("Service {} has no service process, returning zero product costs", serviceId);
            return BigDecimal.ZERO;
        }
        
        return calculateProductCosts(service.getServiceProcess().getId(), branchId, priceBookId);
    }
    
    /**
     * Cập nhật basePrice cho service (chỉ system mới được gọi method này)
     * @param serviceId ID của service
     * @param priceBookId ID của price book
     */
    @Transactional
    public void updateServiceBasePrice(UUID serviceId, UUID priceBookId) {
        log.info("Updating base price for service: {}", serviceId);
        
        Service service = serviceService.getById(serviceId);
        
        // Tính tổng chi phí sản phẩm
        BigDecimal totalProductCosts = calculateProductCostsForService(serviceId, priceBookId);
        
        // Cập nhật basePrice (chỉ system mới được làm việc này)
        service.setBasePrice(totalProductCosts);
        
        serviceService.update(service);
        
        log.info("Updated base price for service {}: {}", serviceId, totalProductCosts);
    }
    
    /**
     * Tính giá dự kiến của service (basePrice + laborCost)
     * @param serviceId ID của service
     * @param priceBookId ID của price book
     * @return Giá dự kiến
     */
    public BigDecimal calculateEstimatedPrice(UUID serviceId, UUID priceBookId) {
        log.info("Calculating estimated price for service: {}", serviceId);
        
        Service service = serviceService.getById(serviceId);
        
        // Tính chi phí sản phẩm
        BigDecimal productCosts = calculateProductCostsForService(serviceId, priceBookId);
        
        // Lấy tiền công lao động
        BigDecimal laborCost = service.getLaborCost() != null ? service.getLaborCost() : BigDecimal.ZERO;
        
        // Tổng giá dự kiến
        BigDecimal estimatedPrice = productCosts.add(laborCost);
        
        log.info("Estimated price for service {}: {} (products: {}, labor: {})", 
            serviceId, estimatedPrice, productCosts, laborCost);
            
        return estimatedPrice;
    }
    
    /**
     * Tính giá cuối cùng của service (estimated price + markup từ PriceBook)
     * @param serviceId ID của service
     * @param priceBookId ID của price book
     * @return Giá cuối cùng
     */
    public BigDecimal calculateFinalPrice(UUID serviceId, UUID priceBookId) {
        log.info("Calculating final price for service: {}", serviceId);
        
        // Tính giá dự kiến
        BigDecimal estimatedPrice = calculateEstimatedPrice(serviceId, priceBookId);
        
        // TODO: Thêm logic tính markup từ PriceBook cho Service
        // Hiện tại trả về giá dự kiến, sẽ implement markup sau
        
        log.info("Final price for service {}: {}", serviceId, estimatedPrice);
        return estimatedPrice;
    }
    
    /**
     * Kiểm tra xem service có cần cập nhật giá không
     * @param serviceId ID của service
     * @return true nếu cần cập nhật
     */
    public boolean needsPriceUpdate(UUID serviceId) {
        Service service = serviceService.getById(serviceId);
        
        // Nếu chưa có basePrice
        if (service.getBasePrice() == null) {
            return true;
        }
        
        // TODO: Thêm logic kiểm tra thời gian cập nhật cuối
        // Có thể cần cập nhật nếu PriceBook thay đổi hoặc ServiceProcess thay đổi
        
        return false;
    }
}
