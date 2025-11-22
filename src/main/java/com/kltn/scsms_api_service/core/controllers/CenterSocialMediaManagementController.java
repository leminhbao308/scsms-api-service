package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.centerSocialMedia.CenterSocialMediaDto;
import com.kltn.scsms_api_service.core.dto.centerSocialMedia.request.CreateCenterSocialMediaRequest;
import com.kltn.scsms_api_service.core.dto.centerSocialMedia.request.UpdateCenterSocialMediaRequest;
import com.kltn.scsms_api_service.core.dto.centerSocialMedia.request.UpdateCenterSocialMediaStatusRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.CenterSocialMediaManagementService;
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
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Center Social Media Management", description = "APIs for managing center social media")
public class CenterSocialMediaManagementController {

    private final CenterSocialMediaManagementService centerSocialMediaManagementService;

    @GetMapping(ApiConstant.GET_ALL_CENTER_SOCIAL_MEDIA_API)
    @Operation(summary = "Get all social media for center", description = "Retrieve all social media for a specific center")
    @SwaggerOperation(summary = "Get all social media for center")
    public ResponseEntity<ApiResponse<List<CenterSocialMediaDto>>> getAllSocialMediaByCenter(
            @Parameter(description = "Center ID") @PathVariable UUID centerId) {
        log.info("Getting all social media for center: {}", centerId);
        List<CenterSocialMediaDto> socialMedia = centerSocialMediaManagementService.getAllSocialMediaByCenter(centerId);
        return ResponseBuilder.success(socialMedia);
    }

    @GetMapping(ApiConstant.GET_CENTER_SOCIAL_MEDIA_BY_ID_API)
    @Operation(summary = "Get social media by ID", description = "Retrieve a specific social media by its ID")
    @SwaggerOperation(summary = "Get social media by ID")
    public ResponseEntity<ApiResponse<CenterSocialMediaDto>> getSocialMediaById(
            @Parameter(description = "Social Media ID") @PathVariable UUID socialMediaId) {
        log.info("Getting social media by ID: {}", socialMediaId);
        CenterSocialMediaDto socialMedia = centerSocialMediaManagementService.getSocialMediaById(socialMediaId);
        return ResponseBuilder.success(socialMedia);
    }

    @PostMapping(ApiConstant.CREATE_CENTER_SOCIAL_MEDIA_API)
    @Operation(summary = "Create social media", description = "Create new social media for a center")
    @SwaggerOperation(summary = "Create social media")
    public ResponseEntity<ApiResponse<CenterSocialMediaDto>> createSocialMedia(
            @Parameter(description = "Social media creation request") @Valid @RequestBody CreateCenterSocialMediaRequest request) {
        log.info("Creating social media for center: {}", request.getCenterId());
        CenterSocialMediaDto socialMedia = centerSocialMediaManagementService.createSocialMedia(request);
        return ResponseBuilder.created(socialMedia);
    }

    @PostMapping(ApiConstant.UPDATE_CENTER_SOCIAL_MEDIA_API)
    @Operation(summary = "Update social media", description = "Update existing social media")
    @SwaggerOperation(summary = "Update social media")
    public ResponseEntity<ApiResponse<CenterSocialMediaDto>> updateSocialMedia(
            @Parameter(description = "Social Media ID") @PathVariable UUID socialMediaId,
            @Parameter(description = "Social media update request") @Valid @RequestBody UpdateCenterSocialMediaRequest request) {
        log.info("Updating social media with ID: {}", socialMediaId);
        CenterSocialMediaDto socialMedia = centerSocialMediaManagementService.updateSocialMedia(socialMediaId, request);
        return ResponseBuilder.success(socialMedia);
    }

    @PostMapping(ApiConstant.DELETE_CENTER_SOCIAL_MEDIA_API)
    @Operation(summary = "Delete social media", description = "Delete social media (soft delete)")
    @SwaggerOperation(summary = "Delete social media")
    public ResponseEntity<ApiResponse<Void>> deleteSocialMedia(
            @Parameter(description = "Social Media ID") @PathVariable UUID socialMediaId) {
        log.info("Deleting social media with ID: {}", socialMediaId);
        centerSocialMediaManagementService.deleteSocialMedia(socialMediaId);
        return ResponseBuilder.success("Social media deleted successfully");
    }

    @PostMapping(ApiConstant.UPDATE_CENTER_SOCIAL_MEDIA_STATUS_API)
    @Operation(summary = "Update social media active status", description = "Update the active status of social media")
    @SwaggerOperation(summary = "Update social media active status")
    public ResponseEntity<ApiResponse<CenterSocialMediaDto>> updateSocialMediaStatus(
            @Parameter(description = "Social Media ID") @PathVariable UUID socialMediaId,
            @Parameter(description = "Status update request") @Valid @RequestBody UpdateCenterSocialMediaStatusRequest request) {
        log.info("Updating social media active status for ID: {} to {}", socialMediaId, request.getIsActive());
        CenterSocialMediaDto socialMedia = centerSocialMediaManagementService.updateSocialMediaStatus(socialMediaId,
                request);
        return ResponseBuilder.success(socialMedia);
    }

}
