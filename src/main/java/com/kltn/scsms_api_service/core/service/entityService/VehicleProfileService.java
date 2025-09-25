package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.repository.VehicleProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleProfileService {
    
    private final VehicleProfileRepository vehicleProfileRepository;
}
