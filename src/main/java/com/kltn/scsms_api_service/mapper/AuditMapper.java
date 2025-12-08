package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditMapper {
    AuditDto toAuditDto(AuditEntity auditEntity);
}
