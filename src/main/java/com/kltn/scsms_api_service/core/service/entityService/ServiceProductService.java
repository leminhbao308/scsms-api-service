package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import com.kltn.scsms_api_service.core.repository.ServiceProductRepository;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
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
public class ServiceProductService {
    
    private final ServiceProductRepository serviceProductRepository;
    private final ProductService productService;
    
    /**
     * Tìm service product theo ID
     */
    public Optional<ServiceProduct> findById(UUID id) {
        return serviceProductRepository.findById(id);
    }
    
    /**
     * Tìm service product theo ID và throw exception nếu không tìm thấy
     */
    public ServiceProduct findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ClientSideException(ErrorCode.SERVICE_PRODUCT_NOT_FOUND));
    }
    
    /**
     * Tìm tất cả sản phẩm của một service
     */
    public List<ServiceProduct> findByServiceId(UUID serviceId) {
        return serviceProductRepository.findByServiceServiceIdAndIsDeletedFalseOrderBySortOrderAsc(serviceId);
    }
    
    /**
     * Tìm tất cả service product với product được load
     */
    public List<ServiceProduct> findByServiceIdWithProduct(UUID serviceId) {
        return serviceProductRepository.findByServiceIdWithProduct(serviceId);
    }
    
    /**
     * Tìm sản phẩm theo service và product
     */
    public Optional<ServiceProduct> findByServiceIdAndProductId(UUID serviceId, UUID productId) {
        return serviceProductRepository.findByServiceIdAndProductIdAndIsDeletedFalse(serviceId, productId);
    }
    
    /**
     * Tìm tất cả service sử dụng một product
     */
    public List<ServiceProduct> findByProductId(UUID productId) {
        return serviceProductRepository.findByProductIdAndIsDeletedFalse(productId);
    }
    
    /**
     * Tìm sản phẩm theo tên sản phẩm trong một service
     */
    public Page<ServiceProduct> findByServiceIdAndProductNameContaining(UUID serviceId, String productName, Pageable pageable) {
        return serviceProductRepository.findByServiceIdAndProductNameContainingIgnoreCase(serviceId, productName, pageable);
    }
    
    /**
     * Tìm sản phẩm theo mã sản phẩm trong một service
     */
    public Page<ServiceProduct> findByServiceIdAndProductSkuContaining(UUID serviceId, String productSku, Pageable pageable) {
        return serviceProductRepository.findByServiceIdAndProductSkuContainingIgnoreCase(serviceId, productSku, pageable);
    }
    
    /**
     * Tìm sản phẩm theo đơn vị tính
     */
    public List<ServiceProduct> findByUnitAndServiceId(UUID serviceId, String unit) {
        return serviceProductRepository.findByUnitAndServiceServiceIdAndIsDeletedFalse(unit, serviceId);
    }
    
    /**
     * Tìm sản phẩm theo số lượng
     */
    public List<ServiceProduct> findByServiceIdAndQuantityBetween(UUID serviceId, Double minQuantity, Double maxQuantity) {
        return serviceProductRepository.findByServiceIdAndQuantityBetween(serviceId, minQuantity, maxQuantity);
    }
    
    /**
     * Tìm sản phẩm theo thương hiệu trong một service
     */
    public List<ServiceProduct> findByServiceIdAndProductBrandContaining(UUID serviceId, String brand) {
        return serviceProductRepository.findByServiceIdAndProductBrandContainingIgnoreCase(serviceId, brand);
    }
    
    /**
     * Tìm tất cả sản phẩm được sử dụng trong các service
     */
    public List<Object> findDistinctProductsUsedInServices() {
        return serviceProductRepository.findDistinctProductsUsedInServices();
    }
    
    /**
     * Tìm sản phẩm được sử dụng nhiều nhất
     */
    public List<Object[]> findMostUsedProducts() {
        return serviceProductRepository.findMostUsedProducts();
    }
    
    /**
     * Đếm số sản phẩm của một service
     */
    public long countByServiceId(UUID serviceId) {
        return serviceProductRepository.countByServiceServiceIdAndIsDeletedFalse(serviceId);
    }
    
    /**
     * Tính tổng số lượng sản phẩm của một service
     */
    public Double sumQuantityByServiceId(UUID serviceId) {
        return serviceProductRepository.sumQuantityByServiceId(serviceId);
    }
    
    /**
     * Lưu service product
     */
    @Transactional
    public ServiceProduct save(ServiceProduct serviceProduct) {
        log.info("Saving service product: {}", serviceProduct.getProductName());
        return serviceProductRepository.save(serviceProduct);
    }
    
    /**
     * Cập nhật service product
     */
    @Transactional
    public ServiceProduct update(ServiceProduct serviceProduct) {
        log.info("Updating service product: {}", serviceProduct.getProductName());
        return serviceProductRepository.save(serviceProduct);
    }
    
    /**
     * Xóa mềm service product
     */
    @Transactional
    public void delete(ServiceProduct serviceProduct) {
        log.info("Deleting service product: {}", serviceProduct.getProductName());
        serviceProduct.setIsDeleted(true);
        serviceProductRepository.save(serviceProduct);
    }
    
    /**
     * Xóa hẳn service product theo ID
     */
    @Transactional
    public void deleteById(UUID id) {
        ServiceProduct serviceProduct = findByIdOrThrow(id);
        serviceProductRepository.delete(serviceProduct);
    }
    
    /**
     * Kiểm tra sản phẩm đã tồn tại trong service chưa (trừ id hiện tại)
     */
    public boolean existsByServiceIdAndProductIdAndIdNot(UUID serviceId, UUID productId, UUID id) {
        return serviceProductRepository.existsByServiceIdAndProductIdAndIdNot(serviceId, productId, id);
    }
    
    /**
     * Thêm sản phẩm vào service
     */
    @Transactional
    public ServiceProduct addProductToService(UUID serviceId, UUID productId, java.math.BigDecimal quantity, String unit, String notes, Boolean isRequired, Integer sortOrder) {
        // Kiểm tra sản phẩm đã tồn tại trong service chưa
        Optional<ServiceProduct> existingProduct = findByServiceIdAndProductId(serviceId, productId);
        if (existingProduct.isPresent()) {
            throw new ClientSideException(ErrorCode.SERVICE_PRODUCT_ALREADY_EXISTS);
        }
        
        // Lấy thông tin sản phẩm
        Product product = productService.getById(productId);
        
        // Tạo service product mới
        ServiceProduct serviceProduct = ServiceProduct.builder()
                .product(product)
                .quantity(quantity)
                .unit(unit)
                .notes(notes)
                .isRequired(isRequired != null ? isRequired : true)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .build();
        
        return save(serviceProduct);
    }
    
    /**
     * Cập nhật số lượng sản phẩm trong service
     */
    @Transactional
    public ServiceProduct updateProductQuantity(UUID serviceProductId, java.math.BigDecimal newQuantity) {
        ServiceProduct serviceProduct = findByIdOrThrow(serviceProductId);
        serviceProduct.setQuantity(newQuantity);
        return update(serviceProduct);
    }
    
    /**
     * Xóa sản phẩm khỏi service
     */
    @Transactional
    public void removeProductFromService(UUID serviceId, UUID productId) {
        Optional<ServiceProduct> serviceProduct = findByServiceIdAndProductId(serviceId, productId);
        if (serviceProduct.isPresent()) {
            serviceProductRepository.delete(serviceProduct.get());
        }
    }
}
