package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID>, JpaSpecificationExecutor<SalesOrder> {

        List<SalesOrder> findByStatusNotOrderByCreatedDateDesc(SalesStatus status);

        Page<SalesOrder> findByStatusNot(SalesStatus status, Pageable pageable);

        List<SalesOrder> findByStatusOrderByCreatedDateDesc(SalesStatus status);

        // ===== OPTIMIZED QUERIES TO PREVENT N+1 =====

        /**
         * Find sales order by ID with all related entities eagerly fetched
         * Prevents N+1 queries by using JOIN FETCH
         * Product attributes fetched separately to avoid MultipleBagFetchException
         */
        @Query("SELECT DISTINCT so FROM SalesOrder so " +
                        "LEFT JOIN FETCH so.lines l " +
                        "LEFT JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH p.productType " +
                        "LEFT JOIN FETCH so.customer c " +
                        "LEFT JOIN FETCH c.role " +
                        "LEFT JOIN FETCH so.branch " +
                        "WHERE so.id = :id")
        Optional<SalesOrder> findByIdWithDetails(UUID id);

        /**
         * Find all sales orders with related entities eagerly fetched
         * Prevents N+1 queries when fetching list of orders
         * Product attributes fetched separately to avoid MultipleBagFetchException
         */
        @Query("SELECT DISTINCT so FROM SalesOrder so " +
                        "LEFT JOIN FETCH so.lines l " +
                        "LEFT JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH p.productType " +
                        "LEFT JOIN FETCH so.customer c " +
                        "LEFT JOIN FETCH c.role " +
                        "LEFT JOIN FETCH so.branch " +
                        "WHERE so.status <> :excludeStatus " +
                        "ORDER BY so.createdDate DESC")
        List<SalesOrder> findAllWithDetailsByStatusNot(SalesStatus excludeStatus);

        /**
         * Find fulfilled sales orders with all details
         * STEP 1: Fetch orders with lines, products, and related entities
         * STEP 2: Use separate query to batch fetch product attributes (in service
         * layer)
         * This avoids MultipleBagFetchException from fetching two collections
         */
        @Query("SELECT DISTINCT so FROM SalesOrder so " +
                        "LEFT JOIN FETCH so.lines l " +
                        "LEFT JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH p.productType " +
                        "LEFT JOIN FETCH so.customer c " +
                        "LEFT JOIN FETCH c.role " +
                        "LEFT JOIN FETCH so.branch " +
                        "WHERE so.status = :status " +
                        "ORDER BY so.createdDate DESC")
        List<SalesOrder> findAllWithDetailsByStatus(SalesStatus status);

        /**
         * STEP 1: Get paginated order IDs (lightweight query, no JOIN FETCH)
         * This query is fast and pagination works correctly in database
         * Use this with Pageable to get the correct page of IDs
         */
        @Query("SELECT so.id FROM SalesOrder so " +
                        "WHERE so.status <> :excludeStatus " +
                        "ORDER BY so.createdDate DESC")
        Page<UUID> findPagedIds(@Param("excludeStatus") SalesStatus excludeStatus, Pageable pageable);

        /**
         * STEP 1 (Customer Filter): Get paginated order IDs for a specific customer
         * Lightweight query for customer-specific orders
         */
        @Query("SELECT so.id FROM SalesOrder so " +
                        "WHERE so.customer.id = :customerId " +
                        "AND so.status <> :excludeStatus " +
                        "ORDER BY so.createdDate DESC")
        Page<UUID> findPagedIdsByCustomer(
                        @Param("customerId") UUID customerId,
                        @Param("excludeStatus") SalesStatus excludeStatus,
                        Pageable pageable);

        /**
         * STEP 2: Fetch full orders with all details by IDs
         * This query uses JOIN FETCH but works on a small set of IDs (one page)
         * No pagination warning because we're not using firstResult/maxResults
         * 
         * NOTE: We fetch product attributes in a separate query to avoid
         * MultipleBagFetchException
         * Use findByIdInWithAttributeValues after this to batch-fetch attributes
         *
         * IMPORTANT: The IN clause maintains the order from the ID query
         */
        @Query("SELECT DISTINCT so FROM SalesOrder so " +
                        "LEFT JOIN FETCH so.lines l " +
                        "LEFT JOIN FETCH l.product p " +
                        "LEFT JOIN FETCH p.productType " +
                        "LEFT JOIN FETCH so.customer c " +
                        "LEFT JOIN FETCH c.role " +
                        "LEFT JOIN FETCH so.branch " +
                        "WHERE so.id IN :ids")
        List<SalesOrder> findByIdInWithDetails(@Param("ids") List<UUID> ids);
}
