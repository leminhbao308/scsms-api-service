package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageServiceDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageServiceRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageServiceRequest;
import com.kltn.scsms_api_service.core.entity.ServicePackageService;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapper.class})
public interface ServicePackageServiceMapper {
    
    @Mapping(target = "servicePackageServiceId", ignore = true)
    @Mapping(target = "servicePackage", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    ServicePackageService toEntity(CreateServicePackageServiceRequest request);
    
    @Mapping(target = "serviceName", source = "service.serviceName")
    @Mapping(target = "serviceUrl", source = "service.serviceUrl")
    @Mapping(target = "serviceDescription", source = "service.description")
    @Mapping(target = "serviceStandardDuration", source = "service.standardDuration")
    @Mapping(target = "serviceBasePrice", source = "service.basePrice")
    @Mapping(target = "packageId", source = "servicePackage.packageId")
    ServicePackageServiceDto toServicePackageServiceDto(ServicePackageService servicePackageService);
    
    List<ServicePackageServiceDto> toServicePackageServiceDtoList(List<ServicePackageService> servicePackageServices);
    
    @Mapping(target = "servicePackageServiceId", ignore = true)
    @Mapping(target = "servicePackage", ignore = true)
    @Mapping(target = "service", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ServicePackageService updateEntity(@MappingTarget ServicePackageService existing, UpdateServicePackageServiceRequest request);
}
