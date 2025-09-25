package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleBrandFilterParam;
import com.kltn.scsms_api_service.core.entity.VehicleBrand;
import com.kltn.scsms_api_service.core.repository.VehicleBrandRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleBrandService {
    
    private final VehicleBrandRepository vehicleBrandRepository;
    private final EntityManager entityManager;
    
    public Page<VehicleBrand> getAllVehicleBrandsWithFilters(VehicleBrandFilterParam filterParam) {
        log.info("Getting vehicle brands with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<VehicleBrand> query = cb.createQuery(VehicleBrand.class);
        Root<VehicleBrand> vehicleBrandRoot = query.from(VehicleBrand.class);
        
        // Apply sorting
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(filterParam.getDirection())
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Order order = sortDirection == Sort.Direction.ASC
            ? cb.asc(vehicleBrandRoot.get(filterParam.getSort()))
            : cb.desc(vehicleBrandRoot.get(filterParam.getSort()));
        query.orderBy(order);
        
        // Execute query with pagination
        TypedQuery<VehicleBrand> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination, no custom filters so no need to count by predicates
        long totalElements = vehicleBrandRepository.count();
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<VehicleBrand> vehicleBrands = typedQuery.getResultList();
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize(), Sort.by(sortDirection, filterParam.getSort()));
        return new PageImpl<>(vehicleBrands, pageRequest, totalElements);
    }
    
    public List<VehicleBrand> getAllActiveVehicleBrands(boolean isActive, boolean isDeleted) {
        log.info("Getting all active vehicle brands");
        return vehicleBrandRepository.findAllByIsActiveAndIsDeleted(
            isActive,
            isDeleted,
            (Sort.by(Sort.Direction.ASC, "brandName")));
    }
    
    public VehicleBrand getVehicleBrandById(UUID brandId) {
        return vehicleBrandRepository.findByBrandId(brandId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle brand not found with ID: " + brandId));
    }
    
    public Optional<VehicleBrand> getOtpVehicleBrandByCode(String brandCode) {
        return vehicleBrandRepository.findByBrandCode(brandCode);
    }
    
    public VehicleBrand saveVehicleBrand(VehicleBrand vehicleBrand) {
        return vehicleBrandRepository.save(vehicleBrand);
    }
    
    public Optional<VehicleBrand> getOtpVehicleBrandById(UUID brandId) {
        return vehicleBrandRepository.findByBrandId(brandId);
    }
    
    public void softDeleteVehicleBrand(UUID brandId) {
        VehicleBrand existingBrand = getVehicleBrandById(brandId);
        existingBrand.setIsDeleted(true);
        vehicleBrandRepository.save(existingBrand);
        log.info("Soft deleted vehicle brand with ID: {}", brandId);
    }
}
