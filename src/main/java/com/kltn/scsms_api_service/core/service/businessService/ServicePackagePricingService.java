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
    private final ServicePricingCalculator servicePricingCalculator;
    private final PriceBookEntityService priceBookEntityService;
    private final PricingBusinessService pricingBusinessService;

    /**
     * Lấy thông tin pricing chi tiết của một service package
     * 
     * @param packageId   ID của service package
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price
     *                    book)
     * @return ServicePackagePricingDto
     */
    public ServicePackagePricingDto getServicePackagePricing(UUID packageId, UUID priceBookId) {
        return getServicePackagePricingForBranch(packageId, null, priceBookId);
    }

    /**
     * Lấy thông tin pricing chi tiết của một service package cho chi nhánh cụ thể
     * 
     * @param packageId   ID của service package
     * @param branchId    ID của chi nhánh (null cho global pricing)
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price
     *                    book cho branch)
     * @return ServicePackagePricingDto
     */
    public ServicePackagePricingDto getServicePackagePricingForBranch(UUID packageId, UUID branchId, UUID priceBookId) {
        log.info("Getting service package pricing for package: {}, branch: {}, priceBook: {}", packageId, branchId,
                priceBookId);

        ServicePackage servicePackage = servicePackageService.getById(packageId);

        // Lấy price book
        PriceBook priceBook = null;
        if (priceBookId != null) {
            priceBook = priceBookEntityService.require(priceBookId);
        } else {
            // Lấy active price book cho branch
            priceBook = pricingBusinessService.resolveActivePriceBook(branchId, LocalDateTime.now())
                    .orElse(null);
        }

        // Tính toán pricing
        BigDecimal calculatedPackagePrice = servicePackagePricingCalculator.calculatePackageCostsForBranch(packageId,
                branchId,
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
                .branchId(servicePackage.getBranchId())
                .branchName(null) // TODO: Resolve branch name if needed
                .build();
    }

    /**
     * Lấy thông tin pricing tóm tắt của một service package
     * 
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

        BigDecimal currentPackagePrice = servicePackage.getPackagePrice() != null ? servicePackage.getPackagePrice()
                : BigDecimal.ZERO;
        BigDecimal currentTotalPrice = currentPackagePrice; // TODO: Add markup if needed

        BigDecimal calculatedPackagePrice = servicePackagePricingCalculator.calculatePackageCosts(packageId,
                activePriceBookId);
        BigDecimal calculatedTotalPrice = calculatedPackagePrice; // TODO: Add markup if needed

        boolean needsUpdate = !currentPackagePrice.equals(calculatedPackagePrice);
        String pricingStatus = needsUpdate ? "NEEDS_UPDATE" : "UP_TO_DATE";
        if (servicePackage.getPackageServices() == null || servicePackage.getPackageServices().isEmpty()) {
            pricingStatus = "NO_SERVICES";
        }

        Integer totalServices = servicePackage.getPackageServices() != null ? servicePackage.getPackageServices().size()
                : 0;
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
                .branchId(servicePackage.getBranchId())
                .branchName(null) // TODO: Resolve branch name if needed
                .build();
    }

    /**
     * Tính lại packagePrice (tổng chi phí) cho một service package.
     * 
     * @param packageId   ID của service package
     * @param priceBookId ID của price book (optional, nếu null sẽ dùng active price
     *                    book)
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
     * Hỗ trợ 2 trường hợp:
     * 1. Combo Services: Lấy từ packageServices
     * 2. Service Process: Tạo virtual service từ process
     * 
     * @param servicePackage ServicePackage entity
     * @return List<ServiceInPackagePricingDto>
     */
    private List<ServiceInPackagePricingDto> getServicesInPackagePricing(ServicePackage servicePackage) {
        // Trường hợp 1: Combo Services
        if (servicePackage.getPackageServices() != null && !servicePackage.getPackageServices().isEmpty()) {
            return getServicesPricingFromPackageServices(servicePackage);
        }

        // Trường hợp 2: Service Process Only - có serviceProcess và KHÔNG có
        // packageServices
        if (servicePackage.getServiceProcess() != null &&
                (servicePackage.getPackageServices() == null || servicePackage.getPackageServices().isEmpty())) {
            return getServicesPricingFromProcess(servicePackage);
        }

        return new ArrayList<>();
    }

    /**
     * Lấy pricing từ các service con (Trường hợp 1: Combo Services)
     */
    private List<ServiceInPackagePricingDto> getServicesPricingFromPackageServices(ServicePackage servicePackage) {
        List<ServiceInPackagePricingDto> servicePricings = new ArrayList<>();

        for (com.kltn.scsms_api_service.core.entity.ServicePackageService packageService : servicePackage
                .getPackageServices()) {
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

    /**
     * Lấy pricing từ quy trình chăm sóc (Trường hợp 2: Service Process)
     * Tạo một virtual service đại diện cho toàn bộ quy trình
     */
    private List<ServiceInPackagePricingDto> getServicesPricingFromProcess(ServicePackage servicePackage) {
        List<ServiceInPackagePricingDto> servicePricings = new ArrayList<>();

        com.kltn.scsms_api_service.core.entity.ServiceProcess process = servicePackage.getServiceProcess();

        // Tính chi phí sản phẩm từ quy trình
        BigDecimal productCosts = servicePricingCalculator.calculateProductCosts(process.getId(), null);

        // Tính tiền công từ quy trình
        BigDecimal laborCost = calculateProcessLaborCost(process);

        BigDecimal totalPrice = productCosts.add(laborCost);

        // Tạo virtual service đại diện cho quy trình
        servicePricings.add(ServiceInPackagePricingDto.builder()
                .serviceId(process.getId()) // Sử dụng process ID làm service ID
                .serviceName(process.getName())
                .serviceUrl("process-" + process.getCode())
                .quantity(1)
                .unitPrice(totalPrice)
                .totalPrice(totalPrice)
                .serviceBasePrice(productCosts)
                .serviceLaborCost(laborCost)
                .serviceTotalPrice(totalPrice)
                .isRequired(true)
                .notes("Quy trình chăm sóc: " + process.getDescription())
                .build());

        return servicePricings;
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
        BigDecimal totalLaborCost = laborRatePerMinute
                .multiply(BigDecimal.valueOf(serviceProcess.getEstimatedDuration()));

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
}
