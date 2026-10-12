package com.subhash.apimonitoring.role.dto;


import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private Set<String> permissions;
    private boolean defaultRole;
    private boolean systemRole;
}