package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.RequireRole;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.productManagement.ProductAttributeValueDto;
import com.kltn.scsms_api_service.core.dto.productManagement.request.AddProductAttributeValueRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.BulkUpdateProductAttributeValueRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.UpdateProductAttributeValueRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.ProductAttributeValueManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Attribute Value Management", description = "APIs for managing product attribute values")
public class ProductAttributeValueManagementController {

    private final ProductAttributeValueManagementService productAttributeValueManagementService;

    @PostMapping(ApiConstant.ADD_PRODUCT_ATTRIBUTE_VALUE_API)
    @Operation(summary = "Add attribute value to product", description = "Add a new attribute value to a specific product")
    @SwaggerOperation(summary = "Add attribute value to product")
//    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductAttributeValueDto>> addProductAttributeValue(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Valid @RequestBody AddProductAttributeValueRequest request) {
        log.info("Adding attribute value for product: {} and attribute: {}", productId, request.getAttributeId());
        
        ProductAttributeValueDto attributeValue = productAttributeValueManagementService.addProductAttributeValue(productId, request);
        return ResponseBuilder.created("Product attribute value added successfully", attributeValue);
    }

    @GetMapping(ApiConstant.GET_PRODUCT_ATTRIBUTE_VALUE_API)
    @Operation(summary = "Get product attribute value", description = "Get a specific attribute value for a product")
    @SwaggerOperation(summary = "Get product attribute value")
    public ResponseEntity<ApiResponse<ProductAttributeValueDto>> getProductAttributeValue(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Attribute ID") @PathVariable UUID attributeId) {
        log.info("Getting attribute value for product: {} and attribute: {}", productId, attributeId);
        
        ProductAttributeValueDto attributeValue = productAttributeValueManagementService.getProductAttributeValue(productId, attributeId);
        return ResponseBuilder.success("Product attribute value fetched successfully", attributeValue);
    }

    @GetMapping(ApiConstant.GET_PRODUCT_ATTRIBUTE_VALUES_API)
    @Operation(summary = "Get all attribute values for product", description = "Get all attribute values for a specific product")
    @SwaggerOperation(summary = "Get all attribute values for product")
    public ResponseEntity<ApiResponse<List<ProductAttributeValueDto>>> getProductAttributeValues(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        log.info("Getting all attribute values for product: {}", productId);
        
        List<ProductAttributeValueDto> attributeValues = productAttributeValueManagementService.getProductAttributeValues(productId);
        return ResponseBuilder.success("Product attribute values fetched successfully", attributeValues);
    }

    @GetMapping(ApiConstant.GET_PRODUCTS_BY_ATTRIBUTE_API)
    @Operation(summary = "Get products by attribute", description = "Get all products that have a specific attribute")
    @SwaggerOperation(summary = "Get products by attribute")
    public ResponseEntity<ApiResponse<List<ProductAttributeValueDto>>> getProductsByAttribute(
            @Parameter(description = "Attribute ID") @PathVariable UUID attributeId) {
        log.info("Getting all products for attribute: {}", attributeId);
        
        List<ProductAttributeValueDto> attributeValues = productAttributeValueManagementService.getProductsByAttribute(attributeId);
        return ResponseBuilder.success("Products by attribute fetched successfully", attributeValues);
    }

    @PostMapping(ApiConstant.UPDATE_PRODUCT_ATTRIBUTE_VALUE_API)
    @Operation(summary = "Update product attribute value", description = "Update an existing attribute value for a product")
    @SwaggerOperation(summary = "Update product attribute value")
//    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductAttributeValueDto>> updateProductAttributeValue(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Attribute ID") @PathVariable UUID attributeId,
            @Valid @RequestBody UpdateProductAttributeValueRequest request) {
        log.info("Updating attribute value for product: {} and attribute: {}", productId, attributeId);
        
        ProductAttributeValueDto attributeValue = productAttributeValueManagementService.updateProductAttributeValue(productId, attributeId, request);
        return ResponseBuilder.success("Product attribute value updated successfully", attributeValue);
    }

    @PostMapping(ApiConstant.DELETE_PRODUCT_ATTRIBUTE_VALUE_API)
    @Operation(summary = "Delete product attribute value", description = "Delete an attribute value for a product")
    @SwaggerOperation(summary = "Delete product attribute value")
