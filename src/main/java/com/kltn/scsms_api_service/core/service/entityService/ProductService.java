package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.repository.ProductRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findAll() {
        log.info("Finding all products");
        return productRepository.findAll();
    }

    public Page<Product> findAll(Pageable pageable) {
        log.info("Finding all products with pagination: {}", pageable);
        return productRepository.findAll(pageable);
    }

    public Page<Product> findAll(Specification<Product> spec, Pageable pageable) {
        log.info("Finding all products with specification and pagination: {}", pageable);
        return productRepository.findAll(spec, pageable);
    }

    public Optional<Product> findById(UUID productId) {
        log.info("Finding product by ID: {}", productId);
        return productRepository.findById(productId);
    }

    public Product getById(UUID productId) {
        log.info("Getting product by ID: {}", productId);
        return findById(productId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.PRODUCT_NOT_FOUND,
                        "Product not found with ID: " + productId));
    }

    public Optional<Product> findByProductUrl(String productUrl) {
        log.info("Finding product by URL: {}", productUrl);
        return productRepository.findByProductUrlAndIsDeletedFalse(productUrl);
    }

    public Product getByProductUrl(String productUrl) {
        log.info("Getting product by URL: {}", productUrl);
        return findByProductUrl(productUrl)
                .orElseThrow(() -> new ClientSideException(ErrorCode.PRODUCT_NOT_FOUND,
                        "Product not found with URL: " + productUrl));
    }

    public Optional<Product> findBySku(String sku) {
        log.info("Finding product by SKU: {}", sku);
        return productRepository.findBySkuAndIsDeletedFalse(sku);
    }

    public Optional<Product> findByBarcode(String barcode) {
        log.info("Finding product by barcode: {}", barcode);
        return productRepository.findByBarcodeAndIsDeletedFalse(barcode);
    }

    public List<Product> findByProductTypeId(UUID productTypeId) {
        log.info("Finding products by product type ID: {}", productTypeId);
        return productRepository.findByProductTypeProductTypeIdAndIsDeletedFalse(productTypeId);
    }

    public List<Product> findBySupplierId(UUID supplierId) {
        log.info("Finding products by supplier ID: {}", supplierId);
        return productRepository.findBySupplierIdAndIsDeletedFalse(supplierId);
    }

    public List<Product> findByBrand(String brand) {
        log.info("Finding products by brand: {}", brand);
        return productRepository.findByBrandAndIsDeletedFalse(brand);
    }

    public List<Product> findFeaturedProducts() {
        log.info("Finding featured products");
        return productRepository.findByIsFeaturedTrueAndIsActiveTrueAndIsDeletedFalse();
    }

    public List<Product> searchByKeyword(String keyword) {
        log.info("Searching products by keyword: {}", keyword);
        return productRepository.searchByKeyword(keyword);
    }

    public boolean existsByProductUrl(String productUrl) {
        log.info("Checking if product exists by URL: {}", productUrl);
        return productRepository.existsByProductUrlAndIsDeletedFalse(productUrl);
    }

    public boolean existsBySku(String sku) {
        log.info("Checking if product exists by SKU: {}", sku);
        return productRepository.existsBySkuAndIsDeletedFalse(sku);
    }

    public boolean existsByBarcode(String barcode) {
        log.info("Checking if product exists by barcode: {}", barcode);
        return productRepository.existsByBarcodeAndIsDeletedFalse(barcode);
    }

    public long countByProductTypeId(UUID productTypeId) {
        log.info("Counting products by product type ID: {}", productTypeId);
        return productRepository.countByProductTypeProductTypeIdAndIsDeletedFalse(productTypeId);
    }

    public long countBySupplierId(UUID supplierId) {
        log.info("Counting products by supplier ID: {}", supplierId);
        return productRepository.countBySupplierIdAndIsDeletedFalse(supplierId);
    }

    @Transactional
    public Product save(Product product) {
        log.info("Saving product: {}", product.getProductName());
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Product product) {
        log.info("Updating product: {}", product.getProductName());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteById(UUID productId) {
        log.info("Deleting product by ID: {}", productId);
        if (!productRepository.existsById(productId)) {
            throw new ClientSideException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + productId);
        }
        productRepository.deleteById(productId);
    }

    @Transactional
    public void softDeleteById(UUID productId) {
        log.info("Soft deleting product by ID: {}", productId);
        Product product = getById(productId);
        product.setIsActive(false);
        product.setIsDeleted(true);
        productRepository.save(product);
    }

    @Transactional
    public void activateById(UUID productId) {
        log.info("Activating product by ID: {}", productId);
        Product product = getById(productId);
        product.setIsActive(true);
        product.setIsDeleted(false);
        productRepository.save(product);
    }

    @Transactional
    public void deactivateById(UUID productId) {
        log.info("Deactivating product by ID: {}", productId);
        Product product = getById(productId);
        product.setIsActive(false);
        productRepository.save(product);
    }

    public Product getRefByProductId(UUID productId) {
        return productRepository.getReferenceById(productId);
    }
}
