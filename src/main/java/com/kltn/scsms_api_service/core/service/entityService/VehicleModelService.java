package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.userManagement.param.UserFilterParam;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleModelFilterParam;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.VehicleModel;
import com.kltn.scsms_api_service.core.repository.VehicleModelRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleModelService {
    
    private final VehicleModelRepository vehicleModelRepository;
    private final EntityManager entityManager;
    
    public Page<VehicleModel> getAllVehicleModelsWithFilters(VehicleModelFilterParam filterParam) {
        log.info("Getting vehicle models with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<VehicleModel> query = cb.createQuery(VehicleModel.class);
        Root<VehicleModel> vehicleModelRoot = query.from(VehicleModel.class);
        
        List<Predicate> predicates = buildPredicates(cb, vehicleModelRoot, filterParam);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(filterParam.getDirection())
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Order order = sortDirection == Sort.Direction.ASC
            ? cb.asc(vehicleModelRoot.get(filterParam.getSort()))
            : cb.desc(vehicleModelRoot.get(filterParam.getSort()));
        query.orderBy(order);
        
        // Execute query with pagination
        TypedQuery<VehicleModel> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination, no custom filters so no need to count by predicates
        long totalElements = getTotalCount(filterParam);
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<VehicleModel> VehicleModels = typedQuery.getResultList();
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize(), Sort.by(sortDirection, filterParam.getSort()));
        return new PageImpl<>(VehicleModels, pageRequest, totalElements);
    }
    
    public List<VehicleModel> getAllActiveVehicleModels(boolean isActive, boolean isDeleted) {
        log.info("Getting all active vehicle models");
        return vehicleModelRepository.findAllByIsActiveAndIsDeleted(
            isActive,
            isDeleted,
            (Sort.by(Sort.Direction.ASC, "modelName")));
    }
    
    public VehicleModel getVehicleModelById(UUID modelId) {
        return vehicleModelRepository.findByModelId(modelId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle model not found with ID: " + modelId));
    }
    
    public Optional<VehicleModel> getOtpVehicleModelByCode(String modelCode) {
        return vehicleModelRepository.findByModelCode(modelCode);
    }
    
    public VehicleModel saveVehicleModel(VehicleModel VehicleModel) {
        return vehicleModelRepository.save(VehicleModel);
    }
    
    public Optional<VehicleModel> getOtpVehicleModelById(UUID brandId) {
        return vehicleModelRepository.findByModelId(brandId);
    }
    
    public void softDeleteVehicleModel(UUID brandId) {
        VehicleModel existingBrand = getVehicleModelById(brandId);
        existingBrand.setIsDeleted(true);
        vehicleModelRepository.save(existingBrand);
        log.info("Soft deleted vehicle brand with ID: {}", brandId);
    }
    
    
    private long getTotalCount(VehicleModelFilterParam filterParam) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<VehicleModel> vehicleModelRoot = countQuery.from(VehicleModel.class);
        
        countQuery.select(cb.count(vehicleModelRoot));
        
        List<Predicate> predicates = buildPredicates(cb, vehicleModelRoot, filterParam);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<VehicleModel> vehicleModelRoot, VehicleModelFilterParam filterParam) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (filterParam.getBrandId() != null) {
            predicates.add(cb.equal(vehicleModelRoot.get("brandId"), filterParam.getBrandId()));
        }
        
        if (filterParam.getTypeId() != null) {
            predicates.add(cb.equal(vehicleModelRoot.get("typeId"), filterParam.getTypeId()));
        }
        
        return predicates;
    }
}
