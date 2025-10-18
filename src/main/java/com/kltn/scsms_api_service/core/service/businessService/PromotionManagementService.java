package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionFilterParam;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.*;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.Promotion;
import com.kltn.scsms_api_service.core.entity.PromotionLine;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.PromotionLineService;
import com.kltn.scsms_api_service.core.service.entityService.PromotionService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.PromotionLineMapper;
import com.kltn.scsms_api_service.mapper.PromotionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class PromotionManagementService {

    private final PromotionService promotionService;
    private final PromotionLineService promotionLineService;
    private final BranchService branchService;
    private final ProductService productService;
    private final com.kltn.scsms_api_service.core.repository.SalesOrderRepository salesOrderRepository;

    private final PromotionMapper promotionMapper;
    private final PromotionLineMapper promotionLineMapper;

    /**
     * Get all promotions with filters
     */
    public Page<PromotionInfoDto> getAllPromotions(PromotionFilterParam filterParam) {
        log.info("Getting all promotions with filters: {}", filterParam);

        Page<Promotion> promotionPage = promotionService.getAllPromotionsWithFilters(filterParam);

        return promotionPage.map(promotionMapper::toPromotionInfoDto);
    }

    /**
     * Get promotion by ID
     */
    public PromotionInfoDto getPromotionById(UUID promotionId) {
        log.info("Getting promotion by ID: {}", promotionId);

        Promotion promotion = promotionService.getById(promotionId);

        return promotionMapper.toPromotionInfoDto(promotion);
    }

    /**
     * Get promotion by code
     */
    public PromotionInfoDto getPromotionByCode(String promotionCode) {
        log.info("Getting promotion by code: {}", promotionCode);

        Promotion promotion = promotionService.findByPromotionCode(promotionCode)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                        "Promotion with code " + promotionCode + " not found."));

        return promotionMapper.toPromotionInfoDto(promotion);
    }

    /**
     * Create new promotion with promotion lines
     */
    @Transactional
    public PromotionInfoDto createPromotion(CreatePromotionRequest createPromotionRequest) {
        log.info("Creating new promotion with code: {}", createPromotionRequest.getPromotionCode());

        // Validate promotion code uniqueness
        if (createPromotionRequest.getPromotionCode() != null &&
                promotionService.existsByPromotionCode(createPromotionRequest.getPromotionCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Promotion code " + createPromotionRequest.getPromotionCode() + " already exists.");
        }

        // Create promotion entity
        Promotion newPromotion = promotionMapper.toEntity(createPromotionRequest);

        // Set branch if provided
        if (createPromotionRequest.getBranchId() != null) {
            Branch branch = branchService.findById(createPromotionRequest.getBranchId()).orElseThrow(
                    () -> new ClientSideException(ErrorCode.BAD_REQUEST,
                            "Branch with ID " + createPromotionRequest.getBranchId() + " not found."));
            newPromotion.setBranch(branch);
        }

        // Save promotion first to get ID
        Promotion savedPromotion = promotionService.savePromotion(newPromotion);

        // Create promotion lines if provided
        if (createPromotionRequest.getPromotionLines() != null &&
                !createPromotionRequest.getPromotionLines().isEmpty()) {

            List<PromotionLine> promotionLines = createPromotionLines(
                    savedPromotion,
                    createPromotionRequest.getPromotionLines());

            savedPromotion.setPromotionLines(promotionLines);
        }

        log.info("Created new promotion with ID: {} and {} lines",
                savedPromotion.getPromotionId(),
                savedPromotion.getPromotionLines().size());

        return promotionMapper.toPromotionInfoDto(savedPromotion);
    }

    /**
     * Update existing promotion with promotion lines
     */
    @Transactional
    public PromotionInfoDto updatePromotion(UUID promotionId, UpdatePromotionRequest updatePromotionRequest) {
        log.info("Updating promotion with ID: {}", promotionId);

        // Get existing promotion
        Promotion existingPromotion = promotionService.getById(promotionId);

        // Validate promotion code uniqueness if being updated
        if (updatePromotionRequest.getPromotionCode() != null &&
                !updatePromotionRequest.getPromotionCode().equals(existingPromotion.getPromotionCode())) {

            if (promotionService.existsByPromotionCodeAndPromotionIdNot(
                    updatePromotionRequest.getPromotionCode(), promotionId)) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                        "Promotion code " + updatePromotionRequest.getPromotionCode() + " already exists.");
            }
        }

        // Update promotion entity
        promotionMapper.updateEntityFromRequest(updatePromotionRequest, existingPromotion);

        // Update branch if provided
        if (updatePromotionRequest.getBranchId() != null) {
            Branch branch = branchService.findById(updatePromotionRequest.getBranchId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                            "Branch with ID " + updatePromotionRequest.getBranchId() + " not found."));
            existingPromotion.setBranch(branch);
        }

        // Update promotion lines if provided
        if (updatePromotionRequest.getPromotionLines() != null) {
            updatePromotionLines(existingPromotion, updatePromotionRequest.getPromotionLines());
        }

        // Save updated promotion
        Promotion updatedPromotion = promotionService.savePromotion(existingPromotion);

        log.info("Updated promotion with ID: {} with {} lines",
                updatedPromotion.getPromotionId(),
                updatedPromotion.getPromotionLines().size());

        return promotionMapper.toPromotionInfoDto(updatedPromotion);
    }

    /**
     * Delete promotion (soft delete)
     */
    @Transactional
    public void deletePromotion(UUID promotionId) {
        log.info("Deleting promotion with ID: {}", promotionId);

        // Check promotion exists
        Promotion existingPromotion = promotionService.getById(promotionId);

        // Check if promotion is being used
        if (existingPromotion.getUsages() != null && !existingPromotion.getUsages().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Cannot delete promotion that has been used. Used count: " + existingPromotion.getUsages().size());
        }

        // Soft delete promotion
        promotionService.deletePromotion(existingPromotion);

        log.info("Deleted promotion with ID: {}", promotionId);
    }

    /**
     * Update promotion status (activate/deactivate)
     */
    @Transactional
    public void updatePromotionStatus(UUID promotionId, UpdatePromotionStatusRequest request) {
        log.info("Updating promotion status with ID: {} to active: {}", promotionId, request.getIsActive());

        promotionService.updatePromotionStatus(promotionId, request.getIsActive());

        log.info("Updated promotion status with ID: {} to active: {}", promotionId, request.getIsActive());
    }

    /**
     * Make promotion visible
     */
    @Transactional
    public void makePromotionVisible(UUID promotionId) {
        log.info("Making promotion visible with ID: {}", promotionId);

        promotionService.updatePromotionVisibility(promotionId, true);

        log.info("Made promotion visible with ID: {}", promotionId);
    }

    /**
     * Make promotion invisible
     */
    @Transactional
    public void makePromotionInvisible(UUID promotionId) {
        log.info("Making promotion invisible with ID: {}", promotionId);

        promotionService.updatePromotionVisibility(promotionId, false);

        log.info("Made promotion invisible with ID: {}", promotionId);
    }

    /**
     * Restore promotion (undo soft delete)
     */
    @Transactional
    public void restorePromotion(UUID promotionId) {
        log.info("Restoring promotion with ID: {}", promotionId);

        promotionService.restorePromotion(promotionId);

        log.info("Restored promotion with ID: {}", promotionId);
    }

    /**
     * Get promotion statistics
     */
    public PromotionStatisticsDto getPromotionStatistics() {
        log.info("Getting promotion statistics");

        return PromotionStatisticsDto.builder()
                .totalPromotions(promotionService.getTotalPromotionsCount())
                .activePromotions(promotionService.getActivePromotionsCount())
                .visiblePromotions(promotionService.getVisiblePromotionsCount())
                .autoApplyPromotions(promotionService.getAutoApplyPromotionsCount())
                .stackablePromotions(promotionService.getStackablePromotionsCount())
                .build();
    }

    /**
     * Get active promotions
     */
    public Page<PromotionInfoDto> getActivePromotions(PromotionFilterParam filterParam) {
        log.info("Getting active promotions");

        filterParam.setIsActive(true);
        filterParam.setIsExpired(false);

        return getAllPromotions(filterParam);
    }

    /**
     * Get visible promotions
     */
    public Page<PromotionInfoDto> getVisiblePromotions(PromotionFilterParam filterParam) {
        log.info("Getting visible promotions");

        filterParam.setIsActive(true);
        filterParam.setIsExpired(false);

        return getAllPromotions(filterParam);
    }

    /**
     * Get expired promotions
     */
    public Page<PromotionInfoDto> getExpiredPromotions(PromotionFilterParam filterParam) {
        log.info("Getting expired promotions");

        filterParam.setIsExpired(true);

        return getAllPromotions(filterParam);
    }

    /**
     * Get promotions starting soon
     */
    public Page<PromotionInfoDto> getPromotionsStartingSoon(PromotionFilterParam filterParam) {
        log.info("Getting promotions starting soon");

        filterParam.setIsStartingSoon(true);

        return getAllPromotions(filterParam);
    }

    /**
     * Get promotions ending soon
     */
    public Page<PromotionInfoDto> getPromotionsEndingSoon(PromotionFilterParam filterParam) {
        log.info("Getting promotions ending soon");

        filterParam.setIsEndingSoon(true);

        return getAllPromotions(filterParam);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Create promotion lines from request
     */
    private List<PromotionLine> createPromotionLines(
            Promotion promotion,
            List<CreatePromotionLineRequest> lineRequests) {

        List<PromotionLine> promotionLines = new ArrayList<>();

        for (CreatePromotionLineRequest lineRequest : lineRequests) {
            // Validate line request
            validatePromotionLineRequest(lineRequest);

            // Build promotion line
            PromotionLine promotionLine = buildPromotionLine(promotion, lineRequest);

            // Save promotion line
            PromotionLine savedLine = promotionLineService.save(promotionLine);
            promotionLines.add(savedLine);

            log.debug("Created promotion line with ID: {} for promotion: {}",
                    savedLine.getPromotionLineId(), promotion.getPromotionId());
        }

        return promotionLines;
    }

    /**
     * Update promotion lines
     */
    private void updatePromotionLines(
            Promotion promotion,
            List<UpdatePromotionLineRequest> lineRequests) {

        // Delete all existing promotion lines
        if (promotion.getPromotionLines() != null && !promotion.getPromotionLines().isEmpty()) {
            List<UUID> existingLineIds = promotion.getPromotionLines().stream()
                    .map(PromotionLine::getPromotionLineId)
                    .collect(Collectors.toList());

            promotionLineService.deleteAllByIds(existingLineIds);
            promotion.getPromotionLines().clear();
        }

        // Create new promotion lines
        if (lineRequests != null && !lineRequests.isEmpty()) {
            List<PromotionLine> newLines = new ArrayList<>();

            for (UpdatePromotionLineRequest lineRequest : lineRequests) {
                // Validate line request
                validatePromotionLineUpdateRequest(lineRequest);

                // Build promotion line
                PromotionLine promotionLine = buildPromotionLineFromUpdate(promotion, lineRequest);

                // Save promotion line
                PromotionLine savedLine = promotionLineService.save(promotionLine);
                newLines.add(savedLine);

                log.debug("Updated promotion line with ID: {} for promotion: {}",
                        savedLine.getPromotionLineId(), promotion.getPromotionId());
            }

            promotion.setPromotionLines(newLines);
        }
    }

    /**
     * Build promotion line from create request
     */
    private PromotionLine buildPromotionLine(
            Promotion promotion,
            CreatePromotionLineRequest request) {

        PromotionLine.PromotionLineBuilder builder = PromotionLine.builder()
                .promotion(promotion)
                .lineType(request.getLineType())
                .targetId(request.getTargetId())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .minQuantity(request.getMinQuantity())
                .buyQty(request.getBuyQty())
                .getQty(request.getGetQty())
                .freeQuantity(request.getFreeQuantity())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .linePriority(request.getLinePriority())
                .isActive(request.getIsActive());

        // Set branch if provided
        if (request.getBranchId() != null) {
            Branch branch = branchService.findById(request.getBranchId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                            "Branch with ID " + request.getBranchId() + " not found."));
            builder.branch(branch);
        }

        // Set free product if provided
        if (request.getFreeProductId() != null) {
            Product freeProduct = productService.findById(request.getFreeProductId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                            "Product with ID " + request.getFreeProductId() + " not found."));
            builder.freeProduct(freeProduct);
        }

        return builder.build();
    }

    /**
     * Build promotion line from update request
     */
    private PromotionLine buildPromotionLineFromUpdate(
            Promotion promotion,
            UpdatePromotionLineRequest request) {

        PromotionLine.PromotionLineBuilder builder = PromotionLine.builder()
                .promotion(promotion)
                .lineType(request.getLineType())
                .targetId(request.getTargetId())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .minQuantity(request.getMinQuantity())
                .buyQty(request.getBuyQty())
                .getQty(request.getGetQty())
                .freeQuantity(request.getFreeQuantity())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .linePriority(request.getLinePriority())
                .isActive(request.getIsActive());

        // Set branch if provided
        if (request.getBranchId() != null) {
            Branch branch = branchService.findById(request.getBranchId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                            "Branch with ID " + request.getBranchId() + " not found."));
            builder.branch(branch);
        }

        // Set free product if provided
        if (request.getFreeProductId() != null) {
            Product freeProduct = productService.findById(request.getFreeProductId())
                    .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST,
                            "Product with ID " + request.getFreeProductId() + " not found."));
            builder.freeProduct(freeProduct);
        }

        return builder.build();
    }

    /**
     * Validate promotion line create request
     */
    private void validatePromotionLineRequest(CreatePromotionLineRequest request) {
        // Validate target_id for specific line types
        if (request.getLineType() != PromotionLine.LineType.ALL &&
                request.getTargetId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Target ID is required for line type: " + request.getLineType());
        }

        // Validate discount value is provided
        if (request.getDiscountValue() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Discount value is required");
        }

        // Validate BUY_X_GET_Y specific fields
        if (request.getDiscountType() == PromotionLine.DiscountType.BUY_X_GET_Y) {
            if (request.getBuyQty() == null || request.getGetQty() == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                        "Buy quantity and Get quantity are required for BUY_X_GET_Y discount type");
            }
        }

        // Validate FREE_PRODUCT specific fields
        if (request.getDiscountType() == PromotionLine.DiscountType.FREE_PRODUCT) {
            if (request.getFreeProductId() == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                        "Free product ID is required for FREE_PRODUCT discount type");
            }
        }
    }

    /**
     * Validate promotion line update request
     */
    private void validatePromotionLineUpdateRequest(UpdatePromotionLineRequest request) {
        // Validate target_id for specific line types
        if (request.getLineType() != null &&
                request.getLineType() != PromotionLine.LineType.ALL &&
                request.getTargetId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Target ID is required for line type: " + request.getLineType());
        }

        // Validate BUY_X_GET_Y specific fields
        if (request.getDiscountType() == PromotionLine.DiscountType.BUY_X_GET_Y) {
            if (request.getBuyQty() == null || request.getGetQty() == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                        "Buy quantity and Get quantity are required for BUY_X_GET_Y discount type");
            }
        }

        // Validate FREE_PRODUCT specific fields
        if (request.getDiscountType() == PromotionLine.DiscountType.FREE_PRODUCT) {
            if (request.getFreeProductId() == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                        "Free product ID is required for FREE_PRODUCT discount type");
            }
        }
    }

    public PromotionInfoDto duplicatePromotion(UUID promotionId) {
        log.info("Duplicating promotion with ID: {}", promotionId);

        // Get existing promotion
        Promotion existingPromotion = promotionService.getById(promotionId);

        // Create a copy of the promotion
        Promotion newPromotion = new Promotion();
        newPromotion.setPromotionCode(null); // New code must be set by user
        newPromotion.setName(existingPromotion.getName() + " (Copy)");
        newPromotion.setDescription(existingPromotion.getDescription());
        newPromotion.setStartAt(existingPromotion.getStartAt());
        newPromotion.setEndAt(existingPromotion.getEndAt());
        newPromotion.setUsageLimit(existingPromotion.getUsageLimit());
        newPromotion.setPerCustomerLimit(existingPromotion.getPerCustomerLimit());
        newPromotion.setPriority(existingPromotion.getPriority());
        newPromotion.setIsStackable(existingPromotion.getIsStackable());
        newPromotion.setCouponRedeemOnce(existingPromotion.getCouponRedeemOnce());
        newPromotion.setBranch(existingPromotion.getBranch());
        newPromotion.setIsActive(false); // New promotion is inactive by default
        newPromotion.setIsDeleted(false);

        // Save new promotion to get ID
        Promotion savedNewPromotion = promotionService.savePromotion(newPromotion);

        // Duplicate promotion lines
        if (existingPromotion.getPromotionLines() != null &&
                !existingPromotion.getPromotionLines().isEmpty()) {

            List<CreatePromotionLineRequest> lineRequests = existingPromotion.getPromotionLines().stream()
                    // Remove IDs to avoid conflicts
                    .map(promotionLineMapper::toCreateRequest)
                    .collect(Collectors.toList());

            List<PromotionLine> newLines = createPromotionLines(savedNewPromotion, lineRequests);
            savedNewPromotion.setPromotionLines(newLines);

            // Save updated promotion with lines
            savedNewPromotion = promotionService.savePromotion(savedNewPromotion);
        }

        log.info("Duplicated promotion with new ID: {}", savedNewPromotion.getPromotionId());

        return promotionMapper.toPromotionInfoDto(savedNewPromotion);
    }

    /**
     * Get promotion usage history
     * Retrieves all sales orders that have applied promotions with filtering
     * support
     */
    public Page<com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionUsageHistoryDto> getPromotionUsageHistory(
            com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionUsageHistoryFilterParam filterParam) {
        log.info("Getting promotion usage history with filters: {}", filterParam);

        // Build query using specifications
        org.springframework.data.jpa.domain.Specification<com.kltn.scsms_api_service.core.entity.SalesOrder> spec = buildPromotionUsageSpec(
                filterParam);

        // Create pageable
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                filterParam.getPage(),
                filterParam.getSize(),
                org.springframework.data.domain.Sort.by(
                        filterParam.getDirection().equalsIgnoreCase("ASC")
                                ? org.springframework.data.domain.Sort.Direction.ASC
                                : org.springframework.data.domain.Sort.Direction.DESC,
                        mapSortField(filterParam.getSort())));

        // Query database
        Page<com.kltn.scsms_api_service.core.entity.SalesOrder> salesOrders = salesOrderRepository.findAll(spec,
                pageable);

        // Map to DTO
        return salesOrders.map(this::mapToPromotionUsageHistoryDto);
    }

    /**
     * Build specification for promotion usage history filtering
     */
    private org.springframework.data.jpa.domain.Specification<com.kltn.scsms_api_service.core.entity.SalesOrder> buildPromotionUsageSpec(
            com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionUsageHistoryFilterParam filterParam) {

        org.springframework.data.jpa.domain.Specification<com.kltn.scsms_api_service.core.entity.SalesOrder> spec = (
                root, query, cb) -> {
            // Only orders with discount (promotions applied)
            return cb.and(
                    cb.isNotNull(root.get("totalDiscountAmount")),
                    cb.greaterThan(root.get("totalDiscountAmount"), java.math.BigDecimal.ZERO),
                    cb.isNotNull(root.get("promotionSnapshot")));
        };

        // Filter by status
        if (filterParam.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filterParam.getStatus()));
        }

        // Filter by branch
        if (filterParam.getBranchId() != null) {
            spec = spec
                    .and((root, query, cb) -> cb.equal(root.get("branch").get("branchId"), filterParam.getBranchId()));
        }

        if (filterParam.getBranchName() != null && !filterParam.getBranchName().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("branch").get("branchName")),
                    "%" + filterParam.getBranchName().toLowerCase() + "%"));
        }

        // Filter by customer
        if (filterParam.getCustomerName() != null && !filterParam.getCustomerName().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("customer").get("fullName")),
                    "%" + filterParam.getCustomerName().toLowerCase() + "%"));
        }

        if (filterParam.getCustomerPhone() != null && !filterParam.getCustomerPhone().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("customer").get("phoneNumber"),
                    "%" + filterParam.getCustomerPhone() + "%"));
        }

        // Filter by order ID
        if (filterParam.getOrderId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("id"), filterParam.getOrderId()));
        }

        // Filter by date range
        if (filterParam.getFromDate() != null) {
            spec = spec.and(
                    (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdDate"), filterParam.getFromDate()));
        }

        if (filterParam.getToDate() != null) {
            spec = spec
                    .and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdDate"), filterParam.getToDate()));
        }

        // Filter by promotion code/name (search in promotionSnapshot JSON)
        if (filterParam.getPromotionCode() != null && !filterParam.getPromotionCode().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("promotionSnapshot")),
                    "%" + filterParam.getPromotionCode().toLowerCase() + "%"));
        }

        if (filterParam.getPromotionName() != null && !filterParam.getPromotionName().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("promotionSnapshot")),
                    "%" + filterParam.getPromotionName().toLowerCase() + "%"));
        }

        return spec;
    }

    /**
     * Map sort field from DTO to entity field
     */
    private String mapSortField(String sortField) {
        return switch (sortField) {
            case "usedDate" -> "createdDate";
            case "orderAmount" -> "originalAmount";
            case "discountAmount" -> "totalDiscountAmount";
            case "finalAmount" -> "finalAmount";
            case "customerName" -> "customer.fullName";
            case "branchName" -> "branch.branchName";
            default -> "createdDate";
        };
    }

    /**
     * Map SalesOrder entity to PromotionUsageHistoryDto
     */
    private com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionUsageHistoryDto mapToPromotionUsageHistoryDto(
            com.kltn.scsms_api_service.core.entity.SalesOrder salesOrder) {

        // Parse promotion snapshot to get promotion details
        String promotionName = "N/A";
        String promotionCode = "N/A";
        String notes = null;

        if (salesOrder.getPromotionSnapshot() != null && !salesOrder.getPromotionSnapshot().isBlank()) {
            try {
                // Parse JSON to extract promotion info
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode promotionsArray = objectMapper
                        .readTree(salesOrder.getPromotionSnapshot());

                if (promotionsArray.isArray() && !promotionsArray.isEmpty()) {
                    com.fasterxml.jackson.databind.JsonNode firstPromotion = promotionsArray.get(0);

                    if (firstPromotion.has("name")) {
                        promotionName = firstPromotion.get("name").asText();
                    }
                    if (firstPromotion.has("promotion_code")) {
                        promotionCode = firstPromotion.get("promotion_code").asText();
                    }

                    // If multiple promotions, add note
                    if (promotionsArray.size() > 1) {
                        notes = "Áp dụng " + promotionsArray.size() + " khuyến mãi";
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse promotion snapshot for order {}: {}", salesOrder.getId(), e.getMessage());
            }
        }

        return com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionUsageHistoryDto.builder()
                .promotionName(promotionName)
                .promotionCode(promotionCode)
                .customerName(salesOrder.getCustomer() != null ? salesOrder.getCustomer().getFullName() : "Khách lẻ")
                .customerPhone(salesOrder.getCustomer() != null ? salesOrder.getCustomer().getPhoneNumber() : "N/A")
                .orderId(salesOrder.getId())
                .orderAmount(salesOrder.getOriginalAmount())
                .discountAmount(salesOrder.getTotalDiscountAmount())
                .finalAmount(salesOrder.getFinalAmount())
                .usedDate(salesOrder.getCreatedDate())
                .branchName(salesOrder.getBranch() != null ? salesOrder.getBranch().getBranchName() : "N/A")
                .status(salesOrder.getStatus())
                .notes(notes)
                .build();
    }

    /**
     * Promotion statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PromotionStatisticsDto {
        private Long totalPromotions;
        private Long activePromotions;
        private Long visiblePromotions;
        private Long autoApplyPromotions;
        private Long stackablePromotions;
    }
}
