package com.subhash.apimonitoring.monitoring.mapper;


import com.subhash.apimonitoring.monitoring.dto.MonitoredApiDTO;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MonitoredApiMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "currentStatus", ignore = true)
    @Mapping(target = "lastCheckTime", ignore = true)
    @Mapping(target = "lastResponseTimeMs", ignore = true)
    @Mapping(target = "lastError", ignore = true)
    @Mapping(target = "uptimePercentage", ignore = true)
    @Mapping(target = "totalChecks", ignore = true)
    @Mapping(target = "failedChecks", ignore = true)
    @Mapping(target = "consecutiveFailures", ignore = true)
    MonitoredApi toEntity(MonitoredApiDTO dto);

    @Mapping(target = "checkInterval", source = "checkInterval")
    @Mapping(target = "timeout", source = "timeout")
    MonitoredApiDTO toDto(MonitoredApi entity);

    List<MonitoredApiDTO> toDtoList(List<MonitoredApi> entities);
}