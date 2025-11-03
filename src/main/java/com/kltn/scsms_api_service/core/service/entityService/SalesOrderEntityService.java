package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.entity.SalesOrderLine;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import com.kltn.scsms_api_service.core.repository.ProductAttributeValueRepository;
import com.kltn.scsms_api_service.core.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderEntityService {

    private final SalesOrderRepository repo;
    private final ProductAttributeValueRepository productAttributeValueRepo;

    public SalesOrder create(SalesOrder salesOrder) {
        return repo.save(salesOrder);
    }

    /**
     * Get sales order by ID (basic, may cause lazy loading)
     *
     * @deprecated Use requireWithDetails() for better performance
     */
    public SalesOrder require(UUID saleOrderId) {
        return repo.findById(saleOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found"));
    }

    /**
     * Get sales order by ID with all related entities (optimized, prevents N+1)
     * Use this method when you need to access lines, products, customer, or branch
     */
    public SalesOrder requireWithDetails(UUID saleOrderId) {
        return repo.findByIdWithDetails(saleOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found"));
    }

    public SalesOrder update(SalesOrder salesOrder) {
        return repo.save(salesOrder);
    }

    /**
     * Get all sales orders excluding RETURNED status
     *
     * @deprecated Use getAllWithDetails() for better performance
     */
    public List<SalesOrder> getAll() {
        return repo.findByStatusNotOrderByCreatedDateDesc(SalesStatus.RETURNED);
    }

    /**
     * Get all sales orders with related entities (optimized, prevents N+1)
     */
    public List<SalesOrder> getAllWithDetails() {
        return repo.findAllWithDetailsByStatusNot(SalesStatus.RETURNED);
    }

    /**
     * Get paged sales orders (basic query, may cause N+1)
     *
     * @deprecated Use getPagedOrdersWithDetails() for better performance
     */
    @Deprecated
    public Page<SalesOrder> getPagedOrders(Pageable pageable) {
        return repo.findByStatusNot(SalesStatus.RETURNED, pageable);
    }

    /**
     * Get paged sales orders with all related entities (optimized, prevents N+1)
     * Uses TWO-STEP QUERY approach: IDs → Orders
     *
     * Step 1: Get paginated IDs (fast, correct pagination in DB)
     * Step 2: Fetch full data with JOIN FETCH for those IDs only
     *
     * NOTE: Product attribute values are NOT fetched (lazy-loaded)
     * Customer order view doesn't need product attributes - only basic product info
     *
     * @param pageable Pagination information
     * @param userId   Optional user ID to filter orders by customer (null = all
     *                 orders)
     * @return Page of sales orders with all details
     * @Transactional ensures Hibernate session stays open for lazy loading
     */
    @Transactional(readOnly = true)
    public Page<SalesOrder> getPagedOrdersWithDetails(Pageable pageable, String userId) {
        log.info("🔍 [SalesOrderService] Fetching paged orders (userId: {}, page: {}, size: {})",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        // Step 1: Get page of IDs (lightweight, fast pagination)
        Page<UUID> idsPage;
        try {
            if (userId != null && !userId.isEmpty()) {
                // Filter by user/customer ID
                UUID userUUID = UUID.fromString(userId);
                log.info("📋 [SalesOrderService] Filtering orders for userId: {}", userId);
                idsPage = repo.findPagedIdsByCustomer(userUUID, SalesStatus.RETURNED, pageable);
                log.info("✅ [SalesOrderService] Found {} order IDs for customer", idsPage.getContent().size());
            } else {
                // Get all orders (admin view)
                log.info("📋 [SalesOrderService] Fetching all orders (no user filter)");
                idsPage = repo.findPagedIds(SalesStatus.RETURNED, pageable);
                log.info("✅ [SalesOrderService] Found {} order IDs total", idsPage.getContent().size());
            }
        } catch (Exception e) {
            log.error("❌ [SalesOrderService] Error fetching order IDs: {}", e.getMessage(), e);
            throw e;
        }

        if (idsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // Step 2: Fetch full orders with JOIN FETCH for these IDs
        log.info("📦 [SalesOrderService] Step 2: Fetching full orders with details...");
        List<SalesOrder> orders;
        try {
            orders = repo.findByIdInWithDetails(idsPage.getContent());
            log.info("✅ [SalesOrderService] Fetched {} orders with basic details", orders.size());
        } catch (Exception e) {
            log.error("❌ [SalesOrderService] Error fetching orders with details: {}", e.getMessage(), e);
            throw e;
        }

        // Maintain order from ID query (important!)
        Map<UUID, SalesOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(SalesOrder::getId, Function.identity()));

        List<SalesOrder> orderedResults = idsPage.getContent().stream()
                .map(orderMap::get)
                .toList();

        // Return Page with same pagination info as ID query
        return new PageImpl<>(orderedResults, pageable, idsPage.getTotalElements());
    }

    /**
     * OLD METHOD - Uses single query with collection fetch (causes in-memory
     * pagination)
     *
     * @deprecated Use getPagedOrdersWithDetails() which uses two-step approach
     */
    @Deprecated
    public Page<SalesOrder> getPagedOrdersWithDetailsOld(Pageable pageable) {
        return repo.findPagedWithDetails(SalesStatus.RETURNED, pageable);
    }

    /**
     * Get fulfilled sales orders
     *
     * @deprecated Use getAllFulfilledWithDetails() for better performance
     */
    public List<SalesOrder> getAllFullfills() {
        return repo.findByStatusOrderByCreatedDateDesc(SalesStatus.FULFILLED);
    }

    /**
     * Get fulfilled sales orders with all related entities (optimized, prevents
     * N+1)
     * Uses TWO-STEP approach to avoid MultipleBagFetchException:
     * Step 1: Fetch orders with lines and products
     * Step 2: Batch fetch product attributes for all products
     *
     * @Transactional ensures all entities are loaded in same session for proper
     *                initialization
     */
    @Transactional(readOnly = true)
    public List<SalesOrder> getAllFulfilledWithDetails() {
        log.debug("Fetching fulfilled orders with two-step query to prevent N+1");

        // Step 1: Fetch orders with lines, products, customer, branch
        List<SalesOrder> orders = repo.findAllWithDetailsByStatus(SalesStatus.FULFILLED);

        if (orders.isEmpty()) {
            return orders;
        }

        // Step 2: Batch fetch product attributes for all products in the orders
        List<UUID> productIds = orders.stream()
                .flatMap(order -> order.getLines().stream())
                .filter(line -> line.getProduct() != null)
                .map(line -> line.getProduct().getProductId())
                .distinct()
                .collect(Collectors.toList());

        if (!productIds.isEmpty()) {
            log.debug("Batch fetching attributes for {} products", productIds.size());
            // This single query fetches all product attributes at once
            // Hibernate will automatically associate them with loaded products in the same
            // session
            productAttributeValueRepo.findByProductIdInAndIsDeletedFalse(productIds);

            // Force initialization of attribute collections within the transaction
            orders.forEach(order -> order.getLines().forEach(line -> {
                if (line.getProduct() != null && line.getProduct().getAttributeValues() != null) {
                    line.getProduct().getAttributeValues().size(); // Force initialization
                }
            }));
        }

        return orders;
    }
}
