package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.centerBusinessHours.CenterBusinessHoursDto;
import com.kltn.scsms_api_service.core.dto.centerBusinessHours.request.CreateCenterBusinessHoursRequest;
import com.kltn.scsms_api_service.core.dto.centerBusinessHours.request.UpdateCenterBusinessHoursRequest;
import com.kltn.scsms_api_service.core.dto.centerBusinessHours.request.UpdateCenterBusinessHoursStatusRequest;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.CenterBusinessHours;
import com.kltn.scsms_api_service.core.service.entityService.CenterBusinessHoursService;
import com.kltn.scsms_api_service.core.service.entityService.CenterService;
import com.kltn.scsms_api_service.mapper.CenterBusinessHoursMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CenterBusinessHoursManagementService {
    
    private final CenterBusinessHoursService centerBusinessHoursService;
    private final CenterBusinessHoursMapper centerBusinessHoursMapper;
    private final CenterService centerService;
    
    public List<CenterBusinessHoursDto> getAllBusinessHoursByCenter(UUID centerId) {
        log.info("Getting all business hours for center: {}", centerId);
        List<CenterBusinessHours> businessHours = centerBusinessHoursService.findByCenterId(centerId);
        return businessHours.stream()
            .map(centerBusinessHoursMapper::toDto)
            .collect(Collectors.toList());
    }
    
    
    public CenterBusinessHoursDto getBusinessHoursById(UUID id) {
        log.info("Getting business hours by ID: {}", id);
        CenterBusinessHours businessHours = centerBusinessHoursService.findById(id);
        return centerBusinessHoursMapper.toDto(businessHours);
    }
    
    @Transactional
    public CenterBusinessHoursDto createBusinessHours(CreateCenterBusinessHoursRequest request) {
        log.info("Creating business hours for center: {}", request.getCenterId());
        Center center = centerService.findById(request.getCenterId())
            .orElseThrow(() -> new RuntimeException("Center not found with ID: " + request.getCenterId()));
        CenterBusinessHours businessHours = centerBusinessHoursMapper.toEntity(request);
        businessHours.setCenter(center);
        CenterBusinessHours savedBusinessHours = centerBusinessHoursService.save(businessHours);
        return centerBusinessHoursMapper.toDto(savedBusinessHours);
    }
    
    @Transactional
    public CenterBusinessHoursDto updateBusinessHours(UUID id, UpdateCenterBusinessHoursRequest request) {
        log.info("Updating business hours with ID: {}", id);
        CenterBusinessHours businessHours = centerBusinessHoursService.findById(id);
        centerBusinessHoursMapper.updateEntity(request, businessHours);
        CenterBusinessHours updatedBusinessHours = centerBusinessHoursService.save(businessHours);
        return centerBusinessHoursMapper.toDto(updatedBusinessHours);
    }
    
    @Transactional
    public void deleteBusinessHours(UUID id) {
        log.info("Deleting business hours with ID: {}", id);
        centerBusinessHoursService.delete(id);
    }
    
    @Transactional
    public CenterBusinessHoursDto updateBusinessHoursStatus(UUID id, UpdateCenterBusinessHoursStatusRequest request) {
        log.info("Updating business hours status for ID: {}", id);
        CenterBusinessHours updatedBusinessHours = centerBusinessHoursService.updateIsClosed(id, request.getIsClosed());
        return centerBusinessHoursMapper.toDto(updatedBusinessHours);
    }
    
}
