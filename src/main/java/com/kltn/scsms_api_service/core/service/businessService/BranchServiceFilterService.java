package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.branchServiceFilter.BranchServiceFilterResult;
import com.kltn.scsms_api_service.core.dto.branchServiceFilter.ServiceAvailabilityInfo;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.InventoryLevel;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service để lọc các service có thể sử dụng theo branch
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchServiceFilterService {
    
    private final BranchService branchService;
    private final InventoryLevelEntityService inventoryLevelEntityService;
    private final ServiceProductService serviceProductService;
    private final ServiceService serviceEntityService;
    private final ServiceMapper serviceMapper;
    
    /**
     * Lọc các service có thể sử dụng theo branch
     */
    public BranchServiceFilterResult filterServicesByBranch(UUID branchId, boolean requireFullInventory) {
        log.info("Filtering services by branch: {} with full inventory requirement: {}", branchId,
            requireFullInventory);
        
        // Validate branch exists
        Branch branch = branchService.findById(branchId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found"));
        
        // Get all active services
        List<com.kltn.scsms_api_service.core.entity.Service> allServices = serviceEntityService.findAll();
        List<ServiceAvailabilityInfo> availableServices = new ArrayList<>();
        List<ServiceAvailabilityInfo> unavailableServices = new ArrayList<>();
        
        for (com.kltn.scsms_api_service.core.entity.Service service : allServices) {
            if (service.getIsActive() && service.getServiceProcess() != null) {
                ServiceAvailabilityInfo serviceInfo = checkServiceAvailability(service, branch,
                    requireFullInventory);
                
                if (serviceInfo.isAvailable()) {
                    availableServices.add(serviceInfo);
                } else {
                    unavailableServices.add(serviceInfo);
                }
            }
        }
        
        return BranchServiceFilterResult.builder()
            .branchId(branchId)
            .branchName(branch.getBranchName())
            .availableServices(availableServices)
            .unavailableServices(unavailableServices)
            .totalServicesChecked(allServices.size())
            .availableServicesCount(availableServices.size())
            .build();
    }
    
    
    /**
     * Lọc tất cả service có thể sử dụng theo branch
     */
    public BranchServiceFilterResult filterAllServicesByBranch(UUID branchId, boolean requireFullInventory) {
        log.info("Filtering all services by branch: {} with full inventory requirement: {}", branchId,
            requireFullInventory);
        
        // Validate branch exists
        Branch branch = branchService.findById(branchId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found"));
        
        List<ServiceAvailabilityInfo> allAvailable = new ArrayList<>();
        List<ServiceAvailabilityInfo> allUnavailable = new ArrayList<>();
        
        // Check services
        List<com.kltn.scsms_api_service.core.entity.Service> allServices = serviceEntityService.findAll();
        for (com.kltn.scsms_api_service.core.entity.Service service : allServices) {
            if (service.getIsActive() && service.getServiceProcess() != null) {
                ServiceAvailabilityInfo serviceInfo = checkServiceAvailability(service, branch,
                    requireFullInventory);
                serviceInfo.setType("SERVICE");
                
                if (serviceInfo.isAvailable()) {
                    allAvailable.add(serviceInfo);
                } else {
                    allUnavailable.add(serviceInfo);
                }
            }
        }
        
        
        return BranchServiceFilterResult.builder()
            .branchId(branchId)
            .branchName(branch.getBranchName())
            .availableServices(allAvailable)
            .unavailableServices(allUnavailable)
            .totalServicesChecked(allServices.size())
            .availableServicesCount(allAvailable.size())
            .build();
    }
    
    /**
     * Kiểm tra tính khả dụng của một service cụ thể tại branch
     * Tối ưu hơn filterServicesByBranch() vì chỉ kiểm tra 1 service thay vì tất cả services
     */
    public ServiceAvailabilityInfo checkSingleServiceAvailability(UUID branchId, UUID serviceId, boolean requireFullInventory) {
        Branch branch = branchService.findById(branchId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found"));
        
        com.kltn.scsms_api_service.core.entity.Service service = serviceEntityService.findById(serviceId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Service not found"));
        
        return checkServiceAvailability(service, branch, requireFullInventory);
    }
    
    /**
     * Kiểm tra tính khả dụng của nhiều services cùng lúc tại branch
     * Tối ưu: Load tất cả tồn kho của branch trước, sau đó kiểm tra từng service
     * 
     * @param branchId ID của branch
     * @param serviceIds Danh sách ID của các service cần kiểm tra
     * @param requireFullInventory Yêu cầu đủ inventory hay không
     * @return Kết quả kiểm tra với danh sách available và unavailable services
     */
    public BranchServiceFilterResult checkMultipleServicesAvailability(UUID branchId, List<UUID> serviceIds, boolean requireFullInventory) {
        log.info("Checking multiple services availability - Branch: {}, Services count: {}, RequireFullInventory: {}", 
            branchId, serviceIds != null ? serviceIds.size() : 0, requireFullInventory);
        
        // Validate branch exists
        Branch branch = branchService.findById(branchId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found"));
        
        if (serviceIds == null || serviceIds.isEmpty()) {
            return BranchServiceFilterResult.builder()
                .branchId(branchId)
                .branchName(branch.getBranchName())
                .availableServices(new ArrayList<>())
                .unavailableServices(new ArrayList<>())
                .totalServicesChecked(0)
                .availableServicesCount(0)
                .build();
        }
        
        // BƯỚC 1: Lấy tất cả tồn kho của chi nhánh trước (tối ưu - chỉ query 1 lần)
        log.debug("Loading all inventory levels for branch: {}", branchId);
        List<InventoryLevel> branchInventoryLevels = inventoryLevelEntityService.findAllByBranchId(branchId);
        
        // Chuyển thành Map để lookup nhanh: productId -> InventoryLevel
        Map<UUID, InventoryLevel> inventoryMap = new HashMap<>();
        for (InventoryLevel level : branchInventoryLevels) {
            inventoryMap.put(level.getProduct().getProductId(), level);
        }
        log.debug("Loaded {} inventory items for branch {}", inventoryMap.size(), branchId);
        
        List<ServiceAvailabilityInfo> availableServices = new ArrayList<>();
        List<ServiceAvailabilityInfo> unavailableServices = new ArrayList<>();
        
        // BƯỚC 2: Lần lượt từng dịch vụ một - lấy sản phẩm trong dịch vụ ra để kiểm tra với tồn kho
        for (UUID serviceId : serviceIds) {
            try {
                com.kltn.scsms_api_service.core.entity.Service service = serviceEntityService.findById(serviceId)
                    .orElse(null);
                
                if (service == null) {
                    // Service not found
                    unavailableServices.add(ServiceAvailabilityInfo.builder()
                        .id(serviceId)
                        .name("Service not found")
                        .type("SERVICE")
                        .available(false)
                        .reason("Service with ID " + serviceId + " not found")
                        .build());
                    continue;
                }
                
                if (!service.getIsActive() || service.getServiceProcess() == null) {
                    // Service is not active or has no process
                    unavailableServices.add(ServiceAvailabilityInfo.builder()
                        .id(serviceId)
                        .name(service.getServiceName())
                        .type("SERVICE")
                        .available(false)
                        .reason("Service is not active or has no process")
                        .serviceInfo(serviceMapper.toServiceInfoDto(service))
                        .build());
                    continue;
                }
                
                // Kiểm tra với inventory map đã load sẵn
                ServiceAvailabilityInfo serviceInfo = checkServiceAvailabilityWithInventoryMap(
                    service, branch, inventoryMap, requireFullInventory);
                
                if (serviceInfo.isAvailable()) {
                    availableServices.add(serviceInfo);
                } else {
                    unavailableServices.add(serviceInfo);
                }
            } catch (Exception e) {
                log.error("Error checking service availability for serviceId: {}", serviceId, e);
                unavailableServices.add(ServiceAvailabilityInfo.builder()
                    .id(serviceId)
                    .name("Error checking service")
                    .type("SERVICE")
                    .available(false)
                    .reason("Error: " + e.getMessage())
                    .build());
            }
        }
        
        return BranchServiceFilterResult.builder()
            .branchId(branchId)
            .branchName(branch.getBranchName())
            .availableServices(availableServices)
            .unavailableServices(unavailableServices)
            .totalServicesChecked(serviceIds.size())
            .availableServicesCount(availableServices.size())
            .build();
    }
    
    /**
     * Kiểm tra tính khả dụng của một service với inventory map đã load sẵn
     * Logic: Nếu dịch vụ không có sản phẩm thì luôn available
     * 
     * @param service Service cần kiểm tra
     * @param branch Branch
     * @param inventoryMap Map productId -> InventoryLevel (đã load sẵn từ branch)
     * @param requireFullInventory Yêu cầu đủ inventory hay không
     * @return ServiceAvailabilityInfo
     */
    private ServiceAvailabilityInfo checkServiceAvailabilityWithInventoryMap(
            com.kltn.scsms_api_service.core.entity.Service service,
            Branch branch,
            Map<UUID, InventoryLevel> inventoryMap,
            boolean requireFullInventory) {
        try {
            // Lấy tất cả sản phẩm trong dịch vụ
            List<ServiceProduct> requiredProducts = serviceProductService.findByServiceIdWithProduct(service.getServiceId());
            
            // TRƯỜNG HỢP ĐẶC BIỆT: Nếu dịch vụ không có sản phẩm thì luôn available
            if (requiredProducts == null || requiredProducts.isEmpty()) {
                log.debug("Service {} has no products - always available", service.getServiceName());
                return ServiceAvailabilityInfo.builder()
                    .id(service.getServiceId())
                    .name(service.getServiceName())
                    .type("SERVICE")
                    .available(true)
                    .totalProductsRequired(0)
                    .availableProductsCount(0)
                    .missingProducts(new ArrayList<>())
                    .insufficientProducts(new ArrayList<>())
                    .serviceInfo(serviceMapper.toServiceInfoDto(service))
                    .build();
            }
            
            int totalProducts = requiredProducts.size();
            int availableProducts = 0;
            List<String> missingProducts = new ArrayList<>();
            List<String> insufficientProducts = new ArrayList<>();
            
            // Kiểm tra từng sản phẩm với tồn kho đã load sẵn
            for (ServiceProduct serviceProduct : requiredProducts) {
                UUID productId = serviceProduct.getProduct().getProductId();
                long requiredQuantity = serviceProduct.getQuantity().longValue();
                
                // Lookup trong inventory map (đã load sẵn)
                InventoryLevel inventoryLevel = inventoryMap.get(productId);
                
                if (inventoryLevel == null) {
                    // Sản phẩm không có trong kho
                    missingProducts.add(serviceProduct.getProduct().getProductName());
                } else {
                    // Kiểm tra số lượng available
                    long availableQuantity = inventoryLevel.getAvailable();
                    if (availableQuantity >= requiredQuantity) {
                        availableProducts++;
                    } else {
                        insufficientProducts.add(String.format("%s (Required: %d, Available: %d)",
                            serviceProduct.getProduct().getProductName(),
                            requiredQuantity,
                            availableQuantity));
                    }
                }
            }
            
            // Xác định tính khả dụng
            boolean isAvailable;
            if (requireFullInventory) {
                // Yêu cầu đủ tất cả sản phẩm
                isAvailable = (availableProducts == totalProducts 
                    && missingProducts.isEmpty() 
                    && insufficientProducts.isEmpty());
            } else {
                // Chỉ cần có một phần sản phẩm
                isAvailable = (availableProducts > 0);
            }
            
            return ServiceAvailabilityInfo.builder()
                .id(service.getServiceId())
                .name(service.getServiceName())
                .type("SERVICE")
                .available(isAvailable)
                .totalProductsRequired(totalProducts)
                .availableProductsCount(availableProducts)
                .missingProducts(missingProducts)
                .insufficientProducts(insufficientProducts)
                .serviceInfo(serviceMapper.toServiceInfoDto(service))
                .build();
            
        } catch (Exception e) {
            log.error("Error checking service availability: {}", service.getServiceName(), e);
            return ServiceAvailabilityInfo.builder()
                .id(service.getServiceId())
                .name(service.getServiceName())
                .type("SERVICE")
                .available(false)
                .reason("Error checking availability: " + e.getMessage())
                .serviceInfo(serviceMapper.toServiceInfoDto(service))
                .build();
        }
    }
    
    /**
     * Kiểm tra tính khả dụng của một service (method cũ - vẫn dùng cho các method khác)
     * @deprecated Sử dụng checkServiceAvailabilityWithInventoryMap cho batch check
     */
    private ServiceAvailabilityInfo checkServiceAvailability(com.kltn.scsms_api_service.core.entity.Service service,
                                                             Branch branch, boolean requireFullInventory) {
        try {
            // Get all products required for this service
            List<ServiceProduct> requiredProducts = serviceProductService.findByServiceIdWithProduct(service.getServiceId());
            
            // Nếu dịch vụ không có sản phẩm thì luôn available
            if (requiredProducts == null || requiredProducts.isEmpty()) {
                return ServiceAvailabilityInfo.builder()
                    .id(service.getServiceId())
                    .name(service.getServiceName())
                    .type("SERVICE")
                    .available(true)
                    .totalProductsRequired(0)
                    .availableProductsCount(0)
                    .missingProducts(new ArrayList<>())
                    .insufficientProducts(new ArrayList<>())
                    .serviceInfo(serviceMapper.toServiceInfoDto(service))
                    .build();
            }
            
            int totalProducts = requiredProducts.size();
            int availableProducts = 0;
            List<String> missingProducts = new ArrayList<>();
            List<String> insufficientProducts = new ArrayList<>();
            
            for (ServiceProduct serviceProduct : requiredProducts) {
                UUID productId = serviceProduct.getProduct().getProductId();
                
                // Check inventory level
                var inventoryLevelOpt = inventoryLevelEntityService.find(branch.getBranchId(), productId);
                
                if (inventoryLevelOpt.isEmpty()) {
                    missingProducts.add(serviceProduct.getProduct().getProductName());
                } else {
                    InventoryLevel inventoryLevel = inventoryLevelOpt.get();
                    if (inventoryLevel.getAvailable() >= serviceProduct.getQuantity().longValue()) {
                        availableProducts++;
                    } else {
                        insufficientProducts.add(String.format("%s (Required: %s, Available: %d)",
                            serviceProduct.getProduct().getProductName(),
                            serviceProduct.getQuantity(),
                            inventoryLevel.getAvailable()));
                    }
                }
            }
            
            boolean isAvailable = requireFullInventory
                ? (availableProducts == totalProducts && missingProducts.isEmpty()
                && insufficientProducts.isEmpty())
                : (availableProducts > 0);
            
            return ServiceAvailabilityInfo.builder()
                .id(service.getServiceId())
                .name(service.getServiceName())
                .type("SERVICE")
                .available(isAvailable)
                .totalProductsRequired(totalProducts)
                .availableProductsCount(availableProducts)
                .missingProducts(missingProducts)
                .insufficientProducts(insufficientProducts)
                .serviceInfo(serviceMapper.toServiceInfoDto(service))
                .build();
            
        } catch (Exception e) {
            log.error("Error checking service availability: {}", service.getServiceName(), e);
            return ServiceAvailabilityInfo.builder()
                .id(service.getServiceId())
                .name(service.getServiceName())
                .type("SERVICE")
                .available(false)
                .reason("Error checking availability: " + e.getMessage())
                .serviceInfo(serviceMapper.toServiceInfoDto(service))
                .build();
        }
    }
    
}
