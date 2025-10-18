package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.bookingManagement.TimeSlotDto;
import com.kltn.scsms_api_service.core.entity.BaySchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BayScheduleMapper {
    
    BayScheduleMapper INSTANCE = Mappers.getMapper(BayScheduleMapper.class);
    
    @Mapping(target = "bayId", source = "serviceBay.bayId")
    @Mapping(target = "bayName", source = "serviceBay.bayName")
    @Mapping(target = "bayCode", source = "serviceBay.bayCode")
    @Mapping(target = "estimatedEndTime", source = "endTime")
    @Mapping(target = "isAvailable", expression = "java(schedule.getStatus() == com.kltn.scsms_api_service.core.entity.BaySchedule.ScheduleStatus.AVAILABLE)")
    @Mapping(target = "durationMinutes", expression = "java(60)") // Default 60 minutes
    TimeSlotDto toTimeSlotDto(BaySchedule schedule);
}
