package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.servicePackageManagement.*;
import com.kltn.scsms_api_service.core.entity.*;
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
 * Business service để quản lý pricing cho ServicePackage
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePackagePricingService {

    private final com.kltn.scsms_api_service.core.service.entityService.ServicePackageService servicePackageService;
    private final ServicePackagePricingCalculator servicePackagePricingCalculator;
    private final PriceBookEntityService priceBookEntityService;
    private final PricingBusinessService pricingBusinessService;

    /**
     * Lấy thông tin pricing chi tiết của một service package
     * @param packageId ID của service package
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book)
     * @return ServicePackagePricingDto
     */
    public ServicePackagePricingDto getServicePackagePricing(UUID packageId, UUID priceBookId) {
        log.info("Getting service package pricing for package: {}, priceBook: {}", packageId, priceBookId);

        ServicePackage servicePackage = servicePackageService.getById(packageId);

        // Lấy price book
        PriceBook priceBook = null;
        if (priceBookId != null) {
            priceBook = priceBookEntityService.require(priceBookId);
        } else {
            // Lấy active price book
            priceBook = pricingBusinessService.resolveActivePriceBook(LocalDateTime.now())
                .orElse(null);
        }

        // Tính toán pricing
        BigDecimal calculatedPackagePrice = servicePackagePricingCalculator.calculatePackageCosts(packageId,
            priceBook != null ? priceBook.getId() : null);
        BigDecimal finalPrice = calculatedPackagePrice; // TODO: Apply package-level markup here if needed

        List<ServiceInPackagePricingDto> services = getServicesInPackagePricing(servicePackage);

        // Calculate total services and duration
        Integer totalServices = services.size();
        Integer totalDuration = servicePackage.getTotalDuration() != null ? servicePackage.getTotalDuration() : 0;

        return ServicePackagePricingDto.builder()
            .packageId(servicePackage.getPackageId())
            .packageName(servicePackage.getPackageName())
            .packageUrl(servicePackage.getPackageUrl())
            .packagePrice(servicePackage.getPackagePrice()) // Current packagePrice in DB
            .finalPrice(finalPrice)
            .lastPriceCalculatedAt(servicePackage.getModifiedDate()) // Use modifiedDate from AuditEntity
            .priceBookId(priceBook != null ? priceBook.getId() : null)
            .priceBookName(priceBook != null ? priceBook.getName() : null)
            .services(services)
            .totalServiceCosts(calculatedPackagePrice)
            .totalServices(totalServices)
            .totalDuration(totalDuration)
            .build();
    }

    /**
     * Lấy thông tin pricing tóm tắt của một service package
     * @param packageId ID của service package
     * @return ServicePackagePricingInfoDto
     */
    public ServicePackagePricingInfoDto getServicePackagePricingInfo(UUID packageId) {
        log.info("Getting service package pricing info for package: {}", packageId);

        ServicePackage servicePackage = servicePackageService.getById(packageId);

        // Lấy active price book để tính toán giá hiện tại
        PriceBook activePriceBook = pricingBusinessService.resolveActivePriceBook(LocalDateTime.now())
            .orElse(null);
        UUID activePriceBookId = activePriceBook != null ? activePriceBook.getId() : null;

        BigDecimal currentPackagePrice = servicePackage.getPackagePrice() != null ? servicePackage.getPackagePrice() : BigDecimal.ZERO;
        BigDecimal currentTotalPrice = currentPackagePrice; // TODO: Add markup if needed

        BigDecimal calculatedPackagePrice = servicePackagePricingCalculator.calculatePackageCosts(packageId, activePriceBookId);
        BigDecimal calculatedTotalPrice = calculatedPackagePrice; // TODO: Add markup if needed

        boolean needsUpdate = !currentPackagePrice.equals(calculatedPackagePrice);
        String pricingStatus = needsUpdate ? "NEEDS_UPDATE" : "UP_TO_DATE";
        if (servicePackage.getPackageServices() == null || servicePackage.getPackageServices().isEmpty()) {
            pricingStatus = "NO_SERVICES";
        }

        Integer totalServices = servicePackage.getPackageServices() != null ? servicePackage.getPackageServices().size() : 0;
        Integer totalDuration = servicePackage.getTotalDuration() != null ? servicePackage.getTotalDuration() : 0;

        return ServicePackagePricingInfoDto.builder()
            .packageId(servicePackage.getPackageId())
            .packageName(servicePackage.getPackageName())
            .currentPackagePrice(currentPackagePrice)
            .currentTotalPrice(currentTotalPrice)
            .calculatedPackagePrice(calculatedPackagePrice)
            .calculatedTotalPrice(calculatedTotalPrice)
            .needsUpdate(needsUpdate)
            .lastCalculatedAt(servicePackage.getModifiedDate()) // Use modifiedDate from AuditEntity
            .pricingStatus(pricingStatus)
            .totalServices(totalServices)
            .totalDuration(totalDuration)
            .build();
    }

    /**
     * Tính lại packagePrice (tổng chi phí) cho một service package.
     * @param packageId ID của service package
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price book)
     * @return ServicePackagePricingDto với giá mới
     */
    @Transactional
    public ServicePackagePricingDto recalculatePackagePrice(UUID packageId, UUID priceBookId) {
        log.info("Recalculating package price for service package: {}, priceBook: {}", packageId, priceBookId);
        servicePackagePricingCalculator.updateServicePackagePrice(packageId, priceBookId);
        return getServicePackagePricing(packageId, priceBookId); // Trả về thông tin pricing chi tiết sau khi cập nhật
    }

    /**
     * Lấy chi tiết pricing của các services trong package
     * @param servicePackage ServicePackage entity
     * @return List<ServiceInPackagePricingDto>
     */
    private List<ServiceInPackagePricingDto> getServicesInPackagePricing(ServicePackage servicePackage) {
        if (servicePackage.getPackageServices() == null || servicePackage.getPackageServices().isEmpty()) {
            return new ArrayList<>();
        }

        List<ServiceInPackagePricingDto> servicePricings = new ArrayList<>();

        for (com.kltn.scsms_api_service.core.entity.ServicePackageService packageService : servicePackage.getPackageServices()) {
            com.kltn.scsms_api_service.core.entity.Service service = packageService.getService();
            if (service == null) {
                continue;
            }

            // Tính giá của service
            BigDecimal serviceBasePrice = service.getBasePrice() != null ? service.getBasePrice() : BigDecimal.ZERO;
            BigDecimal serviceLaborCost = service.getLaborCost() != null ? service.getLaborCost() : BigDecimal.ZERO;
            BigDecimal serviceTotalPrice = serviceBasePrice.add(serviceLaborCost);

            // Tính giá cho package service
            BigDecimal quantity = BigDecimal.valueOf(packageService.getQuantity());
            BigDecimal totalPrice = serviceTotalPrice.multiply(quantity);

            servicePricings.add(ServiceInPackagePricingDto.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .serviceUrl(service.getServiceUrl())
                .quantity(packageService.getQuantity())
                .unitPrice(serviceTotalPrice)
                .totalPrice(totalPrice)
                .serviceBasePrice(serviceBasePrice)
                .serviceLaborCost(serviceLaborCost)
                .serviceTotalPrice(serviceTotalPrice)
                .isRequired(packageService.getIsRequired())
                .notes(packageService.getNotes())
                .build());
        }

        return servicePricings;
    }
}
