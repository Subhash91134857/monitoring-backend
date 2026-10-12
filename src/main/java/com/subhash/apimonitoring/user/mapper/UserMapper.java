package com.subhash.apimonitoring.user.mapper;



import com.subhash.apimonitoring.role.mapper.RoleMapper;
import com.subhash.apimonitoring.user.dto.UserDTO;
import com.subhash.apimonitoring.user.entity.User;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.subhash.apimonitoring.role.repository.RoleRepository;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * User Mapper using MapStruct
 *
 * WHY - Dependency Inversion:
 * MapStruct generates implementation at compile time
 * No reflection, better performance than manual mapping
 *
 * HOW:
 * - Maps User entity to UserDTO and vice versa
 * - @Mapping specifies custom field mappings
 * - @AfterMapping handles derived fields
 *
 * RESULT: Type-safe, fast, and maintainable object mapping
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {RoleMapper.class})
public abstract class UserMapper {

    @Autowired
    protected RoleRepository roleRepository;

    @Mapping(target = "role", expression = "java(user.getRole().getName())")
    @Mapping(target = "permissions", expression = "java(user.getRole().getPermissions())")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    public abstract UserDTO toDto(User user);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract User toEntity(UserDTO userDTO);

    @AfterMapping
    protected void setRoleFromDto(@MappingTarget User user, UserDTO userDTO) {
        if (userDTO.getRole() != null) {
            roleRepository.findByName(userDTO.getRole())
                    .ifPresent(user::setRole);
        }
    }

    public abstract List<UserDTO> toDtoList(List<User> users);
//    public abstract Page<UserDTO> toDtoPage(Page<User> users);
}