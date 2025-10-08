package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.abstracts.BaseResponseData;
import com.kltn.scsms_api_service.annotations.RequirePermission;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.constants.PermissionConstant;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.ServiceProcessTrackingFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.ServiceProcessTrackingInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request.*;
import com.kltn.scsms_api_service.core.service.businessService.ServiceProcessTrackingManagementService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller quản lý theo dõi quá trình thực hiện dịch vụ
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Process Tracking Management", description = "APIs quản lý theo dõi quá trình thực hiện dịch vụ")
public class ServiceProcessTrackingController {

    private final ServiceProcessTrackingManagementService serviceProcessTrackingManagementService;

    /**
     * Tạo tracking mới
     */
    @PostMapping(ApiConstant.CREATE_SERVICE_PROCESS_TRACKING_API)
    @SwaggerOperation(summary = "Tạo tracking mới", description = "Tạo mới tracking cho quá trình thực hiện dịch vụ")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_CREATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> createTracking(
            @Valid @RequestBody CreateServiceProcessTrackingRequest request) {
        log.info("Creating service process tracking for booking: {}", request.getBookingId());

        ServiceProcessTrackingInfoDto response = serviceProcessTrackingManagementService.createTracking(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponseData<>(true, "Tạo tracking thành công", response));
    }

    /**
     * Lấy danh sách tracking với phân trang
     */
    @GetMapping(ApiConstant.GET_ALL_SERVICE_PROCESS_TRACKINGS_API)
    @SwaggerOperation(summary = "Lấy danh sách tracking", description = "Lấy danh sách tracking với phân trang và filter")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<Page<ServiceProcessTrackingInfoDto>>> getTrackings(
            @Parameter(description = "Filter parameters") ServiceProcessTrackingFilterParam filterParam,
            Pageable pageable) {
        log.info("Getting service process trackings with filter: {}", filterParam);

        Page<ServiceProcessTrackingInfoDto> response = serviceProcessTrackingManagementService.getAllTrackings(pageable);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Lấy danh sách tracking thành công", response));
    }

    /**
     * Lấy chi tiết tracking theo ID    
     */
    @GetMapping(ApiConstant.GET_SERVICE_PROCESS_TRACKING_BY_ID_API)
    @SwaggerOperation(summary = "Lấy chi tiết tracking", description = "Lấy thông tin chi tiết tracking theo ID")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> getTrackingById(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId) {
        log.info("Getting service process tracking by ID: {}", trackingId);

        ServiceProcessTrackingInfoDto response = serviceProcessTrackingManagementService.getTrackingById(trackingId);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Lấy thông tin tracking thành công", response));
    }

    /**
     * Cập nhật tracking
     */
    @PostMapping(ApiConstant.UPDATE_SERVICE_PROCESS_TRACKING_API)
    @SwaggerOperation(summary = "Cập nhật tracking", description = "Cập nhật thông tin tracking")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> updateTracking(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody UpdateServiceProcessTrackingRequest request) {
        log.info("Updating service process tracking: {}", trackingId);

        ServiceProcessTrackingInfoDto response = serviceProcessTrackingManagementService.updateTracking(trackingId, request);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Cập nhật tracking thành công", response));
    }

