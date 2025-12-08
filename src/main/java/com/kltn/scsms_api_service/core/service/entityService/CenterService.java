package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.centerManagement.param.CenterFilterParam;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.repository.CenterRepository;
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
public class CenterService {
    
    private final CenterRepository centerRepository;
    private final EntityManager entityManager;
    
    public Optional<Center> findById(UUID centerId) {
        return centerRepository.findById(centerId);
    }
    
    public Optional<Center> findByIdWithBranches(UUID centerId) {
        return centerRepository.findByIdWithBranches(centerId);
    }
    
    public Optional<Center> findByIdWithManager(UUID centerId) {
        return centerRepository.findByIdWithManager(centerId);
    }
    
    public Optional<Center> findByIdWithBranchesAndManager(UUID centerId) {
        return centerRepository.findByIdWithBranchesAndManager(centerId);
    }
    
    public Center saveCenter(Center center) {
        return centerRepository.save(center);
    }
    
    public Page<Center> getAllCentersWithFilters(CenterFilterParam filterParam) {
        log.info("Getting centers with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Center> query = cb.createQuery(Center.class);
        Root<Center> centerRoot = query.from(Center.class);
        
        // Join with User entity for manager information
        Join<Center, User> managerJoin = centerRoot.join("manager", JoinType.LEFT);
        
        List<Predicate> predicates = buildPredicates(cb, centerRoot, managerJoin, filterParam);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(filterParam.getDirection())
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Order order;
        if ("manager.fullName".equals(filterParam.getSort())) {
            order = sortDirection == Sort.Direction.ASC
                ? cb.asc(managerJoin.get("fullName"))
                : cb.desc(managerJoin.get("fullName"));
        } else {
            order = sortDirection == Sort.Direction.ASC
                ? cb.asc(centerRoot.get(filterParam.getSort()))
                : cb.desc(centerRoot.get(filterParam.getSort()));
        }
        query.orderBy(order);
        
        TypedQuery<Center> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination
        long totalElements = getTotalCount(filterParam);
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<Center> centers = typedQuery.getResultList();
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize());
        return new PageImpl<>(centers, pageRequest, totalElements);
    }
    
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Center> centerRoot,
                                            Join<Center, User> managerJoin, CenterFilterParam filterParam) {
        List<Predicate> predicates = new ArrayList<>();
        
        // Active status filter
        if (filterParam.getActive() != null) {
            predicates.add(cb.equal(centerRoot.get("isActive"), filterParam.getActive()));
        }
        
        // Operating status filter
        if (filterParam.getOperatingStatus() != null) {
            predicates.add(cb.equal(centerRoot.get("operatingStatus"), filterParam.getOperatingStatus()));
        }
        
        // Manager filter
        if (filterParam.getManagerId() != null) {
            predicates.add(cb.equal(managerJoin.get("userId"), filterParam.getManagerId()));
        }
        
        // Boolean field filters
        if (filterParam.getHasManager() != null) {
            if (filterParam.getHasManager()) {
                predicates.add(cb.isNotNull(centerRoot.get("manager")));
            } else {
                predicates.add(cb.isNull(centerRoot.get("manager")));
            }
        }
        
        if (filterParam.getHasWebsite() != null) {
            if (filterParam.getHasWebsite()) {
                predicates.add(cb.isNotNull(centerRoot.get("website")));
            } else {
                predicates.add(cb.isNull(centerRoot.get("website")));
            }
        }
        
        if (filterParam.getHasTaxCode() != null) {
            if (filterParam.getHasTaxCode()) {
                predicates.add(cb.isNotNull(centerRoot.get("taxCode")));
            } else {
                predicates.add(cb.isNull(centerRoot.get("taxCode")));
            }
        }
        
        if (filterParam.getHasBusinessLicense() != null) {
            if (filterParam.getHasBusinessLicense()) {
                predicates.add(cb.isNotNull(centerRoot.get("businessLicense")));
            } else {
                predicates.add(cb.isNull(centerRoot.get("businessLicense")));
            }
        }
        
        if (filterParam.getHasLogo() != null) {
            if (filterParam.getHasLogo()) {
                predicates.add(cb.isNotNull(centerRoot.get("logoUrl")));
            } else {
                predicates.add(cb.isNull(centerRoot.get("logoUrl")));
            }
        }
        
        // Search filters
        if (filterParam.getSearch() != null) {
            String searchPattern = "%" + filterParam.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                cb.like(cb.lower(centerRoot.get("centerName")), searchPattern),
                cb.like(cb.lower(centerRoot.get("centerCode")), searchPattern),
                cb.like(cb.lower(centerRoot.get("description")), searchPattern),
                cb.like(cb.lower(centerRoot.get("headquartersAddress")), searchPattern),
                cb.like(cb.lower(centerRoot.get("headquartersEmail")), searchPattern)
            );
            predicates.add(searchPredicate);
        }
        
        // Specific field filters
        if (filterParam.getCenterName() != null) {
            predicates.add(cb.like(cb.lower(centerRoot.get("centerName")),
                "%" + filterParam.getCenterName().toLowerCase() + "%"));
        }
        
        if (filterParam.getCenterCode() != null) {
            predicates.add(cb.like(cb.lower(centerRoot.get("centerCode")),
                "%" + filterParam.getCenterCode().toLowerCase() + "%"));
        }
        
        if (filterParam.getDescription() != null) {
            predicates.add(cb.like(cb.lower(centerRoot.get("description")),
                "%" + filterParam.getDescription().toLowerCase() + "%"));
        }
        
        if (filterParam.getHeadquartersAddress() != null) {
            predicates.add(cb.like(cb.lower(centerRoot.get("headquartersAddress")),
                "%" + filterParam.getHeadquartersAddress().toLowerCase() + "%"));
        }
        
        if (filterParam.getHeadquartersPhone() != null) {
            predicates.add(cb.like(centerRoot.get("headquartersPhone"), "%" + filterParam.getHeadquartersPhone() + "%"));
        }
        
        if (filterParam.getHeadquartersEmail() != null) {
            predicates.add(cb.like(cb.lower(centerRoot.get("headquartersEmail")),
                "%" + filterParam.getHeadquartersEmail().toLowerCase() + "%"));
        }
        
        if (filterParam.getWebsite() != null) {
            predicates.add(cb.like(cb.lower(centerRoot.get("website")),
                "%" + filterParam.getWebsite().toLowerCase() + "%"));
        }
        
        if (filterParam.getTaxCode() != null) {
            predicates.add(cb.like(cb.lower(centerRoot.get("taxCode")),
                "%" + filterParam.getTaxCode().toLowerCase() + "%"));
        }
        
        if (filterParam.getBusinessLicense() != null) {
            predicates.add(cb.like(cb.lower(centerRoot.get("businessLicense")),
                "%" + filterParam.getBusinessLicense().toLowerCase() + "%"));
        }
        
        // Numeric range filters
        
        // Date range filters
        addDateRangePredicates(cb, centerRoot, predicates, filterParam);
        
        return predicates;
    }
    
    private void addDateRangePredicates(CriteriaBuilder cb, Root<Center> centerRoot,
                                        List<Predicate> predicates, CenterFilterParam filterParam) {
        // Created date range
        if (filterParam.getCreatedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(centerRoot.get("createdDate"), filterParam.getCreatedDateFrom()));
        }
        if (filterParam.getCreatedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(centerRoot.get("createdDate"), filterParam.getCreatedDateTo()));
        }
        
        // Updated date range
        if (filterParam.getModifiedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(centerRoot.get("modifiedDate"), filterParam.getModifiedDateFrom()));
        }
        if (filterParam.getModifiedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(centerRoot.get("modifiedDate"), filterParam.getModifiedDateTo()));
        }
        
        // Established date range
        if (filterParam.getEstablishedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(centerRoot.get("establishedDate"), filterParam.getEstablishedDateFrom()));
        }
        if (filterParam.getEstablishedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(centerRoot.get("establishedDate"), filterParam.getEstablishedDateTo()));
        }
    }
    
    private long getTotalCount(CenterFilterParam filterParam) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Center> centerRoot = countQuery.from(Center.class);
        Join<Center, User> managerJoin = centerRoot.join("manager", JoinType.LEFT);
        
        countQuery.select(cb.count(centerRoot));
        
        List<Predicate> predicates = buildPredicates(cb, centerRoot, managerJoin, filterParam);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    public void deleteCenter(Center existingCenter) {
        existingCenter.setIsDeleted(true);
        centerRepository.save(existingCenter);
    }
    
    public Optional<Center> findByCenterName(String centerName) {
        return centerRepository.findByCenterName(centerName);
    }
    
    public Optional<Center> findByCenterCode(String centerCode) {
        return centerRepository.findByCenterCode(centerCode);
    }
    
    public Optional<Center> findByTaxCode(String taxCode) {
        return centerRepository.findByTaxCode(taxCode);
    }
    
    public Optional<Center> findByBusinessLicense(String businessLicense) {
        return centerRepository.findByBusinessLicense(businessLicense);
    }
    
    public boolean existsByCenterName(String centerName) {
        return centerRepository.existsByCenterName(centerName);
    }
    
    public boolean existsByCenterCode(String centerCode) {
        return centerRepository.existsByCenterCode(centerCode);
    }
    
    public boolean existsByTaxCode(String taxCode) {
        return centerRepository.existsByTaxCode(taxCode);
    }
    
    public boolean existsByBusinessLicense(String businessLicense) {
        return centerRepository.existsByBusinessLicense(businessLicense);
    }
    
}
