package com.subhash.apimonitoring.incident.dto;


import lombok.Builder;
import lombok.Data;
import java.util.Map;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IncidentCommentDTO {

    private Long id;
    private Long incidentId;
    private String comment;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
}
