package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.ServicePackage;
import com.kltn.scsms_api_service.core.entity.ServicePackageService;
import com.kltn.scsms_api_service.exception.ServerSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service để tính toán giá cho ServicePackage
 * - packagePrice: Tổng giá từ các Service con (basePrice + laborCost) * quantity
 * - finalPrice: packagePrice + markup (từ PriceBook)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePackagePricingCalculator {

    private final com.kltn.scsms_api_service.core.service.entityService.ServicePackageService servicePackageService;
    private final ServicePricingCalculator servicePricingCalculator;

    /**
     * Tính tổng chi phí cho một ServicePackage
     * Hỗ trợ 2 trường hợp:
     * 1. Combo Services: Tính từ các service con (basePrice + laborCost) * quantity
     * 2. Service Process: Tính từ quy trình chăm sóc (tổng sản phẩm + tiền công)
     * 
     * @param packageId ID của service package
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book)
     * @return Tổng chi phí của package
     */
    public BigDecimal calculatePackageCosts(UUID packageId, UUID priceBookId) {
        return calculatePackageCostsForBranch(packageId, null, priceBookId);
    }
    
    /**
     * Tính tổng chi phí cho một ServicePackage với branch context
     * Hỗ trợ 2 trường hợp:
     * 1. Combo Services: Tính từ các service con (basePrice + laborCost) * quantity
     * 2. Service Process: Tính từ quy trình chăm sóc (tổng sản phẩm + tiền công)
     * 
     * @param packageId ID của service package
     * @param branchId ID của chi nhánh (null cho global pricing)
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book cho branch)
     * @return Tổng chi phí của package
     */
    public BigDecimal calculatePackageCostsForBranch(UUID packageId, UUID branchId, UUID priceBookId) {
        log.info("Calculating package costs for service package: {}, branch: {}, priceBook: {}", packageId, branchId, priceBookId);

        ServicePackage servicePackage = servicePackageService.getById(packageId);
        
        // Trường hợp 1: Combo Services - có packageServices
        if (servicePackage.getPackageServices() != null && !servicePackage.getPackageServices().isEmpty()) {
            return calculatePackageCostsFromServices(servicePackage, branchId, priceBookId);
        }
        
        // Trường hợp 2: Service Process Only - có serviceProcess và KHÔNG có packageServices
        if (servicePackage.getServiceProcess() != null && 
            (servicePackage.getPackageServices() == null || servicePackage.getPackageServices().isEmpty())) {
            return calculatePackageCostsFromProcess(servicePackage, branchId, priceBookId);
        }
        
        // Trường hợp không có cả services và process
        log.warn("Service package {} has no associated services or process, package costs will be zero.", packageId);
        return BigDecimal.ZERO;
    }
    
    /**
     * Tính chi phí từ các service con (Trường hợp 1: Combo Services)
     */
    private BigDecimal calculatePackageCostsFromServices(ServicePackage servicePackage, UUID branchId, UUID priceBookId) {
        log.info("Calculating package costs from services for package: {}", servicePackage.getPackageId());
        
        BigDecimal totalPackageCosts = BigDecimal.ZERO;

        for (ServicePackageService packageService : servicePackage.getPackageServices()) {
            try {
                // Lấy service từ package service
                com.kltn.scsms_api_service.core.entity.Service service = packageService.getService();
                if (service == null) {
                    log.warn("Service is null in package service for package {}", servicePackage.getPackageId());
                    continue;
                }

                // Tính giá của service (basePrice + laborCost)
                BigDecimal serviceBasePrice = service.getBasePrice() != null ? service.getBasePrice() : BigDecimal.ZERO;
                BigDecimal serviceLaborCost = service.getLaborCost() != null ? service.getLaborCost() : BigDecimal.ZERO;
                BigDecimal serviceTotalPrice = serviceBasePrice.add(serviceLaborCost);

                // Tính giá cho package service (serviceTotalPrice * quantity)
                BigDecimal quantity = BigDecimal.valueOf(packageService.getQuantity());
                BigDecimal packageServiceCost = serviceTotalPrice.multiply(quantity);

                totalPackageCosts = totalPackageCosts.add(packageServiceCost);

                log.debug("Service {} in package {}: basePrice={}, laborCost={}, quantity={}, cost={}",
                    service.getServiceName(), servicePackage.getPackageId(), serviceBasePrice, serviceLaborCost, quantity, packageServiceCost);

            } catch (Exception e) {
                log.error("Error calculating cost for service in package {}",
                    servicePackage.getPackageId(), e);
                throw new ServerSideException(ErrorCode.ENTITY_NOT_FOUND,
                    "Cannot calculate cost for service in package: " + servicePackage.getPackageId());
            }
        }

        log.info("Total package costs from services for package {}: {}", servicePackage.getPackageId(), totalPackageCosts);
        return totalPackageCosts;
    }
    
    /**
     * Tính chi phí từ quy trình chăm sóc (Trường hợp 2: Service Process)
     */
    private BigDecimal calculatePackageCostsFromProcess(ServicePackage servicePackage, UUID branchId, UUID priceBookId) {
        log.info("Calculating package costs from process for package: {}", servicePackage.getPackageId());
        
        try {
            // Tính chi phí sản phẩm từ quy trình
            BigDecimal productCosts = servicePricingCalculator.calculateProductCosts(
                servicePackage.getServiceProcess().getId(), branchId, priceBookId);
            
            // Tính tiền công từ quy trình (nếu có)
            BigDecimal laborCost = calculateProcessLaborCost(servicePackage.getServiceProcess());
            
            BigDecimal totalCosts = productCosts.add(laborCost);
            
            log.info("Package costs from process for package {}: products={}, labor={}, total={}", 
                servicePackage.getPackageId(), productCosts, laborCost, totalCosts);
            
            return totalCosts;
            
        } catch (Exception e) {
            log.error("Error calculating cost from process for package {}", 
                servicePackage.getPackageId(), e);
            throw new ServerSideException(ErrorCode.ENTITY_NOT_FOUND,
                "Cannot calculate cost from process for package: " + servicePackage.getPackageId());
        }
    }
    
    /**
     * Tính tiền công từ quy trình chăm sóc
     * Logic: Dựa trên estimatedDuration và complexity của các bước
     */
    private BigDecimal calculateProcessLaborCost(com.kltn.scsms_api_service.core.entity.ServiceProcess serviceProcess) {
        log.info("Calculating labor cost for service process: {}", serviceProcess.getId());
        
        if (serviceProcess.getEstimatedDuration() == null || serviceProcess.getEstimatedDuration() <= 0) {
            log.warn("Service process {} has no estimated duration, labor cost will be zero", serviceProcess.getId());
            return BigDecimal.ZERO;
        }
        
        // Tỷ lệ tiền công theo phút (có thể config từ database sau)
        BigDecimal laborRatePerMinute = new BigDecimal("1000"); // 1000 VND/phút
        
        // Tính tiền công dựa trên thời gian dự kiến
        BigDecimal totalLaborCost = laborRatePerMinute.multiply(BigDecimal.valueOf(serviceProcess.getEstimatedDuration()));
        
        // Tính complexity factor dựa trên số bước và số sản phẩm
        double complexityFactor = calculateProcessComplexity(serviceProcess);
        
        // Áp dụng complexity factor
        BigDecimal finalLaborCost = totalLaborCost.multiply(BigDecimal.valueOf(complexityFactor));
        
        log.info("Labor cost for process {}: base={}, complexity={}, final={}", 
            serviceProcess.getId(), totalLaborCost, complexityFactor, finalLaborCost);
        
        return finalLaborCost;
    }
    
    /**
     * Tính hệ số phức tạp của quy trình
     * Dựa trên: số bước, số sản phẩm, bước bắt buộc
     */
    private double calculateProcessComplexity(com.kltn.scsms_api_service.core.entity.ServiceProcess serviceProcess) {
        if (serviceProcess.getProcessSteps() == null || serviceProcess.getProcessSteps().isEmpty()) {
            return 1.0; // Không có bước nào = độ phức tạp cơ bản
        }
        
        int totalSteps = serviceProcess.getProcessSteps().size();
        int requiredSteps = (int) serviceProcess.getProcessSteps().stream()
            .filter(step -> step.getIsRequired() != null && step.getIsRequired())
            .count();
        
        int totalProducts = serviceProcess.getProcessSteps().stream()
            .mapToInt(step -> step.getStepProducts() != null ? step.getStepProducts().size() : 0)
            .sum();
        
        // Công thức tính complexity:
        // - Base: 1.0
        // - Mỗi bước bắt buộc: +0.1
        // - Mỗi sản phẩm: +0.05
        // - Tối đa: 3.0
        double complexity = 1.0 + (requiredSteps * 0.1) + (totalProducts * 0.05);
        
        return Math.min(complexity, 3.0); // Giới hạn tối đa 3.0
    }

    /**
     * Cập nhật packagePrice (tổng chi phí) cho một ServicePackage.
     * Phương thức này chỉ được gọi bởi hệ thống (ví dụ: qua EventListener).
     * @param packageId ID của service package
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book)
     */
    @Transactional
    public void updateServicePackagePrice(UUID packageId, UUID priceBookId) {
        log.info("Attempting to update package price for service package: {}, priceBook: {}", packageId, priceBookId);
        ServicePackage servicePackage = servicePackageService.getById(packageId);

        // Tính tổng chi phí package
        BigDecimal totalPackageCosts = calculatePackageCosts(packageId, priceBookId);

        // Cập nhật packagePrice (chỉ system mới được làm việc này)
        servicePackage.setPackagePrice(totalPackageCosts);

        servicePackageService.update(servicePackage);

        log.info("Updated package price for service package {}: {}", packageId, totalPackageCosts);
    }

    /**
     * Kiểm tra xem service package có cần cập nhật giá không.
     * @param packageId ID của service package
     * @return true nếu cần cập nhật
     */
    public boolean needsPriceUpdate(UUID packageId) {
        ServicePackage servicePackage = servicePackageService.getById(packageId);

        // Nếu chưa có packagePrice
        if (servicePackage.getPackagePrice() == null) {
            return true;
        }

        // TODO: Thêm logic kiểm tra thời gian cập nhật cuối
        // Có thể cần cập nhật nếu PriceBook thay đổi hoặc các Service con thay đổi

        return false; // Placeholder, cần implement logic so sánh thực tế
    }
}
