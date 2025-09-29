package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import com.kltn.scsms_api_service.core.repository.ServiceProductRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceProductService {
    
    private final ServiceProductRepository serviceProductRepository;
    
    public List<ServiceProduct> findAll() {
        log.info("Finding all service products");
        return serviceProductRepository.findAll();
    }
    
    public Page<ServiceProduct> findAll(Pageable pageable) {
        log.info("Finding all service products with pagination: {}", pageable);
        return serviceProductRepository.findAll(pageable);
    }
    
    public Optional<ServiceProduct> findById(UUID serviceProductId) {
        log.info("Finding service product by ID: {}", serviceProductId);
        return serviceProductRepository.findById(serviceProductId);
    }
    
    public ServiceProduct getById(UUID serviceProductId) {
        log.info("Getting service product by ID: {}", serviceProductId);
        return findById(serviceProductId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PRODUCT_NOT_FOUND, 
                "Service product not found with ID: " + serviceProductId));
    }
    
    public List<ServiceProduct> findByServiceId(UUID serviceId) {
        log.info("Finding service products by service ID: {}", serviceId);
        return serviceProductRepository.findByServiceServiceIdAndIsActiveTrue(serviceId);
    }
    
    public List<ServiceProduct> findByServiceIdOrdered(UUID serviceId) {
        log.info("Finding service products by service ID ordered: {}", serviceId);
        return serviceProductRepository.findByServiceServiceIdOrdered(serviceId);
    }
    
    public List<ServiceProduct> findRequiredProductsByServiceId(UUID serviceId) {
        log.info("Finding required products by service ID: {}", serviceId);
        return serviceProductRepository.findRequiredProductsByServiceId(serviceId);
    }
    
    public List<ServiceProduct> findOptionalProductsByServiceId(UUID serviceId) {
        log.info("Finding optional products by service ID: {}", serviceId);
        return serviceProductRepository.findOptionalProductsByServiceId(serviceId);
    }
    
    public List<ServiceProduct> findByProductId(UUID productId) {
        log.info("Finding service products by product ID: {}", productId);
        return serviceProductRepository.findByProductProductIdAndIsActiveTrue(productId);
    }
    
    public Optional<ServiceProduct> findByServiceIdAndProductId(UUID serviceId, UUID productId) {
        log.info("Finding service product by service ID: {} and product ID: {}", serviceId, productId);
        return serviceProductRepository.findByServiceServiceIdAndProductProductId(serviceId, productId);
    }
    
    public boolean existsByServiceIdAndProductId(UUID serviceId, UUID productId) {
        log.info("Checking if service product exists by service ID: {} and product ID: {}", serviceId, productId);
        return serviceProductRepository.existsByServiceServiceIdAndProductProductId(serviceId, productId);
    }
    
    public BigDecimal calculateTotalProductCostByServiceId(UUID serviceId) {
        log.info("Calculating total product cost by service ID: {}", serviceId);
        return serviceProductRepository.calculateTotalProductCostByServiceId(serviceId);
    }
    
    public long countByServiceId(UUID serviceId) {
        log.info("Counting service products by service ID: {}", serviceId);
        return serviceProductRepository.countByServiceServiceIdAndIsActiveTrue(serviceId);
    }
    
    public long countByProductId(UUID productId) {
        log.info("Counting service products by product ID: {}", productId);
        return serviceProductRepository.countByProductProductIdAndIsActiveTrue(productId);
    }
    
    @Transactional
    public ServiceProduct save(ServiceProduct serviceProduct) {
        log.info("Saving service product for service: {} and product: {}", 
            serviceProduct.getService().getServiceId(), serviceProduct.getProduct().getProductId());
        
        // Calculate total price before saving
        serviceProduct.updateTotalPrice();
        
        return serviceProductRepository.save(serviceProduct);
    }
    
    @Transactional
    public ServiceProduct update(ServiceProduct serviceProduct) {
        log.info("Updating service product: {}", serviceProduct.getServiceProductId());
        
        // Calculate total price before updating
        serviceProduct.updateTotalPrice();
        
        return serviceProductRepository.save(serviceProduct);
    }
    
    @Transactional
    public void deleteById(UUID serviceProductId) {
        log.info("Deleting service product by ID: {}", serviceProductId);
        if (!serviceProductRepository.existsById(serviceProductId)) {
            throw new ClientSideException(ErrorCode.SERVICE_PRODUCT_NOT_FOUND, 
                "Service product not found with ID: " + serviceProductId);
        }
        serviceProductRepository.deleteById(serviceProductId);
    }
    
    @Transactional
    public void softDeleteById(UUID serviceProductId) {
        log.info("Soft deleting service product by ID: {}", serviceProductId);
        ServiceProduct serviceProduct = getById(serviceProductId);
        serviceProduct.setIsActive(false);
        serviceProduct.setIsDeleted(true);
        serviceProductRepository.save(serviceProduct);
    }
    
    @Transactional
    public void activateById(UUID serviceProductId) {
        log.info("Activating service product by ID: {}", serviceProductId);
        ServiceProduct serviceProduct = getById(serviceProductId);
        serviceProduct.setIsActive(true);
        serviceProduct.setIsDeleted(false);
        serviceProductRepository.save(serviceProduct);
    }
    
    @Transactional
    public void deactivateById(UUID serviceProductId) {
        log.info("Deactivating service product by ID: {}", serviceProductId);
        ServiceProduct serviceProduct = getById(serviceProductId);
        serviceProduct.setIsActive(false);
        serviceProductRepository.save(serviceProduct);
    }
    
    @Transactional
    public void deleteByServiceId(UUID serviceId) {
        log.info("Deleting all service products by service ID: {}", serviceId);
        List<ServiceProduct> serviceProducts = findByServiceId(serviceId);
        for (ServiceProduct serviceProduct : serviceProducts) {
            softDeleteById(serviceProduct.getServiceProductId());
        }
    }
}
