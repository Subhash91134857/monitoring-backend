package com.subhash.apimonitoring.monitoring.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MonitoringStatsDTO {

    private long totalApis;
    private long activeApis;
    private long upApis;
    private long downApis;
    private long degradedApis;
    private double overallUptime;
    private double averageResponseTime;
    private Map<String, Long> categoryDistribution;
    private Map<String, Double> categoryUptime;
}
