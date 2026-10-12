package com.subhash.apimonitoring.incident.dto;


import com.subhash.apimonitoring.incident.enums.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateIncidentRequest {

    @NotNull(message = "API ID is required")
    private Long apiId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Severity is required")
    private IncidentSeverity severity;
}
