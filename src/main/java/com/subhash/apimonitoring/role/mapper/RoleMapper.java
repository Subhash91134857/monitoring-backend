package com.subhash.apimonitoring.role.mapper;



import com.subhash.apimonitoring.role.dto.RoleDTO;
import com.subhash.apimonitoring.role.entity.Role;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
    RoleDTO toDto(Role role);
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Role toEntity(RoleDTO roleDTO);

    Set<RoleDTO> toDtoSet(Set<Role> roles);
}