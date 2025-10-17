package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleProfileFilterParam;
import com.kltn.scsms_api_service.core.entity.VehicleProfile;
import com.kltn.scsms_api_service.core.repository.VehicleProfileRepository;
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
public class VehicleProfileService {
    
    private final VehicleProfileRepository vehicleProfileRepository;
    private final EntityManager entityManager;
    
    public Page<VehicleProfile> getAllVehicleProfilesWithFilters(VehicleProfileFilterParam filterParam) {
        log.info("Getting vehicle profiles with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<VehicleProfile> query = cb.createQuery(VehicleProfile.class);
        Root<VehicleProfile> vehicleProfileRoot = query.from(VehicleProfile.class);
        
        List<Predicate> predicates = buildPredicates(cb, vehicleProfileRoot, filterParam);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(filterParam.getDirection())
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Order order = sortDirection == Sort.Direction.ASC
            ? cb.asc(vehicleProfileRoot.get(filterParam.getSort()))
            : cb.desc(vehicleProfileRoot.get(filterParam.getSort()));
        query.orderBy(order);
        
        // Execute query with pagination
        TypedQuery<VehicleProfile> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination, no custom filters so no need to count by predicates
        long totalElements = getTotalCount(filterParam);
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<VehicleProfile> vehicleProfiles = typedQuery.getResultList();
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize(), Sort.by(sortDirection, filterParam.getSort()));
        return new PageImpl<>(vehicleProfiles, pageRequest, totalElements);
    }
    
    private long getTotalCount(VehicleProfileFilterParam filterParam) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<VehicleProfile> vehicleProfileRoot = countQuery.from(VehicleProfile.class);
        
        countQuery.select(cb.count(vehicleProfileRoot));
        
        List<Predicate> predicates = buildPredicates(cb, vehicleProfileRoot, filterParam);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<VehicleProfile> vehicleProfileRoot, VehicleProfileFilterParam filterParam) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (filterParam.getVehicleBrandId() != null) {
            predicates.add(cb.equal(vehicleProfileRoot.get("vehicleBrandId"), filterParam.getVehicleBrandId()));
        }
        
        if (filterParam.getVehicleTypeId() != null) {
            predicates.add(cb.equal(vehicleProfileRoot.get("vehicleTypeId"), filterParam.getVehicleTypeId()));
        }
        
        if (filterParam.getVehicleModelId() != null) {
            predicates.add(cb.equal(vehicleProfileRoot.get("vehicleModelId"), filterParam.getVehicleModelId()));
        }
        
        if (filterParam.getOwnerId() != null) {
            predicates.add(cb.equal(vehicleProfileRoot.get("ownerId"), filterParam.getOwnerId()));
        }
        
        return predicates;
    }
    
    public VehicleProfile getVehicleProfileById(UUID profileId) {
        return vehicleProfileRepository.findById(profileId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle profile not found with ID: " + profileId));
    }
    
    public VehicleProfile saveVehicleProfile(VehicleProfile vehicleProfile) {
        return vehicleProfileRepository.save(vehicleProfile);
    }
    
    
    public Optional<VehicleProfile> getOtpVehicleProfileById(UUID profileId) {
        return vehicleProfileRepository.findById(profileId);
    }
    
    public void softDeleteVehicleProfile(UUID profileId) {
        VehicleProfile existingProfile = getVehicleProfileById(profileId);
        existingProfile.setIsDeleted(true);
        vehicleProfileRepository.save(existingProfile);
        log.info("Soft deleted vehicle profile with ID: {}", profileId);
    }
    
    public Page<VehicleProfile> getAllVehicleProfilesByOwnerIdWithFilters(UUID ownerId, VehicleProfileFilterParam vehicleProfileFilterParam) {
        vehicleProfileFilterParam.setOwnerId(ownerId);
        return getAllVehicleProfilesWithFilters(vehicleProfileFilterParam);
    }
}
