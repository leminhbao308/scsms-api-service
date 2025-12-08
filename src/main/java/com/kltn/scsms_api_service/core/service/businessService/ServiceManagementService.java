package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceProductInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.param.ServiceFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceRequest;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessInfoDto;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.entity.ServiceProcess;
import com.kltn.scsms_api_service.core.entity.ServiceProcessStep;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProcessService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProcessStepService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProductService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceTypeService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ServiceMapper;
import com.kltn.scsms_api_service.mapper.ServiceProductMapper;
import com.kltn.scsms_api_service.mapper.ServiceProcessMapper;
import com.kltn.scsms_api_service.mapper.ServiceProcessStepMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceManagementService {

    private final ServiceService serviceService;
    private final CategoryService categoryService;
    private final ServiceProcessService serviceProcessService;
    private final ServiceProcessStepService serviceProcessStepService;
    private final ServiceProductService serviceProductService;
    private final ProductService productService;
    private final ServiceTypeService serviceTypeService;
    private final ServiceMapper serviceMapper;
    private final ServiceProductMapper serviceProductMapper;
    private final ServiceProcessMapper serviceProcessMapper;
    private final ServiceProcessStepMapper serviceProcessStepMapper;

    public List<ServiceInfoDto> getAllServices() {
        log.info("Getting all services");
        List<Service> services = serviceService.findAll();
        return services.stream()
                .map(this::enrichServiceWithDetails)
                .collect(Collectors.toList());
    }

    public Page<ServiceInfoDto> getAllServices(ServiceFilterParam filterParam) {
        log.info("Getting all services with filter: {}", filterParam);

        // Standardize filter
        filterParam = filterParam.standardizeFilterRequest(filterParam);

        // Create pageable
        Sort sort = Sort.by(
                filterParam.getDirection().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC,
                filterParam.getSort());
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), sort);

        // Build specification from filter params
        Specification<Service> spec = buildServiceSpecification(filterParam);

        // Get services with specification and pagination
        Page<Service> servicePage = serviceService.findAll(spec, pageable);

        return servicePage.map(this::enrichServiceWithDetails);
    }
    
    /**
     * Build JPA Specification from ServiceFilterParam
     */
    private Specification<Service> buildServiceSpecification(ServiceFilterParam filterParam) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude deleted services
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // Filter by is_active
            if (filterParam.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filterParam.getIsActive()));
            }

            // Filter by is_featured
            if (filterParam.getIsFeatured() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFeatured"), filterParam.getIsFeatured()));
            }

            // Filter by category
            if (filterParam.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("categoryId"), filterParam.getCategoryId()));
            }

            // Filter by service type
            if (filterParam.getServiceTypeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceTypeId"), filterParam.getServiceTypeId()));
            }

            // Filter by skill level
            if (filterParam.getRequiredSkillLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("requiredSkillLevel"), filterParam.getRequiredSkillLevel()));
            }

            // Filter by duration range
            if (filterParam.getMinDuration() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("estimatedDuration"), filterParam.getMinDuration()));
            }
            if (filterParam.getMaxDuration() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("estimatedDuration"), filterParam.getMaxDuration()));
            }

            // Search by keyword (service name or description)
            if (filterParam.getSearch() != null && !filterParam.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filterParam.getSearch().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("serviceName")), searchPattern);
                Predicate descPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            // Filter by service name
            if (filterParam.getServiceName() != null && !filterParam.getServiceName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("serviceName")),
                        "%" + filterParam.getServiceName().toLowerCase() + "%"));
            }

            // Filter by created date range
            if (filterParam.getCreatedDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), filterParam.getCreatedDateFrom()));
            }
            if (filterParam.getCreatedDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), filterParam.getCreatedDateTo()));
            }

            // Filter by modified date range
            if (filterParam.getModifiedDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("modifiedDate"), filterParam.getModifiedDateFrom()));
            }
            if (filterParam.getModifiedDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("modifiedDate"), filterParam.getModifiedDateTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public ServiceInfoDto getServiceById(UUID serviceId) {
        log.info("Getting service by ID: {}", serviceId);
        Service service = serviceService.getById(serviceId);
        return enrichServiceWithDetails(service);
    }

    public ServiceInfoDto getServiceByUrl(String serviceUrl) {
        log.info("Getting service by URL: {}", serviceUrl);
        Service service = serviceService.getByServiceUrl(serviceUrl);
        return enrichServiceWithDetails(service);
    }

    public List<ServiceInfoDto> getServicesByCategory(UUID categoryId) {
        List<Service> services = serviceService.findByCategoryId(categoryId);
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }

    public List<ServiceInfoDto> getServicesByTypeId(UUID serviceTypeId) {
        List<Service> services = serviceService.findByServiceTypeId(serviceTypeId);
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }

    public List<ServiceInfoDto> getServicesBySkillLevel(Service.SkillLevel skillLevel) {
        List<Service> services = serviceService.findBySkillLevel(skillLevel);
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }

    public List<ServiceInfoDto> searchServices(String keyword) {
        List<Service> services = serviceService.searchByKeyword(keyword);
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }

    public List<ServiceInfoDto> getFeaturedServices() {
        List<Service> services = serviceService.findFeaturedServices();
        return services.stream()
                .map(serviceMapper::toServiceInfoDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceInfoDto createService(CreateServiceRequest createServiceRequest) {
        // Validate service URL uniqueness
        if (serviceService.existsByServiceUrl(createServiceRequest.getServiceUrl())) {
            throw new ClientSideException(ErrorCode.SERVICE_URL_EXISTS,
                    "Service URL already exists: " + createServiceRequest.getServiceUrl());
        }

        // Validate category exists if provided
        Category category = null;
        if (createServiceRequest.getCategoryId() != null) {
            category = categoryService.getById(createServiceRequest.getCategoryId());
        }

        // Handle service process
        ServiceProcess serviceProcess = null;
        if (createServiceRequest.getServiceProcess() != null) {
            // Create new service process
            serviceProcess = createServiceProcessFromRequest(createServiceRequest.getServiceProcess());
        } else if (createServiceRequest.getServiceProcessId() != null) {
            // Use existing service process
            serviceProcess = serviceProcessService.findByIdOrThrow(createServiceRequest.getServiceProcessId());
        }

        // Create service
        Service service = serviceMapper.toEntity(createServiceRequest);
        service.setCategory(category);
        service.setServiceProcess(serviceProcess);

        // Set default values
        if (service.getIsFeatured() == null) {
            service.setIsFeatured(false);
        }

        // Save service
        Service savedService = serviceService.save(service);

        // Handle service products
        if (createServiceRequest.getServiceProducts() != null && !createServiceRequest.getServiceProducts().isEmpty()) {
            processServiceProducts(savedService, createServiceRequest.getServiceProducts());
        }

        return getServiceById(savedService.getServiceId());
    }

    @Transactional
    public ServiceInfoDto updateService(UUID serviceId, UpdateServiceRequest updateServiceRequest) {
        log.info("Updating service with ID: {}", serviceId);

        // Get existing service
        Service existingService = serviceService.getById(serviceId);

        // Validate service URL uniqueness if changed
        if (updateServiceRequest.getServiceUrl() != null &&
                !updateServiceRequest.getServiceUrl().equals(existingService.getServiceUrl()) &&
                serviceService.existsByServiceUrl(updateServiceRequest.getServiceUrl())) {
            throw new ClientSideException(ErrorCode.SERVICE_URL_EXISTS,
                    "Service URL already exists: " + updateServiceRequest.getServiceUrl());
        }

        // Validate category exists if provided
        if (updateServiceRequest.getCategoryId() != null) {
            Category category = categoryService.getById(updateServiceRequest.getCategoryId());
            existingService.setCategory(category);
        }

        // Handle service process
        if (updateServiceRequest.getServiceProcess() != null) {
            // Update existing service process
            updateServiceProcessFromRequest(existingService.getServiceProcess(),
                    updateServiceRequest.getServiceProcess());
        } else if (updateServiceRequest.getServiceProcessId() != null) {
            // Use different service process
            ServiceProcess serviceProcess = serviceProcessService
                    .findByIdOrThrow(updateServiceRequest.getServiceProcessId());
            existingService.setServiceProcess(serviceProcess);
        }

        // Update service
        Service updatedService = serviceMapper.updateEntity(existingService, updateServiceRequest);
        Service savedService = serviceService.update(updatedService);

        // Handle service products
        if (updateServiceRequest.getServiceProducts() != null) {
            processUpdateServiceProducts(savedService, updateServiceRequest.getServiceProducts());
        }

        return getServiceById(savedService.getServiceId());
    }

    @Transactional
    public void deleteService(UUID serviceId) {
        log.info("Deleting service with ID: {}", serviceId);
        serviceService.softDeleteById(serviceId);
    }

    @Transactional
    public void updateServiceStatus(UUID serviceId, Boolean isActive) {
        log.info("Updating service status for ID: {} to active: {}", serviceId, isActive);
        Service service = serviceService.getById(serviceId);
        service.setIsActive(isActive);
        // Note: Only update isActive, do not touch isDeleted field
        // isDeleted should only be updated when explicitly deleting the service
        serviceService.update(service);
    }

    public long getServiceCountByCategory(UUID categoryId) {
        log.info("Getting service count by category ID: {}", categoryId);
        return serviceService.countByCategoryId(categoryId);
    }

    public long getServiceCountByTypeId(UUID serviceTypeId) {
        log.info("Getting service count by type ID: {}", serviceTypeId);
        return serviceService.countByServiceTypeId(serviceTypeId);
    }

    public long getServiceCountBySkillLevel(Service.SkillLevel skillLevel) {
        log.info("Getting service count by skill level: {}", skillLevel);
        return serviceService.countBySkillLevel(skillLevel);
    }

    // ========== SERVICE PRODUCT MANAGEMENT ==========

    /**
     * Lấy tất cả sản phẩm của một service
     */
    public List<ServiceProductInfoDto> getServiceProducts(UUID serviceId) {
        log.info("Getting products for service: {}", serviceId);
        List<ServiceProduct> serviceProducts = serviceProductService.findByServiceIdWithProduct(serviceId);
        return serviceProducts.stream()
                .map(serviceProductMapper::toServiceProductInfoDto)
                .collect(Collectors.toList());
    }

    /**
     * Thêm sản phẩm vào service
     */
    @Transactional
    public ServiceProductInfoDto addProductToService(UUID serviceId,
            com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceProductRequest request) {
        log.info("Adding product to service: {}", serviceId);

        // Validate service exists
        Service service = serviceService.getById(serviceId);

        // Validate product exists
        Product product = productService.getById(request.getProductId());

        // Check if product already exists in service
        if (serviceProductService.existsByServiceIdAndProductIdAndIdNot(serviceId, request.getProductId(), null)) {
            throw new ClientSideException(ErrorCode.SERVICE_PRODUCT_ALREADY_EXISTS);
        }

        // Create ServiceProduct
        ServiceProduct serviceProduct = ServiceProduct.builder()
                .service(service)
                .product(product)
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .notes(request.getNotes())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        ServiceProduct savedProduct = serviceProductService.save(serviceProduct);
        return serviceProductMapper.toServiceProductInfoDto(savedProduct);
    }

    /**
     * Cập nhật service product
     */
    @Transactional
    public ServiceProductInfoDto updateServiceProduct(UUID serviceProductId,
            com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceProductRequest request) {
        log.info("Updating service product: {}", serviceProductId);

        ServiceProduct existingProduct = serviceProductService.findByIdOrThrow(serviceProductId);

        // Check if product already exists in service (if productId changed)
        if (request.getProductId() != null
                && !request.getProductId().equals(existingProduct.getProduct().getProductId())) {
            if (serviceProductService.existsByServiceIdAndProductIdAndIdNot(
                    existingProduct.getService().getServiceId(), request.getProductId(), serviceProductId)) {
                throw new ClientSideException(ErrorCode.SERVICE_PRODUCT_ALREADY_EXISTS);
            }
        }

        // Update product information
        serviceProductMapper.updateEntity(existingProduct, request);

        // Update product if changed
        if (request.getProductId() != null
                && !request.getProductId().equals(existingProduct.getProduct().getProductId())) {
            Product product = productService.getById(request.getProductId());
            existingProduct.setProduct(product);
        }

        ServiceProduct updatedProduct = serviceProductService.update(existingProduct);
        return serviceProductMapper.toServiceProductInfoDto(updatedProduct);
    }

    /**
     * Xóa hẳn sản phẩm khỏi service
     */
    @Transactional
    public void removeProductFromService(UUID serviceProductId) {
        log.info("Removing service product permanently: {}", serviceProductId);
        serviceProductService.deleteById(serviceProductId);
    }

    // ========== HELPER METHODS ==========

    /**
     * Enrich service with full details (products and process)
     */
    private ServiceInfoDto enrichServiceWithDetails(Service service) {
        ServiceInfoDto dto = serviceMapper.toServiceInfoDto(service);

        // Load service type name
        if (service.getServiceTypeId() != null) {
            serviceTypeService.findById(service.getServiceTypeId())
                    .ifPresent(serviceType -> dto.setServiceTypeName(serviceType.getName()));
        }

        // Load service products
        log.info("Loading service products for service ID: {}", service.getServiceId());
        List<ServiceProduct> serviceProducts = serviceProductService.findByServiceIdWithProduct(service.getServiceId());
        log.info("Found {} service products for service: {}", serviceProducts.size(), service.getServiceName());
        List<ServiceProductInfoDto> serviceProductDtos = serviceProducts.stream()
                .map(serviceProductMapper::toServiceProductInfoDto)
                .collect(Collectors.toList());
        dto.setServiceProducts(serviceProductDtos);

        // Load service process if exists
        if (service.getServiceProcess() != null) {
            log.info("Loading service process with steps for process ID: {}", service.getServiceProcess().getId());
            // Load process with steps
            ServiceProcess processWithSteps = serviceProcessService
                    .findByIdWithProcessStepsOrThrow(service.getServiceProcess().getId());
            log.info("Loaded process: {} with {} steps", processWithSteps.getCode(),
                    processWithSteps.getProcessSteps() != null ? processWithSteps.getProcessSteps().size() : 0);
            if (processWithSteps.getProcessSteps() != null && !processWithSteps.getProcessSteps().isEmpty()) {
                log.info("First step entity: {}", processWithSteps.getProcessSteps().get(0));
            }
            ServiceProcessInfoDto processDto = serviceProcessMapper.toServiceProcessInfoDto(processWithSteps);
            log.info("Mapped process DTO with {} steps",
                    processDto.getProcessSteps() != null ? processDto.getProcessSteps().size() : 0);
            if (processDto.getProcessSteps() != null && !processDto.getProcessSteps().isEmpty()) {
                log.info("First step DTO: {}", processDto.getProcessSteps().get(0));
            }
            dto.setServiceProcess(processDto);
            log.info("Set service process to DTO. DTO process steps count: {}",
                    dto.getServiceProcess() != null && dto.getServiceProcess().getProcessSteps() != null
                            ? dto.getServiceProcess().getProcessSteps().size()
                            : 0);
        } else {
            log.info("No service process found for service: {}", service.getServiceName());
        }

        return dto;
    }

    /**
     * Tạo ServiceProcess từ CreateServiceProcessRequest
     */
    private ServiceProcess createServiceProcessFromRequest(
            com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.CreateServiceProcessRequest request) {
        ServiceProcess serviceProcess = ServiceProcess.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        ServiceProcess savedProcess = serviceProcessService.save(serviceProcess);

        // Xử lý các bước nếu có
        log.info("Process steps from request: {}", request.getProcessSteps());
        if (request.getProcessSteps() != null && !request.getProcessSteps().isEmpty()) {
            log.info("Creating {} process steps for process: {}", request.getProcessSteps().size(),
                    savedProcess.getCode());
            for (com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.CreateServiceProcessStepRequest stepRequest : request
                    .getProcessSteps()) {
                log.info("Processing step request: {}", stepRequest);
                ServiceProcessStep step = serviceProcessStepMapper.toEntity(stepRequest);
                step.setServiceProcess(savedProcess);
                ServiceProcessStep savedStep = serviceProcessStepService.save(step);
                log.info("Created process step: {} (order: {}) with ID: {}", savedStep.getName(),
                        savedStep.getStepOrder(), savedStep.getId());
            }
        } else {
            log.warn("No process steps found in request or processSteps is empty");
        }

        return savedProcess;
    }

    /**
     * Cập nhật ServiceProcess từ UpdateServiceProcessRequest
     */
    private void updateServiceProcessFromRequest(ServiceProcess existingProcess,
            com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.UpdateServiceProcessRequest request) {
        if (existingProcess == null) {
            return;
        }

        if (request.getCode() != null) {
            existingProcess.setCode(request.getCode());
        }
        if (request.getName() != null) {
            existingProcess.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingProcess.setDescription(request.getDescription());
        }
        // Loại bỏ estimated_duration - thời gian được quản lý ở Service level
        if (request.getIsDefault() != null) {
            existingProcess.setIsDefault(request.getIsDefault());
        }
        if (request.getIsActive() != null) {
            existingProcess.setIsActive(request.getIsActive());
        }

        serviceProcessService.update(existingProcess);

        // Xử lý các bước nếu có
        if (request.getProcessSteps() != null) {
            log.info("Updating process steps for process: {}", existingProcess.getCode());
            updateProcessSteps(existingProcess, request.getProcessSteps());
        }
    }

    /**
     * Cập nhật process steps
     */
    private void updateProcessSteps(ServiceProcess serviceProcess,
            List<com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.UpdateServiceProcessStepRequest> stepRequests) {
        log.info("Processing {} process steps for process: {}", stepRequests.size(), serviceProcess.getCode());

        // Lấy danh sách steps hiện tại
        List<ServiceProcessStep> existingSteps = serviceProcessStepService.findByProcessId(serviceProcess.getId());
        log.info("Found {} existing steps for process: {}", existingSteps.size(), serviceProcess.getCode());

        // Tạo map để track steps cần cập nhật
        Map<UUID, ServiceProcessStep> existingStepsMap = existingSteps.stream()
                .collect(Collectors.toMap(ServiceProcessStep::getId, step -> step));

        // Xử lý từng step request
        for (com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.UpdateServiceProcessStepRequest stepRequest : stepRequests) {
            if (stepRequest.getId() != null && existingStepsMap.containsKey(stepRequest.getId())) {
                // Cập nhật step hiện tại
                ServiceProcessStep existingStep = existingStepsMap.get(stepRequest.getId());
                log.info("Updating existing step: {} (ID: {})", existingStep.getName(), existingStep.getId());

                // Cập nhật thông tin step
                if (stepRequest.getStepOrder() != null) {
                    existingStep.setStepOrder(stepRequest.getStepOrder());
                }
                if (stepRequest.getName() != null) {
                    existingStep.setName(stepRequest.getName());
                }
                if (stepRequest.getDescription() != null) {
                    existingStep.setDescription(stepRequest.getDescription());
                }
                // Loại bỏ estimated_time - thời gian được quản lý ở Service level
                if (stepRequest.getIsRequired() != null) {
                    existingStep.setIsRequired(stepRequest.getIsRequired());
                }
                if (stepRequest.getIsActive() != null) {
                    existingStep.setIsActive(stepRequest.getIsActive());
                }

                serviceProcessStepService.update(existingStep);
                log.info("Updated step: {} with order: {}", existingStep.getName(), existingStep.getStepOrder());
            } else {
                // Tạo step mới
                log.info("Creating new step: {} with order: {}", stepRequest.getName(), stepRequest.getStepOrder());
                ServiceProcessStep newStep = ServiceProcessStep.builder()
                        .serviceProcess(serviceProcess)
                        .stepOrder(stepRequest.getStepOrder())
                        .name(stepRequest.getName())
                        .description(stepRequest.getDescription())
                        .isRequired(stepRequest.getIsRequired() != null ? stepRequest.getIsRequired() : true)
                        .isActive(stepRequest.getIsActive() != null ? stepRequest.getIsActive() : true)
                        .isDeleted(false)
                        .build();
                ServiceProcessStep savedStep = serviceProcessStepService.save(newStep);
                log.info("Created new step: {} (ID: {}) with order: {}", savedStep.getName(), savedStep.getId(),
                        savedStep.getStepOrder());
            }
        }

        // Xóa các steps không còn trong request (nếu cần)
        Set<UUID> requestStepIds = stepRequests.stream()
                .map(com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.UpdateServiceProcessStepRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (ServiceProcessStep existingStep : existingSteps) {
            if (!requestStepIds.contains(existingStep.getId())) {
                log.info("Hard deleting step not in request: {} (ID: {})", existingStep.getName(),
                        existingStep.getId());
                serviceProcessStepService.deleteById(existingStep.getId());
            }
        }

        log.info("Completed updating process steps for process: {}", serviceProcess.getCode());
    }

    /**
     * Xử lý ServiceProducts khi tạo service
     */
    private void processServiceProducts(Service service,
            List<com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceProductRequest> productRequests) {
        for (com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceProductRequest productRequest : productRequests) {
            // Validate product exists
            Product product = productService.getById(productRequest.getProductId());

            // Create ServiceProduct
            ServiceProduct serviceProduct = ServiceProduct.builder()
                    .service(service)
                    .product(product)
                    .quantity(productRequest.getQuantity())
                    .unit(productRequest.getUnit())
                    .notes(productRequest.getNotes())
                    .isRequired(productRequest.getIsRequired() != null ? productRequest.getIsRequired() : true)
                    .sortOrder(productRequest.getSortOrder() != null ? productRequest.getSortOrder() : 0)
                    .build();

            serviceProductService.save(serviceProduct);
        }
    }

    /**
     * Xử lý cập nhật ServiceProducts
     */
    private void processUpdateServiceProducts(Service service,
            List<com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceProductRequest> productRequests) {
        // Xóa tất cả service products cũ
        List<ServiceProduct> existingProducts = serviceProductService.findByServiceId(service.getServiceId());
        for (ServiceProduct product : existingProducts) {
            serviceProductService.delete(product);
        }

        // Tạo lại các service products mới
        for (com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceProductRequest productRequest : productRequests) {
            // Validate product exists
            Product product = productService.getById(productRequest.getProductId());

            // Create ServiceProduct
            ServiceProduct serviceProduct = ServiceProduct.builder()
                    .service(service)
                    .product(product)
                    .quantity(productRequest.getQuantity())
                    .unit(productRequest.getUnit())
                    .notes(productRequest.getNotes())
                    .isRequired(productRequest.getIsRequired() != null ? productRequest.getIsRequired() : true)
                    .sortOrder(productRequest.getSortOrder() != null ? productRequest.getSortOrder() : 0)
                    .build();

            serviceProductService.save(serviceProduct);
        }
    }
}