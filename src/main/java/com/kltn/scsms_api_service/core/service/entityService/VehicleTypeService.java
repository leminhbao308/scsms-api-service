package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.repository.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleTypeService {
    
    private final VehicleTypeRepository vehicleTypeRepository;
}
