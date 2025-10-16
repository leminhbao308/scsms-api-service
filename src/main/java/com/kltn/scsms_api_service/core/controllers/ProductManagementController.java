package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.productManagement.param.ProductFilterParam;
import com.kltn.scsms_api_service.core.dto.productManagement.request.CreateProductRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.UpdateProductRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.ProductStatusUpdateRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.ProductManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductManagementController {
    
    private final ProductManagementService productManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_PRODUCTS_API)
    @Operation(summary = "Get all products", description = "Retrieve all products with optional filtering and pagination")
    @SwaggerOperation(summary = "Get all products")
    public ResponseEntity<ApiResponse<Page<ProductInfoDto>>> getAllProducts(
            @Parameter(description = "Filter parameters") @ModelAttribute ProductFilterParam filterParam) {
        log.info("Getting all products with filter: {}", filterParam);
        Page<ProductInfoDto> products = productManagementService.getAllProducts(filterParam);
        return ResponseBuilder.success("Products fetched successfully", products);
    }
    
    @GetMapping(ApiConstant.GET_PRODUCT_BY_ID_API)
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @SwaggerOperation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductInfoDto>> getProductById(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        log.info("Getting product by ID: {}", productId);
        ProductInfoDto product = productManagementService.getProductById(productId);
        return ResponseBuilder.success("Product fetched successfully", product);
    }
    
    @GetMapping(ApiConstant.GET_PRODUCT_BY_URL_API)
    @Operation(summary = "Get product by URL", description = "Retrieve a specific product by its URL")
    @SwaggerOperation(summary = "Get product by URL")
    public ResponseEntity<ApiResponse<ProductInfoDto>> getProductByUrl(
        @Parameter(description = "Product URL") @PathVariable String productUrl) {
        log.info("Getting product by URL: {}", productUrl);
        ProductInfoDto product = productManagementService.getProductByUrl(productUrl);
        return ResponseBuilder.success("Product fetched successfully", product);
    }
    
    @GetMapping(ApiConstant.GET_PRODUCTS_BY_PRODUCT_TYPE_API)
    @Operation(summary = "Get products by product type", description = "Retrieve all products in a specific product type")
    @SwaggerOperation(summary = "Get products by product type")
    public ResponseEntity<ApiResponse<List<ProductInfoDto>>> getProductsByProductType(
            @Parameter(description = "Product Type ID") @PathVariable UUID productTypeId) {
        log.info("Getting products by product type ID: {}", productTypeId);
        List<ProductInfoDto> products = productManagementService.getProductsByProductType(productTypeId);
        return ResponseBuilder.success("Products fetched by product type successfully", products);
    }
    
    @GetMapping(ApiConstant.GET_PRODUCTS_BY_SUPPLIER_API)
    @Operation(summary = "Get products by supplier", description = "Retrieve all products from a specific supplier")
    @SwaggerOperation(summary = "Get products by supplier")
    public ResponseEntity<ApiResponse<List<ProductInfoDto>>> getProductsBySupplier(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId) {
        log.info("Getting products by supplier ID: {}", supplierId);
        List<ProductInfoDto> products = productManagementService.getProductsBySupplier(supplierId);
        return ResponseBuilder.success("Products fetched by supplier successfully", products);
    }
    
    @GetMapping(ApiConstant.SEARCH_PRODUCTS_API)
    @Operation(summary = "Search products", description = "Search products by keyword")
    @SwaggerOperation(summary = "Search products")
    public ResponseEntity<ApiResponse<List<ProductInfoDto>>> searchProducts(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("Searching products by keyword: {}", keyword);
        List<ProductInfoDto> products = productManagementService.searchProducts(keyword);
        return ResponseBuilder.success("Products searched successfully", products);
    }
    
    @GetMapping(ApiConstant.GET_FEATURED_PRODUCTS_API)
    @Operation(summary = "Get featured products", description = "Retrieve all featured products")
    @SwaggerOperation(summary = "Get featured products")
    public ResponseEntity<ApiResponse<List<ProductInfoDto>>> getFeaturedProducts() {
        log.info("Getting featured products");
        List<ProductInfoDto> products = productManagementService.getFeaturedProducts();
        return ResponseBuilder.success("Featured products fetched successfully", products);
    }
    
    @PostMapping(ApiConstant.CREATE_PRODUCT_API)
    @Operation(summary = "Create product", description = "Create a new product")
    @SwaggerOperation(summary = "Create product")
    public ResponseEntity<ApiResponse<ProductInfoDto>> createProduct(
            @Parameter(description = "Product creation request") @Valid @RequestBody CreateProductRequest createProductRequest) {
        log.info("Creating product: {}", createProductRequest.getProductName());
        ProductInfoDto product = productManagementService.createProduct(createProductRequest);
        return ResponseBuilder.created("Product created successfully", product);
    }
    
    @PostMapping(ApiConstant.UPDATE_PRODUCT_API)
    @Operation(summary = "Update product", description = "Update an existing product")
    @SwaggerOperation(summary = "Update product")
    public ResponseEntity<ApiResponse<ProductInfoDto>> updateProduct(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Product update request") @Valid @RequestBody UpdateProductRequest updateProductRequest) {
        log.info("Updating product with ID: {}", productId);
        ProductInfoDto product = productManagementService.updateProduct(productId, updateProductRequest);
        return ResponseBuilder.success("Product updated successfully", product);
    }
    
    @PostMapping(ApiConstant.DELETE_PRODUCT_API)
    @Operation(summary = "Delete product", description = "Delete a product (soft delete)")
    @SwaggerOperation(summary = "Delete product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        log.info("Deleting product with ID: {}", productId);
        productManagementService.deleteProduct(productId);
        return ResponseBuilder.success("Product deleted successfully");
    }
    
    @PostMapping(ApiConstant.UPDATE_PRODUCT_STATUS_API)
    @Operation(summary = "Update product status", description = "Update the active status of a product")
    @SwaggerOperation(summary = "Update product status")
    public ResponseEntity<ApiResponse<ProductInfoDto>> updateProductStatus(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Valid @RequestBody ProductStatusUpdateRequest statusRequest) {
        log.info("Updating product status for ID: {} to {}", productId, statusRequest.getIsActive());
        
        ProductInfoDto updatedProduct = productManagementService.updateProductStatus(productId, statusRequest);
        
        return ResponseBuilder.success("Product status updated successfully", updatedProduct);
    }
}
