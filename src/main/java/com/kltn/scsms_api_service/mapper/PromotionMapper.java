package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.CreatePromotionRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionRequest;
import com.kltn.scsms_api_service.core.entity.Promotion;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.entity.Category;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {
    
    /**
     * Convert CreatePromotionRequest to Promotion entity
     */
    @Mapping(target = "promotionId", ignore = true)
    @Mapping(target = "category", ignore = true) // Will be set manually
    @Mapping(target = "freeProduct", ignore = true) // Will be set manually
    @Mapping(target = "freeService", ignore = true) // Will be set manually
    @Mapping(target = "buyProduct", ignore = true) // Will be set manually
    @Mapping(target = "getProduct", ignore = true) // Will be set manually
    @Mapping(target = "usages", ignore = true)
    @Mapping(target = "promotionLines", ignore = true)
    @Mapping(target = "targetCustomerRanks", expression = "java(convertListToJson(request.getTargetCustomerRanks()))")
    @Mapping(target = "targetVehicleTypes", expression = "java(convertListToJson(request.getTargetVehicleTypes()))")
    @Mapping(target = "targetServices", expression = "java(convertUuidListToJson(request.getTargetServices()))")
    @Mapping(target = "targetProducts", expression = "java(convertUuidListToJson(request.getTargetProducts()))")
    @Mapping(target = "targetBranches", expression = "java(convertUuidListToJson(request.getTargetBranches()))")
    @Mapping(target = "imageUrls", expression = "java(convertListToJson(request.getImageUrls()))")
    @Mapping(target = "usedCount", constant = "0")
    Promotion toEntity(CreatePromotionRequest request);
    
    /**
     * Update Promotion entity from UpdatePromotionRequest
     */
    @Mapping(target = "promotionId", ignore = true)
    @Mapping(target = "category", ignore = true) // Will be set manually
    @Mapping(target = "freeProduct", ignore = true) // Will be set manually
    @Mapping(target = "freeService", ignore = true) // Will be set manually
    @Mapping(target = "buyProduct", ignore = true) // Will be set manually
    @Mapping(target = "getProduct", ignore = true) // Will be set manually
    @Mapping(target = "usages", ignore = true)
    @Mapping(target = "promotionLines", ignore = true)
    @Mapping(target = "targetCustomerRanks", expression = "java(convertListToJson(request.getTargetCustomerRanks()))")
    @Mapping(target = "targetVehicleTypes", expression = "java(convertListToJson(request.getTargetVehicleTypes()))")
    @Mapping(target = "targetServices", expression = "java(convertUuidListToJson(request.getTargetServices()))")
    @Mapping(target = "targetProducts", expression = "java(convertUuidListToJson(request.getTargetProducts()))")
    @Mapping(target = "targetBranches", expression = "java(convertUuidListToJson(request.getTargetBranches()))")
    @Mapping(target = "imageUrls", expression = "java(convertListToJson(request.getImageUrls()))")
    @Mapping(target = "usedCount", ignore = true)
    void updateEntityFromRequest(UpdatePromotionRequest request, @MappingTarget Promotion promotion);
    
    /**
     * Convert Promotion entity to PromotionInfoDto
     */
    @Mapping(target = "category", expression = "java(convertCategoryToFlatDto(promotion.getCategory()))")
    @Mapping(target = "freeProduct", expression = "java(convertProductToFlatDto(promotion.getFreeProduct()))")
    @Mapping(target = "freeService", expression = "java(convertServiceToFlatDto(promotion.getFreeService()))")
    @Mapping(target = "buyProduct", expression = "java(convertProductToFlatDto(promotion.getBuyProduct()))")
    @Mapping(target = "getProduct", expression = "java(convertProductToFlatDto(promotion.getGetProduct()))")
    @Mapping(target = "targetCustomerRanks", expression = "java(convertJsonToList(promotion.getTargetCustomerRanks()))")
    @Mapping(target = "targetVehicleTypes", expression = "java(convertJsonToList(promotion.getTargetVehicleTypes()))")
    @Mapping(target = "targetServices", expression = "java(convertJsonToUuidList(promotion.getTargetServices()))")
    @Mapping(target = "targetProducts", expression = "java(convertJsonToUuidList(promotion.getTargetProducts()))")
    @Mapping(target = "targetBranches", expression = "java(convertJsonToUuidList(promotion.getTargetBranches()))")
    @Mapping(target = "imageUrls", expression = "java(convertJsonToList(promotion.getImageUrls()))")
    @Mapping(target = "promotionLines", ignore = true) // TODO: Implement if needed
    @Mapping(target = "totalUsageCount", ignore = true) // TODO: Calculate from usages
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "isExpired", expression = "java(isExpired(promotion))")
    @Mapping(target = "isAvailable", expression = "java(isAvailable(promotion))")
    PromotionInfoDto toDetailedInfoDto(Promotion promotion);
    
    /**
     * Convert Category to CategoryFlatDto
     */
    default PromotionInfoDto.CategoryFlatDto convertCategoryToFlatDto(Category category) {
        if (category == null) {
            return null;
        }
        
        return PromotionInfoDto.CategoryFlatDto.builder()
            .categoryId(category.getCategoryId())
            .categoryName(category.getCategoryName())
            .categoryUrl(category.getCategoryUrl())
            .build();
    }
    
    /**
     * Convert Product to ProductFlatDto
     */
    default PromotionInfoDto.ProductFlatDto convertProductToFlatDto(Product product) {
        if (product == null) {
            return null;
        }
        
        return PromotionInfoDto.ProductFlatDto.builder()
            .productId(product.getProductId())
            .productName(product.getProductName())
            .productUrl(product.getProductUrl())
            .build();
    }
    
    /**
     * Convert Service to ServiceFlatDto
     */
    default PromotionInfoDto.ServiceFlatDto convertServiceToFlatDto(Service service) {
        if (service == null) {
            return null;
        }
        
        return PromotionInfoDto.ServiceFlatDto.builder()
            .serviceId(service.getServiceId())
            .serviceName(service.getServiceName())
            .serviceUrl(service.getServiceUrl())
            .build();
    }
    
    /**
     * Convert List<String> to JSON string
     */
    default String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Convert List<UUID> to JSON string
     */
    default String convertUuidListToJson(List<java.util.UUID> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Convert JSON string to List<String>
     */
    default List<String> convertJsonToList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Convert JSON string to List<UUID>
     */
    default List<java.util.UUID> convertJsonToUuidList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<List<java.util.UUID>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Check if promotion is expired
     */
    default Boolean isExpired(Promotion promotion) {
        if (promotion == null || promotion.getEndDate() == null) {
            return false;
        }
        
        return java.time.LocalDateTime.now().isAfter(promotion.getEndDate());
    }
    
    /**
     * Check if promotion is available (active, not expired, not deleted, within usage limits)
     */
    default Boolean isAvailable(Promotion promotion) {
        if (promotion == null) {
            return false;
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        // Check if promotion is active and not deleted
        if (!promotion.getIsActive() || promotion.getIsDeleted()) {
            return false;
        }
        
        // Check if promotion is within date range
        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            return false;
        }
        
        // Check if promotion has usage limit exceeded
        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            return false;
        }
        
        return true;
    }
}
