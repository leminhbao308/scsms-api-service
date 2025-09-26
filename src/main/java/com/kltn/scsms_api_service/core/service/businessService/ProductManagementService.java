package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.productManagement.param.ProductFilterParam;
import com.kltn.scsms_api_service.core.dto.productManagement.request.CreateProductRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.UpdateProductRequest;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductManagementService {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    
    public List<ProductInfoDto> getAllProducts() {
        log.info("Getting all products");
        List<Product> products = productService.findAll();
        return products.stream()
                .map(productMapper::toProductInfoDto)
                .collect(Collectors.toList());
    }
    
    public Page<ProductInfoDto> getAllProducts(ProductFilterParam filterParam) {
        log.info("Getting all products with filter: {}", filterParam);
        
        // Standardize filter
        filterParam = filterParam.standardizeFilterRequest(filterParam);
        
        // Create pageable
        Sort sort = Sort.by(
            filterParam.getDirection().equalsIgnoreCase("DESC") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, 
            filterParam.getSort()
        );
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);
        
        // Get products (simplified - in real implementation, you'd use custom repository methods)
        Page<Product> productPage = productService.findAll(pageable);
        
        return productPage.map(productMapper::toProductInfoDto);
    }
    
    public ProductInfoDto getProductById(UUID productId) {
        log.info("Getting product by ID: {}", productId);
        Product product = productService.getById(productId);
        return productMapper.toProductInfoDto(product);
    }
    
    public ProductInfoDto getProductByUrl(String productUrl) {
        log.info("Getting product by URL: {}", productUrl);
        Product product = productService.getByProductUrl(productUrl);
        return productMapper.toProductInfoDto(product);
    }
    
    public List<ProductInfoDto> getProductsByCategory(UUID categoryId) {
        log.info("Getting products by category ID: {}", categoryId);
        List<Product> products = productService.findByCategoryId(categoryId);
        return products.stream()
                .map(productMapper::toProductInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductInfoDto> getProductsBySupplier(UUID supplierId) {
        log.info("Getting products by supplier ID: {}", supplierId);
        List<Product> products = productService.findBySupplierId(supplierId);
        return products.stream()
                .map(productMapper::toProductInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductInfoDto> searchProducts(String keyword) {
        log.info("Searching products by keyword: {}", keyword);
        List<Product> products = productService.searchByKeyword(keyword);
        return products.stream()
                .map(productMapper::toProductInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductInfoDto> getFeaturedProducts() {
        log.info("Getting featured products");
        List<Product> products = productService.findFeaturedProducts();
        return products.stream()
                .map(productMapper::toProductInfoDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductInfoDto> getLowStockProducts() {
        log.info("Getting low stock products");
        List<Product> products = productService.findLowStockProducts();
        return products.stream()
                .map(productMapper::toProductInfoDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ProductInfoDto createProduct(CreateProductRequest createProductRequest) {
        log.info("Creating product: {}", createProductRequest.getProductName());
        
        // Validate product URL uniqueness
        if (productService.existsByProductUrl(createProductRequest.getProductUrl())) {
            throw new ClientSideException(ErrorCode.PRODUCT_URL_EXISTS, 
                "Product URL already exists: " + createProductRequest.getProductUrl());
        }
        
        // Validate SKU uniqueness if provided
        if (createProductRequest.getSku() != null && 
            productService.existsBySku(createProductRequest.getSku())) {
            throw new ClientSideException(ErrorCode.PRODUCT_SKU_EXISTS, 
                "Product SKU already exists: " + createProductRequest.getSku());
        }
        
        // Validate barcode uniqueness if provided
        if (createProductRequest.getBarcode() != null && 
            productService.existsByBarcode(createProductRequest.getBarcode())) {
            throw new ClientSideException(ErrorCode.PRODUCT_BARCODE_EXISTS, 
                "Product barcode already exists: " + createProductRequest.getBarcode());
        }
        
        // Validate category exists if provided
        Category category = null;
        if (createProductRequest.getCategoryId() != null) {
            category = categoryService.getById(createProductRequest.getCategoryId());
        }
        
        // Create product
        Product product = productMapper.toEntity(createProductRequest);
        product.setCategory(category);
        
        // Set default values
        if (product.getMinStockLevel() == null) {
            product.setMinStockLevel(0);
        }
        if (product.getIsTrackable() == null) {
            product.setIsTrackable(false);
        }
        if (product.getIsConsumable() == null) {
            product.setIsConsumable(true);
        }
        if (product.getIsFeatured() == null) {
            product.setIsFeatured(false);
        }
        if (product.getSortOrder() == null) {
            product.setSortOrder(0);
        }
        
        Product savedProduct = productService.save(product);
        return productMapper.toProductInfoDto(savedProduct);
    }
    
    @Transactional
    public ProductInfoDto updateProduct(UUID productId, UpdateProductRequest updateProductRequest) {
        log.info("Updating product with ID: {}", productId);
        
        // Get existing product
        Product existingProduct = productService.getById(productId);
        
        // Validate product URL uniqueness if changed
        if (updateProductRequest.getProductUrl() != null && 
            !updateProductRequest.getProductUrl().equals(existingProduct.getProductUrl()) &&
            productService.existsByProductUrl(updateProductRequest.getProductUrl())) {
            throw new ClientSideException(ErrorCode.PRODUCT_URL_EXISTS, 
                "Product URL already exists: " + updateProductRequest.getProductUrl());
        }
        
        // Validate SKU uniqueness if changed
        if (updateProductRequest.getSku() != null && 
            !updateProductRequest.getSku().equals(existingProduct.getSku()) &&
            productService.existsBySku(updateProductRequest.getSku())) {
            throw new ClientSideException(ErrorCode.PRODUCT_SKU_EXISTS, 
                "Product SKU already exists: " + updateProductRequest.getSku());
        }
        
        // Validate barcode uniqueness if changed
        if (updateProductRequest.getBarcode() != null && 
            !updateProductRequest.getBarcode().equals(existingProduct.getBarcode()) &&
            productService.existsByBarcode(updateProductRequest.getBarcode())) {
            throw new ClientSideException(ErrorCode.PRODUCT_BARCODE_EXISTS, 
                "Product barcode already exists: " + updateProductRequest.getBarcode());
        }
        
        // Validate category exists if provided
        if (updateProductRequest.getCategoryId() != null) {
            Category category = categoryService.getById(updateProductRequest.getCategoryId());
            existingProduct.setCategory(category);
        }
        
        // Update product
        Product updatedProduct = productMapper.updateEntity(existingProduct, updateProductRequest);
        Product savedProduct = productService.update(updatedProduct);
        
        return productMapper.toProductInfoDto(savedProduct);
    }
    
    @Transactional
    public void deleteProduct(UUID productId) {
        log.info("Deleting product with ID: {}", productId);
        productService.softDeleteById(productId);
    }
    
    @Transactional
    public void activateProduct(UUID productId) {
        log.info("Activating product with ID: {}", productId);
        productService.activateById(productId);
    }
    
    @Transactional
    public void deactivateProduct(UUID productId) {
        log.info("Deactivating product with ID: {}", productId);
        productService.deactivateById(productId);
    }
    
    public long getProductCountByCategory(UUID categoryId) {
        log.info("Getting product count by category ID: {}", categoryId);
        return productService.countByCategoryId(categoryId);
    }
    
    public long getProductCountBySupplier(UUID supplierId) {
        log.info("Getting product count by supplier ID: {}", supplierId);
        return productService.countBySupplierId(supplierId);
    }
}