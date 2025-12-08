package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.productManagement.param.ProductFilterParam;
import com.kltn.scsms_api_service.core.dto.productManagement.request.CreateProductRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.UpdateProductRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.ProductStatusUpdateRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.ProductAttributeValueRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.ProductImageRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.ReorderProductImagesRequest;
import com.kltn.scsms_api_service.core.dto.mediaManagement.MediaInfoDto;
import com.kltn.scsms_api_service.core.dto.mediaManagement.request.CreateMediaRequest;
import com.kltn.scsms_api_service.core.dto.mediaManagement.request.UpdateMediaRequest;
import com.kltn.scsms_api_service.core.dto.mediaManagement.request.UpdateMediaMainStatusRequest;
import com.kltn.scsms_api_service.core.entity.S3File;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.ProductType;
import com.kltn.scsms_api_service.core.entity.ProductAttribute;
import com.kltn.scsms_api_service.core.entity.ProductAttributeValue;
import com.kltn.scsms_api_service.core.entity.Media;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.ProductTypeService;
import com.kltn.scsms_api_service.core.service.entityService.ProductAttributeService;
import com.kltn.scsms_api_service.core.service.entityService.ProductAttributeValueService;
import com.kltn.scsms_api_service.core.service.entityService.S3FileService;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
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
    private final MediaManagementService mediaManagementService;
    private final S3FileService s3FileService;
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
        filterParam.standardizeFilterRequest(filterParam);

        // Create pageable
        Sort sort = Sort.by(
                filterParam.getDirection().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filterParam.getSort());
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);

        // Build specification from filter params
        Specification<Product> spec = buildProductSpecification(filterParam);

        // Get products with specification
        Page<Product> productPage = productService.findAll(spec, pageable);

        return productPage.map(productMapper::toProductInfoDto);
    }

    private Specification<Product> buildProductSpecification(ProductFilterParam filterParam) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude deleted products
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Filter by is_active
            if (filterParam.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filterParam.getIsActive()));
            }

            // Filter by is_reward
            if (filterParam.getIsReward() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isReward"), filterParam.getIsReward()));
            }

            // Filter by is_featured
            if (filterParam.getIsFeatured() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFeatured"), filterParam.getIsFeatured()));
            }

            // Filter by product type
            if (filterParam.getProductTypeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("productType").get("productTypeId"),
                        filterParam.getProductTypeId()));
            }

            // Filter by supplier
            if (filterParam.getSupplierId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("supplierId"), filterParam.getSupplierId()));
            }

            // Filter by brand
            if (filterParam.getBrand() != null && !filterParam.getBrand().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("brand")),
                        "%" + filterParam.getBrand().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public ProductInfoDto getProductById(UUID productId) {
        log.info("Getting product by ID: {}", productId);
        Product product = productService.getById(productId);
        ProductInfoDto dto = productMapper.toProductInfoDto(product);
        
        // Fetch and populate attribute values
        List<ProductAttributeValue> attributeValues = productAttributeValueService.getProductAttributeValues(productId);
        dto.setAttributeValues(productAttributeValueMapper.toDtoList(attributeValues));
        
        log.debug("Product {} has {} attribute values", productId, attributeValues.size());
        return dto;
    }

    public ProductInfoDto getProductByUrl(String productUrl) {
        log.info("Getting product by URL: {}", productUrl);
        Product product = productService.getByProductUrl(productUrl);
        ProductInfoDto dto = productMapper.toProductInfoDto(product);
        
        // Fetch and populate attribute values
        List<ProductAttributeValue> attributeValues = productAttributeValueService.getProductAttributeValues(product.getProductId());
        dto.setAttributeValues(productAttributeValueMapper.toDtoList(attributeValues));
        
        log.debug("Product {} (URL: {}) has {} attribute values", product.getProductId(), productUrl, attributeValues.size());
        return dto;
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
                    .orElseThrow(() -> new ClientSideException(ErrorCode.ENTITY_NOT_FOUND,
                            "Product type not found with ID: " + createProductRequest.getProductTypeId()));
        }

        // Create product
        Product product = productMapper.toEntity(createProductRequest);
        product.setProductType(productType);

        // Set default values
        if (product.getIsFeatured() == null) {
            product.setIsFeatured(false);
        }
        if (product.getIsReward() == null) {
            product.setIsReward(false);
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
                    .orElseThrow(() -> new ClientSideException(ErrorCode.ENTITY_NOT_FOUND,
                            "Product type not found with ID: " + updateProductRequest.getProductTypeId()));
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
     * Process product attribute values - create or update attribute values for a
     * product
     */
    @Transactional
    protected void processProductAttributeValues(Product product,
            List<ProductAttributeValueRequest> attributeValueRequests) {
        log.debug("Processing {} attribute values for product: {}", attributeValueRequests.size(),
                product.getProductId());

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

    // ==================== Product Image Management Methods ====================

    /**
     * Get all images for a product
     */
    public List<MediaInfoDto> getProductImages(UUID productId) {
        log.info("Getting images for product ID: {}", productId);

        // Verify product exists
        productService.getById(productId);

        // Get media for product
        return mediaManagementService.getMediaByEntity(
                Media.EntityType.PRODUCT.name(),
                productId);
    }

    /**
     * Upload and add a new image file to a product
     */
    @Transactional
    public MediaInfoDto uploadProductImage(UUID productId, MultipartFile file, String altText, Boolean isMain) {
        log.info("Uploading image file for product ID: {}", productId);

        // Verify product exists
        productService.getById(productId);

        // Upload file to S3
        S3File s3File = s3FileService.uploadAndSave(
                file,
                "products/" + productId,
                null, // uploadedBy - can be set to current user if available
                "PRODUCT",
                productId);

        // If this is set as main, unset any existing main image
        if (Boolean.TRUE.equals(isMain)) {
            List<MediaInfoDto> existingImages = getProductImages(productId);
            existingImages.stream()
                    .filter(MediaInfoDto::getIsMain)
                    .forEach(img -> {
                        UpdateMediaMainStatusRequest statusRequest = new UpdateMediaMainStatusRequest();
                        statusRequest.setIsMain(false);
                        mediaManagementService.updateMediaMainStatus(img.getMediaId(), statusRequest);
                    });
        }

        // Get current max sort order
        List<MediaInfoDto> existingImages = getProductImages(productId);
        int nextSortOrder = existingImages.stream()
                .mapToInt(MediaInfoDto::getSortOrder)
                .max()
                .orElse(-1) + 1;

        // Create media request with uploaded file URL
        CreateMediaRequest createRequest = CreateMediaRequest.builder()
                .entityType(Media.EntityType.PRODUCT.name())
                .entityId(productId)
                .mediaUrl(s3File.getFileUrl())
                .mediaType(Media.MediaType.IMAGE.name())
                .isMain(Boolean.TRUE.equals(isMain))
                .sortOrder(nextSortOrder)
                .altText(altText)
                .build();

        return mediaManagementService.createMedia(createRequest);
    }

    /**
     * Add a new image to a product
     */
    @Transactional
    public MediaInfoDto addProductImage(UUID productId, ProductImageRequest imageRequest) {
        log.info("Adding image to product ID: {}", productId);

        // Verify product exists
        productService.getById(productId);

        // If this is set as main, unset any existing main image
        if (Boolean.TRUE.equals(imageRequest.getIsMain())) {
            List<MediaInfoDto> existingImages = getProductImages(productId);
            existingImages.stream()
                    .filter(MediaInfoDto::getIsMain)
                    .forEach(img -> {
                        UpdateMediaMainStatusRequest statusRequest = new UpdateMediaMainStatusRequest();
                        statusRequest.setIsMain(false);
                        mediaManagementService.updateMediaMainStatus(img.getMediaId(), statusRequest);
                    });
        }

        // Create media request
        CreateMediaRequest createRequest = CreateMediaRequest.builder()
                .entityType(Media.EntityType.PRODUCT.name())
                .entityId(productId)
                .mediaUrl(imageRequest.getMediaUrl())
                .mediaType(Media.MediaType.IMAGE.name())
                .isMain(imageRequest.getIsMain())
                .sortOrder(imageRequest.getSortOrder())
                .altText(imageRequest.getAltText())
                .build();

        return mediaManagementService.createMedia(createRequest);
    }

    /**
     * Update an existing product image
     */
    @Transactional
    public MediaInfoDto updateProductImage(UUID productId, UUID mediaId, ProductImageRequest imageRequest) {
        log.info("Updating image {} for product ID: {}", mediaId, productId);

        // Verify product exists
        productService.getById(productId);

        // Verify media belongs to this product
        MediaInfoDto existingMedia = mediaManagementService.getMediaById(mediaId);
        if (!existingMedia.getEntityId().equals(productId) ||
                !Media.EntityType.PRODUCT.name().equals(existingMedia.getEntityType())) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT,
                    "Media does not belong to this product");
        }

        // Update media URL, alt text, and sort order
        UpdateMediaRequest updateRequest = new UpdateMediaRequest();
        updateRequest.setMediaUrl(imageRequest.getMediaUrl());
        updateRequest.setAltText(imageRequest.getAltText());
        updateRequest.setSortOrder(imageRequest.getSortOrder());

        MediaInfoDto updatedMedia = mediaManagementService.updateMedia(mediaId, updateRequest);

        // If setting as main, handle main status separately
        if (imageRequest.getIsMain() != null &&
                !imageRequest.getIsMain().equals(existingMedia.getIsMain())) {

            if (imageRequest.getIsMain()) {
                // Unset other main images
                List<MediaInfoDto> existingImages = getProductImages(productId);
                existingImages.stream()
                        .filter(MediaInfoDto::getIsMain)
                        .filter(img -> !img.getMediaId().equals(mediaId))
                        .forEach(img -> {
                            UpdateMediaMainStatusRequest statusRequest = new UpdateMediaMainStatusRequest();
                            statusRequest.setIsMain(false);
                            mediaManagementService.updateMediaMainStatus(img.getMediaId(), statusRequest);
                        });
            }

            // Set new main status
            UpdateMediaMainStatusRequest statusRequest = new UpdateMediaMainStatusRequest();
            statusRequest.setIsMain(imageRequest.getIsMain());
            updatedMedia = mediaManagementService.updateMediaMainStatus(mediaId, statusRequest);
        }

        return updatedMedia;
    }

    /**
     * Delete a product image
     */
    @Transactional
    public void deleteProductImage(UUID productId, UUID mediaId) {
        log.info("Deleting image {} from product ID: {}", mediaId, productId);

        // Verify product exists
        productService.getById(productId);

        // Verify media belongs to this product
        MediaInfoDto existingMedia = mediaManagementService.getMediaById(mediaId);
        if (!existingMedia.getEntityId().equals(productId) ||
                !Media.EntityType.PRODUCT.name().equals(existingMedia.getEntityType())) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT,
                    "Media does not belong to this product");
        }

        // Delete media
        mediaManagementService.deleteMedia(mediaId);
    }

    /**
     * Set an image as the main image for a product
     */
    @Transactional
    public MediaInfoDto setMainProductImage(UUID productId, UUID mediaId) {
        log.info("Setting image {} as main for product ID: {}", mediaId, productId);

        // Verify product exists
        productService.getById(productId);

        // Verify media belongs to this product
        MediaInfoDto existingMedia = mediaManagementService.getMediaById(mediaId);
        if (!existingMedia.getEntityId().equals(productId) ||
                !Media.EntityType.PRODUCT.name().equals(existingMedia.getEntityType())) {
            throw new ClientSideException(ErrorCode.INVALID_INPUT,
                    "Media does not belong to this product");
        }

        // Unset all other main images for this product
        List<MediaInfoDto> existingImages = getProductImages(productId);
        existingImages.stream()
                .filter(MediaInfoDto::getIsMain)
                .filter(img -> !img.getMediaId().equals(mediaId))
                .forEach(img -> {
                    UpdateMediaMainStatusRequest statusRequest = new UpdateMediaMainStatusRequest();
                    statusRequest.setIsMain(false);
                    mediaManagementService.updateMediaMainStatus(img.getMediaId(), statusRequest);
                });

        // Set this image as main
        UpdateMediaMainStatusRequest statusRequest = new UpdateMediaMainStatusRequest();
        statusRequest.setIsMain(true);

        return mediaManagementService.updateMediaMainStatus(mediaId, statusRequest);
    }

    /**
     * Reorder product images
     */
    @Transactional
    public void reorderProductImages(UUID productId, ReorderProductImagesRequest reorderRequest) {
        log.info("Reordering images for product ID: {}", productId);

        // Verify product exists
        productService.getById(productId);

        // Update sort order for each media
        for (ReorderProductImagesRequest.MediaOrderDto mediaOrder : reorderRequest.getMediaOrders()) {
            MediaInfoDto existingMedia = mediaManagementService.getMediaById(mediaOrder.getMediaId());

            // Verify media belongs to this product
            if (!existingMedia.getEntityId().equals(productId) ||
                    !Media.EntityType.PRODUCT.name().equals(existingMedia.getEntityType())) {
                throw new ClientSideException(ErrorCode.INVALID_INPUT,
                        "Media " + mediaOrder.getMediaId() + " does not belong to this product");
            }

            // Update only sort order, keep other fields unchanged
            UpdateMediaRequest updateRequest = new UpdateMediaRequest();
            updateRequest.setMediaUrl(existingMedia.getMediaUrl());
            updateRequest.setSortOrder(mediaOrder.getSortOrder());
            updateRequest.setAltText(existingMedia.getAltText());
            updateRequest.setMediaType(existingMedia.getMediaType());

            mediaManagementService.updateMedia(mediaOrder.getMediaId(), updateRequest);
        }
    }
}
