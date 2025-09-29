package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ServicePackageProduct;
import com.kltn.scsms_api_service.core.repository.ServicePackageProductRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePackageProductService {
    
    private final ServicePackageProductRepository servicePackageProductRepository;
    
    public List<ServicePackageProduct> findByPackageIdOrdered(UUID packageId) {
        log.info("Finding service package products by package ID: {}", packageId);
        return servicePackageProductRepository.findByPackageIdOrdered(packageId);
    }
    
    public Optional<ServicePackageProduct> findByPackageIdAndProductId(UUID packageId, UUID productId) {
        log.info("Finding service package product by package ID: {} and product ID: {}", packageId, productId);
        return servicePackageProductRepository.findByPackageIdAndProductId(packageId, productId);
    }
    
    public ServicePackageProduct getByPackageIdAndProductId(UUID packageId, UUID productId) {
        log.info("Getting service package product by package ID: {} and product ID: {}", packageId, productId);
        return findByPackageIdAndProductId(packageId, productId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_PRODUCT_NOT_FOUND, 
                "Service package product not found with package ID: " + packageId + " and product ID: " + productId));
    }
    
    public boolean existsByPackageIdAndProductId(UUID packageId, UUID productId) {
        log.info("Checking if service package product exists by package ID: {} and product ID: {}", packageId, productId);
        return servicePackageProductRepository.existsByServicePackagePackageIdAndProductProductIdAndIsActiveTrue(packageId, productId);
    }
    
    public List<ServicePackageProduct> findByProductId(UUID productId) {
        log.info("Finding service package products by product ID: {}", productId);
        return servicePackageProductRepository.findByProductId(productId);
    }
    
    public long countByPackageId(UUID packageId) {
        log.info("Counting service package products by package ID: {}", packageId);
        return servicePackageProductRepository.countByServicePackagePackageIdAndIsActiveTrue(packageId);
    }
    
    @Transactional
    public ServicePackageProduct save(ServicePackageProduct servicePackageProduct) {
        log.info("Saving service package product for package ID: {} and product ID: {}", 
            servicePackageProduct.getServicePackage().getPackageId(), 
            servicePackageProduct.getProduct().getProductId());
        
        // Calculate total price before saving
        servicePackageProduct.updateTotalPrice();
        
        return servicePackageProductRepository.save(servicePackageProduct);
    }
    
    @Transactional
    public ServicePackageProduct update(ServicePackageProduct servicePackageProduct) {
        log.info("Updating service package product with ID: {}", servicePackageProduct.getServicePackageProductId());
        
        // Recalculate total price before updating
        servicePackageProduct.updateTotalPrice();
        
        return servicePackageProductRepository.save(servicePackageProduct);
    }
    
    @Transactional
    public void softDeleteById(UUID servicePackageProductId) {
        log.info("Soft deleting service package product by ID: {}", servicePackageProductId);
        ServicePackageProduct servicePackageProduct = servicePackageProductRepository.findById(servicePackageProductId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PACKAGE_PRODUCT_NOT_FOUND, 
                "Service package product not found with ID: " + servicePackageProductId));
        
        servicePackageProduct.setIsActive(false);
        servicePackageProduct.setIsDeleted(true);
        servicePackageProductRepository.save(servicePackageProduct);
    }
}
