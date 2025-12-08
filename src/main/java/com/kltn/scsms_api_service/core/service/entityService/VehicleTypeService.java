package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.vehicleManagement.param.VehicleTypeFilterParam;
import com.kltn.scsms_api_service.core.entity.VehicleType;
import com.kltn.scsms_api_service.core.repository.VehicleTypeRepository;
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
public class VehicleTypeService {
    
    private final VehicleTypeRepository vehicleTypeRepository;
    private final EntityManager entityManager;
    
    public Page<VehicleType> getAllVehicleTypesWithFilters(VehicleTypeFilterParam filterParam) {
        log.info("Getting vehicle types with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<VehicleType> query = cb.createQuery(VehicleType.class);
        Root<VehicleType> vehicleBrandRoot = query.from(VehicleType.class);
        
        // Apply sorting
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(filterParam.getDirection())
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Order order = sortDirection == Sort.Direction.ASC
            ? cb.asc(vehicleBrandRoot.get(filterParam.getSort()))
            : cb.desc(vehicleBrandRoot.get(filterParam.getSort()));
        query.orderBy(order);
        
        // Execute query with pagination
        TypedQuery<VehicleType> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination, no custom filters so no need to count by predicates
        long totalElements = vehicleTypeRepository.count();
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<VehicleType> vehicleTypes = typedQuery.getResultList();
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize(),
            Sort.by(sortDirection, filterParam.getSort()));
        return new PageImpl<>(vehicleTypes, pageRequest, totalElements);
    }
    
    public List<VehicleType> getAllActiveVehicleTypes(boolean isActive, boolean isDeleted) {
        log.info("Getting all active vehicle types");
        return vehicleTypeRepository.findAllByIsActiveAndIsDeleted(
            isActive,
            isDeleted,
            (Sort.by(Sort.Direction.ASC, "typeName")));
    }
    
    public VehicleType getVehicleTypeById(UUID typeId) {
        return vehicleTypeRepository.findByTypeId(typeId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Vehicle type not found with ID: " + typeId));
    }
    
    public Optional<VehicleType> getOtpVehicleTypeByCode(String typeCode) {
        return vehicleTypeRepository.findByTypeCode(typeCode);
    }
    
    public VehicleType saveVehicleType(VehicleType vehicleType) {
        return vehicleTypeRepository.save(vehicleType);
    }
    
    public Optional<VehicleType> getOtpVehicleTypeById(UUID typeId) {
        return vehicleTypeRepository.findByTypeId(typeId);
    }
    
    public void softDeleteVehicleType(UUID typeId) {
        VehicleType vehicleType = getVehicleTypeById(typeId);
        vehicleType.setIsDeleted(true);
        
        vehicleTypeRepository.save(vehicleType);
        log.info("Soft deleted vehicle type with ID: {}", typeId);
    }
    
    public VehicleType getVehicleTypeRefById(UUID typeId) {
        return vehicleTypeRepository.getReferenceById(typeId);
    }
}
