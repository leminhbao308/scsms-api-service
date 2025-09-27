package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.productManagement.request.CreateProductRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.UpdateProductRequest;
import com.kltn.scsms_api_service.core.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, CategoryMapper.class, SupplierMapper.class}
)
public interface ProductMapper {
    
    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.categoryName")
    ProductInfoDto toProductInfoDto(Product product);
    
    @Mapping(target = "category", ignore = true) // Will be set in service
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    Product toEntity(CreateProductRequest createProductRequest);
    
    default Product updateEntity(Product existingProduct, UpdateProductRequest updateRequest) {
        if (updateRequest == null) {
            return existingProduct;
        }
        
        if (updateRequest.getProductName() != null) {
            existingProduct.setProductName(updateRequest.getProductName());
        }
        if (updateRequest.getDescription() != null) {
            existingProduct.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getUnitOfMeasure() != null) {
            existingProduct.setUnitOfMeasure(updateRequest.getUnitOfMeasure());
        }
        if (updateRequest.getBrand() != null) {
            existingProduct.setBrand(updateRequest.getBrand());
        }
        if (updateRequest.getModel() != null) {
            existingProduct.setModel(updateRequest.getModel());
        }
        if (updateRequest.getSpecifications() != null) {
            existingProduct.setSpecifications(updateRequest.getSpecifications());
        }
        if (updateRequest.getSku() != null) {
            existingProduct.setSku(updateRequest.getSku());
        }
        if (updateRequest.getBarcode() != null) {
            existingProduct.setBarcode(updateRequest.getBarcode());
        }
        if (updateRequest.getCostPrice() != null) {
            existingProduct.setCostPrice(updateRequest.getCostPrice());
        }
        if (updateRequest.getSellingPrice() != null) {
            existingProduct.setSellingPrice(updateRequest.getSellingPrice());
        }
        if (updateRequest.getMinStockLevel() != null) {
            existingProduct.setMinStockLevel(updateRequest.getMinStockLevel());
        }
        if (updateRequest.getMaxStockLevel() != null) {
            existingProduct.setMaxStockLevel(updateRequest.getMaxStockLevel());
        }
        if (updateRequest.getReorderPoint() != null) {
            existingProduct.setReorderPoint(updateRequest.getReorderPoint());
        }
        if (updateRequest.getWeight() != null) {
            existingProduct.setWeight(updateRequest.getWeight());
        }
        if (updateRequest.getDimensions() != null) {
            existingProduct.setDimensions(updateRequest.getDimensions());
        }
        if (updateRequest.getWarrantyPeriodMonths() != null) {
            existingProduct.setWarrantyPeriodMonths(updateRequest.getWarrantyPeriodMonths());
        }
        if (updateRequest.getIsTrackable() != null) {
            existingProduct.setIsTrackable(updateRequest.getIsTrackable());
        }
        if (updateRequest.getIsConsumable() != null) {
            existingProduct.setIsConsumable(updateRequest.getIsConsumable());
        }
        if (updateRequest.getImageUrls() != null) {
            existingProduct.setImageUrls(updateRequest.getImageUrls());
        }
        if (updateRequest.getTags() != null) {
            existingProduct.setTags(updateRequest.getTags());
        }
        if (updateRequest.getSupplierId() != null) {
            existingProduct.setSupplierId(updateRequest.getSupplierId());
        }
        if (updateRequest.getIsFeatured() != null) {
            existingProduct.setIsFeatured(updateRequest.getIsFeatured());
        }
        if (updateRequest.getSortOrder() != null) {
            existingProduct.setSortOrder(updateRequest.getSortOrder());
        }
        if (updateRequest.getIsActive() != null) {
            existingProduct.setIsActive(updateRequest.getIsActive());
        }
        
        return existingProduct;
    }
}
