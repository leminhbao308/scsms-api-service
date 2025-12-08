package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.supplierManagement.param.SupplierFilterParam;
import com.kltn.scsms_api_service.core.entity.Supplier;
import com.kltn.scsms_api_service.core.repository.SupplierRepository;
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
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    private final EntityManager entityManager;
    
    public Optional<Supplier> findById(UUID supplierId) {
        return supplierRepository.findById(supplierId);
    }
    
    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
    
    public Page<Supplier> getAllSuppliersWithFilters(SupplierFilterParam filterParam) {
        log.info("Getting suppliers with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Supplier> query = cb.createQuery(Supplier.class);
        Root<Supplier> supplierRoot = query.from(Supplier.class);
        
        List<Predicate> predicates = buildPredicates(cb, supplierRoot, filterParam);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(filterParam.getDirection())
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Order order = sortDirection == Sort.Direction.ASC
            ? cb.asc(supplierRoot.get(filterParam.getSort()))
            : cb.desc(supplierRoot.get(filterParam.getSort()));
        query.orderBy(order);
        
        TypedQuery<Supplier> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination
        long totalElements = getTotalCount(filterParam);
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<Supplier> suppliers = typedQuery.getResultList();
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize());
        return new PageImpl<>(suppliers, pageRequest, totalElements);
    }
    
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Supplier> supplierRoot,
                                            SupplierFilterParam filterParam) {
        List<Predicate> predicates = new ArrayList<>();
        
        // Active status filter
        if (filterParam.getActive() != null) {
            predicates.add(cb.equal(supplierRoot.get("isActive"), filterParam.getActive()));
        }
        
        // Boolean field filters
        if (filterParam.getHasContactPerson() != null) {
            if (filterParam.getHasContactPerson()) {
                predicates.add(cb.isNotNull(supplierRoot.get("contactPerson")));
            } else {
                predicates.add(cb.isNull(supplierRoot.get("contactPerson")));
            }
        }
        
        if (filterParam.getHasPhone() != null) {
            if (filterParam.getHasPhone()) {
                predicates.add(cb.isNotNull(supplierRoot.get("phone")));
            } else {
                predicates.add(cb.isNull(supplierRoot.get("phone")));
            }
        }
        
        if (filterParam.getHasEmail() != null) {
            if (filterParam.getHasEmail()) {
                predicates.add(cb.isNotNull(supplierRoot.get("email")));
            } else {
                predicates.add(cb.isNull(supplierRoot.get("email")));
            }
        }
        
        if (filterParam.getHasAddress() != null) {
            if (filterParam.getHasAddress()) {
                predicates.add(cb.isNotNull(supplierRoot.get("address")));
            } else {
                predicates.add(cb.isNull(supplierRoot.get("address")));
            }
        }
        
        if (filterParam.getHasBankInfo() != null) {
            if (filterParam.getHasBankInfo()) {
                predicates.add(cb.or(
                    cb.isNotNull(supplierRoot.get("bankName")),
                    cb.isNotNull(supplierRoot.get("bankAccount"))
                ));
            } else {
                predicates.add(cb.and(
                    cb.isNull(supplierRoot.get("bankName")),
                    cb.isNull(supplierRoot.get("bankAccount"))
                ));
            }
        }
        
        // Search filters
        if (filterParam.getSearch() != null) {
            String searchPattern = "%" + filterParam.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                cb.like(cb.lower(supplierRoot.get("supplierName")), searchPattern),
                cb.like(cb.lower(supplierRoot.get("contactPerson")), searchPattern),
                cb.like(cb.lower(supplierRoot.get("email")), searchPattern),
                cb.like(cb.lower(supplierRoot.get("address")), searchPattern)
            );
            predicates.add(searchPredicate);
        }
        
        // Specific field filters
        if (filterParam.getSupplierName() != null) {
            predicates.add(cb.like(cb.lower(supplierRoot.get("supplierName")),
                "%" + filterParam.getSupplierName().toLowerCase() + "%"));
        }
        
        if (filterParam.getContactPerson() != null) {
            predicates.add(cb.like(cb.lower(supplierRoot.get("contactPerson")),
                "%" + filterParam.getContactPerson().toLowerCase() + "%"));
        }
        
        if (filterParam.getPhone() != null) {
            predicates.add(cb.like(supplierRoot.get("phone"), "%" + filterParam.getPhone() + "%"));
        }
        
        if (filterParam.getEmail() != null) {
            predicates.add(cb.like(cb.lower(supplierRoot.get("email")),
                "%" + filterParam.getEmail().toLowerCase() + "%"));
        }
        
        if (filterParam.getAddress() != null) {
            predicates.add(cb.like(cb.lower(supplierRoot.get("address")),
                "%" + filterParam.getAddress().toLowerCase() + "%"));
        }
        
        if (filterParam.getBankName() != null) {
            predicates.add(cb.like(cb.lower(supplierRoot.get("bankName")),
                "%" + filterParam.getBankName().toLowerCase() + "%"));
        }
        
        // Date range filters
        addDateRangePredicates(cb, supplierRoot, predicates, filterParam);
        
        return predicates;
    }
    
    private void addDateRangePredicates(CriteriaBuilder cb, Root<Supplier> supplierRoot,
                                        List<Predicate> predicates, SupplierFilterParam filterParam) {
        // Created date range
        if (filterParam.getCreatedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(supplierRoot.get("createdDate"), filterParam.getCreatedDateFrom()));
        }
        if (filterParam.getCreatedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(supplierRoot.get("createdDate"), filterParam.getCreatedDateTo()));
        }
        
        // Updated date range
        if (filterParam.getModifiedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(supplierRoot.get("modifiedDate"), filterParam.getModifiedDateFrom()));
        }
        if (filterParam.getModifiedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(supplierRoot.get("modifiedDate"), filterParam.getModifiedDateTo()));
        }
    }
    
    private long getTotalCount(SupplierFilterParam filterParam) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Supplier> supplierRoot = countQuery.from(Supplier.class);
        
        countQuery.select(cb.count(supplierRoot));
        
        List<Predicate> predicates = buildPredicates(cb, supplierRoot, filterParam);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    public void deleteSupplier(Supplier existingSupplier) {
        existingSupplier.setIsDeleted(true);
        supplierRepository.save(existingSupplier);
    }
    
    public Optional<Supplier> findBySupplierName(String supplierName) {
        return supplierRepository.findBySupplierName(supplierName);
    }
    
    public Optional<Supplier> findByEmail(String email) {
        return supplierRepository.findByEmail(email);
    }
    
    public Optional<Supplier> findByPhone(String phone) {
        return supplierRepository.findByPhone(phone);
    }
    
    public boolean existsBySupplierName(String supplierName) {
        return supplierRepository.existsBySupplierName(supplierName);
    }
    
    public boolean existsByEmail(String email) {
        return supplierRepository.existsByEmail(email);
    }
    
    public boolean existsByPhone(String phone) {
        return supplierRepository.existsByPhone(phone);
    }
    
    public Supplier getRefById(UUID supplierId) {
        return supplierRepository.getReferenceById(supplierId);
    }
}