//    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Void>> deleteProductAttributeValue(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Attribute ID") @PathVariable UUID attributeId) {
        log.info("Deleting attribute value for product: {} and attribute: {}", productId, attributeId);
        
        productAttributeValueManagementService.deleteProductAttributeValue(productId, attributeId);
        return ResponseBuilder.success("Product attribute value deleted successfully");
    }

    @PostMapping(ApiConstant.BULK_UPDATE_PRODUCT_ATTRIBUTE_VALUES_API)
    @Operation(summary = "Bulk update product attribute values", description = "Update attribute values for multiple products")
    @SwaggerOperation(summary = "Bulk update product attribute values")
//    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductAttributeValueDto>>> bulkUpdateProductAttributeValues(
            @Valid @RequestBody BulkUpdateProductAttributeValueRequest request) {
        log.info("Bulk updating attribute values for {} products and attribute: {}", request.getProductIds().size(), request.getAttributeId());
        
        List<ProductAttributeValueDto> updatedValues = productAttributeValueManagementService.bulkUpdateProductAttributeValues(request);
        return ResponseBuilder.success("Product attribute values bulk updated successfully", updatedValues);
    }

    @GetMapping(ApiConstant.SEARCH_PRODUCTS_BY_ATTRIBUTE_VALUE_API)
    @Operation(summary = "Search products by attribute value", description = "Search products by specific attribute value")
    @SwaggerOperation(summary = "Search products by attribute value")
    public ResponseEntity<ApiResponse<List<ProductAttributeValueDto>>> searchProductsByAttributeValue(
            @Parameter(description = "Attribute ID") @PathVariable UUID attributeId,
            @Parameter(description = "Search value") @RequestParam String value) {
        log.info("Searching products by attribute value for attribute: {} and value: {}", attributeId, value);
        
        List<ProductAttributeValueDto> attributeValues = productAttributeValueManagementService.searchProductsByAttributeValue(attributeId, value);
        return ResponseBuilder.success("Products found by attribute value", attributeValues);
    }

    @GetMapping(ApiConstant.SEARCH_PRODUCTS_BY_ATTRIBUTE_VALUE_RANGE_API)
    @Operation(summary = "Search products by attribute value range", description = "Search products by attribute value within a range")
    @SwaggerOperation(summary = "Search products by attribute value range")
    public ResponseEntity<ApiResponse<List<ProductAttributeValueDto>>> searchProductsByAttributeValueRange(
            @Parameter(description = "Attribute ID") @PathVariable UUID attributeId,
            @Parameter(description = "Minimum value") @RequestParam BigDecimal minValue,
            @Parameter(description = "Maximum value") @RequestParam BigDecimal maxValue) {
        log.info("Searching products by attribute value range for attribute: {} between {} and {}", attributeId, minValue, maxValue);
        
        List<ProductAttributeValueDto> attributeValues = productAttributeValueManagementService.searchProductsByAttributeValueRange(attributeId, minValue, maxValue);
        return ResponseBuilder.success("Products found by attribute value range", attributeValues);
    }

    @GetMapping(ApiConstant.GET_PRODUCT_ATTRIBUTE_VALUE_COUNT_API)
    @Operation(summary = "Get product attribute value count", description = "Get the number of attribute values for a product")
    @SwaggerOperation(summary = "Get product attribute value count")
    public ResponseEntity<ApiResponse<Long>> getProductAttributeValueCount(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        log.info("Getting attribute value count for product: {}", productId);
        
        long count = productAttributeValueManagementService.getProductAttributeValueCount(productId);
        return ResponseBuilder.success("Product attribute value count fetched successfully", count);
    }

    @GetMapping(ApiConstant.GET_PRODUCT_COUNT_BY_ATTRIBUTE_API)
    @Operation(summary = "Get product count by attribute", description = "Get the number of products that have a specific attribute")
    @SwaggerOperation(summary = "Get product count by attribute")
    public ResponseEntity<ApiResponse<Long>> getProductCountByAttribute(
            @Parameter(description = "Attribute ID") @PathVariable UUID attributeId) {
        log.info("Getting product count for attribute: {}", attributeId);
        
        long count = productAttributeValueManagementService.getProductCountByAttribute(attributeId);
        return ResponseBuilder.success("Product count by attribute fetched successfully", count);
    }
}
