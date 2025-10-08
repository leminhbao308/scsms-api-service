package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.centerBusinessHours.CenterBusinessHoursDto;
import com.kltn.scsms_api_service.core.dto.centerBusinessHours.request.CreateCenterBusinessHoursRequest;
import com.kltn.scsms_api_service.core.dto.centerBusinessHours.request.UpdateCenterBusinessHoursRequest;
import com.kltn.scsms_api_service.core.dto.centerBusinessHours.request.UpdateCenterBusinessHoursStatusRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.CenterBusinessHoursManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Center Business Hours Management", description = "APIs for managing center business hours")
public class CenterBusinessHoursManagementController {

    private final CenterBusinessHoursManagementService centerBusinessHoursManagementService;

    @GetMapping(ApiConstant.GET_ALL_CENTER_BUSINESS_HOURS_API)
    @Operation(summary = "Get all business hours for center", description = "Retrieve all business hours for a specific center")
    @SwaggerOperation(summary = "Get all business hours for center")
    public ResponseEntity<ApiResponse<List<CenterBusinessHoursDto>>> getAllBusinessHoursByCenter(
            @Parameter(description = "Center ID") @PathVariable UUID centerId) {
        log.info("Getting all business hours for center: {}", centerId);
        List<CenterBusinessHoursDto> businessHours = centerBusinessHoursManagementService
                .getAllBusinessHoursByCenter(centerId);
        return ResponseBuilder.success(businessHours);
    }


    @GetMapping(ApiConstant.GET_CENTER_BUSINESS_HOURS_BY_ID_API)
    @Operation(summary = "Get business hours by ID", description = "Retrieve a specific business hours by its ID")
    @SwaggerOperation(summary = "Get business hours by ID")
    public ResponseEntity<ApiResponse<CenterBusinessHoursDto>> getBusinessHoursById(
            @Parameter(description = "Business Hours ID") @PathVariable UUID businessHoursId) {
        log.info("Getting business hours by ID: {}", businessHoursId);
        CenterBusinessHoursDto businessHours = centerBusinessHoursManagementService.getBusinessHoursById(businessHoursId);
        return ResponseBuilder.success(businessHours);
    }

    @PostMapping(ApiConstant.CREATE_CENTER_BUSINESS_HOURS_API)
    @Operation(summary = "Create business hours", description = "Create new business hours for a center")
    @SwaggerOperation(summary = "Create business hours")
    public ResponseEntity<ApiResponse<CenterBusinessHoursDto>> createBusinessHours(
            @Parameter(description = "Business hours creation request") @Valid @RequestBody CreateCenterBusinessHoursRequest request) {
        log.info("Creating business hours for center: {}", request.getCenterId());
        CenterBusinessHoursDto businessHours = centerBusinessHoursManagementService.createBusinessHours(request);
        return ResponseBuilder.created(businessHours);
    }

    @PostMapping(ApiConstant.UPDATE_CENTER_BUSINESS_HOURS_API)
    @Operation(summary = "Update business hours", description = "Update existing business hours")
    @SwaggerOperation(summary = "Update business hours")
    public ResponseEntity<ApiResponse<CenterBusinessHoursDto>> updateBusinessHours(
            @Parameter(description = "Business Hours ID") @PathVariable UUID businessHoursId,
            @Parameter(description = "Business hours update request") @Valid @RequestBody UpdateCenterBusinessHoursRequest request) {
        log.info("Updating business hours with ID: {}", businessHoursId);
        CenterBusinessHoursDto businessHours = centerBusinessHoursManagementService.updateBusinessHours(businessHoursId, request);
        return ResponseBuilder.success(businessHours);
    }

    @PostMapping(ApiConstant.DELETE_CENTER_BUSINESS_HOURS_API)
    @Operation(summary = "Delete business hours", description = "Delete business hours (soft delete)")
    @SwaggerOperation(summary = "Delete business hours")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessHours(
            @Parameter(description = "Business Hours ID") @PathVariable UUID businessHoursId) {
        log.info("Deleting business hours with ID: {}", businessHoursId);
        centerBusinessHoursManagementService.deleteBusinessHours(businessHoursId);
        return ResponseBuilder.success("Business hours deleted successfully");
    }

    @PostMapping(ApiConstant.UPDATE_CENTER_BUSINESS_HOURS_STATUS_API)
    @Operation(summary = "Update business hours closed status", description = "Update the closed status of business hours")
    @SwaggerOperation(summary = "Update business hours closed status")
    public ResponseEntity<ApiResponse<CenterBusinessHoursDto>> updateBusinessHoursStatus(
            @Parameter(description = "Business Hours ID") @PathVariable UUID businessHoursId,
            @Parameter(description = "Status update request") @Valid @RequestBody UpdateCenterBusinessHoursStatusRequest request) {
        log.info("Updating business hours closed status for ID: {} to {}", businessHoursId, request.getIsClosed());
        CenterBusinessHoursDto businessHours = centerBusinessHoursManagementService.updateBusinessHoursStatus(businessHoursId,
                request);
        return ResponseBuilder.success(businessHours);
    }

}
