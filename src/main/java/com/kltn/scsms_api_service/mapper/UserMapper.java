package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.auth.CustomerDto;
import com.kltn.scsms_api_service.core.dto.auth.EmployeeDto;
import com.kltn.scsms_api_service.core.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {RoleMapper.class})
public interface UserMapper {
    
    CustomerDto toCustomerDto(User user);
    
    EmployeeDto toEmployeeDto(User user);
    
    User toEntity(CustomerDto customerDto);
    
    User toEntity(EmployeeDto employeeDto);
}
