package com.subhash.apimonitoring.event.monitoring;

import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApiDownEvent extends ApplicationEvent {

    private final Long apiId;
    private final String apiName;
    private final String apiUrl;
    private final String errorMessage;
    private final int consecutiveFailures;
    private final MonitoredApi api;

    public ApiDownEvent(Object source, MonitoredApi api, String errorMessage, int consecutiveFailures) {
        super(source);
        this.apiId = api.getId();
        this.apiName = api.getName();
        this.apiUrl = api.getUrl();
        this.errorMessage = errorMessage;
        this.consecutiveFailures = consecutiveFailures;
//        this.timestamp = System.currentTimeMillis();
        this.api = api;
    }
}