package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.branchManagement.param.BranchFilterParam;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.repository.BranchRepository;
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
public class BranchService {
    
    private final BranchRepository branchRepository;
    private final EntityManager entityManager;
    
    public Optional<Branch> findById(UUID branchId) {
        return branchRepository.findById(branchId);
    }
    
    public Optional<Branch> findByIdWithCenter(UUID branchId) {
        return branchRepository.findByIdWithCenter(branchId);
    }
    
    public Optional<Branch> findByIdWithManager(UUID branchId) {
        return branchRepository.findByIdWithManager(branchId);
    }
    
    public Optional<Branch> findByIdWithCenterAndManager(UUID branchId) {
        return branchRepository.findByIdWithCenterAndManager(branchId);
    }
    
    public Branch saveBranch(Branch branch) {
        return branchRepository.save(branch);
    }
    
    public Page<Branch> getAllBranchesWithFilters(BranchFilterParam filterParam) {
        log.info("Getting branches with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Branch> query = cb.createQuery(Branch.class);
        Root<Branch> branchRoot = query.from(Branch.class);
        
        // Join with Center and User entities
        Join<Branch, Center> centerJoin = branchRoot.join("center", JoinType.LEFT);
        Join<Branch, User> managerJoin = branchRoot.join("manager", JoinType.LEFT);
        
        List<Predicate> predicates = buildPredicates(cb, branchRoot, centerJoin, managerJoin, filterParam);
        
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
                ? cb.asc(branchRoot.get(filterParam.getSort()))
                : cb.desc(branchRoot.get(filterParam.getSort()));
        }
        query.orderBy(order);
        
        TypedQuery<Branch> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination
        long totalElements = getTotalCount(filterParam);
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<Branch> branches = typedQuery.getResultList();
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize());
        return new PageImpl<>(branches, pageRequest, totalElements);
    }
    
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Branch> branchRoot,
                                            Join<Branch, Center> centerJoin, Join<Branch, User> managerJoin,
                                            BranchFilterParam filterParam) {
        List<Predicate> predicates = new ArrayList<>();
        
        // Active status filter
        if (filterParam.getActive() != null) {
            predicates.add(cb.equal(branchRoot.get("isActive"), filterParam.getActive()));
        }
        
        // Operating status filter
        if (filterParam.getOperatingStatus() != null) {
            predicates.add(cb.equal(branchRoot.get("operatingStatus"), filterParam.getOperatingStatus()));
        }
        
        
        // Center filter
        if (filterParam.getCenterId() != null) {
            predicates.add(cb.equal(centerJoin.get("centerId"), filterParam.getCenterId()));
        }
        
        // Manager filter
        if (filterParam.getManagerId() != null) {
            predicates.add(cb.equal(managerJoin.get("userId"), filterParam.getManagerId()));
        }
        
        
        if (filterParam.getHasManager() != null) {
            if (filterParam.getHasManager()) {
                predicates.add(cb.isNotNull(branchRoot.get("manager")));
            } else {
                predicates.add(cb.isNull(branchRoot.get("manager")));
            }
        }
        
        if (filterParam.getHasPhone() != null) {
            if (filterParam.getHasPhone()) {
                predicates.add(cb.isNotNull(branchRoot.get("phone")));
            } else {
                predicates.add(cb.isNull(branchRoot.get("phone")));
            }
        }
        
        if (filterParam.getHasEmail() != null) {
            if (filterParam.getHasEmail()) {
                predicates.add(cb.isNotNull(branchRoot.get("email")));
            } else {
                predicates.add(cb.isNull(branchRoot.get("email")));
            }
        }
        
        
        // Search filters
        if (filterParam.getSearch() != null) {
            String searchPattern = "%" + filterParam.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                cb.like(cb.lower(branchRoot.get("branchName")), searchPattern),
                cb.like(cb.lower(branchRoot.get("branchCode")), searchPattern),
                cb.like(cb.lower(branchRoot.get("description")), searchPattern),
                cb.like(cb.lower(branchRoot.get("address")), searchPattern),
                cb.like(cb.lower(branchRoot.get("email")), searchPattern),
                cb.like(cb.lower(managerJoin.get("fullName")), searchPattern)
            );
            predicates.add(searchPredicate);
        }
        
        // Specific field filters
        if (filterParam.getBranchName() != null) {
            predicates.add(cb.like(cb.lower(branchRoot.get("branchName")),
                "%" + filterParam.getBranchName().toLowerCase() + "%"));
        }
        
        if (filterParam.getBranchCode() != null) {
            predicates.add(cb.like(cb.lower(branchRoot.get("branchCode")),
                "%" + filterParam.getBranchCode().toLowerCase() + "%"));
        }
        
        if (filterParam.getDescription() != null) {
            predicates.add(cb.like(cb.lower(branchRoot.get("description")),
                "%" + filterParam.getDescription().toLowerCase() + "%"));
        }
        
        if (filterParam.getAddress() != null) {
            predicates.add(cb.like(cb.lower(branchRoot.get("address")),
                "%" + filterParam.getAddress().toLowerCase() + "%"));
        }
        
        if (filterParam.getPhone() != null) {
            predicates.add(cb.like(branchRoot.get("phone"), "%" + filterParam.getPhone() + "%"));
        }
        
        if (filterParam.getEmail() != null) {
            predicates.add(cb.like(cb.lower(branchRoot.get("email")),
                "%" + filterParam.getEmail().toLowerCase() + "%"));
        }
        
        
        // Numeric range filters
        if (filterParam.getMinServiceCapacity() != null) {
            predicates.add(cb.greaterThanOrEqualTo(branchRoot.get("serviceCapacity"), filterParam.getMinServiceCapacity()));
        }
        if (filterParam.getMaxServiceCapacity() != null) {
            predicates.add(cb.lessThanOrEqualTo(branchRoot.get("serviceCapacity"), filterParam.getMaxServiceCapacity()));
        }
        
        
        // Date range filters
        addDateRangePredicates(cb, branchRoot, predicates, filterParam);
        
        return predicates;
    }
    
    private void addDateRangePredicates(CriteriaBuilder cb, Root<Branch> branchRoot,
                                        List<Predicate> predicates, BranchFilterParam filterParam) {
        // Created date range
        if (filterParam.getCreatedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(branchRoot.get("createdDate"), filterParam.getCreatedDateFrom()));
        }
        if (filterParam.getCreatedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(branchRoot.get("createdDate"), filterParam.getCreatedDateTo()));
        }
        
        // Updated date range
        if (filterParam.getModifiedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(branchRoot.get("modifiedDate"), filterParam.getModifiedDateFrom()));
        }
        if (filterParam.getModifiedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(branchRoot.get("modifiedDate"), filterParam.getModifiedDateTo()));
        }
        
        // Established date range
        if (filterParam.getEstablishedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(branchRoot.get("establishedDate"), filterParam.getEstablishedDateFrom()));
        }
        if (filterParam.getEstablishedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(branchRoot.get("establishedDate"), filterParam.getEstablishedDateTo()));
        }
    }
    
    private long getTotalCount(BranchFilterParam filterParam) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Branch> branchRoot = countQuery.from(Branch.class);
        Join<Branch, Center> centerJoin = branchRoot.join("center", JoinType.LEFT);
        Join<Branch, User> managerJoin = branchRoot.join("manager", JoinType.LEFT);
        
        countQuery.select(cb.count(branchRoot));
        
        List<Predicate> predicates = buildPredicates(cb, branchRoot, centerJoin, managerJoin, filterParam);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    public void deleteBranch(Branch existingBranch) {
        existingBranch.setIsDeleted(true);
        branchRepository.save(existingBranch);
    }
    
    public Optional<Branch> findByBranchName(String branchName) {
        return branchRepository.findByBranchName(branchName);
    }
    
    public Optional<Branch> findByBranchCode(String branchCode) {
        return branchRepository.findByBranchCode(branchCode);
    }
    
    public boolean existsByBranchName(String branchName) {
        return branchRepository.existsByBranchName(branchName);
    }
    
    public boolean existsByBranchCode(String branchCode) {
        return branchRepository.existsByBranchCode(branchCode);
    }
    
    public List<Branch> findBranchesByCenterId(UUID centerId) {
        return branchRepository.findByCenterId(centerId);
    }
    
    
    public Branch getRefById(UUID branchId) {
        return branchRepository.getReferenceById(branchId);
    }
}
