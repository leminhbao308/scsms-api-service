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
     * @param packageId ID của service package
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book)
     * @return Tổng chi phí của package
     */
    public BigDecimal calculatePackageCosts(UUID packageId, UUID priceBookId) {
        log.info("Calculating package costs for service package: {}, priceBook: {}", packageId, priceBookId);

        ServicePackage servicePackage = servicePackageService.getById(packageId);
        if (servicePackage.getPackageServices() == null || servicePackage.getPackageServices().isEmpty()) {
            log.warn("Service package {} has no associated services, package costs will be zero.", packageId);
            return BigDecimal.ZERO;
        }

        BigDecimal totalPackageCosts = BigDecimal.ZERO;

        for (ServicePackageService packageService : servicePackage.getPackageServices()) {
            try {
                // Lấy service từ package service
                com.kltn.scsms_api_service.core.entity.Service service = packageService.getService();
                if (service == null) {
                    log.warn("Service is null in package service for package {}", packageId);
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
                    service.getServiceName(), packageId, serviceBasePrice, serviceLaborCost, quantity, packageServiceCost);

            } catch (Exception e) {
                log.error("Error calculating cost for service in package {}",
                    packageId, e);
                throw new ServerSideException(ErrorCode.ENTITY_NOT_FOUND,
                    "Cannot calculate cost for service in package: " + packageId);
            }
        }

        log.info("Total package costs for service package {}: {}", packageId, totalPackageCosts);
        return totalPackageCosts;
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
