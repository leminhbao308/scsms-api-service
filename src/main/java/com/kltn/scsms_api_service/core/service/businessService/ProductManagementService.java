package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.productManagement.param.ProductFilterParam;
import com.kltn.scsms_api_service.core.dto.productManagement.request.CreateProductRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.UpdateProductRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.ProductStatusUpdateRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.ProductAttributeValueRequest;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.ProductType;
import com.kltn.scsms_api_service.core.entity.ProductAttribute;
import com.kltn.scsms_api_service.core.entity.ProductAttributeValue;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.ProductTypeService;
import com.kltn.scsms_api_service.core.service.entityService.ProductAttributeService;
import com.kltn.scsms_api_service.core.service.entityService.ProductAttributeValueService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ProductMapper;
import com.kltn.scsms_api_service.mapper.ProductAttributeValueMapper;
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
    private final ProductTypeService productTypeService;
    private final ProductAttributeService productAttributeService;
    private final ProductAttributeValueService productAttributeValueService;
    private final ProductMapper productMapper;
    private final ProductAttributeValueMapper productAttributeValueMapper;

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

    public List<ProductInfoDto> getProductsByProductType(UUID productTypeId) {
        log.info("Getting products by product type ID: {}", productTypeId);
        List<Product> products = productService.findByProductTypeId(productTypeId);
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

        // Validate product type exists if provided
        ProductType productType = null;
        if (createProductRequest.getProductTypeId() != null) {
            productType = productTypeService.findById(createProductRequest.getProductTypeId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.ENTITY_NOT_FOUND, "Product type not found with ID: " + createProductRequest.getProductTypeId()));
        }

        // Create product
        Product product = productMapper.toEntity(createProductRequest);
        product.setProductType(productType);

        // Set default values
        if (product.getIsFeatured() == null) {
            product.setIsFeatured(false);
        }

        Product savedProduct = productService.save(product);
        
        // Process attribute values if provided
        if (createProductRequest.getAttributeValues() != null && !createProductRequest.getAttributeValues().isEmpty()) {
            processProductAttributeValues(savedProduct, createProductRequest.getAttributeValues());
        }
        
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

        // Validate product type exists if provided
        if (updateProductRequest.getProductTypeId() != null) {
            ProductType productType = productTypeService.findById(updateProductRequest.getProductTypeId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.ENTITY_NOT_FOUND, "Product type not found with ID: " + updateProductRequest.getProductTypeId()));
            existingProduct.setProductType(productType);
        }

        // Update product
        productMapper.updateEntityFromRequest(updateProductRequest, existingProduct);
        Product savedProduct = productService.update(existingProduct);
        
        // Process attribute values if provided
        if (updateProductRequest.getAttributeValues() != null) {
            processProductAttributeValues(savedProduct, updateProductRequest.getAttributeValues());
        }

        return productMapper.toProductInfoDto(savedProduct);
    }

    /**
     * Process product attribute values - create or update attribute values for a product
     */
    @Transactional
    private void processProductAttributeValues(Product product, List<ProductAttributeValueRequest> attributeValueRequests) {
        log.debug("Processing {} attribute values for product: {}", attributeValueRequests.size(), product.getProductId());
        
        for (ProductAttributeValueRequest attrValueReq : attributeValueRequests) {
            // Validate attribute exists
            ProductAttribute attribute = productAttributeService.findById(attrValueReq.getAttributeId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.ENTITY_NOT_FOUND, 
                            "Product attribute not found with ID: " + attrValueReq.getAttributeId()));
            
            // Check if attribute value already exists for this product
            ProductAttributeValue existingAttrValue = productAttributeValueService
                    .getProductAttributeValue(product.getProductId(), attrValueReq.getAttributeId())
                    .orElse(null);
            
            if (existingAttrValue != null) {
                // Update existing attribute value
                log.debug("Updating existing attribute value for product: {} and attribute: {}", 
                        product.getProductId(), attrValueReq.getAttributeId());
                
                existingAttrValue.setValueText(attrValueReq.getValueText());
                existingAttrValue.setValueNumber(attrValueReq.getValueNumber());
                productAttributeValueService.update(existingAttrValue);
            } else {
                // Create new attribute value
                log.debug("Creating new attribute value for product: {} and attribute: {}", 
                        product.getProductId(), attrValueReq.getAttributeId());
                
                ProductAttributeValue newAttrValue = productAttributeValueMapper.toEntity(attrValueReq);
                newAttrValue.setProductId(product.getProductId());
                newAttrValue.setAttributeId(attrValueReq.getAttributeId());
                newAttrValue.setProduct(product);
                newAttrValue.setProductAttribute(attribute);
                
                productAttributeValueService.save(newAttrValue);
            }
        }
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

    /**
     * Update product active status
     */
    @Transactional
    public ProductInfoDto updateProductStatus(UUID productId, ProductStatusUpdateRequest statusRequest) {
        log.info("Updating product status for ID: {} to {}", productId, statusRequest.getIsActive());

        // Get existing product
        Product existingProduct = productService.getById(productId);

        // Update status
        existingProduct.setIsActive(statusRequest.getIsActive());

        // Save updated product
        Product updatedProduct = productService.update(existingProduct);

        return productMapper.toProductInfoDto(updatedProduct);
    }

    public long getProductCountByProductType(UUID productTypeId) {
        log.info("Getting product count by product type ID: {}", productTypeId);
        return productService.countByProductTypeId(productTypeId);
    }

    public long getProductCountBySupplier(UUID supplierId) {
        log.info("Getting product count by supplier ID: {}", supplierId);
        return productService.countBySupplierId(supplierId);
    }
}