    /**
     * Xóa tracking
     */
    @PostMapping(ApiConstant.DELETE_SERVICE_PROCESS_TRACKING_API)
    @SwaggerOperation(summary = "Xóa tracking", description = "Xóa tracking theo ID")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_DELETE })
    public ResponseEntity<BaseResponseData<Void>> deleteTracking(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId) {
        log.info("Deleting service process tracking: {}", trackingId);

        serviceProcessTrackingManagementService.deleteTracking(trackingId);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Xóa tracking thành công", null));
    }

    /**
     * Lấy tracking theo booking
     */
    @GetMapping(ApiConstant.GET_SERVICE_PROCESS_TRACKINGS_BY_BOOKING_API)
    @SwaggerOperation(summary = "Lấy tracking theo booking", description = "Lấy danh sách tracking theo booking ID")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<List<ServiceProcessTrackingInfoDto>>> getTrackingsByBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        log.info("Getting service process trackings by booking: {}", bookingId);

        List<ServiceProcessTrackingInfoDto> response = serviceProcessTrackingManagementService.getTrackingsByBooking(bookingId);

        return ResponseEntity
                .ok(new BaseResponseData<>(true, "Lấy danh sách tracking theo booking thành công", response));
    }

    /**
     * Lấy tracking theo kỹ thuật viên
     */
    @GetMapping(ApiConstant.GET_SERVICE_PROCESS_TRACKINGS_BY_TECHNICIAN_API)
    @SwaggerOperation(summary = "Lấy tracking theo kỹ thuật viên", description = "Lấy danh sách tracking theo kỹ thuật viên")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<List<ServiceProcessTrackingInfoDto>>> getTrackingsByTechnician(
            @Parameter(description = "Technician ID") @PathVariable UUID technicianId) {
        log.info("Getting service process trackings by technician: {}", technicianId);

        List<ServiceProcessTrackingInfoDto> response = serviceProcessTrackingManagementService.getTrackingsByTechnician(technicianId);

        return ResponseEntity
                .ok(new BaseResponseData<>(true, "Lấy danh sách tracking theo kỹ thuật viên thành công", response));
    }

    /**
     * Lấy tracking theo bay
     */
    @GetMapping(ApiConstant.GET_SERVICE_PROCESS_TRACKINGS_BY_BAY_API)
    @SwaggerOperation(summary = "Lấy tracking theo bay", description = "Lấy danh sách tracking theo bay")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<List<ServiceProcessTrackingInfoDto>>> getTrackingsByBay(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId) {
        log.info("Getting service process trackings by bay: {}", bayId);

        List<ServiceProcessTrackingInfoDto> response = serviceProcessTrackingManagementService.getTrackingsByBay(bayId);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Lấy danh sách tracking theo bay thành công", response));
    }

    /**
     * Lấy tracking đang thực hiện
     */
    @GetMapping(ApiConstant.GET_IN_PROGRESS_SERVICE_PROCESS_TRACKINGS_API)
    @SwaggerOperation(summary = "Lấy tracking đang thực hiện", description = "Lấy danh sách tracking đang thực hiện")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<List<ServiceProcessTrackingInfoDto>>> getInProgressTrackings() {
        log.info("Getting in-progress service process trackings");

        List<ServiceProcessTrackingInfoDto> response = serviceProcessTrackingManagementService.getInProgressTrackings();

        return ResponseEntity
                .ok(new BaseResponseData<>(true, "Lấy danh sách tracking đang thực hiện thành công", response));
    }

    /**
     * Bắt đầu thực hiện bước
     */
    @PostMapping(ApiConstant.START_SERVICE_PROCESS_TRACKING_STEP_API)
    @SwaggerOperation(summary = "Bắt đầu thực hiện bước", description = "Bắt đầu thực hiện bước dịch vụ")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> startStep(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody StartStepRequest request) {
        log.info("Starting step for tracking: {}", trackingId);

        ServiceProcessTrackingInfoDto response = serviceProcessTrackingManagementService.startStep(trackingId, request);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Bắt đầu thực hiện bước thành công", response));
    }

    /**
     * Cập nhật tiến độ
     */
    @PostMapping(ApiConstant.UPDATE_SERVICE_PROCESS_TRACKING_PROGRESS_API)
    @SwaggerOperation(summary = "Cập nhật tiến độ", description = "Cập nhật tiến độ thực hiện bước")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> updateProgress(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody ProgressUpdateRequest request) {
        log.info("Updating progress for tracking: {}", trackingId);

        ServiceProcessTrackingInfoDto response = serviceProcessTrackingManagementService.updateProgress(trackingId, request);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Cập nhật tiến độ thành công", response));
    }

    /**
     * Hoàn thành bước
     */
    @PostMapping(ApiConstant.COMPLETE_SERVICE_PROCESS_TRACKING_STEP_API)
    @SwaggerOperation(summary = "Hoàn thành bước", description = "Hoàn thành thực hiện bước dịch vụ")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> completeStep(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody CompleteStepRequest request) {
        log.info("Completing step for tracking: {}", trackingId);

        ServiceProcessTrackingInfoDto response = serviceProcessTrackingManagementService.completeStep(trackingId, request);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Hoàn thành bước thành công", response));
    }

    /**
     * Hủy bước
     */
    @PostMapping(ApiConstant.CANCEL_SERVICE_PROCESS_TRACKING_STEP_API)
    @SwaggerOperation(summary = "Hủy bước", description = "Hủy thực hiện bước dịch vụ")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> cancelStep(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody CancelStepRequest request) {
        log.info("Cancelling step for tracking: {}", trackingId);

        ServiceProcessTrackingInfoDto response = serviceProcessTrackingManagementService.cancelStep(trackingId, request);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Hủy bước thành công", response));
    }

    /**
     * Thêm ghi chú
     */
    @PostMapping(ApiConstant.ADD_SERVICE_PROCESS_TRACKING_NOTE_API)
    @SwaggerOperation(summary = "Thêm ghi chú", description = "Thêm ghi chú cho tracking")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> addNote(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody ProgressUpdateRequest request) {
        log.info("Adding note for tracking: {}", trackingId);

        ServiceProcessTrackingInfoDto response = serviceProcessTrackingManagementService.updateProgress(trackingId, request);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Thêm ghi chú thành công", response));
    }

    /**
     * Thêm media evidence
     */
    @PostMapping(ApiConstant.ADD_SERVICE_PROCESS_TRACKING_EVIDENCE_API)
    @SwaggerOperation(summary = "Thêm media evidence", description = "Thêm ảnh/video chứng minh hoàn thành")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> addEvidenceMedia(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @RequestParam String mediaUrl) {
        log.info("Adding evidence media for tracking: {}", trackingId);

        ServiceProcessTrackingInfoDto response = serviceProcessTrackingManagementService.addEvidenceMedia(trackingId, mediaUrl);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Thêm media evidence thành công", response));
    }
}