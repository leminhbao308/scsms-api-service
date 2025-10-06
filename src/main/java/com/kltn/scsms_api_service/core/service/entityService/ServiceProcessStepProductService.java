package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.ServiceProcessStepProduct;
import com.kltn.scsms_api_service.core.repository.ServiceProcessStepProductRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceProcessStepProductService {
    
    private final ServiceProcessStepProductRepository serviceProcessStepProductRepository;
    private final ProductService productService;
    
    /**
     * Tìm service process step product theo ID
     */
    public Optional<ServiceProcessStepProduct> findById(UUID id) {
        return serviceProcessStepProductRepository.findById(id);
    }
    
    /**
     * Tìm service process step product theo ID và throw exception nếu không tìm thấy
     */
    public ServiceProcessStepProduct findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PROCESS_STEP_PRODUCT_NOT_FOUND));
    }
    
    /**
     * Tìm tất cả sản phẩm của một bước
     */
    public List<ServiceProcessStepProduct> findByStepId(UUID stepId) {
        return serviceProcessStepProductRepository.findByServiceProcessStepIdAndIsDeletedFalseOrderByCreatedDateAsc(stepId);
    }
    
    /**
     * Tìm sản phẩm theo bước và sản phẩm
     */
    public Optional<ServiceProcessStepProduct> findByStepIdAndProductId(UUID stepId, UUID productId) {
        return serviceProcessStepProductRepository.findByServiceProcessStepIdAndProductIdAndIsDeletedFalse(stepId, productId);
    }
    
    /**
     * Tìm tất cả sản phẩm của một service process
     */
    public List<ServiceProcessStepProduct> findByProcessId(UUID processId) {
        return serviceProcessStepProductRepository.findByServiceProcessId(processId);
    }
    
    /**
     * Tìm sản phẩm theo tên sản phẩm trong một bước
     */
    public Page<ServiceProcessStepProduct> findByProductNameContainingAndStepId(UUID stepId, String productName, Pageable pageable) {
        return serviceProcessStepProductRepository.findByProductNameContainingIgnoreCaseAndStepId(stepId, productName, pageable);
    }
    
    /**
     * Tìm sản phẩm theo mã sản phẩm trong một bước
     */
    public Page<ServiceProcessStepProduct> findByProductSkuContainingAndStepId(UUID stepId, String productSku, Pageable pageable) {
        return serviceProcessStepProductRepository.findByProductSkuContainingIgnoreCaseAndStepId(stepId, productSku, pageable);
    }
    
    /**
     * Tìm sản phẩm theo đơn vị tính
     */
    public List<ServiceProcessStepProduct> findByUnitAndStepId(UUID stepId, String unit) {
        return serviceProcessStepProductRepository.findByUnitAndStepId(stepId, unit);
    }
    
    /**
     * Tìm sản phẩm theo số lượng
     */
    public List<ServiceProcessStepProduct> findByQuantityBetweenAndStepId(UUID stepId, Double minQuantity, Double maxQuantity) {
        return serviceProcessStepProductRepository.findByQuantityBetweenAndStepId(stepId, minQuantity, maxQuantity);
    }
    
    /**
     * Tìm sản phẩm theo thương hiệu trong một bước
     */
    public List<ServiceProcessStepProduct> findByProductBrandContainingAndStepId(UUID stepId, String brand) {
        return serviceProcessStepProductRepository.findByProductBrandContainingIgnoreCaseAndStepId(stepId, brand);
    }
    
    /**
     * Tìm tất cả sản phẩm được sử dụng trong các bước
     */
    public List<Object> findDistinctProductsUsedInSteps() {
        return serviceProcessStepProductRepository.findDistinctProductsUsedInSteps();
    }
    
    /**
     * Tìm sản phẩm được sử dụng nhiều nhất
     */
    public List<Object[]> findMostUsedProducts() {
        return serviceProcessStepProductRepository.findMostUsedProducts();
    }
    
    /**
     * Đếm số sản phẩm của một bước
     */
    public long countProductsByStepId(UUID stepId) {
        return serviceProcessStepProductRepository.countProductsByStepId(stepId);
    }
    
    /**
     * Đếm số sản phẩm của một service process
     */
    public long countProductsByProcessId(UUID processId) {
        return serviceProcessStepProductRepository.countProductsByProcessId(processId);
    }
    
    /**
     * Tính tổng số lượng sản phẩm của một bước
     */
    public Double sumQuantityByStepId(UUID stepId) {
        return serviceProcessStepProductRepository.sumQuantityByStepId(stepId);
    }
    
    /**
     * Tính tổng số lượng sản phẩm của một service process
     */
    public Double sumQuantityByProcessId(UUID processId) {
        return serviceProcessStepProductRepository.sumQuantityByProcessId(processId);
    }
    
    /**
     * Lưu service process step product
     */
    @Transactional
    public ServiceProcessStepProduct save(ServiceProcessStepProduct serviceProcessStepProduct) {
        log.info("Saving service process step product: {}", serviceProcessStepProduct.getProductName());
        return serviceProcessStepProductRepository.save(serviceProcessStepProduct);
    }
    
    /**
     * Cập nhật service process step product
     */
    @Transactional
    public ServiceProcessStepProduct update(ServiceProcessStepProduct serviceProcessStepProduct) {
        log.info("Updating service process step product: {}", serviceProcessStepProduct.getProductName());
        return serviceProcessStepProductRepository.save(serviceProcessStepProduct);
    }
    
    /**
     * Xóa mềm service process step product
     */
    @Transactional
    public void delete(ServiceProcessStepProduct serviceProcessStepProduct) {
        log.info("Deleting service process step product: {}", serviceProcessStepProduct.getProductName());
        serviceProcessStepProduct.setIsDeleted(true);
        serviceProcessStepProductRepository.save(serviceProcessStepProduct);
    }
    
    /**
     * Xóa mềm service process step product theo ID
     */
    @Transactional
    public void deleteById(UUID id) {
        ServiceProcessStepProduct serviceProcessStepProduct = findByIdOrThrow(id);
        delete(serviceProcessStepProduct);
    }
    
    /**
     * Kiểm tra sản phẩm đã tồn tại trong bước chưa (trừ id hiện tại)
     */
    public boolean existsByStepIdAndProductIdAndIdNot(UUID stepId, UUID productId, UUID id) {
        return serviceProcessStepProductRepository.existsByStepIdAndProductIdAndIdNot(stepId, productId, id);
    }
    
    /**
     * Thêm sản phẩm vào bước
     */
    @Transactional
    public ServiceProcessStepProduct addProductToStep(UUID stepId, UUID productId, java.math.BigDecimal quantity, String unit) {
        // Kiểm tra sản phẩm đã tồn tại trong bước chưa
        Optional<ServiceProcessStepProduct> existingProduct = findByStepIdAndProductId(stepId, productId);
        if (existingProduct.isPresent()) {
            throw new ClientSideException(ErrorCode.SERVICE_PROCESS_STEP_PRODUCT_ALREADY_EXISTS);
        }
        
        // Lấy thông tin sản phẩm
        Product product = productService.getById(productId);
        
        // Tạo service process step product mới
        ServiceProcessStepProduct stepProduct = ServiceProcessStepProduct.builder()
                .product(product)
                .quantity(quantity)
                .unit(unit)
                .build();
        
        return save(stepProduct);
    }
    
    /**
     * Cập nhật số lượng sản phẩm trong bước
     */
    @Transactional
    public ServiceProcessStepProduct updateProductQuantity(UUID stepProductId, java.math.BigDecimal newQuantity) {
        ServiceProcessStepProduct stepProduct = findByIdOrThrow(stepProductId);
        stepProduct.setQuantity(newQuantity);
        return update(stepProduct);
    }
    
    /**
     * Xóa sản phẩm khỏi bước
     */
    @Transactional
    public void removeProductFromStep(UUID stepId, UUID productId) {
        Optional<ServiceProcessStepProduct> stepProduct = findByStepIdAndProductId(stepId, productId);
        if (stepProduct.isPresent()) {
            delete(stepProduct.get());
        }
    }
}
