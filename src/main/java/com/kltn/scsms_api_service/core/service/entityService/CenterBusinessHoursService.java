package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.CenterBusinessHours;
import com.kltn.scsms_api_service.core.repository.CenterBusinessHoursRepository;
import com.kltn.scsms_api_service.core.repository.CenterRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CenterBusinessHoursService {
    
    private final CenterBusinessHoursRepository centerBusinessHoursRepository;
    private final CenterRepository centerRepository;
    
    public List<CenterBusinessHours> findByCenterId(UUID centerId) {
        log.info("Finding business hours for center: {}", centerId);
        return centerBusinessHoursRepository.findByCenter_CenterIdAndIsDeletedFalse(centerId);
    }
    
    public List<CenterBusinessHours> findOpenByCenterId(UUID centerId) {
        log.info("Finding open business hours for center: {}", centerId);
        return centerBusinessHoursRepository.findByCenter_CenterIdAndIsClosedFalseAndIsDeletedFalse(centerId);
    }
    
    public List<CenterBusinessHours> findByCenterIdOrderByDayOfWeek(UUID centerId) {
        log.info("Finding business hours ordered by day of week for center: {}", centerId);
        return centerBusinessHoursRepository.findByCenterCenterIdOrderByDayOfWeek(centerId);
    }
    
    public Optional<CenterBusinessHours> findByCenterAndDayOfWeek(UUID centerId, String dayOfWeek) {
        log.info("Finding business hours for center: {} and day: {}", centerId, dayOfWeek);
        return Optional.ofNullable(centerBusinessHoursRepository.findByCenterAndDayOfWeek(centerId, dayOfWeek));
    }
    
    public CenterBusinessHours findById(UUID id) {
        log.info("Finding business hours by ID: {}", id);
        return centerBusinessHoursRepository.findById(id)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Business hours not found with ID: " + id));
    }
    
    @Transactional
    public CenterBusinessHours save(CenterBusinessHours centerBusinessHours) {
        log.info("Saving business hours for center: {}", centerBusinessHours.getCenter().getCenterId());
        
        // Validate center exists
        Center center = centerRepository.findById(centerBusinessHours.getCenter().getCenterId())
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Center not found"));
        
        centerBusinessHours.setCenter(center);
        
        // Check if business hours for this day already exists
        Optional<CenterBusinessHours> existing = findByCenterAndDayOfWeek(
            center.getCenterId(), 
            centerBusinessHours.getDayOfWeek()
        );
        
        if (existing.isPresent() && !existing.get().getBusinessHoursId().equals(centerBusinessHours.getBusinessHoursId())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Business hours for " + centerBusinessHours.getDayOfWeek() + " already exists for this center");
        }
        
        return centerBusinessHoursRepository.save(centerBusinessHours);
    }
    
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting business hours with ID: {}", id);
        CenterBusinessHours centerBusinessHours = findById(id);
        centerBusinessHours.setIsDeleted(true);
        centerBusinessHoursRepository.save(centerBusinessHours);
    }
    
    @Transactional
    public CenterBusinessHours updateIsClosed(UUID id, Boolean isClosed) {
        log.info("Updating isClosed status for business hours ID: {} to {}", id, isClosed);
        CenterBusinessHours centerBusinessHours = findById(id);
        centerBusinessHours.setIsClosed(isClosed);
        return centerBusinessHoursRepository.save(centerBusinessHours);
    }
    
    @Transactional
    public CenterBusinessHours updateIsActive(UUID id, Boolean isActive) {
        log.info("Updating isActive status for business hours ID: {} to {}", id, isActive);
        CenterBusinessHours centerBusinessHours = findById(id);
        centerBusinessHours.setIsActive(isActive);
        return centerBusinessHoursRepository.save(centerBusinessHours);
    }
}
