package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.abstracts.BaseResponseData;
import com.kltn.scsms_api_service.annotations.RequirePermission;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.PermissionConstant;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.ServiceProcessTrackingFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.ServiceProcessTrackingInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request.*;
import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import com.kltn.scsms_api_service.core.service.entityService.ServiceProcessTrackingService;
import com.kltn.scsms_api_service.mapper.ServiceProcessTrackingMapper;
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
import java.util.stream.Collectors;

/**
 * Controller quản lý theo dõi quá trình thực hiện dịch vụ
 */
@RestController
@RequestMapping("/service-process-trackings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Process Tracking Management", description = "APIs quản lý theo dõi quá trình thực hiện dịch vụ")
public class ServiceProcessTrackingController {

    private final ServiceProcessTrackingService serviceProcessTrackingService;
    private final ServiceProcessTrackingMapper serviceProcessTrackingMapper;

    /**
     * Tạo tracking mới
     */
    @PostMapping("/create")
    @SwaggerOperation(summary = "Tạo tracking mới", description = "Tạo mới tracking cho quá trình thực hiện dịch vụ")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_CREATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> createTracking(
            @Valid @RequestBody CreateServiceProcessTrackingRequest request) {
        log.info("Creating service process tracking for booking: {}", request.getBookingId());

        ServiceProcessTracking tracking = serviceProcessTrackingMapper.toEntity(request);
        ServiceProcessTracking savedTracking = serviceProcessTrackingService.save(tracking);
        ServiceProcessTrackingInfoDto response = serviceProcessTrackingMapper
                .toServiceProcessTrackingInfoDto(savedTracking);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponseData<>(true, "Tạo tracking thành công", response));
    }

    /**
     * Lấy danh sách tracking với phân trang
     */
    @GetMapping("/get-all")
    @SwaggerOperation(summary = "Lấy danh sách tracking", description = "Lấy danh sách tracking với phân trang và filter")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<Page<ServiceProcessTrackingInfoDto>>> getTrackings(
            @Parameter(description = "Filter parameters") ServiceProcessTrackingFilterParam filterParam,
            Pageable pageable) {
        log.info("Getting service process trackings with filter: {}", filterParam);

        Page<ServiceProcessTracking> trackings = serviceProcessTrackingService.findAll(pageable);
        Page<ServiceProcessTrackingInfoDto> response = trackings
                .map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Lấy danh sách tracking thành công", response));
    }

    /**
     * Lấy chi tiết tracking theo ID    
     */
    @GetMapping("/{trackingId}")
    @SwaggerOperation(summary = "Lấy chi tiết tracking", description = "Lấy thông tin chi tiết tracking theo ID")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> getTrackingById(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId) {
        log.info("Getting service process tracking by ID: {}", trackingId);

        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        ServiceProcessTrackingInfoDto response = serviceProcessTrackingMapper.toServiceProcessTrackingInfoDto(tracking);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Lấy thông tin tracking thành công", response));
    }

    /**
     * Cập nhật tracking
     */
    @PostMapping("/{trackingId}/update")
    @SwaggerOperation(summary = "Cập nhật tracking", description = "Cập nhật thông tin tracking")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> updateTracking(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody UpdateServiceProcessTrackingRequest request) {
        log.info("Updating service process tracking: {}", trackingId);

        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        serviceProcessTrackingMapper.updateEntity(tracking, request);
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        ServiceProcessTrackingInfoDto response = serviceProcessTrackingMapper
                .toServiceProcessTrackingInfoDto(updatedTracking);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Cập nhật tracking thành công", response));
    }

    /**
     * Xóa tracking
     */
    @PostMapping("/{trackingId}/delete")
    @SwaggerOperation(summary = "Xóa tracking", description = "Xóa tracking theo ID")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_DELETE })
    public ResponseEntity<BaseResponseData<Void>> deleteTracking(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId) {
        log.info("Deleting service process tracking: {}", trackingId);

        serviceProcessTrackingService.delete(trackingId);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Xóa tracking thành công", null));
    }

    /**
     * Lấy tracking theo booking
     */
    @GetMapping("/booking/{bookingId}")
    @SwaggerOperation(summary = "Lấy tracking theo booking", description = "Lấy danh sách tracking theo booking ID")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<List<ServiceProcessTrackingInfoDto>>> getTrackingsByBooking(
            @Parameter(description = "Booking ID") @PathVariable UUID bookingId) {
        log.info("Getting service process trackings by booking: {}", bookingId);

        List<ServiceProcessTracking> trackings = serviceProcessTrackingService.findByBooking(bookingId);
        List<ServiceProcessTrackingInfoDto> response = trackings.stream()
                .map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto)
                .collect(Collectors.toList());

        return ResponseEntity
                .ok(new BaseResponseData<>(true, "Lấy danh sách tracking theo booking thành công", response));
    }

    /**
     * Lấy tracking theo kỹ thuật viên
     */
    @GetMapping("/technician/{technicianId}")
    @SwaggerOperation(summary = "Lấy tracking theo kỹ thuật viên", description = "Lấy danh sách tracking theo kỹ thuật viên")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<List<ServiceProcessTrackingInfoDto>>> getTrackingsByTechnician(
            @Parameter(description = "Technician ID") @PathVariable UUID technicianId) {
        log.info("Getting service process trackings by technician: {}", technicianId);

        List<ServiceProcessTracking> trackings = serviceProcessTrackingService.findByTechnician(technicianId);
        List<ServiceProcessTrackingInfoDto> response = trackings.stream()
                .map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto)
                .collect(Collectors.toList());

        return ResponseEntity
                .ok(new BaseResponseData<>(true, "Lấy danh sách tracking theo kỹ thuật viên thành công", response));
    }

    /**
     * Lấy tracking theo slot
     */
    @GetMapping("/slot/{slotId}")
    @SwaggerOperation(summary = "Lấy tracking theo slot", description = "Lấy danh sách tracking theo slot")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<List<ServiceProcessTrackingInfoDto>>> getTrackingsBySlot(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId) {
        log.info("Getting service process trackings by slot: {}", slotId);

        List<ServiceProcessTracking> trackings = serviceProcessTrackingService.findBySlot(slotId);
        List<ServiceProcessTrackingInfoDto> response = trackings.stream()
                .map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new BaseResponseData<>(true, "Lấy danh sách tracking theo slot thành công", response));
    }

    /**
     * Lấy tracking đang thực hiện
     */
    @GetMapping("/in-progress")
    @SwaggerOperation(summary = "Lấy tracking đang thực hiện", description = "Lấy danh sách tracking đang thực hiện")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_READ })
    public ResponseEntity<BaseResponseData<List<ServiceProcessTrackingInfoDto>>> getInProgressTrackings() {
        log.info("Getting in-progress service process trackings");

        List<ServiceProcessTracking> trackings = serviceProcessTrackingService.findInProgressTrackings();
        List<ServiceProcessTrackingInfoDto> response = trackings.stream()
                .map(serviceProcessTrackingMapper::toServiceProcessTrackingInfoDto)
                .collect(Collectors.toList());

        return ResponseEntity
                .ok(new BaseResponseData<>(true, "Lấy danh sách tracking đang thực hiện thành công", response));
    }

    /**
     * Bắt đầu thực hiện bước
     */
    @PostMapping("/{trackingId}/start")
    @SwaggerOperation(summary = "Bắt đầu thực hiện bước", description = "Bắt đầu thực hiện bước dịch vụ")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> startStep(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody StartStepRequest request) {
        log.info("Starting step for tracking: {}", trackingId);

        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        // TODO: Get technician from request.technicianId
        // tracking.startStep(technician);
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        ServiceProcessTrackingInfoDto response = serviceProcessTrackingMapper
                .toServiceProcessTrackingInfoDto(updatedTracking);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Bắt đầu thực hiện bước thành công", response));
    }

    /**
     * Cập nhật tiến độ
     */
    @PostMapping("/{trackingId}/progress/update")
    @SwaggerOperation(summary = "Cập nhật tiến độ", description = "Cập nhật tiến độ thực hiện bước")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> updateProgress(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody ProgressUpdateRequest request) {
        log.info("Updating progress for tracking: {}", trackingId);

        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        // TODO: Get current user
        // tracking.updateProgress(request.getProgressPercent(), currentUser);
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        ServiceProcessTrackingInfoDto response = serviceProcessTrackingMapper
                .toServiceProcessTrackingInfoDto(updatedTracking);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Cập nhật tiến độ thành công", response));
    }

    /**
     * Hoàn thành bước
     */
    @PostMapping("/{trackingId}/complete")
    @SwaggerOperation(summary = "Hoàn thành bước", description = "Hoàn thành thực hiện bước dịch vụ")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> completeStep(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody CompleteStepRequest request) {
        log.info("Completing step for tracking: {}", trackingId);

        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        tracking.completeStep();
        if (request.getNotes() != null) {
            // TODO: Get current user
            // tracking.addNote(request.getNotes(), currentUser);
        }
        if (request.getEvidenceMediaUrls() != null) {
            // TODO: Parse and add media URLs
        }
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        ServiceProcessTrackingInfoDto response = serviceProcessTrackingMapper
                .toServiceProcessTrackingInfoDto(updatedTracking);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Hoàn thành bước thành công", response));
    }

    /**
     * Hủy bước
     */
    @PostMapping("/{trackingId}/cancel")
    @SwaggerOperation(summary = "Hủy bước", description = "Hủy thực hiện bước dịch vụ")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> cancelStep(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody CancelStepRequest request) {
        log.info("Cancelling step for tracking: {}", trackingId);

        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        // TODO: Get current user
        // tracking.cancelStep(request.getReason(), currentUser);
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        ServiceProcessTrackingInfoDto response = serviceProcessTrackingMapper
                .toServiceProcessTrackingInfoDto(updatedTracking);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Hủy bước thành công", response));
    }

    /**
     * Thêm ghi chú
     */
    @PostMapping("/{trackingId}/notes")
    @SwaggerOperation(summary = "Thêm ghi chú", description = "Thêm ghi chú cho tracking")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> addNote(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @Valid @RequestBody ProgressUpdateRequest request) {
        log.info("Adding note for tracking: {}", trackingId);

        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        // TODO: Get current user
        // tracking.addNote(request.getNotes(), currentUser);
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        ServiceProcessTrackingInfoDto response = serviceProcessTrackingMapper
                .toServiceProcessTrackingInfoDto(updatedTracking);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Thêm ghi chú thành công", response));
    }

    /**
     * Thêm media evidence
     */
    @PostMapping("/{trackingId}/evidence")
    @SwaggerOperation(summary = "Thêm media evidence", description = "Thêm ảnh/video chứng minh hoàn thành")
    @RequirePermission(permissions = { PermissionConstant.SERVICE_PROCESS_TRACKING_UPDATE })
    public ResponseEntity<BaseResponseData<ServiceProcessTrackingInfoDto>> addEvidenceMedia(
            @Parameter(description = "Tracking ID") @PathVariable UUID trackingId,
            @RequestParam String mediaUrl) {
        log.info("Adding evidence media for tracking: {}", trackingId);

        ServiceProcessTracking tracking = serviceProcessTrackingService.getById(trackingId);
        // TODO: Get current user
        // tracking.addEvidenceMedia(mediaUrl, currentUser);
        ServiceProcessTracking updatedTracking = serviceProcessTrackingService.update(tracking);
        ServiceProcessTrackingInfoDto response = serviceProcessTrackingMapper
                .toServiceProcessTrackingInfoDto(updatedTracking);

        return ResponseEntity.ok(new BaseResponseData<>(true, "Thêm media evidence thành công", response));
    }
}