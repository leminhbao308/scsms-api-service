package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.userManagement.param.UserFilterParam;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.repository.UserRepository;
import com.kltn.scsms_api_service.mapper.UserMapper;
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
public class UserService {
    
    private final UserMapper userMapper;
    
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public Page<User> getAllUsersWithFilters(UserFilterParam filterParam) {
        log.info("Getting users with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);
        
        // Join with Role entity for role-based filtering and response mapping
        Join<Object, Object> roleJoin = userRoot.join("role", JoinType.LEFT);
        
        List<Predicate> predicates = buildPredicates(cb, userRoot, roleJoin, filterParam);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(filterParam.getDirection())
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Order order;
        if ("role.roleName".equals(filterParam.getSort())) {
            order = sortDirection == Sort.Direction.ASC
                ? cb.asc(roleJoin.get("roleName"))
                : cb.desc(roleJoin.get("roleName"));
        } else {
            order = sortDirection == Sort.Direction.ASC
                ? cb.asc(userRoot.get(filterParam.getSort()))
                : cb.desc(userRoot.get(filterParam.getSort()));
        }
        query.orderBy(order);
        
        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination
        long totalElements = getTotalCount(filterParam);
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<User> users = typedQuery.getResultList();
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize());
        return new PageImpl<>(users, pageRequest, totalElements);
    }
    
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<User> userRoot,
                                            Join<Object, Object> roleJoin, UserFilterParam filterParam) {
        List<Predicate> predicates = new ArrayList<>();
        
        // Role filter
        if (filterParam.getRole() != null) {
            predicates.add(cb.equal(cb.upper(roleJoin.get("roleCode")), filterParam.getRole().toUpperCase()));
        }
        
        // Active status filter
        if (filterParam.getActive() != null) {
            predicates.add(cb.equal(userRoot.get("isActive"), filterParam.getActive()));
        }
        
        // Gender filter
        if (filterParam.getGender() != null) {
            predicates.add(cb.equal(userRoot.get("gender"), filterParam.getGender()));
        }
        
        // User type filter
        if (filterParam.getUserType() != null) {
            predicates.add(cb.equal(userRoot.get("userType"), filterParam.getUserType()));
        }
        
        // Customer rank filter
        if (filterParam.getCustomerRank() != null) {
            predicates.add(cb.equal(userRoot.get("customerRank"), filterParam.getCustomerRank()));
        }
        
        // Boolean field filters
        if (filterParam.getHasGoogleId() != null) {
            if (filterParam.getHasGoogleId()) {
                predicates.add(cb.isNotNull(userRoot.get("googleId")));
            } else {
                predicates.add(cb.isNull(userRoot.get("googleId")));
            }
        }
        
        if (filterParam.getHasPhoneNumber() != null) {
            if (filterParam.getHasPhoneNumber()) {
                predicates.add(cb.isNotNull(userRoot.get("phoneNumber")));
            } else {
                predicates.add(cb.isNull(userRoot.get("phoneNumber")));
            }
        }
        
        if (filterParam.getHasDateOfBirth() != null) {
            if (filterParam.getHasDateOfBirth()) {
                predicates.add(cb.isNotNull(userRoot.get("dateOfBirth")));
            } else {
                predicates.add(cb.isNull(userRoot.get("dateOfBirth")));
            }
        }
        
        if (filterParam.getHasAvatar() != null) {
            if (filterParam.getHasAvatar()) {
                predicates.add(cb.isNotNull(userRoot.get("avatarUrl")));
            } else {
                predicates.add(cb.isNull(userRoot.get("avatarUrl")));
            }
        }
        
        // Search filters
        if (filterParam.getSearch() != null) {
            String searchPattern = "%" + filterParam.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                cb.like(cb.lower(userRoot.get("fullName")), searchPattern),
                cb.like(cb.lower(userRoot.get("email")), searchPattern),
                cb.like(cb.lower(userRoot.get("phoneNumber")), searchPattern),
                cb.like(cb.lower(userRoot.get("address")), searchPattern)
            );
            predicates.add(searchPredicate);
        }
        
        if (filterParam.getEmail() != null) {
            predicates.add(cb.like(cb.lower(userRoot.get("email")),
                "%" + filterParam.getEmail().toLowerCase() + "%"));
        }
        
        if (filterParam.getFullName() != null) {
            predicates.add(cb.like(cb.lower(userRoot.get("fullName")),
                "%" + filterParam.getFullName().toLowerCase() + "%"));
        }
        
        if (filterParam.getPhoneNumber() != null) {
            predicates.add(cb.like(userRoot.get("phoneNumber"), "%" + filterParam.getPhoneNumber() + "%"));
        }
        
        if (filterParam.getAddress() != null) {
            predicates.add(cb.like(cb.lower(userRoot.get("address")),
                "%" + filterParam.getAddress().toLowerCase() + "%"));
        }
        
        // Date range filters
        addDateRangePredicates(cb, userRoot, predicates, filterParam);
        
        return predicates;
    }
    
    private void addDateRangePredicates(CriteriaBuilder cb, Root<User> userRoot,
                                        List<Predicate> predicates, UserFilterParam filterParam) {
        // Created date range
        if (filterParam.getCreatedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(userRoot.get("createDate"), filterParam.getCreatedDateFrom()));
        }
        if (filterParam.getCreatedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(userRoot.get("createDate"), filterParam.getCreatedDateTo()));
        }
        
        // Updated date range
        if (filterParam.getModifiedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(userRoot.get("modifiedDate"), filterParam.getModifiedDateFrom()));
        }
        if (filterParam.getModifiedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(userRoot.get("modifiedDate"), filterParam.getModifiedDateTo()));
        }
        
        // Date of birth range
        if (filterParam.getDateOfBirthFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(userRoot.get("dateOfBirth"), filterParam.getDateOfBirthFrom()));
        }
        if (filterParam.getDateOfBirthTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(userRoot.get("dateOfBirth"), filterParam.getDateOfBirthTo()));
        }
    }
    
    private long getTotalCount(UserFilterParam filterParam) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> userRoot = countQuery.from(User.class);
        Join<Object, Object> roleJoin = userRoot.join("role", JoinType.LEFT);
        
        countQuery.select(cb.count(userRoot));
        
        List<Predicate> predicates = buildPredicates(cb, userRoot, roleJoin, filterParam);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    public void deleteUser(User existingUser) {
        existingUser.setIsDeleted(true);
        
        userRepository.save(existingUser);
    }
}
