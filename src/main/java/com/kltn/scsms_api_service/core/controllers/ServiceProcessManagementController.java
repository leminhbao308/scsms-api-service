package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.abstracts.BaseResponse;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessStepInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessStepProductInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.param.ServiceProcessFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.*;
import com.kltn.scsms_api_service.core.service.businessService.ServiceProcessManagementService;
import com.kltn.scsms_api_service.interfaces.FilterStandardlize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstant.SERVICE_PROCESS_MANAGEMENT_PREFIX)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Process Management", description = "API quản lý quy trình chăm sóc xe")
public class ServiceProcessManagementController implements FilterStandardlize<ServiceProcessFilterParam> {

    private final ServiceProcessManagementService serviceProcessManagementService;

    @Override
    public ServiceProcessFilterParam standardizeFilterRequest(ServiceProcessFilterParam filterParam) {
        if (filterParam == null) {
            return new ServiceProcessFilterParam();
        }
        return filterParam;
    }

    // ========== SERVICE PROCESS MANAGEMENT ==========

    @GetMapping
    @SwaggerOperation(summary = "Lấy danh sách quy trình chăm sóc xe", description = "Lấy danh sách tất cả quy trình chăm sóc xe với phân trang và lọc")
    @Operation(summary = "Lấy danh sách quy trình chăm sóc xe")
    public ResponseEntity<Page<ServiceProcessInfoDto>> getAllServiceProcesses(
            @Parameter(description = "Tham số lọc và phân trang") ServiceProcessFilterParam filterParam,
            @Parameter(description = "Thông tin phân trang") Pageable pageable) {

        log.info("Getting all service processes with filter: {}", filterParam);
        filterParam = standardizeFilterRequest(filterParam);

        Page<ServiceProcessInfoDto> result = serviceProcessManagementService.getAllServiceProcesses(filterParam,
                pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{processId}")
    @SwaggerOperation(summary = "Lấy thông tin quy trình theo ID", description = "Lấy thông tin chi tiết của một quy trình chăm sóc xe")
    @Operation(summary = "Lấy thông tin quy trình theo ID")
    public ResponseEntity<ServiceProcessInfoDto> getServiceProcessById(
            @Parameter(description = "ID của quy trình") @PathVariable UUID processId) {

        log.info("Getting service process by ID: {}", processId);
        ServiceProcessInfoDto result = serviceProcessManagementService.getServiceProcessById(processId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/code/{code}")
    @SwaggerOperation(summary = "Lấy thông tin quy trình theo mã", description = "Lấy thông tin chi tiết của một quy trình chăm sóc xe theo mã")
    @Operation(summary = "Lấy thông tin quy trình theo mã")
    public ResponseEntity<ServiceProcessInfoDto> getServiceProcessByCode(
            @Parameter(description = "Mã của quy trình") @PathVariable String code) {

        log.info("Getting service process by code: {}", code);
        ServiceProcessInfoDto result = serviceProcessManagementService.getServiceProcessByCode(code);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/default")
    @SwaggerOperation(summary = "Lấy quy trình mặc định", description = "Lấy thông tin quy trình chăm sóc xe mặc định")
    @Operation(summary = "Lấy quy trình mặc định")
    public ResponseEntity<ServiceProcessInfoDto> getDefaultServiceProcess() {

        log.info("Getting default service process");
        ServiceProcessInfoDto result = serviceProcessManagementService.getDefaultServiceProcess();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    @SwaggerOperation(summary = "Lấy danh sách quy trình đang hoạt động", description = "Lấy danh sách tất cả quy trình chăm sóc xe đang hoạt động")
    @Operation(summary = "Lấy danh sách quy trình đang hoạt động")
    public ResponseEntity<List<ServiceProcessInfoDto>> getAllActiveServiceProcesses() {

        log.info("Getting all active service processes");
        List<ServiceProcessInfoDto> result = serviceProcessManagementService.getAllActiveServiceProcesses();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @SwaggerOperation(summary = "Tạo quy trình chăm sóc xe mới", description = "Tạo một quy trình chăm sóc xe mới với các bước và sản phẩm")
    @Operation(summary = "Tạo quy trình chăm sóc xe mới")
    public ResponseEntity<ServiceProcessInfoDto> createServiceProcess(
            @Parameter(description = "Thông tin quy trình mới") @Valid @RequestBody CreateServiceProcessRequest request) {

        log.info("Creating new service process: {}", request.getName());
        ServiceProcessInfoDto result = serviceProcessManagementService.createServiceProcess(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{processId}/update")
    @SwaggerOperation(summary = "Cập nhật quy trình chăm sóc xe", description = "Cập nhật thông tin của một quy trình chăm sóc xe")
    @Operation(summary = "Cập nhật quy trình chăm sóc xe")
    public ResponseEntity<ServiceProcessInfoDto> updateServiceProcess(
            @Parameter(description = "ID của quy trình") @PathVariable UUID processId,
            @Parameter(description = "Thông tin cập nhật") @Valid @RequestBody UpdateServiceProcessRequest request) {

        log.info("Updating service process: {}", processId);
        ServiceProcessInfoDto result = serviceProcessManagementService.updateServiceProcess(processId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{processId}/delete")
    @SwaggerOperation(summary = "Xóa quy trình chăm sóc xe", description = "Xóa một quy trình chăm sóc xe (chỉ khi không được sử dụng)")
    @Operation(summary = "Xóa quy trình chăm sóc xe")
    public ResponseEntity<BaseResponse> deleteServiceProcess(
            @Parameter(description = "ID của quy trình") @PathVariable UUID processId) {

        log.info("Deleting service process: {}", processId);
        BaseResponse result = serviceProcessManagementService.deleteServiceProcess(processId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{processId}/set-default")
    @SwaggerOperation(summary = "Đặt quy trình làm mặc định", description = "Đặt một quy trình chăm sóc xe làm mặc định")
    @Operation(summary = "Đặt quy trình làm mặc định")
    public ResponseEntity<ServiceProcessInfoDto> setDefaultServiceProcess(
            @Parameter(description = "ID của quy trình") @PathVariable UUID processId) {

        log.info("Setting default service process: {}", processId);
        ServiceProcessInfoDto result = serviceProcessManagementService.setDefaultServiceProcess(processId);
        return ResponseEntity.ok(result);
    }

    // ========== SERVICE PROCESS STEP MANAGEMENT ==========

    @GetMapping("/{processId}/steps")
    @SwaggerOperation(summary = "Lấy danh sách bước của quy trình", description = "Lấy danh sách tất cả bước của một quy trình chăm sóc xe")
    @Operation(summary = "Lấy danh sách bước của quy trình")
    public ResponseEntity<List<ServiceProcessStepInfoDto>> getServiceProcessSteps(
            @Parameter(description = "ID của quy trình") @PathVariable UUID processId) {

        log.info("Getting steps for service process: {}", processId);
        List<ServiceProcessStepInfoDto> result = serviceProcessManagementService.getServiceProcessSteps(processId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/steps/{stepId}")
    @SwaggerOperation(summary = "Lấy thông tin bước theo ID", description = "Lấy thông tin chi tiết của một bước trong quy trình")
    @Operation(summary = "Lấy thông tin bước theo ID")
    public ResponseEntity<ServiceProcessStepInfoDto> getServiceProcessStepById(
            @Parameter(description = "ID của bước") @PathVariable UUID stepId) {

        log.info("Getting service process step by ID: {}", stepId);
        ServiceProcessStepInfoDto result = serviceProcessManagementService.getServiceProcessStepById(stepId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{processId}/steps")
    @SwaggerOperation(summary = "Thêm bước vào quy trình", description = "Thêm một bước mới vào quy trình chăm sóc xe")
    @Operation(summary = "Thêm bước vào quy trình")
    public ResponseEntity<ServiceProcessStepInfoDto> addStepToServiceProcess(
            @Parameter(description = "ID của quy trình") @PathVariable UUID processId,
            @Parameter(description = "Thông tin bước mới") @Valid @RequestBody CreateServiceProcessStepRequest request) {

        log.info("Adding step to service process: {}", processId);
        ServiceProcessStepInfoDto result = serviceProcessManagementService.addStepToServiceProcess(processId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/steps/{stepId}/update")
    @SwaggerOperation(summary = "Cập nhật bước", description = "Cập nhật thông tin của một bước trong quy trình")
    @Operation(summary = "Cập nhật bước")
    public ResponseEntity<ServiceProcessStepInfoDto> updateServiceProcessStep(
            @Parameter(description = "ID của bước") @PathVariable UUID stepId,
            @Parameter(description = "Thông tin cập nhật") @Valid @RequestBody UpdateServiceProcessStepRequest request) {

        log.info("Updating service process step: {}", stepId);
        ServiceProcessStepInfoDto result = serviceProcessManagementService.updateServiceProcessStep(stepId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/steps/{stepId}/delete")
    @SwaggerOperation(summary = "Xóa bước", description = "Xóa một bước khỏi quy trình chăm sóc xe")
    @Operation(summary = "Xóa bước")
    public ResponseEntity<BaseResponse> deleteServiceProcessStep(
            @Parameter(description = "ID của bước") @PathVariable UUID stepId) {

        log.info("Deleting service process step: {}", stepId);
        BaseResponse result = serviceProcessManagementService.deleteServiceProcessStep(stepId);
        return ResponseEntity.ok(result);
    }


    // ========== SERVICE PROCESS STEP PRODUCT MANAGEMENT ==========

    @GetMapping("/steps/{stepId}/products")
    @SwaggerOperation(summary = "Lấy danh sách sản phẩm của bước", description = "Lấy danh sách tất cả sản phẩm của một bước trong quy trình")
    @Operation(summary = "Lấy danh sách sản phẩm của bước")
    public ResponseEntity<List<ServiceProcessStepProductInfoDto>> getServiceProcessStepProducts(
            @Parameter(description = "ID của bước") @PathVariable UUID stepId) {

        log.info("Getting products for service process step: {}", stepId);
        List<ServiceProcessStepProductInfoDto> result = serviceProcessManagementService
                .getServiceProcessStepProducts(stepId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/step-products/{productId}")
    @SwaggerOperation(summary = "Lấy thông tin sản phẩm theo ID", description = "Lấy thông tin chi tiết của một sản phẩm trong bước")
    @Operation(summary = "Lấy thông tin sản phẩm theo ID")
    public ResponseEntity<ServiceProcessStepProductInfoDto> getServiceProcessStepProductById(
            @Parameter(description = "ID của sản phẩm") @PathVariable UUID productId) {

        log.info("Getting service process step product by ID: {}", productId);
        ServiceProcessStepProductInfoDto result = serviceProcessManagementService
                .getServiceProcessStepProductById(productId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/steps/{stepId}/products")
    @SwaggerOperation(summary = "Thêm sản phẩm vào bước", description = "Thêm một sản phẩm mới vào bước trong quy trình")
    @Operation(summary = "Thêm sản phẩm vào bước")
    public ResponseEntity<ServiceProcessStepProductInfoDto> addProductToStep(
            @Parameter(description = "ID của bước") @PathVariable UUID stepId,
            @Parameter(description = "Thông tin sản phẩm mới") @Valid @RequestBody CreateServiceProcessStepProductRequest request) {

        log.info("Adding product to step: {}", stepId);
        ServiceProcessStepProductInfoDto result = serviceProcessManagementService.addProductToStep(stepId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/step-products/{productId}/update")
    @SwaggerOperation(summary = "Cập nhật sản phẩm trong bước", description = "Cập nhật thông tin của một sản phẩm trong bước")
    @Operation(summary = "Cập nhật sản phẩm trong bước")
    public ResponseEntity<ServiceProcessStepProductInfoDto> updateServiceProcessStepProduct(
            @Parameter(description = "ID của sản phẩm") @PathVariable UUID productId,
            @Parameter(description = "Thông tin cập nhật") @Valid @RequestBody UpdateServiceProcessStepProductRequest request) {

        log.info("Updating service process step product: {}", productId);
        ServiceProcessStepProductInfoDto result = serviceProcessManagementService
                .updateServiceProcessStepProduct(productId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/step-products/{productId}/delete")
    @SwaggerOperation(summary = "Xóa sản phẩm khỏi bước", description = "Xóa một sản phẩm khỏi bước trong quy trình")
    @Operation(summary = "Xóa sản phẩm khỏi bước")
    public ResponseEntity<BaseResponse> deleteServiceProcessStepProduct(
            @Parameter(description = "ID của sản phẩm") @PathVariable UUID productId) {

        log.info("Deleting service process step product: {}", productId);
        BaseResponse result = serviceProcessManagementService.deleteServiceProcessStepProduct(productId);
        return ResponseEntity.ok(result);
    }
}
