package com.subhash.apimonitoring.event.monitoring;

import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApiRecoveredEvent extends ApplicationEvent {

    private final Long apiId;
    private final String apiName;
    private final long downDurationMs;
    private final MonitoredApi api;

    public ApiRecoveredEvent(Object source, MonitoredApi api, long downDurationMs) {
        super(source);
        this.apiId = api.getId();
        this.apiName = api.getName();
        this.downDurationMs = downDurationMs;
//        this.timestamp = System.currentTimeMillis();
        this.api = api;
    }
}