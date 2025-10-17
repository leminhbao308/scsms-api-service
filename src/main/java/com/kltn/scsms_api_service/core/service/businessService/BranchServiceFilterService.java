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
import java.util.List;
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
     * Kiểm tra tính khả dụng của một service
     */
    private ServiceAvailabilityInfo checkServiceAvailability(com.kltn.scsms_api_service.core.entity.Service service,
                                                             Branch branch, boolean requireFullInventory) {
        try {
            // Get all products required for this service
            List<ServiceProduct> requiredProducts = serviceProductService.findByServiceIdWithProduct(service.getServiceId());
            
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
