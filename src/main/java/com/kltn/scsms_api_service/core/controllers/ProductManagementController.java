package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.abstracts.BaseController;
import com.kltn.scsms_api_service.annotations.RequireRole;
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
public class ProductManagementController extends BaseController {
    
    private final ProductManagementService productManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_PRODUCTS_API)
    @Operation(summary = "Get all products", description = "Retrieve all products with optional filtering and pagination")
    @SwaggerOperation(summary = "Get all products")
    public ResponseEntity<ApiResponse<Object>> getAllProducts(
            @Parameter(description = "Filter parameters") ProductFilterParam filterParam) {
        log.info("Getting all products with filter: {}", filterParam);
        
        if (filterParam != null && filterParam.getPage() > 0) {
            // Return paginated results
            Page<ProductInfoDto> productPage = productManagementService.getAllProducts(filterParam);
            return ResponseBuilder.success(productPage);
        } else {
            // Return all results
            List<ProductInfoDto> products = productManagementService.getAllProducts();
            return ResponseBuilder.success(products);
        }
    }
    
    @GetMapping(ApiConstant.GET_PRODUCT_BY_ID_API)
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @SwaggerOperation(summary = "Get product by ID")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductInfoDto>> getProductById(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        log.info("Getting product by ID: {}", productId);
        ProductInfoDto product = productManagementService.getProductById(productId);
        return ResponseBuilder.success(product);
    }
    
    @GetMapping(ApiConstant.GET_PRODUCT_BY_URL_API)
    @Operation(summary = "Get product by URL", description = "Retrieve a specific product by its URL")
    @SwaggerOperation(summary = "Get product by URL")
    public ResponseEntity<ApiResponse<ProductInfoDto>> getProductByUrl(
        @Parameter(description = "Product URL") @PathVariable String productUrl) {
        log.info("Getting product by URL: {}", productUrl);
        ProductInfoDto product = productManagementService.getProductByUrl(productUrl);
        return ResponseBuilder.success(product);
    }
    
    @GetMapping(ApiConstant.GET_PRODUCTS_BY_CATEGORY_API)
    @Operation(summary = "Get products by category", description = "Retrieve all products in a specific category")
    @SwaggerOperation(summary = "Get products by category")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductInfoDto>>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        log.info("Getting products by category ID: {}", categoryId);
        List<ProductInfoDto> products = productManagementService.getProductsByCategory(categoryId);
        return ResponseBuilder.success(products);
    }
    
    @GetMapping(ApiConstant.GET_PRODUCTS_BY_SUPPLIER_API)
    @Operation(summary = "Get products by supplier", description = "Retrieve all products from a specific supplier")
    @SwaggerOperation(summary = "Get products by supplier")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductInfoDto>>> getProductsBySupplier(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId) {
        log.info("Getting products by supplier ID: {}", supplierId);
        List<ProductInfoDto> products = productManagementService.getProductsBySupplier(supplierId);
        return ResponseBuilder.success(products);
    }
    
    @GetMapping(ApiConstant.SEARCH_PRODUCTS_API)
    @Operation(summary = "Search products", description = "Search products by keyword")
    @SwaggerOperation(summary = "Search products")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductInfoDto>>> searchProducts(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("Searching products by keyword: {}", keyword);
        List<ProductInfoDto> products = productManagementService.searchProducts(keyword);
        return ResponseBuilder.success(products);
    }
    
    @GetMapping(ApiConstant.GET_FEATURED_PRODUCTS_API)
    @Operation(summary = "Get featured products", description = "Retrieve all featured products")
    @SwaggerOperation(summary = "Get featured products")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductInfoDto>>> getFeaturedProducts() {
        log.info("Getting featured products");
        List<ProductInfoDto> products = productManagementService.getFeaturedProducts();
        return ResponseBuilder.success(products);
    }
    
    @GetMapping(ApiConstant.GET_LOW_STOCK_PRODUCTS_API)
    @Operation(summary = "Get low stock products", description = "Retrieve all products with low stock levels")
    @SwaggerOperation(summary = "Get low stock products")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductInfoDto>>> getLowStockProducts() {
        log.info("Getting low stock products");
        List<ProductInfoDto> products = productManagementService.getLowStockProducts();
        return ResponseBuilder.success(products);
    }
    
    @PostMapping(ApiConstant.CREATE_PRODUCT_API)
    @Operation(summary = "Create product", description = "Create a new product")
    @SwaggerOperation(summary = "Create product")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductInfoDto>> createProduct(
            @Parameter(description = "Product creation request") @RequestBody CreateProductRequest createProductRequest) {
        log.info("Creating product: {}", createProductRequest.getProductName());
        ProductInfoDto product = productManagementService.createProduct(createProductRequest);
        return ResponseBuilder.created(product);
    }
    
    @PostMapping(ApiConstant.UPDATE_PRODUCT_API)
    @Operation(summary = "Update product", description = "Update an existing product")
    @SwaggerOperation(summary = "Update product")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductInfoDto>> updateProduct(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Product update request") @Valid @RequestBody UpdateProductRequest updateProductRequest) {
        log.info("Updating product with ID: {}", productId);
        ProductInfoDto product = productManagementService.updateProduct(productId, updateProductRequest);
        return ResponseBuilder.success(product);
    }
    
    @PostMapping(ApiConstant.DELETE_PRODUCT_API)
    @Operation(summary = "Delete product", description = "Delete a product (soft delete)")
    @SwaggerOperation(summary = "Delete product")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        log.info("Deleting product with ID: {}", productId);
        productManagementService.deleteProduct(productId);
        return ResponseBuilder.success("Product deleted successfully");
    }
    
    @PostMapping(ApiConstant.ACTIVATE_PRODUCT_API)
    @Operation(summary = "Activate product", description = "Activate a product")
    @SwaggerOperation(summary = "Activate product")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Void>> activateProduct(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        log.info("Activating product with ID: {}", productId);
        productManagementService.activateProduct(productId);
        return ResponseBuilder.success("Product activated successfully");
    }
    
    @PostMapping(ApiConstant.DEACTIVATE_PRODUCT_API)
    @Operation(summary = "Deactivate product", description = "Deactivate a product")
    @SwaggerOperation(summary = "Deactivate product")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        log.info("Deactivating product with ID: {}", productId);
        productManagementService.deactivateProduct(productId);
        return ResponseBuilder.success("Product deactivated successfully");
    }
    
    @PostMapping(ApiConstant.UPDATE_PRODUCT_STATUS_API)
    @Operation(summary = "Update product status", description = "Update the active status of a product")
    @SwaggerOperation(summary = "Update product status")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductInfoDto>> updateProductStatus(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Valid @RequestBody ProductStatusUpdateRequest statusRequest) {
        log.info("Updating product status for ID: {} to {}", productId, statusRequest.getIsActive());
        
        ProductInfoDto updatedProduct = productManagementService.updateProductStatus(productId, statusRequest);
        
        return ResponseBuilder.success("Product status updated successfully", updatedProduct);
    }
    
    @GetMapping(ApiConstant.GET_PRODUCT_COUNT_BY_CATEGORY_API)
    @Operation(summary = "Get product count by category", description = "Get the number of products in a category")
    @SwaggerOperation(summary = "Get product count by category")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Long>> getProductCountByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        log.info("Getting product count by category ID: {}", categoryId);
        long count = productManagementService.getProductCountByCategory(categoryId);
        return ResponseBuilder.success(count);
    }
    
    @GetMapping(ApiConstant.GET_PRODUCT_COUNT_BY_SUPPLIER_API)
    @Operation(summary = "Get product count by supplier", description = "Get the number of products from a supplier")
    @SwaggerOperation(summary = "Get product count by supplier")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Long>> getProductCountBySupplier(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId) {
        log.info("Getting product count by supplier ID: {}", supplierId);
        long count = productManagementService.getProductCountBySupplier(supplierId);
        return ResponseBuilder.success(count);
    }
}
